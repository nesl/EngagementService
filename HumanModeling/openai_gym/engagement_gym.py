"""Example of a custom gym environment. Run this for a demo."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import numpy as np
import gym
from gym.spaces import Discrete, Tuple
from gym.envs.registration import EnvSpec

from constant import *
from human_modeling_utils import utils
from human_modeling_utils.chronometer import Chronometer


class EngagementGym(gym.Env):
    """Example of a custom env in which you have to walk down a corridor.
    You can configure the length of the corridor via the env config."""

    def __init__(self, config):
        self.rewardCriteria = config['rewardCriteria']
        self.environment = config['environment']
        self.behavior = config['behavior']
        self.verbose = config['verbose']

        self.action_space = Discrete(2)
        self.observation_space = Tuple((
                Discrete(4),  # time
                Discrete(2),  # day
                Discrete(3),  # location
                Discrete(5),  # activity
                Discrete(2),  # last notification
        ))
        self._spec = EnvSpec("EngagementGym-v0")


    def reset(self):
        # set chronometer which automatically skips 10pm to 8am because it's usually when people
        # sleep
        self.chronometer = Chronometer(skipFunc=(lambda hour, _m, _d: hour < 8 or hour >= 22))

        self.stepWidthMinutes = 10
        #self.simulationWeek = simulationWeek

        self.lastNotificationMinute = 0
        self.lastNotificationHour = 0
        self.lastNotificationNumDays = 0

        # statistics
        self.simulationResults = []
        self.totalReward = 0.
        self.numSteps = 0
        self.lastPrintedWeek = 0
        
        # fast forward a little bit to the next available time
        numDaysPassed, currentHour, currentMinute, currentDay = self.chronometer.forward(
                self.stepWidthMinutes)

        return self._generate_state()

    def step(self, action):

        assert action in [0, 1]  # 0: silent, 1: send notification

        reward = self._generate_reward(action)
        
        # prepare for the next state
        numDaysPassed, currentHour, currentMinute, currentDay = self.chronometer.forward(
                self.stepWidthMinutes)
        if self.verbose:
            print("Day %d %d:%02d" % (numDaysPassed, currentHour, currentMinute))

        gymState = self._generate_state()
        done = False

        self.totalReward += reward
        self.numSteps += 1

        # print some intermediate results
        currentWeek = numDaysPassed // 7
        if currentWeek > self.lastPrintedWeek:
            print()
            print("===== Week %d ====" % (currentWeek - 1))
            results = self._filterByWeek(self.simulationResults, currentWeek - 1)
            self._printResults(results)
            self.lastPrintedWeek = currentWeek

        return gymState, reward, done, {}

    def _generate_state(self):

        # retrieve current state
        numDaysPassed, currentHour, currentMinute, currentDay = self.chronometer.getCurrentTime()

        # get environment info (user context)
        self.lastNotificationTime = utils.getDeltaMinutes(
                numDaysPassed, currentHour, currentMinute,
                self.lastNotificationNumDays, self.lastNotificationHour, self.lastNotificationMinute,
        )
        self.stateLastNotification = utils.getLastNotificationState(self.lastNotificationTime)
        self.stateLocation, self.stateActivity = self.behavior.getLocationActivity(
                currentHour, currentMinute, currentDay)

        # prepare observables and get action
        self.stateTime = utils.getTimeState(currentHour, currentMinute)
        self.stateDay = utils.getDayState(currentDay)

        return (self.stateTime, self.stateDay, self.stateLocation, self.stateActivity, self.stateLastNotification)

    def _generate_reward(self, action):
        
        # retrieve current state
        numDaysPassed, currentHour, currentMinute, currentDay = self.chronometer.getCurrentTime()

        # get probability of each possible user reaction
        probReactions = self.environment.getResponseDistribution(
                currentHour, currentMinute, currentDay,
                self.stateLocation, self.stateActivity, self.lastNotificationTime,
        )
        probReactions = utils.normalize(*probReactions)
        probAnsweringNotification, probIgnoringNotification, probDismissingNotification = probReactions
        
        # calculate reward
        sendNotification = (action == 1)
        if not sendNotification:
            reward = 0
        else:
            userReaction = np.random.choice(
                    a=[ANSWER_NOTIFICATION_ACCEPT, ANSWER_NOTIFICATION_IGNORE, ANSWER_NOTIFICATION_DISMISS],
                    p=[probAnsweringNotification, probIgnoringNotification, probDismissingNotification],
            )
            reward = self.rewardCriteria[userReaction]
            self.lastNotificationNumDays = numDaysPassed
            self.lastNotificationHour = currentHour
            self.lastNotificationMinute = currentMinute

        # log this session
        self.simulationResults.append({
                'context': {
                    'numDaysPassed': numDaysPassed,
                    'hour': currentHour,
                    'minute': currentMinute,
                    'day': currentDay,
                    'location': self.stateLocation,
                    'activity': self.stateActivity,
                    'lastNotification': self.lastNotificationTime,
                },
                'probOfAnswering': probAnsweringNotification,
                'probOfIgnoring': probIgnoringNotification,
                'probOfDismissing': probDismissingNotification,
                'decision': sendNotification,
                'reward': reward,
        })

        return reward

    def _printResults(self, results):
        notificationEvents = [r for r in results if r['decision']]
        numNotifications = len(notificationEvents)
        numAcceptedNotis = len([r for r in notificationEvents if r['reward'] > 0])
        numDismissedNotis = len([r for r in notificationEvents if r['reward'] < 0])
        
        answerRate = numAcceptedNotis / numNotifications
        dismissRate = numDismissedNotis / numNotifications
        responseRate = numAcceptedNotis / (numAcceptedNotis + numDismissedNotis)

        totalReward = sum([r['reward'] for r in results])

        expectedNumDeliveredNotifications = sum([r['probOfAnswering'] for r in results])
        deltaDays = results[-1]['context']['numDaysPassed'] - results[0]['context']['numDaysPassed'] + 1

        print("  reward=%f / step=%d (%f)" % (totalReward, len(results), totalReward / len(results)))
        print("  %d notifications have been sent (%.1f / day):" % (numNotifications, numNotifications / deltaDays))
        print("    - %d are answered (%.2f%%)"  % (numAcceptedNotis, answerRate * 100.))
        print("    - %d are dismissed (%.2f%%)"  % (numDismissedNotis, dismissRate * 100.))
        print("    - response rate: %.2f%%"  % (responseRate * 100.))
        print("  Expectation of total delivered notifications is %.2f" % expectedNumDeliveredNotifications)

    def _filterByWeek(self, results, week):
        startDay = week * 7
        endDay = startDay + 7
        return [r for r in results
                if startDay <= r['context']['numDaysPassed'] < endDay]

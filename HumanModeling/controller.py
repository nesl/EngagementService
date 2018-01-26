import random

from constant import *

from agent import *
from environment import *

class Controller:

    def __init__(self, agent, environment):
        self.currentMinute = 0
        self.currentHour = 0
        self.currentDay = 0

        self.simulationWeek = 10
        self.numDaysPassed = 0

        self.lastNotificationMinute = 0
        self.lastNotificationHour = 0
        self.lastNotificationNumDays = 0

        self.agent = agent
        self.environment = environment

        self.simulationResults = []

    def execute(self):
        self.setNextTime()
        while self.numDaysPassed < self.simulationWeek * 7:
            print(self.currentDay, self.currentHour, self.currentMinute)
            # get environment info (user context)
            lastNotificationTime = self.getDeltaMinutes(
                    self.numDaysPassed, self.currentHour, self.currentDay,
                    self.lastNotificationNumDays, self.lastNotificationHour, self.lastNotificationMinute,
            )
            stateLastNotification = self.getLastNotificationState(lastNotificationTime)
            stateLocation, stateActivity, probAnsweringNotification = (
                    self.environment.getUserContext(self.currentHour, self.currentMinute, self.currentDay, lastNotificationTime))

            # prepare observables and get action
            stateTime = self.getTimeState(self.currentHour, self.currentMinute)
            stateDay = self.getDayState(self.currentDay)
            sendNotification = self.agent.getAction(stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)

            # calculate reward
            if not sendNotification:
                reward = 0
            else:
                answerNotification = (random.random() < probAnsweringNotification)
                reward = (1 if answerNotification else -10)
            self.agent.feedReward(reward)

            # log this session
            self.simulationResults.append({
                    'context': {
                        'numDaysPassed': self.numDaysPassed,
                        'day': self.currentDay,
                        'hour': self.currentHour,
                        'minute': self.currentMinute,
                        'location': stateLocation,
                        'activity': stateActivity,
                        'lastNotification': lastNotificationTime,
                    },
                    'decision': sendNotification,
                    'reward': reward,
            })

            self.setNextTime()

        return self.simulationResults

    def setNextTime(self, timeDeltaMin=10):
        """
        Set the clock to next timeDeltaMin minutes. We skip 10pm to 8am because we are not
        interested in getting data in this period
        """
        needSetTime = True
        while needSetTime:
            self.currentMinute += timeDeltaMin
            if self.currentMinute >= 60:
                self.currentMinute -= 60
                self.currentHour += 1
                if self.currentHour >= 24:
                    self.currentHour -= 24
                    self.currentDay += 1
                    self.numDaysPassed += 1
                    if self.currentDay >= 7:
                        self.currentDay %= 7
            needSetTime = (self.currentHour < 8 or self.currentHour >= 22)

    def getTimeState(self, hour, minute):
        if hour < 8:
            return STATE_TIME_SLEEPING
        elif hour < 12:
            return STATE_TIME_MORNING
        elif hour < 18:
            return STATE_TIME_AFTERNOON
        else:
            return STATE_TIME_EVENING

    def getDayState(self, day):
        if day == 0 or day == 6:
            return STATE_DAY_WEEKEND
        else:
            return STATE_DAY_WEEKDAY

    def getLastNotificationState(self, last_notification_time):
        if last_notification_time <= 60:
            return STATE_LAST_NOTIFICATION_WITHIN_1HR
        else:
            return STATE_LAST_NOTIFICATION_LONG

    def getDeltaMinutes(self, day1, hour1, minute1, day2, hour2, minute2):
        return (day2 - day1) * 24 * 60 + (hour2 - hour1) * 60 + (minute2 - minute1)

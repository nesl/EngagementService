import random
import numpy

from constant import *
import utils
from .base_environment import BaseEnvironment

class LessStubbornUser(BaseEnvironment):
    """
    LessStubbornUser behaves similarly to Stubborn, the difference is that instead of being
    deterministic on each state, a stochastic process is introduced: It has a certain probability
    to deviate away from his original plan.
    """

    def __init__(self, deviationProb=0.1):
        self.behavior = {}
        for sTime in utils.allTimeStates():
            for sDay in utils.allDayStates():
                for sLocation in utils.allLocationStates():
                    for sActivity in utils.allActivityStates():
                        for sNotification in utils.allLastNotificationStates():
                            state = (sTime, sDay, sLocation, sActivity, sNotification)
                            self.behavior[state] = (random.random() < 0.5)
        self.probTake = 1. - deviationProb
        self.probNotTake = deviationProb

    def getUserContext(self, hour, minute, day, lastNotificationTime):
        stateLocation = numpy.random.choice(
            a=[STATE_LOCATION_HOME, STATE_LOCATION_WORK, STATE_LOCATION_OTHER],
            p=[0.5, 0.4, 0.1],
        )
        stateActivity = numpy.random.choice(
            a=[STATE_ACTIVITY_STATIONARY, STATE_ACTIVITY_WALKING, STATE_ACTIVITY_RUNNING, STATE_ACTIVITY_DRIVING],
            p=[0.7, 0.1, 0.1, 0.1],
        )

        stateTime = utils.getTimeState(hour, minute)
        stateDay = utils.getDayState(day)
        stateNotification = utils.getLastNotificationState(lastNotificationTime)
        state = (stateTime, stateDay, stateLocation, stateActivity, stateNotification)

        probAnswerNotification = (self.probTake if self.behavior[state] else self.probNotTake)
        return (stateLocation, stateActivity, probAnswerNotification)

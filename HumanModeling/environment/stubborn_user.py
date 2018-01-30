import random
import numpy

from constant import *
import utils
from .base_environment import BaseEnvironment

class StubbornUser(BaseEnvironment):
    """
    StubbornUser behaves in the following way: It distinguishes the state very clearly. For each
    state, he either always responds notifications, or always dismisses notifications.
    """

    def __init__(self):
        self.behavior = {}
        for sTime in utils.allTimeStates():
            for sDay in utils.allDayStates():
                for sLocation in utils.allLocationStates():
                    for sActivity in utils.allActivityStates():
                        for sNotification in utils.allLastNotificationStates():
                            state = (sTime, sDay, sLocation, sActivity, sNotification)
                            self.behavior[state] = (random.random() < 0.5)

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

        probAnswerNotification = (1.0 if self.behavior[state] else 0.0)
        return (stateLocation, stateActivity, probAnswerNotification)

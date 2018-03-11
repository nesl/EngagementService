import numpy

from constant import *
from .base_environment import BaseEnvironment

class AlwaysSayOKUser(BaseEnvironment):

    def getUserContext(self, hour, minute, day, lastNotificationTime):
        return (
                numpy.random.choice(
                    a=[STATE_LOCATION_HOME, STATE_LOCATION_WORK, STATE_LOCATION_OTHER],
                    p=[0.5, 0.4, 0.1],
                ),
                numpy.random.choice(
                    a=[STATE_ACTIVITY_STATIONARY, STATE_ACTIVITY_WALKING, STATE_ACTIVITY_RUNNING, STATE_ACTIVITY_DRIVING],
                    p=[0.7, 0.1, 0.1, 0.1],
                ),
                1.0,  # probAnsweringNotification
                0.0,  # probIgnoringNotification
                0.0,  # probDismissingNotification
        )

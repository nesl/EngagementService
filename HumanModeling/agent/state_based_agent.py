from constant import *
from utils import utils
from .base_agent import BaseAgent


class StateBasedAgent(BaseAgent):
    """
    `StateBasedAgent` converts an observation into a 5-tuple state, i.e., 
    (stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)
    """

    def getAction(self, hour, minute, day, stateLocation, stateActivity, lastNotificationTime):
        super().getAction(hour, minute, day, stateLocation, stateActivity, lastNotificationTime)

        state = self._convertObservationToState(
                hour, minute, day, stateLocation, stateActivity, lastNotificationTime)
        return self.getActionByState(*state)

    def getActionByState(self, stateTime, stateDay, stateLocation, stateActivity,
            stateLastNotification):
        pass

    def feedBatchRewards(self, history):
        super().feedBatchRewards(history)
        return self.feedBatchRewardsByState(
                [(self._convertObservationToState(o), a, r) for o, a, r in history])

    def feedBatchRewardsByState(self, history):
        """
        Params:
          - history: A list of (state, action, reward) tuples
            - observation is a 5-tuple which fits the argument interface of
              `self.getActionByState()`, i.e.,
              (stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)
        """
        pass

    def _convertObservationToState(self, hour, minute, day, stateLocation, stateActivity,
            lastNotificationTime):
        stateTime = utils.getTimeState(hour, minute)
        stateDay = utils.getDayState(day)
        stateLastNotification = utils.getLastNotificationState(lastNotificationTime)

        return (stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)


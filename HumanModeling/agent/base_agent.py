from constant import *


class BaseAgent:

    STAGE_WAIT_ACTION = 0
    STAGE_WAIT_REWARD = 1

    def __init__(self):
        self.stage = BaseAgent.STAGE_WAIT_ACTION
        self.generateInitialModel()

    def getAction(self, stateTime, stateDay, stateLocation, stateActivity, stateLastNotification):
        """
        The function feedObservation() receives the 4-tuple elements (i.e., time, location,
        activity, and time elapsed since last notification) and makes a decision of sending a
        notificatiion or not.

        The function is anticipated to be provided implementation

        Returns:
          A bool indicating whether to send the notification or not
        """

        # check stage
        if self.stage != BaseAgent.STAGE_WAIT_ACTION:
            raise Exception("It is not in the stage of determining action")
        self.stage = BaseAgent.STAGE_WAIT_REWARD

        # check argument value 
        if stateTime not in [STATE_TIME_MORNING, STATE_TIME_AFTERNOON, STATE_TIME_EVENING, STATE_TIME_SLEEPING]:
            raise Exception("Invalid stateTime value (got %d)" % stateTime)
        if stateDay not in [STATE_DAY_WEEKDAY, STATE_DAY_WEEKEND]:
            raise Exception("Invalid stateDay value (got %d)" % stateDay)
        if stateLocation not in [STATE_LOCATION_HOME, STATE_LOCATION_WORK, STATE_LOCATION_OTHER]:
            raise Exception("Invalid stateLocation value (got %d)" % stateLocation)
        if stateActivity not in [STATE_ACTIVITY_STATIONARY, STATE_ACTIVITY_WALKING, STATE_ACTIVITY_RUNNING, STATE_ACTIVITY_DRIVING]:
            raise Exception("Invalid stateActivity value (got %d)" % stateActivity)
        if stateLastNotification not in [STATE_LAST_NOTIFICATION_WITHIN_1HR, STATE_LAST_NOTIFICATION_LONG]:
            raise Exception("Invalid stateActivity value (got %d)" % stateLastNotification)

    def feedReward(self, reward):
        """
        After the agent gives out the action by the function feedObservation(), the controller is
        anticipated to signal the reward to this agent via this function feedReward().
        
        The function is anticipated to be provided implementation
        """
        if self.stage != BaseAgent.STAGE_WAIT_REWARD:
            raise Exception("It is not in the stage of receiving reward")
        self.stage = BaseAgent.STAGE_WAIT_ACTION

    def generateInitialModel(self):
        """
        To initialize the blank policy.
        """
        pass
    
    def loadModel(self, filepath):
        """
        The function loadModel() loads the predefined policy.
        """
        pass

    def saveModel(self, filepath):
        """
        The function saveModel() saves the current policy.
        """
        pass


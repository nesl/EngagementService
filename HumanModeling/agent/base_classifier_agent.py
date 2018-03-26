import sys
import numpy as np

from .base_agent import BaseAgent
from constant import *


class BaseClassifierAgent(BaseAgent):

    def __init__(self, negRewardWeight=3):
        super().__init__()
        self.negRewardWeight = negRewardWeight

    def loadModel(self, filepath):
        with open(filepath) as f:
            lines = f.readlines()
        mat = np.array([list(map(int, l.strip().split(','))) for l in lines])

        trainX = []
        trainY = []
        for reward, h, m, d, l, a, ln in mat:
            vec = self.encode(h, m, d, l, a, ln)
            repeat = 1 if reward >= 0 else self.negRewardWeight
            for _ in range(repeat):
                trainX.append(vec)
                trainY.append(reward)

        trainX = np.array(trainX)
        trainY = np.array(trainY)

        self.model = self.trainModel(trainX, trainY)
    
    def getAction(self, hour, minute, day, stateLocation, stateActivity, lastNotificationTime):
        super().getAction(hour, minute, day, stateLocation, stateActivity, lastNotificationTime)
        self.currentX = self.encode(
                hour, minute, day, stateLocation, stateActivity, lastNotificationTime)
        self.expectedReward = self.getRewardLabel(self.model, self.currentX)
        return self.expectedReward > 0
    
    def encode(self, hour, minute, day, stateLocation, stateActivity, lastNotificationTime):
        return [
            hour,
            minute,
            day,
            1 if stateLocation == STATE_LOCATION_HOME else 0,
            1 if stateLocation == STATE_LOCATION_WORK else 0,
            1 if stateLocation == STATE_LOCATION_OTHER else 0,
            1 if stateActivity == STATE_ACTIVITY_STATIONARY else 0,
            1 if stateActivity == STATE_ACTIVITY_WALKING else 0,
            1 if stateActivity == STATE_ACTIVITY_RUNNING else 0,
            1 if stateActivity == STATE_ACTIVITY_DRIVING else 0,
            lastNotificationTime,
        ]

    def trainModel(self, dataX, dataY):
        sys.stderr.write("ERROR: Please implement trainModel()\n")
        exit(0)

    def getRewardLabel(self, model, dataXVec):
        sys.stderr.write("ERROR: Please implement getRewardLabel()\n")
        exit(0)

import sys
import numpy as np

from .base_agent import BaseAgent
from utils import utils
from constant import *


kProbeRate = 0.25

class ClassifierDataCollectionAgent(BaseAgent):

    def getAction(self, hour, minute, day, stateLocation, stateActivity, lastNotificationTime):
        super().getAction(hour, minute, day, stateLocation, stateActivity, lastNotificationTime)
        observation = (hour, minute, day, stateLocation, stateActivity, lastNotificationTime)
        self.currentObservation = observation
        self.chosenAction = np.random.random() < kProbeRate
        return self.chosenAction
    
    def feedReward(self, reward):
        super().feedReward(reward)
        if self.chosenAction:
            row = [reward] + list(self.currentObservation)
            self.data.append(row)
    
    def generateInitialModel(self):
        self.data = []  # a list of lists, each child list has
                        # [reward, hour, minute, day, stateLocation, stateActivity, lastNotifTime]
    
    def loadModel(self, filepath):
        sys.stderr.write("Warning: loadModel() does not support\n")        
    
    def saveModel(self, filepath):
        with open(filepath, 'w') as fo:
            for data in self.data:
                fo.write("%s\n" % ",".join(list(map(str, data))))

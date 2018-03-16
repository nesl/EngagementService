import sys
import numpy as np

from .base_agent import BaseAgent
import utils
from constant import *

kInitialLearningRate = 1.0
kMinLearningRate = 0.1
kGamma = 0.9
kInitEps = 0.3
kMinEps = 0.05


class QLearningAgent2(BaseAgent):

    def getAction(self, stateTime, stateDay, stateLocation, stateActivity, stateLastNotification):
        super().getAction(stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)

        state = (stateTime, stateDay, stateLocation, stateActivity, stateLastNotification)
        self.currentState = state

        # Now we get last state, last action, last reward, and current state, time to update
        # Q-table
        if self.numSteps > 0:
            self._updateQTable()

        eps = max(kMinEps, kInitEps * (0.85 ** (self.numSteps // 100)))
        if np.random.random() < eps:
            self.chosenAction = np.random.choice([a for a in self.qTable[state]])
        else:
            self.chosenAction = utils.argmaxDict(self.qTable[state])
        return self.chosenAction
    
    def feedReward(self, reward):
        super().feedReward(reward)

        self.lastState = self.currentState
        self.lastAction = self.chosenAction
        self.lastReward = reward
        self.numSteps += 1
    
    def generateInitialModel(self):
        self.qTable = {}
        for sTime in utils.allTimeStates():
            for sDay in utils.allDayStates():
                for sLocation in utils.allLocationStates():
                    for sActivity in utils.allActivityStates():
                        for sNotification in utils.allLastNotificationStates():
                            state = (sTime, sDay, sLocation, sActivity, sNotification)
                            self.qTable[state] = {a: 0.0 for a in [True, False]}
        self.lastState = None
        self.lastAction = None
        self.lastReward = None
        self.numSteps = 0
    
    def loadModel(self, filepath):
        sys.stderr.write("Warning: loadModel() does not support\n")        
    
    def saveModel(self, filepath):
        sys.stderr.write("Warning: saveModel() does not support\n")

    def printQTable(self):
        for state in self.qTable:
            print(state, self.qTable[state])

    def _updateQTable(self):
        lstStt, lstAct = self.lastState, self.lastAction
        reward = self.lastReward
        curStt = self.currentState

        eta = max(kMinLearningRate, kInitialLearningRate * (0.85 ** (self.numSteps // 100)))
        maxNextQVal = utils.maxDictVal(self.qTable[curStt])
        self.qTable[lstStt][lstAct] = self.qTable[lstStt][lstAct] + eta * (reward + kGamma * maxNextQVal - self.qTable[lstStt][lstAct])

import sys
import numpy

from .base_agent import BaseAgent
import utils
from constant import *

kInitialLearningRate = 1.0
kMinLearningRate = 0.003
kGamma = 1.0
kInitEps = 0.5
kMinEps = 0.01


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
        if numpy.random.uniform(1.) < eps:
            self.chosenAction = numpy.random.choice([a for a in qTable[state]])
        else:
            self.chosenAction = self.chooseAction(self.qTable[state])
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

    def maxDictVal(self, d):
        return max([d[k] for k in d])

    def chooseAction(self, actionQValue):
        # use softmax as action distribution
        actions = [a for a in actionQValue]
        logics = numpy.exp([actionQValue[a] for a in actions])
        prob = logics / numpy.sum(logics)
        return numpy.random.choice(actions, p=prob)

    def _updateQTable(self):
        lstStt, lstAct = self.lastState, self.lastAction
        reward = self.lastReward
        curStt = self.currentState

        eta = max(kMinLearningRate, kInitialLearningRate * (0.85 ** (self.numSteps // 100)))
        maxNextQVal = self.maxDictVal(self.qTable[curStt])
        self.qTable[lstStt][lstAct] = self.qTable[lstStt][lstAct] + eta * (reward + kGamma * maxNextQVal - self.qTable[lstStt][lstAct])

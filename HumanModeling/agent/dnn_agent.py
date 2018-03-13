import sys
import numpy as np

from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.wrappers.scikit_learn import KerasClassifier

from .base_classifier_agent import BaseClassifierAgent


class DNNAgent(BaseClassifierAgent):

    def trainModel(self, dataX, dataY):
        print("Begin to train the model")
        model = Sequential()
        model.add(Dense(32, input_dim=dataX.shape[1], activation='relu'))
        model.add(Dropout(0.5))
        model.add(Dense(32, activation='relu'))
        model.add(Dropout(0.5))
        model.add(Dense(3, activation='softmax'))

        print("Finish training the model")
        return {
                'classifier': clf,
                'scaler': scaler,
        }
    
    def saveModel(self, filepath):
        sys.stderr.write("Warning: saveModel() does not support\n")
    
    def getRewardLabel(self, model, dataXVec):
        clf = model['classifier']
        scaler = model['scaler']
        
        scaledDataX = scaler.transform([dataXVec])
        res = clf.predict(scaledDataX)
        return res[0] > 0

    def _createModel(self):
        model = Sequential()
        model.add(Dense(32, input_dim=dataX.shape[1], activation='relu'))
        model.add(Dropout(0.5))
        model.add(Dense(32, activation='relu'))
        model.add(Dropout(0.5))
        model.add(Dense(3, activation='softmax'))

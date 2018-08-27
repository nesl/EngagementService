from sklearn.model_selection import GridSearchCV
from sklearn.svm import SVC
import numpy as np

from nurture.learning.agents.classification.base_classifier import BaseClassifier
from nurture.learning import learning_utils


kTunedParameters = [{
    'kernel': ['rbf'],
    'gamma': [2**e for e in [-6, -3, 0, 3, 6]],
    'C': [2**e for e in [-6, -3, 0, 3, 6]],
}]

kFold = 5


class SVMClassifier(BaseClassifier):

    def state_to_vector_converter(self, state):
        return learning_utils.get_feature_vector_one_hot_no_log(state)

    def _train_cross_validation(self, X, Y):
        X = np.array(X)
        Y = np.array(Y)
        self.clf = GridSearchCV(SVC(), kTunedParameters, cv=kFold)
        self.clf.fit(X, Y)

    def _predict(self, Xvec):
        X = np.array([Xvec])
        return self.clf.predict(X)[0]

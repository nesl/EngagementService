from sklearn.model_selection import GridSearchCV
from sklearn.ensemble import RandomForestClassifier
import numpy as np

from nurture.learning.agents.classification.base_classifier import BaseClassifier
from nurture.learning import learning_utils


kTunedParameters = [{ 
        'n_estimators': [1, 2, 4, 8, 16, 32],
        'max_features': ['auto', 'sqrt', 'log2'],
}]

kFold = 5


class RFClassifier(BaseClassifier):

    def state_to_vector_converter(self, state):
        return learning_utils.get_feature_vector_one_hot_no_log(state)

    def _train_cross_validation(self, X, Y):
        X = np.array(X)
        Y = np.array(Y)
        self.clf = GridSearchCV(RandomForestClassifier(), kTunedParameters, cv=kFold)
        self.clf.fit(X, Y)

    def _predict(self, Xvec):
        X = np.array([Xvec])
        return self.clf.predict(X)[0]

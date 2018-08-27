import itertools

import numpy as np

from sklearn.model_selection import StratifiedKFold

from keras.models import Sequential
from keras.layers import Dense, Dropout
from keras.utils.np_utils import to_categorical

from nurture.learning.agents.classification.base_classifier import BaseClassifier
from nurture.learning import learning_utils


kTunedParameters = { 
        'layer1_node': [4, 8, 14, 22, 32],
        'layer1_activation': ['relu', 'tanh'],
        'layer2_node': [4, 8, 14, 22, 32],
        'layer2_activation': ['relu', 'tanh'],
}
kFold = 5


class NNClassifier(BaseClassifier):

    def state_to_vector_converter(self, state):
        return learning_utils.get_feature_vector_one_hot_no_log(state)

    def _train_cross_validation(self, X, Y):
        X = np.array(X)
        Y = np.array(Y)
        self.x_dim = X.shape[1]

        param_names, param_values = zip(*kTunedParameters.items())
        all_params = (dict(zip(param_names, value_set))
                for value_set in itertools.product(*param_values))
        best_param = None
        best_accuracy = 0.
        for param in all_params:
            accu = self._cross_validation(param, X, Y)
            if accu > best_accuracy:
                best_param, best_accuracy = param, accu

        self.clf = self._make_model(best_param)
        self.clf.fit(X, to_categorical(Y, 2), epochs=20, batch_size=16, verbose=0)

    def _predict(self, Xvec):
        Y = self.clf.predict(np.array([Xvec]))
        return np.argmax(Y[0])

    def _cross_validation(self, param, X, Y):
        kfold_helper = StratifiedKFold(n_splits=kFold, shuffle=False)
        cv_scores = []
        for cv_train_idxs, cv_test_idxs in kfold_helper.split(X, Y):
            model = self._make_model(param)

            # Fit the model
            train_x = X[cv_train_idxs]
            train_y = to_categorical(Y[cv_train_idxs], 2)
            model.fit(train_x, train_y, epochs=20, batch_size=16, verbose=0)
            
            # evaluate the model
            test_x = X[cv_test_idxs]
            test_y = to_categorical(Y[cv_test_idxs], 2)
            scores = model.evaluate(test_x, test_y, verbose=0)
            cv_scores.append(scores[1])

        print(param, np.mean(cv_scores))
        return np.mean(cv_scores)

    def _make_model(self, param):
        # create model
        model = Sequential()
        model.add(Dense(
            units=param['layer1_node'],
            input_dim=self.x_dim,
            activation=param['layer1_activation'],
        ))
        model.add(Dropout(0.5))
        model.add(Dense(param['layer2_node'], activation=param['layer2_activation']))
        model.add(Dropout(0.5))
        model.add(Dense(2, activation='softmax'))
        model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
        return model

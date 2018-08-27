import abc


class BaseClassifier:

    @abc.abstractmethod
    def state_to_vector_converter(self, state):
        """
        In a high level, a classifier takes Nurture learning `State' and gives an output.
        `state_to_vector_converter` converts a state to a vector.

        Return:
          a `list` for the output vector.
        """

    def train(self, states, labels):
        self._train_cross_validation(
                X=list(map(self.state_to_vector_converter, states)),
                Y=labels,
        )

    @abc.abstractmethod
    def _train_cross_validation(self, X, Y):
        """
        Prepare a model that fits X and Y
        """

    @abc.abstractmethod
    def predict(self, state):
        return self._predict(self.state_to_vector_converter(state))

    def _predict(self, X):
        """
        Return a label Y given the input X
        """

import datetime

from nurture.learning import learning_utils
from nurture.learning.agents.base_agent import BaseAgent
from nurture.learning.agents.classification import *


kMinNotificationGapSeconds = 5 * 60  # 5 minutes


class ClassificationAgent(BaseAgent):

    STAGE_WAIT_TRAINING = 0
    STAGE_PREDICTION = 1

    @classmethod
    def get_policy_name(cls):
        return 'classification'

    @classmethod
    def is_user_dependent(cls):
        return True

    def _process_state_and_get_action(self, state):
        if self.stage == ClassificationAgent.STAGE_WAIT_TRAINING:
            raise Exception("No training has done yet")

        if self._too_close_to_previous_notification():
            send_notification = False
        else:
            action = self.model.predict(state)
            send_notification = (action == 1)

        # remember when we send notification
        if send_notification:
            self.last_notification_time = datetime.datetime.now()

        return send_notification

    def _process_reward(self, reward):
        if self.stage == ClassificationAgent.STAGE_WAIT_TRAINING:
            raise Exception("No training has done yet")

        # don't care about the reward
    
    def _restart_episode(self):
        pass

    def generate_initial_model(self):
        self.gym_stage = ClassificationAgent.STAGE_PREDICTION
        self.last_notification_time = datetime.datetime(2000, 1, 1, 0, 0, 0)

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    @classmethod
    def non_disturb_mode_during_night(cls):
        return True

    def on_pickle_save(self):
        pass

    def on_pickle_load(self):
        pass

    def _too_close_to_previous_notification(self):
        now = datetime.datetime.now()
        time_delta = now - self.last_notification_time
        return time_delta.total_seconds() < kMinNotificationGapSeconds

    def _count_correct_instances(self, clf, states, labels):
        for s, l in zip(states, labels):
            print(clf.predict(s), l)
        print("^^^^^^^^^^")
        return sum([clf.predict(s) == l for s, l in zip(states, labels)])

    def prepare_classifier(self, states, labels):
        """
        Unless `prepare_classifier()` is called, the agent cannot operate classification tasks.
        `prepare_classifier()` chooses one classifier among SVM, RF, and NN algorithms, in which
        the hyperparameters are tuned internally.
        """
        models = {
                'svm': SVMClassifier(),
                'rf': RFClassifier(),
                'nn': NNClassifier(),
        }
        mid = int(len(states) * 0.8)
        for name in models:
            clf = models[name]
            clf.train(states[:mid], labels[:mid])

        performances = {name: self._count_correct_instances(clf, states, labels)
                for name, clf in models.items()}
        print("total instances", len(states))
        print("performances", performances)

        self.classifier_name = learning_utils.argmax_dict(performances)
        self.classifier = models[self.classifier_name]
        
        self.stage = ClassificationAgent.STAGE_PREDICTION

    def output_classifier_name(self):
        if self.stage == ClassificationAgent.STAGE_WAIT_TRAINING:
            raise Exception("No training has done yet")

        return self.classifier_name

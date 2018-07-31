import sys
import numpy as np
import itertools

from nurture.learning.agents.base_agent import BaseAgent
from nurture.learning.state import State
from nurture.learning import learning_utils


kInitialLearningRate = 1.0
kMinLearningRate = 0.1
kGamma = 0.9
kInitEps = 0.3
kMinEps = 0.05


class QLearningAgent(BaseAgent):
            
    TIME_SLEEPING = 0
    TIME_MORNING = 1
    TIME_AFTERNOON = 2
    TIME_EVENING = 3

    DAY_WEEKEND = 0
    DAY_WEEKDAY = 1

    LAST_NOTIFICATION_WITHIN_3MIN = 0
    LAST_NOTIFICATION_WITHIN_10MIN = 1
    LAST_NOTIFICATION_WITHIN_30MIN = 2
    LAST_NOTIFICATION_WITHIN_1HR = 3
    LAST_NOTIFICATION_LONG = 4


    @classmethod
    def get_policy_name(cls):
        return 'q-learning'

    @classmethod
    def is_user_dependent(cls):
        return False

    def _process_state_and_get_action(self, state):
        q_learning_state = self._get_qlearning_state(state)
        self.current_state = q_learning_state

        self._update_q_table(
                self.last_state, self.last_action, self.last_reward, self.current_state)

        eps = max(kMinEps, kInitEps * (0.85 ** (self.num_steps // 100)))
        if np.random.random() < eps:
            self.chosen_action = np.random.choice([a for a in self.qTable[q_learning_state]])
        else:
            self.chosen_action = learning_utils.argmax_dict(self.qTable[q_learning_state])
        return self.chosen_action
    
    def _process_reward(self, reward):
        self.last_state = self.current_state
        self.last_action = self.chosen_action
        self.last_reward = reward
        self.num_steps += 1
    
    def _restart_episode(self):
        self.last_state = None
        self.last_action = None
        self.last_reward = None

    def generate_initial_model(self):
        self.qTable = {}
        all_qlearning_states = itertools.product(
                self._get_all_time_categories(),
                self._get_all_day_categories(),
                State.allMotionValues(),
                State.allLocationValues(),
                self._get_all_last_notification_categories(),
                State.allRingerModeValues(),
                State.allScreenStatusValues(),
        )
        for state in all_qlearning_states:
            self.qTable[state] = {True: 1e-5, False: 0.}

        self.num_steps = 0
    
    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    def _get_qlearning_state(self, state):
        return (
                self._get_time_category(state.timeOfDay),
                self._get_day_category(state.dayOfWeek),
                state.motion,
                state.location,
                self._get_last_notification_category(state.notificationTimeElapsed),
                state.ringerMode,
                state.screenStatus,
        )

    def _get_time_category(self, time_of_day):
        hour = int(time_of_day * 24.)
        if hour < 8:
            return QLearningAgent.TIME_SLEEPING
        elif hour < 12:
            return QLearningAgent.TIME_MORNING
        elif hour < 18:
            return QLearningAgent.TIME_AFTERNOON
        else:
            return QLearningAgent.TIME_EVENING

    def _get_day_category(self, day_of_week):
        day = int(day_of_week * 7.)
        if day == 0 or day == 6:
            return QLearningAgent.DAY_WEEKEND
        else:
            return QLearningAgent.DAY_WEEKDAY

    def _get_last_notification_category(self, notification_time_elapsed):
        if notification_time_elapsed <= 3.:
            return QLearningAgent.LAST_NOTIFICATION_WITHIN_3MIN
        elif notification_time_elapsed <= 10.:
            return QLearningAgent.LAST_NOTIFICATION_WITHIN_10MIN
        elif notification_time_elapsed <= 30.:
            return QLearningAgent.LAST_NOTIFICATION_WITHIN_30MIN
        elif notification_time_elapsed <= 60.:
            return QLearningAgent.LAST_NOTIFICATION_WITHIN_1HR
        else:
            return QLearningAgent.LAST_NOTIFICATION_LONG

    def _get_all_time_categories(self):
        return [
            QLearningAgent.TIME_SLEEPING,
            QLearningAgent.TIME_MORNING,
            QLearningAgent.TIME_AFTERNOON,
            QLearningAgent.TIME_EVENING,
        ]

    def _get_all_day_categories(self):
        return [
            QLearningAgent.DAY_WEEKEND,
            QLearningAgent.DAY_WEEKDAY,
        ]

    def _get_all_last_notification_categories(self):
        return [
            QLearningAgent.LAST_NOTIFICATION_WITHIN_3MIN,
            QLearningAgent.LAST_NOTIFICATION_WITHIN_10MIN,
            QLearningAgent.LAST_NOTIFICATION_WITHIN_30MIN,
            QLearningAgent.LAST_NOTIFICATION_WITHIN_1HR,
            QLearningAgent.LAST_NOTIFICATION_LONG,
        ]

    def _update_q_table(self, curStt, curAct, reward, nxtStt):
        """
        curStt = current state
        curAct = current action
        reward = reward
        nxtStt = next state
        """
        if curStt is None:
            return

        eta = max(kMinLearningRate, kInitialLearningRate * (0.85 ** (self.num_steps // 100)))
        maxNextQVal = learning_utils.max_dict_val(self.qTable[nxtStt])
        self.qTable[curStt][curAct] = (self.qTable[curStt][curAct]
                + eta * (reward + kGamma * maxNextQVal - self.qTable[curStt][curAct]))
    
    def print_q_table(self):
        for state in self.qTable:
            print(state, self.qTable[state])


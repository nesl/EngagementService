import random
import datetime

from nurture.learning.agents.base_agent import BaseAgent


class RandomAgent(BaseAgent):

    @classmethod
    def get_policy_name(cls):
        return 'random'

    @classmethod
    def is_user_dependent(cls):
        return True

    def _process_state_and_get_action(self, state):
        now = datetime.datetime.now()
        time_delta = now - self.last_notification_time
        minutes_passed = min(1000, round(time_delta.total_seconds() / 60.))
        self.last_notification_time = now

        for _ in range(minutes_passed):
            if random.randint(0, self.cycle_length) == 0:
                return True
        return False

    def _process_reward(self, reward):
        pass
    
    def _restart_episode(self):
        pass

    def generate_initial_model(self):
        self.cycle_length = 30
        self.last_notification_time = datetime.datetime(2000, 1, 1, 0, 0, 0)

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    def on_pickle_save(self):
        pass

    def on_pickle_load(self):
        pass

    def change_cycle_length(self, cycle_length):
        self.cycle_length = cycle_length


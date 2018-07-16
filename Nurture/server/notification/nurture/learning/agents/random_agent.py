import random

from nurture.learning.agents.base_agent import BaseAgent


class RandomAgent(BaseAgent):

    @classmethod
    def get_policy_name(cls):
        return 'random'

    @classmethod
    def is_user_dependent(cls):
        return True

    def _process_state_and_get_action(self, state):
        return random.randint(0, self.cycle_length) == 0

    def _process_reward(self, reward):
        pass
    
    def _restart_episode(self):
        pass

    def generate_initial_model(self):
        self.cycle_length = 30

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    def change_cycle_length(self, cycle_length):
        self.cycle_length = cycle_length


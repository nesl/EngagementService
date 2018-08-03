from nurture.learning.agents.base_agent import BaseAgent


class DebugAgent(BaseAgent):

    @classmethod
    def get_policy_name(cls):
        return 'debug'

    @classmethod
    def is_user_dependent(cls):
        return False

    def _process_state_and_get_action(self, state):
        return True

    def _process_reward(self, reward):
        pass
    
    def _restart_episode(self):
        pass

    def generate_initial_model(self):
        pass

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    def on_pickle_save(self):
        pass

    def on_pickle_load(self):
        pass

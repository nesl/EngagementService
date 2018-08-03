import abc


class BaseAgent:
    """
    The following programming pattern is expected:

        for each step:
            agent.get_action(state)
            ...
            agent.feed_reward(reward)
    """

    STAGE_RESTARTED = -1
    STAGE_WAIT_ACTION = 0
    STAGE_WAIT_REWARD = 1

    def __init__(self):
        self.stage = BaseAgent.STAGE_RESTARTED
        self.generate_initial_model()
        self.restart_episode()

    @classmethod
    @abc.abstractmethod
    def get_policy_name(cls):
        """
        Return an `str` to indicate the policy name
        """

    @classmethod
    @abc.abstractmethod
    def is_user_dependent(cls):
        """
        Return a `bool` to indicate if the model is user dependent or not.
        """

    @classmethod
    def get_policy_file_name(cls):
        return cls.get_policy_name() + '.p'

    def get_action(self, state):
        """
        The function `get_action()` receives a state from the environment, and returns an action
        based on the given state.

        Returns:
          A bool indicating whether to send the notification or not
        """

        # check stage
        if self.stage not in [BaseAgent.STAGE_RESTARTED, BaseAgent.STAGE_WAIT_ACTION]:
            raise Exception("It is not in the stage of determining action")
        self.stage = BaseAgent.STAGE_WAIT_REWARD

        return self._process_state_and_get_action(state)

    @abc.abstractmethod
    def _process_state_and_get_action(self, state):
        """
        The function `_process_state_and_get_action()` processes the state and compute the action.

        Returns:
          A bool indicating whether to send the notification or not
        """

    def feed_reward(self, reward):
        """
        After the agent gives out the action by the function `get_action()`, the controller is
        anticipated to signal the reward to this agent via this function `feed_reward()`.
        """
        if self.stage not in [BaseAgent.STAGE_RESTARTED, BaseAgent.STAGE_WAIT_REWARD]:
            raise Exception("It is not in the stage of receiving reward")

        if self.stage == BaseAgent.STAGE_WAIT_REWARD:
            self._process_reward(reward)

        self.stage = BaseAgent.STAGE_WAIT_ACTION
    
    @abc.abstractmethod
    def _process_reward(self, reward):
        """
        Internal reward processing logics
        """

    def restart_episode(self):
        """
        Due to unexpected reason (e.g., network failure), the learning process can be discontinued
        and have to be restart. In that case, this function is called.
        """
        self.stage = BaseAgent.STAGE_RESTARTED
        self._restart_episode()

    @abc.abstractmethod
    def _restart_episode(self):
        """
        Internal restart preparation
        """

    @abc.abstractmethod
    def generate_initial_model(self):
        """
        To initialize the blank policy.
        """
    
    @abc.abstractmethod
    def load_model(self, filepath):
        """
        The function load_model() loads the predefined policy.
        """

    @abc.abstractmethod
    def save_model(self, filepath):
        """
        The function save_model() saves the current policy.
        """

    @classmethod
    def non_disturb_mode_during_night(cls):
        return True

    @abc.abstractmethod
    def on_pickle_save(self):
        """
        The callback before the controller calls `dill.dump()`
        """

    @abc.abstractmethod
    def on_pickle_load(self):
        """
        The callback before the controller calls `dill.load()`
        """


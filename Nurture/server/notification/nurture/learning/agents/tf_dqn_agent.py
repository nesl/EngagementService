import datetime

from nurture.learning import learning_utils
from nurture.learning.state import State
from nurture.learning.agents.base_agent import BaseAgent


kEpisodeLengthSteps = 100
kMinNotificationGapSeconds = 5 * 60  # 5 minutes


class TensorForceDQNAgent(BaseAgent):

    GYM_STAGE_WAIT_STATE = 0
    GYM_STAGE_WAIT_REWARD = 1

    @classmethod
    def get_policy_name(cls):
        return 'tf-dqn'

    @classmethod
    def is_user_dependent(cls):
        return True

    def _process_state_and_get_action(self, state):
        if self.gym_stage == TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD:
            # strange status, but let's feed a dummy reward and move on
            self.agent.observe(reward=0., terminal=False)

        gym_state = self._to_gym_state(state)
        action = self.agent.act(gym_state)  # 0->silent, 1->send
        send_notification = (action == 1)
        print("[tf-dqn] the agent says send_notification=", send_notification)
        
        # override the decision if the previous notification is too close
        now = datetime.datetime.now()
        time_delta = now - self.last_notification_time
        if time_delta.total_seconds() < kMinNotificationGapSeconds:
            send_notification = False
            print("[tf-dqn] override send_notification to False, time delta in seconds =",
                    time_delta.total_seconds())

        # remember when we send notification
        if send_notification:
            self.last_notification_time = now

        self.gym_stage = TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD
        self.last_send_notification = send_notification

        return send_notification

    def _process_reward(self, reward):
        if self.gym_stage != TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD:
            # strange status, just ignore it
            return

        # calibrate reward
        if self.last_send_notification and reward == 0.:
            reward = -0.5

        self.num_steps += 1
        done = (self.num_steps % kEpisodeLengthSteps == 0)
        self.agent.observe(reward=reward, terminal=done)

        self.gym_stage = TensorForceDQNAgent.GYM_STAGE_WAIT_STATE
    
    def _restart_episode(self):
        if self.gym_stage == TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD:
            self.agent.observe(reward=0., terminal=False)
            self.gym_stage = TensorForceDQNAgent.GYM_STAGE_WAIT_STATE

    def generate_initial_model(self):
        print("[tf-dqn] generate_initial_model()")
        self.agent = self._get_agent(self._get_exploration_rate(num_steps=0))
        print("[tf-dqn] spawn the agent")
        
        self.gym_stage = TensorForceDQNAgent.GYM_STAGE_WAIT_STATE
        self.last_notification_time = datetime.datetime(2000, 1, 1, 0, 0, 0)
        self.num_steps = 0
        print("[tf-dqn] generate_initial_model() done")

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    @classmethod
    def non_disturb_mode_during_night(cls):
        return True

    def on_pickle_save(self):
        print("[tf-dqn] on_pickle_save()")
        tmp_folder = learning_utils.make_tmp_folder()
        self.agent.save_model(tmp_folder)
        self.pickle_data = learning_utils.load_files_in_memory(tmp_folder)
        self.agent = None
        print("[tf-dqn] on_pickle_save() done")

    def on_pickle_load(self):
        print("[tf-dqn] on_pickle_load()")
        tmp_folder = learning_utils.make_tmp_folder()
        learning_utils.restore_files_to_disk(tmp_folder, self.pickle_data)
        self.pickle_data = None
        self.agent = self._get_agent(self._get_exploration_rate(num_steps=self.num_steps))
        self.agent.restore_model(tmp_folder)
        print("[tf-dqn] on_pickle_load() done")

    def _to_gym_state(self, state):
        return learning_utils.get_feature_vector_one_hot_classic(state)

    def _get_exploration_rate(self, num_steps):
        # in the beginning = 0.1
        # in the end, 0.01
        print('[tf-dqn] num_steps', num_steps, 'p', (0.999 ** num_steps) * 0.1 + 0.01)
        return (0.999 ** num_steps) * 0.1 + 0.01

    def _get_agent(self, exploration_rate):
        print("[tf-dqn] _get_agent()")
        # import from here to boost performance
        from tensorforce.agents import DQNAgent
        print("[tf-dqn] done importing")

        num_gym_state_dimensions = len(self._to_gym_state(State.getExampleState()))
        print("[tf-dqn] figured out number of dimensions", num_gym_state_dimensions)
        return DQNAgent(
                states=dict(type='float', shape=(num_gym_state_dimensions,)),
                actions=dict(type='int', num_actions=2),
                network=[
                    dict(type='dense', size=20),
                    dict(type='dense', size=20)
                ],
                batched_observe=False,
                actions_exploration={
                    'type': 'epsilon_decay',
                    'initial_epsilon': exploration_rate + 0.001,
                    'final_epsilon': exploration_rate,
                    'timesteps': 500000,  # don't make it affect the exploration rate
                },
        )

import math
import itertools
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
        print("the agent says send_notification=", send_notification)
        
        # override the decision if the previous notification is too close
        now = datetime.datetime.now()
        time_delta = now - self.last_notification_time
        if time_delta.total_seconds() < kMinNotificationGapSeconds:
            send_notification = False
            print("override send_notification to False, time delta in seconds =", time_delta.total_seconds())

        # remember when we send notification
        if send_notification:
            self.last_notification_time = now

        self.gym_stage = TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD

        return send_notification

    def _process_reward(self, reward):
        if self.gym_stage != TensorForceDQNAgent.GYM_STAGE_WAIT_REWARD:
            # strange status, just ignore it
            return

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
        self.agent = self._get_naive_agent()
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
        return False

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
        self.agent = self._get_naive_agent()
        self.agent.restore_model(tmp_folder)
        print("[tf-dqn] on_pickle_load() done")

    def _to_gym_state(self, state):
        return learning_utils.smart_list_concatenation(
                state.timeOfDay,
                state.dayOfWeek,
                learning_utils.one_hot_list(state.motion, State.allMotionValues()),
                learning_utils.one_hot_list(state.location, State.allLocationValues()),
                math.log(learning_utils.clip(state.notificationTimeElapsed, 5.0, 60.0)),
                learning_utils.one_hot_list(state.ringerMode, State.allRingerModeValues()),
                state.screenStatus,
        )

    def _get_naive_agent(self):
        print("[tf-dqn] _get_native_agent()")
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
                    'initial_epsilon': 1.00,
                    'final_epsilon': 0.01,
                    'timesteps': 5000,  # cover around 1 week
                },
        )

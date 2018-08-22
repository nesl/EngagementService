import math
import datetime
import time
import os

from notification import settings

from nurture.learning import learning_utils
from nurture.learning.state import State
from nurture.learning.agents.base_agent import BaseAgent


kMinNotificationGapSeconds = 5 * 60  # 5 minutes
kEpisodeLengthSteps = 100

kDatetimeFormat = "%Y/%b/%d %H:%M:%S.%f"


class CoachA3CAgent(BaseAgent):

    GYM_STAGE_WAIT_STATE = 0
    GYM_STAGE_WAIT_REWARD = 1

    @classmethod
    def get_policy_name(cls):
        return 'coach-a3c'

    @classmethod
    def is_user_dependent(cls):
        return True

    def _process_state_and_get_action(self, state):
        if self.gym_stage == CoachA3CAgent.GYM_STAGE_WAIT_REWARD:
            # strange status, but let's feed a dummy reward and move on
            self.last_reward = 0.

        if self._too_close_to_previous_notification():
            send_notification = False
        else:
            gym_state = self._to_gym_state(state)
            self.num_steps += 1
            done = (self.num_steps % kEpisodeLengthSteps == 0)
            action = self._wait_for_action(self.last_reward, done, gym_state)
            send_notification = (action == '1')  # None->bad connect, 0->silent, 1->send
            self.last_reward = 0.

        # remember when we send notification
        if send_notification:
            self.last_notification_time = datetime.datetime.now()

        self.gym_stage = CoachA3CAgent.GYM_STAGE_WAIT_REWARD
        self.last_send_notification = send_notification

        return send_notification

    def _process_reward(self, reward):
        if self.gym_stage != CoachA3CAgent.GYM_STAGE_WAIT_REWARD:
            # strange status, just ignore it
            return

        # calibrate reward
        if self.last_send_notification and reward == 0.:
            reward = -0.05

        # accumulate the rewards anyway
        self.last_reward += reward
        
        self.gym_stage = CoachA3CAgent.GYM_STAGE_WAIT_STATE
    
    def _restart_episode(self):
        if self.gym_stage == CoachA3CAgent.GYM_STAGE_WAIT_REWARD:
            self.last_reward = 0.
            self.gym_stage = CoachA3CAgent.GYM_STAGE_WAIT_STATE

    def generate_initial_model(self):
        self.gym_stage = CoachA3CAgent.GYM_STAGE_WAIT_STATE
        self.last_notification_time = datetime.datetime(2000, 1, 1, 0, 0, 0)
        self.num_steps = 0

        self.last_reward = 0.

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

    def _wait_for_action(self, reward, done, state):
        #if not self._is_heartbeat_warm():
        #    os.system("python3 ../coach_daemon/main.py %s &" % self.get_user_code())

        print("reward", reward, "done", done, "state", state)

        self._get_action_from_file()

        self._write_reward_state(reward, done, state)
        deadline = datetime.datetime.now() + datetime.timedelta(seconds=10)
        while datetime.datetime.now() < deadline:
            action = self._get_action_from_file()
            if action is not None:
                return action
            time.sleep(0.5)

        return None

    def _get_file_path(self, file_name):
        return os.path.join(
                settings.COACH_A3C_CHECKPOINT_ROOT, self.get_user_code(), file_name)

    def _read_file_lines(self, file_name, destroy=False):
        file_path = self._get_file_path(file_name)
        if not os.path.isfile(file_path):
            return None

        with open(file_path, 'r') as f:
            lines = f.readlines()

        if destroy:
            os.remove(file_path)

        return lines

    def _is_heartbeat_warm(self):
        lines = self._read_file_lines('heartbeat.txt')
        if lines is None or len(lines) == 0:
            return False
        
        return self._timestamp_within_x_seconds(lines[0], seconds=10)

    def _get_action_from_file(self):
        lines = self._read_file_lines('action.txt', destroy=True)
        if lines is None or len(lines) < 2:
            return None
        if not self._timestamp_within_x_seconds(lines[0].strip(), seconds=30):
            return None
        return lines[1].strip()

    def _write_reward_state(self, reward, done, state):
        with open(self._get_file_path('reward_state.txt'), 'w') as fo:
            fo.write(datetime.datetime.now().strftime(kDatetimeFormat) + "\n")
            fo.write(str(reward) + "\n")
            fo.write(str(done) + "\n")
            fo.write(','.join(list(map(str, list(state)))) + "\n")

    def _timestamp_within_x_seconds(self, line, seconds):
        try:
            timestamp = datetime.datetime.strptime(line, kDatetimeFormat)
        except:
            return False
        
        return datetime.datetime.now() - timestamp < datetime.timedelta(seconds=seconds)

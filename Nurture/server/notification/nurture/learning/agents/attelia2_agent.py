import random

from nurture.learning.agents.base_agent import BaseAgent
from nurture.learning.state import State


class Attelia2Agent(BaseAgent):
    """
    The idea of this notification scheduler policy is from a UbiComp paper called "Reducing Users'
    Perceived Mental Effort due to Interruptive Notifications in Multi-Device Mobile Environments"
    published in 2015, authored by Okoshi et al. This paper shows that users are usually available
    during breakpoints, which can be defined as the transition of two consecutive activities. We
    implement the result reported in Table 2 in the paper.
    """

    ATTELIA_STATE_BIKING = 0
    ATTELIA_STATE_RUNNING = 1
    ATTELIA_STATE_WALKING = 2
    ATTELIA_STATE_STILL = 3
    ATTELIA_STATE_WORK = 4
    ATTELIA_STATE_UNKNOWN = -1

    BREAKPOINT_TRANSITIONS = [
            (ATTELIA_STATE_BIKING, ATTELIA_STATE_WALKING),
            (ATTELIA_STATE_BIKING, ATTELIA_STATE_STILL),
            (ATTELIA_STATE_RUNNING, ATTELIA_STATE_WALKING),
            (ATTELIA_STATE_RUNNING, ATTELIA_STATE_STILL),
            (ATTELIA_STATE_WALKING, ATTELIA_STATE_WORK),
            (ATTELIA_STATE_WALKING, ATTELIA_STATE_STILL),
            (ATTELIA_STATE_WORK, ATTELIA_STATE_RUNNING),
            (ATTELIA_STATE_WORK, ATTELIA_STATE_WALKING),
            (ATTELIA_STATE_WORK, ATTELIA_STATE_STILL),
            (ATTELIA_STATE_STILL, ATTELIA_STATE_RUNNING),
            (ATTELIA_STATE_STILL, ATTELIA_STATE_WALKING),
    ]

    @classmethod
    def get_policy_name(cls):
        return 'attelia2'

    @classmethod
    def is_user_dependent(cls):
        return False

    def _process_state_and_get_action(self, state):
        cur_attelia_state = self._get_attelia_state(state)
        transition = (self.prev_attelia_state, cur_attelia_state)
        send_notification = (transition in self.BREAKPOINT_TRANSITIONS)
        self.prev_attelia_state = cur_attelia_state
        return send_notification

    def _process_reward(self, reward):
        pass
    
    def _restart_episode(self):
        pass

    def generate_initial_model(self):
        self.prev_attelia_state = Attelia2Agent.ATTELIA_STATE_UNKNOWN

    def load_model(self, filepath):
        pass

    def save_model(self, filepath):
        pass

    def change_cycle_length(self, cycle_length):
        self.cycle_length = cycle_length

    def _get_attelia_state(self, state):
        if state.location == State.LOCATION_WORK and state.motion == State.MOTION_STATIONARY:
            return Attelia2Agent.ATTELIA_STATE_WORK
        elif state.motion == State.MOTION_STATIONARY:
            return Attelia2Agent.ATTELIA_STATE_STILL
        elif state.motion == State.MOTION_WALKING:
            return Attelia2Agent.ATTELIA_STATE_WALKING
        elif state.motion == State.MOTION_RUNNING:
            return Attelia2Agent.ATTELIA_STATE_RUNNING
        elif state.motion == State.MOTION_BIKING:
            return Attelia2Agent.ATTELIA_STATE_BIKING

        return Attelia2Agent.ATTELIA_STATE_UNKNOWN


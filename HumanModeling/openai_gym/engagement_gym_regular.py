import numpy as np

from gym.spaces import Discrete, Tuple

from .engagement_gym_base import EngagementGymBase


class EngagementGym(EngagementGymBase):

    def get_observation_space(self):
        return Tuple((
                Discrete(4),  # time
                Discrete(2),  # day
                Discrete(3),  # location
                Discrete(5),  # activity
                Discrete(2),  # last notification
        ))

    def intepret_state(self, state_tuple):
        return state_tuple

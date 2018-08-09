import numpy as np
import os
import sys
import math

import gym
from gym.spaces import Discrete, Box
from gym.envs.registration import EnvSpec


class EngagementShellGym(gym.Env):
    """Example of a custom env in which you have to walk down a corridor.
    You can configure the length of the corridor via the env config."""

    def __init__(self, config=None):

        self.action_space = Discrete(2)
        self.observation_space = self.get_observation_space()
        self._spec = EnvSpec("Engagement-v0")

        self.state = self.get_initial_state()
        self.reward = 0.
        self.done = False

    def get_observation_space(self):
        return Box(
                low=np.array([
                    0.,  # time of day
                    0.,  # day of week
                    0.,  # motion - stationary
                    0.,  # motion - walking
                    0.,  # motion - running
                    0.,  # motion - driving
                    0.,  # motion - biking
                    0.,  # location - home
                    0.,  # location - work
                    0.,  # location - other
                    0.,  # notificatoin time
                    0.,  # ringer mode - silent
                    0.,  # ringer mode - vibrate
                    0.,  # ringer mode - normal
                    0.,  # screen status
                ]),
                high=np.array([
                    1.,  # time of day
                    1.,  # day of week
                    1.,  # motion - stationary
                    1.,  # motion - walking
                    1.,  # motion - running
                    1.,  # motion - driving
                    1.,  # motion - biking
                    1.,  # location - home
                    1.,  # location - work
                    1.,  # location - other
                    math.log(60.),  # notificatoin time
                    1.,  # ringer mode - silent
                    1.,  # ringer mode - vibrate
                    1.,  # ringer mode - normal
                    1.,  # screen status
                ]),
        )

    def get_initial_state(self):
        return np.array([
            0.,  # time of day
            0.,  # day of week
            1.,  # motion - stationary
            0.,  # motion - walking
            0.,  # motion - running
            0.,  # motion - driving
            0.,  # motion - biking
            1.,  # location - home
            0.,  # location - work
            0.,  # location - other
            math.log(5.),  # notificatoin time
            1.,  # ringer mode - silent
            0.,  # ringer mode - vibrate
            0.,  # ringer mode - normal
            0.,  # screen status
        ])

    def reset(self):
        return self.state

    def step(self, action):
        return self.state, self.reward, self.done, {}

    def supply(self, reward, done, state):
        self.reward = reward
        self.done = done
        self.state = state

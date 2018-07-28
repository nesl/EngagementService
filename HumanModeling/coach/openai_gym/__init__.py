from gym.envs.registration import register
#from openai_gym import utils
#from openai_gym.constant import *
#from openai_gym.behavior import *
#from openai_gym.environment import *

register(
        id='Engagement-v0',
        entry_point='openai_gym.engagement_gym_coach:EngagementGymCoach',
        max_episode_steps = 10000
    )


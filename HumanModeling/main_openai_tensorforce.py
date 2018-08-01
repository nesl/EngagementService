from tensorforce.agents import DQNAgent
from tensorforce.agents import PPOAgent
from tensorforce.agents import TRPOAgent
from tensorforce.agents import VPGAgent

from constant import *
from environment import *
from behavior import *
from openai_gym.engagement_gym import EngagementGym


# NOTE: This is an experimental code, please use comments to switch different options.


def get_environment():
    ### simulation configuration
    rewardCriteria = {
            ANSWER_NOTIFICATION_ACCEPT: 1,
            ANSWER_NOTIFICATION_IGNORE: 0,
            ANSWER_NOTIFICATION_DISMISS: -2,
    }
    verbose = False

    ### user habit model

    #environment = AlwaysSayOKUser()
    #environment = StubbornUser()
    #environment = LessStubbornUser()
    #environment = SurveyUser('survey/ver1_pilot/data/02.txt')
    environment = MTurkSurveyUser(filePaths=[
            'survey/ver2_mturk/results/01_1st_Batch_3137574_batch_results.csv',
            'survey/ver2_mturk/results/02_Batch_3148398_batch_results.csv',
            'survey/ver2_mturk/results/03_Batch_3149214_batch_results.csv',
    ], filterFunc=(lambda r: ord(r['rawWorkerID'][-1]) % 3 == 2))

    ### user daily routing modevior = RandomBehavior()

    #behavior = ExtraSensoryBehavior('behavior/data/2.txt')
    #behavior = ExtraSensoryBehavior('behavior/data/4.txt')
    #behavior = ExtraSensoryBehavior('behavior/data/5.txt')
    behavior = ExtraSensoryBehavior('behavior/data/6.txt')

    episodeLengthDay = 7  # E.g., 1 or 7 (1 week)

    env_config = {
            "rewardCriteria": rewardCriteria,
            "environment": environment,
            "behavior": behavior,
            "verbose": verbose,
            "episodeLengthDay": episodeLengthDay,
    }
    return EngagementGym(env_config)


def get_agent():
    ### DQNAgent
    return DQNAgent(
            states=dict(type='float', shape=(5,)),
            actions=dict(type='int', num_actions=2),
            network=[
                dict(type='dense', size=8),
                dict(type='dense', size=8)
            ],
            batched_observe=False,
    )


if __name__ == "__main__":
    env = get_environment()
    agent = get_agent()

    # here we go
    state = env.reset()
    total_reward = 0
    for i in range(100000):
        #print(state)
        action = agent.act(state)
        #print(action)
        state, reward, done, _ = env.step(action)
        #print(reward)
        agent.observe(reward=reward, terminal=done)
        if done:
            env.reset()

import dill

from django.test import TestCase

from nurture.learning.state import State
from nurture.learning.agents import *


class LearningAgentTestCase(TestCase):

    def test_going_through_learning_process(self):

        AllAgentClasses = [
                DebugAgent,
                SilentAgent,
                RandomAgent,
                Attelia2Agent,
                QLearningAgent,
                QLearningPrioritizedReplayAgent,
                TensorForceDQNAgent,
        ]

        model_path = '/tmp/nurture_test_model.p'

        dummyState1 = State(
                timeOfDay=0.501,
                dayOfWeek=0.123,
                motion=State.MOTION_STATIONARY,
                location=State.LOCATION_HOME,
                notificationTimeElapsed=3.,
                ringerMode=State.RINGER_MODE_SILENT,
                screenStatus=State.SCREEN_STATUS_ON,
        )
        dummyState2 = State(
                timeOfDay=0.505,
                dayOfWeek=0.124,
                motion=State.MOTION_WALKING,
                location=State.LOCATION_WORK,
                notificationTimeElapsed=1.,
                ringerMode=State.RINGER_MODE_VIBRATE,
                screenStatus=State.SCREEN_STATUS_OFF,
        )
        dummyState3 = State(
                timeOfDay=0.509,
                dayOfWeek=0.125,
                motion=State.MOTION_RUNNING,
                location=State.LOCATION_OTHER,
                notificationTimeElapsed=2.,
                ringerMode=State.RINGER_MODE_NORMAL,
                screenStatus=State.SCREEN_STATUS_ON,
        )

        for LearningClass in AllAgentClasses:

            # model initialization
            agent = LearningClass()
            agent.on_pickle_save()
            dill.dump(agent, open(model_path, "wb"))
            
            # normal process
            agent = dill.load(open(model_path, 'rb'))
            agent.on_pickle_load()
            agent.feed_reward(0.2)
            _ = agent.get_action(dummyState1)
            agent.on_pickle_save()
            dill.dump(agent, open(model_path, "wb"))
            
            # restart signal rings
            agent = dill.load(open(model_path, 'rb'))
            agent.on_pickle_load()
            agent.restart_episode()
            _ = agent.get_action(dummyState1)
            agent.on_pickle_save()
            dill.dump(agent, open(model_path, "wb"))

            # exhaustive process
            agent = dill.load(open(model_path, 'rb'))
            agent.on_pickle_load()
            for _ in range(500):
                agent.feed_reward(0.4)
                _ = agent.get_action(dummyState1)
                agent.feed_reward(0.0)
                _ = agent.get_action(dummyState2)
                agent.feed_reward(-5.0)
                _ = agent.get_action(dummyState3)

            agent.restart_episode()
            agent.on_pickle_save()
            dill.dump(agent, open(model_path, "wb"))


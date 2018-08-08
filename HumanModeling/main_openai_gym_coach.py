import os
import json
import sys

sys.path.append('coach')
from presets import json_to_preset
from configurations import Frameworks
from environments import create_environment
from agents import *

#from constant import *
#from environment import *
#from behavior import *
#from openai_gym.engagement_gym import EngagementGym


# NOTE: This is an experimental code, please use comments to switch different options.


def get_dqn_agent():
    return DQNAgent(
            states=dict(type='float', shape=(5,)),
            actions=dict(type='int', num_actions=2),
            network=[
                dict(type='dense', size=20),
                dict(type='dense', size=20)
            ],
            batched_observe=False,
    )

def get_ppo_agent():
    return PPOAgent(
        states=dict(type='float', shape=(5,)),
        actions=dict(type='int', num_actions=2),
        network=[
            dict(type='dense', size=20, activation='tanh'),
            dict(type='dense', size=20, activation='tanh'),
        ],
        #batch_size=256,
        # BatchAgent
        #keep_last_timestep=True,
        # PPOAgent
        step_optimizer=dict(
            type='adam',
            learning_rate=1e-3
        ),
        optimization_steps=10,
        # Model
        scope='ppo',
        discount=0.99,
        # DistributionModel
        #distributions_spec=None,
        entropy_regularization=0.01,
        # PGModel
        baseline_mode=None,
        baseline=None,
        baseline_optimizer=None,
        gae_lambda=None,
        # PGLRModel
        likelihood_ratio_clipping=0.2,
        #summary_spec=None,
        #distributed_spec=None,
    )

def get_agent():
    return get_dqn_agent()
    #return get_ppo_agent()

def run_dict_to_json(_run_dict, task_id=''):
    if task_id != '':
        json_path = os.path.join(_run_dict['experiment_path'], 'run_dict_worker{}.json'.format(task_id))
    else:
        json_path = os.path.join(_run_dict['experiment_path'], 'run_dict.json')

    with open(json_path, 'w') as outfile:
        json.dump(_run_dict, outfile, indent=2)

    return json_path

def set_framework(framework_type):
    # choosing neural network framework
    """
    framework = Frameworks().get(framework_type)
    sess = None
    if framework == Frameworks.TensorFlow:
        import tensorflow as tf
        config = tf.ConfigProto()
        config.allow_soft_placement = True
        config.gpu_options.allow_growth = True
        config.gpu_options.per_process_gpu_memory_fraction = 0.2
        sess = tf.Session(config=config)
    elif framework == Frameworks.Neon:
        import ngraph as ng
        sess = ng.transformers.make_transformer()
    screen.log_title("Using {} framework".format(Frameworks().to_string(framework)))
    return sess
    """

    framework = Frameworks().get(framework_type)
    if framework != Frameworks.TensorFlow:
        raise Exception("force to use TensorFlow")
    
    import tensorflow as tf
    config = tf.ConfigProto()
    config.allow_soft_placement = True
    config.gpu_options.allow_growth = True
    config.gpu_options.per_process_gpu_memory_fraction = 0.2
    return tf.Session(config=config)
    

if __name__ == "__main__":
    run_dict = {
        'agent_type': None,
        'environment_type': None,
        'exploration_policy_type': None,
        'level': None,
        'preset':
        'BasicEngagement_A3C',
        'custom_parameter': None,
        'experiment_path': '/tmp/coach',
        'framework': 1,
        'play': False,
        'evaluate': False,
        'num_threads': 1,
        'save_model_sec': None,
        'save_model_dir': None,
        'checkpoint_restore_dir': None,
        'visualization.dump_gifs': False,
        'visualization.render': False,
        'visualization.tensorboard': False,
    }

    json_run_dict_path = run_dict_to_json(run_dict)
    tuning_parameters = json_to_preset(json_run_dict_path)
    tuning_parameters.sess = set_framework('tensorflow')

    #if args.print_parameters:
    #    print('tuning_parameters', tuning_parameters)

    # Single-thread runs
    tuning_parameters.task_index = 0
    env_instance = create_environment(tuning_parameters)
    agent = eval(tuning_parameters.agent.type + '(env_instance, tuning_parameters)')

    print("========================")
    print(tuning_parameters.evaluate)
    print("========================")

    # Start the training or evaluation
    #if tuning_parameters.evaluate:
    #    agent.evaluate(sys.maxsize, keep_networks_synced=True)  # evaluate forever
    #else:
    #    agent.improve()

    gen = agent.improve()
    for _ in range(10):
        next(gen)


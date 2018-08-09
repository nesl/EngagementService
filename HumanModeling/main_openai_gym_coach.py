import os
import json
import sys

sys.path.append('coach')
from presets import json_to_preset
from configurations import Frameworks
from environments import create_environment
from agents import *


# NOTE: This is an experimental code, please use comments to switch different options.


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
    
class NurturePreset(Preset):
    def __init__(self):
        Preset.__init__(self, ActorCritic, GymVectorObservation, CategoricalExploration)
        self.env.level = 'Engagement-v0'
        self.agent.policy_gradient_rescaler = 'GAE'
        self.learning_rate = 0.0001
        self.num_heatup_steps = 200
        self.env.reward_scaling = 1.
        self.agent.apply_gradients_every_x_episodes = 1
        self.agent.num_steps_between_gradient_updates = 20
        self.agent.gae_lambda = 1
        self.agent.beta_entropy = 0.05
        self.clip_gradients = 40.0
        self.agent.middleware_type = MiddlewareTypes.FC


def get_tuning_parameters(run_dict):

    #if run_dict['preset'] is None:
    #    tuning_parameters = Preset(eval(run_dict['agent_type']), eval(run_dict['environment_type']),
    #                               eval(run_dict['exploration_policy_type']))
    #else:
    #    tuning_parameters = eval(run_dict['preset'])()
    #    # Override existing parts of the preset
    #    if run_dict['agent_type'] is not None:
    #        tuning_parameters.agent = eval(run_dict['agent_type'])()
    #
    #    if run_dict['environment_type'] is not None:
    #        tuning_parameters.env = eval(run_dict['environment_type'])()
    #
    #    if run_dict['exploration_policy_type'] is not None:
    #        tuning_parameters.exploration = eval(run_dict['exploration_policy_type'])()

    tuning_parameters = NurturePreset()

    # human control
    if run_dict['play']:
        tuning_parameters.agent.type = 'HumanAgent'
        tuning_parameters.env.human_control = True
        tuning_parameters.num_heatup_steps = 0
        
    if run_dict['level']:
        tuning_parameters.env.level = run_dict['level']

    if run_dict['custom_parameter'] is not None:
        unstripped_key_value_pairs = [pair.split('=') for pair in run_dict['custom_parameter'].split(';')]
        stripped_key_value_pairs = [tuple([pair[0].strip(), ast.literal_eval(pair[1].strip())]) for pair in
                                    unstripped_key_value_pairs]

        # load custom parameters into run_dict
        for key, value in stripped_key_value_pairs:
            run_dict[key] = value

    for key in ['agent_type', 'environment_type', 'exploration_policy_type', 'preset', 'custom_parameter']:
        run_dict.pop(key, None)

    # load parameters from run_dict to tuning_parameters
    for key, value in run_dict.items():
        if ((sys.version_info[0] == 2 and type(value) == unicode) or
                (sys.version_info[0] == 3 and type(value) == str)):
            value = '"{}"'.format(value)
        exec('tuning_parameters.{} = {}'.format(key, value)) in globals(), locals()

    return tuning_parameters

if __name__ == "__main__":
    run_dict = {
        'agent_type': None,
        'environment_type': None,
        'exploration_policy_type': None,
        'level': None,
        'preset': 'BasicEngagement_A3C',
        'custom_parameter': None,
        'experiment_path': '/tmp/coach',
        'framework': 1,
        'play': False,
        'evaluate': False,
        'num_threads': 1,
        'save_model_sec': 10,
        'save_model_dir': '/tmp/coach_y/',
        'checkpoint_restore_dir': '/tmp/coach_x/',
        #'save_model_sec': None,
        #'save_model_dir': None,
        #'checkpoint_restore_dir': None,
        'visualization.dump_gifs': False,
        'visualization.render': False,
        'visualization.tensorboard': False,
    }

    tuning_parameters = get_tuning_parameters(run_dict)
    tuning_parameters.sess = set_framework('tensorflow')

    #if args.print_parameters:
    #    print('tuning_parameters', tuning_parameters)

    # Single-thread runs
    tuning_parameters.task_index = 0
    env_instance = create_environment(tuning_parameters)
    agent = eval(tuning_parameters.agent.type + '(env_instance, tuning_parameters)')

    #print("========================")
    #print(type(tuning_parameters))
    #print("========================")
    #print(tuning_parameters)
    #print("========================")

    # Start the training or evaluation
    #if tuning_parameters.evaluate:
    #    agent.evaluate(sys.maxsize, keep_networks_synced=True)  # evaluate forever
    #else:
    #    agent.improve()

    gen = agent.improve()
    for _ in range(30):
        next(gen)

    """
    #agent.save_model(100)
    agent.save_model_to_dir('/tmp/coach_v8/')
    agent.save_model_to_dir('/tmp/coach_v8/')
    
    for _ in range(30000):
        next(gen)

    agent.restore_model_from_dir('/tmp/coach_v8/')
    for _ in range(30000):
        next(gen)
    """

    """
    import dill
    sys.setrecursionlimit(50000)
    agent.main_network = None
    agent.networks = None
    agent.sess = None
    agent.env = None
    agent.memory = None
    #agent.exploration_policy = None
    #agent.evaluation_exploration_policy = None
    #agent.signals = None
    #agent.entropy = None
    #agent.action_advantages = None
    #agent.state_values = None
    #agent.unclipped_grads = None
    #agent.num_episodes_where_step_has_been_seen = None
    #agent.curr_stack = None
    #agent.loss = None
    #agent.curr_state = None
    agent.renderer = None
    #agent.curr_learning_rate = None
    #agent.mean_return_over_multiple_episodes = None
    #agent.value_loss = None
    #agent.policy_loss = None
    #agent.state_value = None
    agent.tp = None

    print(agent.__dict__)
    dill.dump(agent, open("/tmp/a3c.p", "wb"))
    dill.dump(gen, open("/tmp/gen.p", "wb"))
    """



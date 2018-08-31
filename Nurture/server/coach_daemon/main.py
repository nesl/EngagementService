"""
This script is tested and copied from <EngagementService>/HumanModeling/main_openai_gym_coach.py.
The goal of this script is to serve as a daemon to serve `CoachA3CAgent` in Django webserver.
"""

import os
import json
import sys
import shutil
import datetime

from gym.envs.registration import register

os.chdir(os.path.dirname(os.path.abspath(__file__)))
sys.path.append('coach')
from configurations import Frameworks
from environments import create_environment
from agents import *
from exploration_policies.e_greedy import EGreedy


DATETIME_FORMAT = "%Y/%b/%d %H:%M:%S.%f"


def set_framework(framework_type):
    # choosing neural network framework
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
        #Preset.__init__(self, ActorCritic, GymVectorObservation, EGreedyExploration)
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

"""
class NurturePreset(Preset):
    def __init__(self):
        Preset.__init__(self, ClippedPPO, GymVectorObservation, CategoricalExploration)
        self.env.level = 'Engagement-v0'
        self.learning_rate = 0.0001
        self.num_heatup_steps = 0
        self.agent.num_consecutive_training_steps = 1
        self.agent.num_consecutive_playing_steps = 512
        self.agent.discount = 0.99
        self.batch_size = 64
        self.agent.policy_gradient_rescaler = 'GAE'
        self.agent.gae_lambda = 0.95
        self.visualization.dump_csv = True
        self.agent.optimizer_type = 'Adam'
        self.env.normalize_observation = True
"""

def load_step(step_file_path):
    if not os.path.isfile(step_file_path):
        raise Exception("Step file does not exist")
    with open(step_file_path) as f:
        res = int(f.readline().strip())
    return res

def get_epsilon_params(num_steps):
    # e_greedy policy in Coach decays in a linear manner. we adjust the params since we start
    # the learning process in the middle
    original_initial_epsilon = 0.5
    original_final_epsilon = 0.01
    original_epsilon_decay = 7000

    step_buffer = 100
    
    if num_steps > original_epsilon_decay - step_buffer:
        return original_final_epsilon * 1.01, original_final_epsilon, step_buffer
    else:
        delta = original_final_epsilon - original_initial_epsilon
        ratio = num_steps / original_epsilon_decay
        adjusted_initial_epsilon = original_initial_epsilon + delta * ratio
        adjusted_epsilon_decay = original_epsilon_decay - num_steps
        return adjusted_initial_epsilon, original_final_epsilon, adjusted_epsilon_decay

def get_tuning_parameters(run_dict):
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

def get_file_and_destroy(file_path):
    if not os.path.isfile(file_path):
        return None

    with open(file_path, 'r') as f:
        lines = f.readlines()

    os.remove(file_path)

    return lines

def get_reward_state(file_path):
    lines = get_file_and_destroy(file_path)
    if lines is None:
        return None

    if len(lines) != 4:
        print("not enough lines")
        return None
    lines = [l.strip() for l in lines]
    
    # parse time
    try:
        request_time = datetime.datetime.strptime(lines[0], DATETIME_FORMAT)
    except:
        print("cannot get timestamp")
        return None

    if datetime.datetime.now() - request_time > datetime.timedelta(seconds=30):
        print("seems to be an old request")
        return None

    try:
        reward = float(lines[1])
    except:
        print("cannot get reward")
        return None

    try:
        done = (lines[2] == 'True')
    except:
        print("cannot get done")
        return None

    try:
        state = [float(v) for v in lines[3].split(',')]
    except:
        print("cannot get states")
        return None
    
    if len(state) != 15:
        print("wrong state dimension")
        return None

    return reward, done, state


if __name__ == "__main__":

    # parse sys
    if len(sys.argv) <= 1:
        print('Error: %s uid' % sys.argv[0])
        exit(0)

    uid = sys.argv[1]
    if len(uid) != 5 or not uid.isdigit():
        print('Error: UID should be a 5-digit string. Abort')
        exit(0)

    # prepare models
    user_folder = os.path.join('checkpoints', uid)
    if not os.path.isdir(user_folder):
        initial_checkpoint_folder = os.path.join('checkpoints', 'initial')
        shutil.copytree(initial_checkpoint_folder, user_folder)
        checkpoint_restore_dir = user_folder

    if not os.path.isdir(user_folder):
        os.makedirs(user_folder)

    checkpoint_save_dir = os.path.join(user_folder, 'checkpoints')
    checkpoint_restore_dir = checkpoint_save_dir if os.path.isdir(checkpoint_save_dir) else None
    

    experiment_path = os.path.join('/tmp', 'nurture-coach', uid,
            datetime.datetime.now().strftime('%m%d-%H%M%S'))
    os.makedirs(experiment_path, exist_ok=True)

    reward_state_path = os.path.join(user_folder, 'reward_state.txt')
    action_path = os.path.join(user_folder, 'action.txt')
    heartbeat_path = os.path.join(user_folder, 'heartbeat.txt')
    step_path = os.path.join(user_folder, 'step.txt')
    action_likelihood_path = os.path.join(user_folder, 'action_likelihood.txt')

    num_steps = load_step(step_path) if checkpoint_restore_dir is not None else 0

    initial_epsilon, final_epsilon, epsilon_decay = get_epsilon_params(num_steps)

    # prepare the gym environment
    register(
        id='Engagement-v0',
        entry_point='engagement_shell_gym:EngagementShellGym',
        max_episode_steps = 10000,
    )

    # prepare the agent
    run_dict = {
        'agent_type': None,
        'environment_type': None,
        'exploration_policy_type': None,
        'level': None,
        'preset': 'BasicEngagement_A3C',
        'custom_parameter': None,
        'experiment_path': experiment_path,
        'framework': 1,
        'play': False,
        'evaluate': False,
        'num_threads': 1,
        'save_model_sec': 30,
        'save_model_dir': checkpoint_save_dir,
        'checkpoint_restore_dir': checkpoint_restore_dir,
        'visualization.dump_gifs': False,
        'visualization.render': False,
        'visualization.tensorboard': False,
        'exploration.initial_epsilon': initial_epsilon,
        'exploration.final_epsilon': final_epsilon,
        'exploration.epsilon_decay_steps': epsilon_decay,
        # extra customized properties
        'action_likelihood_path': action_likelihood_path,
    }

    tuning_parameters = get_tuning_parameters(run_dict)
    tuning_parameters.sess = set_framework('tensorflow')

    # Single-thread runs
    tuning_parameters.task_index = 0
    env_instance = create_environment(tuning_parameters)
    agent = eval(tuning_parameters.agent.type + '(env_instance, tuning_parameters)')

    gen = agent.improve()
    while True:
        results = get_reward_state(reward_state_path)
        if results is not None:
            reward, done, state = results
            with open(action_likelihood_path, 'a') as fo:
                fo.write("%s\treward\t%.2f\n" % (
                    datetime.datetime.now().strftime(DATETIME_FORMAT), reward))
                fo.write("%s\tstate\t%s\n" % (
                    datetime.datetime.now().strftime(DATETIME_FORMAT),
                    ','.join(list(map(str, state))),
                ))
            num_steps += 1
            print('[%s-%s-%d] processing, get reward: %.1f' %
                    (uid, datetime.datetime.now().strftime("%H:%M:%S"), num_steps, reward))
            env_instance.env.env.supply(reward, done, state)
            action = next(gen)
            with open(action_path, 'w') as fo:
                fo.write(datetime.datetime.now().strftime(DATETIME_FORMAT) + "\n")
                fo.write(str(action) + "\n")
            with open(action_likelihood_path, 'a') as fo:
                fo.write("%s\taction\t%s\n" % (
                    datetime.datetime.now().strftime(DATETIME_FORMAT), str(action)))
            with open(step_path, 'w') as fo:
                fo.write(str(num_steps))
            print('processed, action', action)

        with open(heartbeat_path, 'w') as fo:
            fo.write(datetime.datetime.now().strftime(DATETIME_FORMAT))

        time.sleep(0.3)

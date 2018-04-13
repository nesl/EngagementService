from multiprocessing import Pool
from functools import partial
import os
import numpy as np

from mdp_sequence_reader import MDPSequenceReader
from training_pomc_util import *
from pomc import PartiallyObservableMarkovChain


def train_model(num_states, round_idx, action_space, num_observables, actions, observations,
                max_em_iterations=6, converge_improvement_threshold=2., converge_improve_retry=3):
    
    # initialize model
    init_model = initialize_random_pomc(num_states, action_space, num_observables)
    
    # iterate the model
    model = init_model
    log_likelihood = -1e100  # very small
    best_log_likelihood = log_likelihood
    convergence_count = 0
    for r in range(max_em_iterations):
        alist, c = improve_params(xs=actions, ys=observations, m=model)
        new_model = PartiallyObservableMarkovChain(alist, c, model.init)

        next_log_likelihood = get_log_likelihood(make_tableaus(xs=actions, ys=observations, m=new_model))
        #print("round=%d, log likelihood=%f" % (r+1, next_log_likelihood))

        model = new_model
        log_likelihood = next_log_likelihood
    
        # check convergence condition
        if log_likelihood - best_log_likelihood > converge_improvement_threshold:
            convergence_count = 0
        else:
            convergence_count += 1
            if convergence_count == converge_improve_retry:
                break
        
        best_log_likelihood = max(best_log_likelihood, log_likelihood)
    
    print("num_states=%d, round_idx=%d, log_likelihood=%f" % (num_states, round_idx, log_likelihood))
    return num_states, round_idx,  model, log_likelihood, best_log_likelihood


def process_grid_world(sequence_root_dir, pomdp_setup_root_dir, scenario_name, max_em_iterations,
                       min_num_states, max_num_states, model_retries):
    # read the sequence
    reader = MDPSequenceReader(os.path.join(sequence_root_dir, scenario_name + '.txt'))

    # generate environment file
    pomdp_setup_folder = os.path.join(pomdp_setup_root_dir, scenario_name)
    if not os.path.exists(pomdp_setup_folder):
        os.makedirs(pomdp_setup_folder)
    reader.generate_grid_world_setup_file(os.path.join(pomdp_setup_folder, 'world.txt'))

    # get the sequences
    observations = reader.get_observation_sequence()
    actions = reader.get_action_sequence()

    # get the initial model
    action_space = [0, 1, 2, 3]   # (up, right, down, left)
    num_observables = 16   # binary measurements of 4 directions

    # generate task info for multi-processing
    tasks = []
    for i_num_states in range(min_num_states, max_num_states+1):
        for i_round in range(model_retries):
            tasks.append((i_num_states, i_round))

    # do it so fast!
    partially_fed_train_model_func = partial(
        train_model,
        action_space=action_space,
        num_observables=num_observables,
        actions=actions,
        observations=observations,
        max_em_iterations=max_em_iterations,
    )
    results = Pool().starmap(partially_fed_train_model_func, tasks)

    # generate outputs
    result_table = [[None for _ in range(model_retries)] for _ in range(max_num_states+1)]
    for result in results:
        num_states, round_idx, *rest = result
        result_table[num_states][round_idx] = rest
        
    pomc_models_folder = os.path.join(pomdp_setup_folder, 'pomc_models')
    if not os.path.exists(pomc_models_folder):
        os.makedirs(pomc_models_folder)
    for i_num_states in range(min_num_states, max_num_states+1):
        best_model_idx = np.argmax([logl for _, logl, _ in result_table[i_num_states]])
        best_model, best_final_likelihood, _ = result_table[i_num_states][best_model_idx]
        print("%d states, best likelihood=%f" % (i_num_states, best_final_likelihood))
        best_model.dump(os.path.join(pomc_models_folder, "best_%d_states.txt" % i_num_states))

    # generate likelihood report
    likelihood_report_file_path = os.path.join('output', scenario_name + '.txt')
    with open(likelihood_report_file_path, 'a') as fo:
        for i_num_states in range(min_num_states, max_num_states+1):
            best_model_idx = np.argmax([logl for _, logl, _ in result_table[i_num_states]])
            best_model, best_final_likelihood, _ = result_table[i_num_states][best_model_idx]
            fo.write("    %f,  # %d states\n" % (best_final_likelihood, i_num_states))


def try_process_grid_world(*args, **kwargs):
    try:
        process_grid_world(*args, **kwargs)
    except:
        pass



# main function
try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='sharp_0.25_0.10',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=17,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.05_0.02',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.10_0.04',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.15_0.06',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.20_0.08',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.25_0.10',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.05_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.10_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.15_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.20_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0.25_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0.02',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0.04',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0.06',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0.08',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='eight_0_0.10',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=25,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='o_0.25_0',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=15,
    model_retries=6,
)

try_process_grid_world(
    sequence_root_dir='sequence',
    pomdp_setup_root_dir='pomdp_setup',
    scenario_name='o_0_0.10',
    max_em_iterations=50,
    min_num_states=2,
    max_num_states=15,
    model_retries=6,
)

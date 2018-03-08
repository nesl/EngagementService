import itertools
import random
import numpy as np


def generate_map_and_sequence(in_filename, out_filename, prob_actuator_error, prob_sensor_error,
        num_steps=10000):
    """
    This function first reads a grid file, convert each square into a state, and simulate a
    robot walk around this area. All the states are represented by integers ranging from 0 to N-1,
    where N is the number of states. The robot will randomly go up, right, down, and left. The
    corresponding action codes are 0, 1, 2, 3. If the robot bump into a wall, it will stay in the
    same square. The prob_actuator_error indicates the probability that the actuator of the robot
    does not function correctly and will randomly move to any direction. When stepping into a new
    square, the robot makes an observation. The observation is represented by a 4-digit binary
    number, each digit corresponds to whether there is a wall to the north, east, south, and west
    of the robot.

        0            up
      3   1    left      right
        2           down

    The prob_sensor_error shows how likely the sensor gets the wrong reading.

    The file is composed of several lines, each character represents the status of a square. '#'
    symbol means the square is available. '.' means it is occupied. 'R' means it is the destination
    that the agent should reach. We provide an example below:

    .#..
    ##R#
    .#..
    .#..

    Note there should be exactly one 'R' symbol in the grid.

    The output file includes the following sections:

    - state assignment map
    - state details
    - simulation parameters
    - sequence

    The 4 sections are separated by == line.

    State assignment section includes a matrix showing the state id. If it is occupied, a period
    ('.') is filled in.

    State details section shows the attributes of each state, one per line. The format is
    "state_id coor_row coor_column up_state_id right_state_id down_state_id left_state_id".
    If there is a wall, the next state id will be itself.

    Simulation parameters section lists all the parameters.

    Sequence section lists the records of all the steps, including state, observation, and action.
    One per line.
    """
    
    # state assignment
    with open(in_filename) as f:
        lines = f.readlines()

    num_rows = len(lines)
    num_cols = max([len(l) for l in lines])
    if num_rows == 0 or num_cols == 0:
        raise Exception("Invalid grid (empty map)")

    coors_nested_list = [[(ridx, cidx, ch) for cidx, ch in enumerate(line) if ch in ['#', 'R']]
            for ridx, line in enumerate(lines)]
    square_bundles = list(itertools.chain.from_iterable(coors_nested_list))

    reward_square_coor_candidates = [
        (ridx, cidx) for ridx, cidx, ch in square_bundles if ch == 'R']
    if len(reward_square_coor_candidates) != 1:
        raise Exception("Invalid grid (there should be exactly one reward spot)")

    reward_coor = reward_square_coor_candidates[0]

    square_coors = [(ridx, cidx) for ridx, cidx, ch in square_bundles]

    if len(square_coors) == 1:
        raise Exception("Invalid grid (no other available spots)")

    with open(out_filename, 'w') as fo:
        
        # produce state assignment section
        coors_to_ids = {}
        ids_to_coors = []
        cnt = 0
        for r in range(num_rows):
            for c in range(num_cols):
                coor = (r, c)
                if coor in square_coors:
                    fo.write("%3d" % cnt)
                    coors_to_ids[coor] = cnt
                    ids_to_coors.append(coor)
                    cnt += 1
                else:
                    fo.write("   ")
            fo.write("\n")

        fo.write("==\n")

        # state detail section
        actions_to_movements = [
            (-1,  0),  # 0, up
            ( 0,  1),  # 1, right
            ( 1,  0),  # 2, down
            ( 0, -1),  # 3, left
        ]

        num_coors = len(ids_to_coors)

        next_ids = [None for _ in range(num_coors)]
        for i in range(num_coors):
            next_spots = [0, 0, 0, 0]
            curr, curc = ids_to_coors[i]
            for action_id, movement in enumerate(actions_to_movements):
                mover, movec = actions_to_movements[action_id]
                new_coor = (curr + mover, curc + movec)
                next_spots[action_id] = coors_to_ids[new_coor] if new_coor in coors_to_ids else i
            next_ids[i] = next_spots

        for i in range(num_coors):
            fo.write("%s\n" % " ".join(list(map(str, list(itertools.chain.from_iterable([
                    [i],
                    list(ids_to_coors[i]),
                    [10 if ids_to_coors[i] == reward_coor else -1],
                    next_ids[i],
            ]))))))

        fo.write("==\n")

        # simulation parameters section
        fo.write(str(prob_actuator_error) + "  # prob_actuator_error\n")
        fo.write(str(prob_sensor_error) + "  # prob_sensor_error\n")

        fo.write("==\n")

        # sequence section
        cur_coor = random.choice(list(coors_to_ids.keys()))
        
        for _ in range(num_steps):
            cur_state_id = coors_to_ids[cur_coor]
            curr, curc = cur_coor

            observation = 0
            for action_id, movement in enumerate(actions_to_movements):
                dr, dc = movement
                obs_coor = (curr + dr, curc + dc)
                has_wall = int(obs_coor not in coors_to_ids)
                obs_error = int(random.random() < prob_sensor_error)
                observation |= (has_wall ^ obs_error) << action_id

            chosen_action_id = np.random.randint(4)
            taken_action_id = (np.random.randint(4) if random.random() < prob_actuator_error
                    else chosen_action_id)
            mover, movec = actions_to_movements[taken_action_id]
            new_coor = (curr + mover, curc + movec)
            if new_coor not in coors_to_ids:
                new_coor = cur_coor

            fo.write("%d %d %d\n" % (cur_state_id, observation, chosen_action_id))

            cur_coor = new_coor

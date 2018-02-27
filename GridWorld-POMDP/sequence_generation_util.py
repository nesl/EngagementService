import itertools
import random
import numpy as np


def generate_map_and_sequence(in_filename, out_filename, prob_actuator_error, prob_sensor_error, num_steps=10000):
    """
    This function first reads a grid file, convert each square into a state, and simulate a
    robot walk around this area. All the states are represented by integers ranging from 0 to N-1,
    where N is the number of states. The robot will randomly go up, right, down, and left. The
    corresponding action codes are 0, 1, 2, 3. If the robot bump into a wall, it will stay in the
    same square. The prob_actuator_error indicates the probability that the actuator of the robot
    does not function correctly and will randomly move to any direction. When stepping into a new
    square, the robot makes an observation. The observation is represented by a 4-digit binary
    number, each digit corresponds if there is a wall to the north, east, south, and west of the
    robot.

        0            up
      3   1    left      right
        2           down

    The prob_sensor_error shows how likely the sensor gets the wrong reading.

    The file format is a couple lines representing the grid. '#' symbol means the square is
    available. '.' means it is occupied. We provide an example below:

    .#..
    ####
    .#..
    .#..

    The output file includes the following sections:

    - state assignment
    - Simulation parameters
    - sequence

    The 3 sections are separated by == line.

    State assignment section includes a matrix showing the state id. If it is occupied, a period
    ('.') is filled in.

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

    coors_nested_list = [[(ridx, cidx) for cidx, ch in enumerate(line) if ch == '#']
            for ridx, line in enumerate(lines)]
    square_coors = list(itertools.chain.from_iterable(coors_nested_list))

    if len(square_coors) == 0:
        raise Exception("Invalid grid (no available spot)")

    with open(out_filename, 'w') as fo:
        
        # produce state assignment section
        coors_to_ids = {}
        cnt = 0
        for r in range(num_rows):
            for c in range(num_cols):
                coor = (r, c)
                if coor in square_coors:
                    fo.write("%3d" % cnt)
                    coors_to_ids[coor] = cnt
                    cnt += 1
                else:
                    fo.write("   ")
            fo.write("\n")

        fo.write("==\n")

        # simulation parameters section
        fo.write(str(prob_actuator_error) + "  # prob_actuator_error\n")
        fo.write(str(prob_sensor_error) + "  # prob_sensor_error\n")

        fo.write("==\n")

        # sequence section
        cur_coor = random.choice(list(coors_to_ids.keys()))
        
        actions_to_movements = [
            (-1,  0),  # 0, up
            ( 0,  1),  # 1, right
            ( 1,  0),  # 2, down
            ( 0, -1),  # 3, left
        ]

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

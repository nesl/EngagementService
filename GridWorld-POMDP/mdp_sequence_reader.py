import itertools


class MDPSequenceReader:
    
    def __init__(self, in_filename):
        with open(in_filename) as f:
            lines = [l.strip() for l in f.readlines()]

        partition_line_idx = [i for i, line in enumerate(lines) if line == '==']
        if len(partition_line_idx) < 3:
            raise Exception("Bad format (expect 4 sections)")

        p1, p2, p3 = partition_line_idx[:3]
        self._parse_grid(lines[:p1])
        self._parse_state_details(lines[p1+1:p2])
        self._parse_params(lines[p2+1:p3])
        self._parse_sequence(lines[p3+1:])

    def get_num_states(self):
        return self.num_states

    def get_state_sequence(self):
        return self.state_sequence

    def get_action_sequence(self):
        return self.action_sequence

    def get_observation_sequence(self):
        return self.observation_sequence

    def get_num_steps(self):
        return len(self.state_sequence)

    def get_prob_actuator_error(self):
        return self.prob_actuator_error

    def get_prob_sensor_error(self):
        return self.prob_sensor_error

    def generate_grid_world_setup_file(self, out_filename):
        """
        The format of the file is the following:

        number_of_states
        state0_ID coor_row coor_column reward state0_up_ID state0_right_ID state0_down_ID state0_left_ID
        state1_ID coor_row coor_column reward state1_up_ID state1_right_ID state1_down_ID state1_left_ID
        ...

        Example:

        8
        0 0 0 -1 0 1 3 0
        1 0 1 -1 1 2 1 0
        2 0 2 10 2 2 4 1
        3 1 0 -1 0 3 5 3
        4 1 2 -1 2 4 7 4
        5 2 0 -1 3 6 5 5
        6 2 1 -1 6 7 6 5
        7 2 2 -1 4 7 7 6
        """
        with open(out_filename, 'w') as fo:
            fo.write("\n".join(list(map(str, list(itertools.chain.from_iterable([
                    [str(self.num_states)],
                    self.state_details_raw_lines,
            ]))))))

    def _parse_grid(self, lines):
        for line in reversed(lines):
            terms = list(filter(lambda s: len(s) > 0, line.split(' ')))
            if len(terms) > 0:
                self.num_states = int(terms[-1]) + 1  # seems it's 0-indexed
                return
        raise Exception("Incorrect grid format, no state is found")

    def _parse_state_details(self, lines):
        self.state_details_raw_lines = lines

    def _parse_params(self, lines):
        self.prob_actuator_error = self._get_float_from_string_head(lines[0])
        self.prob_sensor_error = self._get_float_from_string_head(lines[1])

    def _parse_sequence(self, lines):
        tuple_list = [tuple(map(int, l.split(' '))) for l in lines]
        self.state_sequence, self.observation_sequence, self.action_sequence = zip(*tuple_list)

    def _get_float_from_string_head(self, line):
        return float(line.strip().split(' ')[0])

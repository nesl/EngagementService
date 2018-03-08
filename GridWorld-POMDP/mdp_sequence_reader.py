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

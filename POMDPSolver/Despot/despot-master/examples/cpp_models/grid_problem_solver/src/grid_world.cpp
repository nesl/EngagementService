#include "grid_world.h"


using namespace std;

namespace despot {

int GridWorld::GetNextStateIdx_(int cur_state_idx, int intented_action) const {
	int action = intented_action;
	if (Random::RANDOM.NextDouble() < prob_actuator_error_)
		action = (int)(Random::RANDOM.NextDouble() * 4.);
	return next_state_idxs_[cur_state_idx][action];
}

OBS_TYPE GridWorld::GetNoisyObservation_(int state_idx) const {
	OBS_TYPE obs = 0;
	for (int i = 0; i < 4; i++) {
		bool is_wall = HitWall(state_idx, i);
		bool has_error = (Random::RANDOM.NextDouble() < prob_sensor_error_);
		obs |= (is_wall ^ has_error) << i;
	}
	return obs;
}


GridWorld::GridWorld(string filename) {
	/*
	 *  The description is copied from "mdp_sequence_reader.py", `generate_grid_world_setup_file()`
	 *  in `MDPSequenceReader` class.
	 *
     *  Format:
     *
	 *      number_of_states
     *      state0_ID coor_row coor_column reward state0_up_ID state0_right_ID state0_down_ID state0_left_ID
     *      state1_ID coor_row coor_column reward state1_up_ID state1_right_ID state1_down_ID state1_left_ID
     *      ...
     *      prob_actuator_error
	 *      prob_sensor_error
     *
     *  Example:
     *
     *      8
     *      0 0 0 -1 0 1 3 0
     *      1 0 1 -1 1 2 1 0
     *      2 0 2 10 2 2 4 1
     *      3 1 0 -1 0 3 5 3
     *      4 1 2 -1 2 4 7 4
     *      5 2 0 -1 3 6 5 5
     *      6 2 1 -1 6 7 6 5
     *      7 2 2 -1 4 7 7 6
	 *      0.1
	 *      0.1
	 */

	ifstream fin(filename.c_str());
	if (fin.fail()) {
		cerr << "Cannot open world file\"" << filename << "\". Abort" << endl;
		exit(0);
	}
	
	// get number of states
	fin >> num_true_states_;

	rewards_.resize(num_true_states_);
	square_coors_.resize(num_true_states_);
	next_state_idxs_.resize(num_true_states_);

	// get state infomation
	for (int i = 0; i < num_true_states_; i++) {
		int state_id;
		fin >> state_id
			>> square_coors_[i].first
			>> square_coors_[i].second
			>> rewards_[i]
			>> next_state_idxs_[i][ACT_UP]
			>> next_state_idxs_[i][ACT_RIGHT]
			>> next_state_idxs_[i][ACT_DOWN]
			>> next_state_idxs_[i][ACT_LEFT];
	}

	// determine terminal state
	terminal_state_idx_ = -1;
	for (int i = 0; i < num_true_states_; i++) {
		if (rewards_[i] > 0)
			terminal_state_idx_ = i;
	}

	// simulation settings
	fin >> prob_actuator_error_ >> prob_sensor_error_;

	fin.close();
}

bool GridWorld::HitWall(int cur_state_idx, int action) const {
	return next_state_idxs_[cur_state_idx][action] == cur_state_idx;
}

OBS_TYPE GridWorld::GetExpectedObservation(int state_idx) const {
	return GetNoisyObservation_(state_idx);
}

void GridWorld::PrintState(int state_idx, ostream& out) const {
	int r = square_coors_[state_idx].first;
	int c = square_coors_[state_idx].second;
	out << "true_state=" << state_idx << " (" << r << ", " << c << ")";
}

bool GridWorld::Step(int& state_idx, int action, double& reward, OBS_TYPE& obs) const {
	//cout << "in the GridWorld::Step function, action: " << action << endl;
	int next_state_idx = GetNextStateIdx_(state_idx, action);
	//cout << "get next state " << next_state_idx << endl;

	reward = rewards_[next_state_idx];

	//cout << "reward is " << reward << endl;
	obs = GetNoisyObservation_(next_state_idx);

	state_idx = next_state_idx;

	//cout << "observation is " << obs << endl;
	return next_state_idx == terminal_state_idx_;
}

} // namespace despot

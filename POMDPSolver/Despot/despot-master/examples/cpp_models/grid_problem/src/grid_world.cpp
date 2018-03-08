#include "grid_world.h"


using namespace std;

namespace despot {

const int GridWorld::MOVE_DR[4] = {-1,  0,  1,  0};
const int GridWorld::MOVE_DC[4] = { 0,  1,  0, -1};


int GridWorld::GetNextStateIdx_(int cur_state_idx, int action) const {
	int cur_r = square_coors_[cur_state_idx].first;
	int cur_c = square_coors_[cur_state_idx].second;
	int next_r = cur_r + MOVE_DR[action];
	int next_c = cur_c + MOVE_DC[action];

	if (next_r < 0 || next_r >= size_r_ || next_c < 0 || next_c >= size_c_)
		return cur_state_idx;

	if (state_idx_mat_[next_r][next_c] == -1)
		return cur_state_idx;

	return state_idx_mat_[next_r][next_c];
}

OBS_TYPE GridWorld::GetNoisyObservation_(int state_idx, double random_num) const {
	double dice[4];
	for (int i = 0; i < 4; i++) {
		random_num *= 100.;
		int t = (int)random_num;
		random_num -= t;
		dice[i] = (double)t / 100.;
	}

	OBS_TYPE obs = 0;
	for (int i = 0; i < 4; i++) {
		bool is_available = !HitWall(state_idx, i);
		bool has_error = (dice[i] < prob_sensor_error_);
		if (is_available ^ has_error)
			obs |= (1 << i);
	}
	return obs;
}


GridWorld::GridWorld(string filename) {
	/*
	 *  The file format is:
	 *  
	 *  size_r  size_c
	 *  r0_0  r0_1  ...
	 *  r1_0  r1_1  ...
	 *  ...
	 *  terminal_coor_r terminal_coor_c
	 *  prob_actuator_error
	 *  prob_sensor_error
	 *
	 *  An example:
	 *
	 *  3 3
	 *  -1  -1  10
	 *  -1  x   -1
	 *  -1  -1  -1
	 *  0 2
	 *  0.0
	 *  0.0
	 */

	ifstream fin(filename.c_str());
	
	// get size
	fin >> size_r_ >> size_c_;

	// inflate matrices
	state_idx_mat_.resize(size_r_, vector<int>(size_c_, -1));
	reward_mat_.resize(size_r_, vector<double>(size_c_));

	// fill in state index and reward
	num_true_states_ = 0;
	for (int i = 0; i < size_r_; i++) {
		for (int j = 0; j < size_c_; j++) {
			string tmp;
			fin >> tmp;
			if (tmp != "x") {
				double r = stod(tmp);
				reward_mat_[i][j] = r;
				state_idx_mat_[i][j] = num_true_states_++;
				square_coors_.push_back(make_pair(i, j));
			}
		}
	}

	// determine terminal state
	int terminal_coor_r, terminal_coor_c;
	fin >> terminal_coor_r >> terminal_coor_c;
	terminal_state_idx_ = state_idx_mat_[terminal_coor_r][terminal_coor_c];

	// simulation settings
	fin >> prob_actuator_error_ >> prob_sensor_error_;

	fin.close();
}

bool GridWorld::HitWall(int cur_state_idx, int action) const {
	return GetNextStateIdx_(cur_state_idx, action) == cur_state_idx;
}

OBS_TYPE GridWorld::GetExpectedObservation(int state_idx) const {
	return GetNoisyObservation_(state_idx, 0.);
}

double GridWorld::GetProbObservationGivenState(OBS_TYPE& obs, int state_idx) const {
	double res_prob = 1.;
	for (int i = 0; i < 4; i++) {
		bool sensedWall = !(obs & (1 << i));
		bool isWall = HitWall(state_idx, i);
		if (sensedWall != isWall)
			res_prob *= prob_sensor_error_;
		else
			res_prob *= 1.0 - prob_sensor_error_;
	}
	return res_prob;
}

void GridWorld::Print(int state_idx, ostream& out) const {
	int r = square_coors_[state_idx].first;
	int c = square_coors_[state_idx].second;
	out << "state=" << state_idx << " (" << r << ", " << c << ")";
}

bool GridWorld::Step(GridState &state, double random_num, int action, double& reward,
		OBS_TYPE& obs) const{
	//cout << "in the GridWorld::Step function, action: " << action << endl;
	int next_state_idx = GetNextStateIdx_(state.state_idx, action);
	//cout << "get next state " << next_state_idx << endl;
	state.state_idx = next_state_idx;

	int next_r = square_coors_[next_state_idx].first;
	int next_c = square_coors_[next_state_idx].second;
	reward = reward_mat_[next_r][next_c];

	//cout << "reward is " << reward << endl;
	obs = GetNoisyObservation_(next_state_idx, random_num);

	//cout << "observation is " << obs << endl;
	return next_state_idx == terminal_state_idx_;
}

} // namespace despot

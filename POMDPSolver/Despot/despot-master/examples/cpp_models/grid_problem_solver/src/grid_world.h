#ifndef GRID_WORLD_H
#define GRID_WORLD_H

#include <iostream>
#include <fstream>
#include <vector>
#include <cmath>

#include <despot/core/pomdp.h>


using namespace std;

namespace despot {

class GridWorld {
private:
	int num_true_states_;

	vector<double> rewards_;
	vector< pair<int, int> > square_coors_;
	vector< array<int, 4> > next_state_idxs_;
	double prob_actuator_error_;
	double prob_sensor_error_;

	int terminal_state_idx_;

	int GetNextStateIdx_(int cur_state_idx, int intented_action) const;
	OBS_TYPE GetNoisyObservation_(int state_idx) const;

public:
	static const int ACT_UP    = 0;
	static const int ACT_RIGHT = 1;
	static const int ACT_DOWN  = 2;
	static const int ACT_LEFT  = 3;

	GridWorld() {}
	GridWorld(string filename);

	inline int get_num_true_states() const { return num_true_states_; }
	bool HitWall(int cur_state_idx, int action) const;
	OBS_TYPE GetExpectedObservation(int state_idx) const;

	void PrintState(int state_idx, ostream& out) const;
	
	bool Step(int& state_idx, int action, double& reward, OBS_TYPE& obs) const;
};

} // namespace despot

#endif

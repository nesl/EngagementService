#ifndef GRID_STATE_H
#define GRID_STATE_H

#include <despot/core/pomdp.h>

#include <iostream>
#include <fstream>
#include <vector>


using namespace std;

namespace despot {

class GridState: public State {
public:
	int env_state_idx;
	int agent_state_idx;

	GridState() {
		// don't want to use it
		env_state_idx = -1;
		agent_state_idx = -2;
	}

	GridState(int env_idx, int agent_idx) : env_state_idx(env_idx), agent_state_idx(agent_idx) {}
};

} // namespace despot

#endif

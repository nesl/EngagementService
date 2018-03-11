#ifndef GRID_STATE_H
#define GRID_STATE_H

#include <despot/core/pomdp.h>

#include <iostream>
#include <fstream>
#include <vector>


using namespace std;

namespace despot {

class GridState: public State {
private:
	const static int OBFUSCATION_RANGE = 10000;
	const static int MOD = 10000;

	int state_idx_;

public:

	GridState() { state_idx_ = -1; } // don't want to use it
	GridState(int idx) : state_idx_(idx) {}

	inline void set_state_idx(int val, double random_val = 0.0) {
		state_idx_ = (int)(random_val * OBFUSCATION_RANGE) * MOD + val;
	}

	inline int get_state_idx() const {
		return state_idx_ % MOD;
	}

	inline int get_obfuscation_number() const {
		return state_idx_ / MOD;
	}
};

} // namespace despot

#endif

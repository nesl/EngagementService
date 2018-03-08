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
	int state_idx;

	GridState() { state_idx = -1; } // don't want to use it
	GridState(int idx) : state_idx(idx) {}
};

} // namespace despot

#endif

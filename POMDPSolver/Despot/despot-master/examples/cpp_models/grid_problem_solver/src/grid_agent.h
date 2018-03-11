#ifndef GRID_AGENT_H
#define GRID_AGENT_H

#include <iostream>
#include <vector>

#include <despot/core/pomdp.h>


using namespace std;

namespace despot {

class GridAgent {
private:
	int num_states_;
	int num_actions_;
	int num_observables_;

	vector< vector< vector<double> > > mat_T_;  // mat_T_[a][s1][s2] = Prob(s2 | s1, a)
	vector< vector<double> > mat_Z_;  // mat_Z_[o][s] = Prob(o | s)

	template<class T>
	vector< vector< vector<T> > > Init3DVector_(int d1, int d2, int d3);

	template<class T>
	vector< vector<T> > Init2DVector_(int d1, int d2);

public:
	GridAgent() {}
	GridAgent(string filename);

	int GetNumStates() const;

	void Step(int& state_idx, int action) const;
	double GetProbObservationGivenState(OBS_TYPE& obs, int state_idx) const;

	void PrintState(int state_idx, ostream& out) const;
};

} // namespace despot

#endif

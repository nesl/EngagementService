#ifndef GRID_H
#define GRID_H

#include <despot/core/pomdp.h>

#include "grid_state.h"
#include "grid_world.h"

using namespace std;

namespace despot {


class Grid: public DSPOMDP {
private:
	//mutable MemoryPool<State> memory_pool_;
	mutable MemoryPool<GridState> memory_pool_;

	GridWorld world_;
	

public:
	Grid(string params_file);

	int NumStates() const;
	
	inline int NumActions() const {
		return 4;  // up, down, left, right
	}

	bool Step(State& s, double random_num, int action, double& reward,
		OBS_TYPE& obs) const;
	double ObsProb(OBS_TYPE obs, const State& s, int a) const;

	State* CreateStartState(string type) const;
	Belief* InitialBelief(const State* start, string type = "DEFAULT") const;

	inline double GetMaxReward() const {
		return 10;
	}

	inline ValuedAction GetMinRewardAction() const {
		//return ValuedAction(LISTEN, -1);
		return ValuedAction(0, -1);
	}
	ScenarioLowerBound* CreateScenarioLowerBound(string name = "DEFAULT",
		string particle_bound_name = "DEFAULT") const;

	void PrintState(const State& state, ostream& out = cout) const;
	void PrintBelief(const Belief& belief, ostream& out = cout) const;
	void PrintObs(const State& state, OBS_TYPE obs, ostream& out = cout) const;
	void PrintAction(int action, ostream& out = cout) const;

	State* Allocate(int state_id, double weight) const;
	State* Copy(const State* particle) const;
	void Free(State* particle) const;
	int NumActiveParticles() const;
};

} // namespace despot

#endif

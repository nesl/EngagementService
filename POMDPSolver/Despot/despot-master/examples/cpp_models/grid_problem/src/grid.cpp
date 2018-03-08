#include "grid.h"

using namespace std;

namespace despot {


Grid::Grid(string params_file) : world_(params_file) {
}

int Grid::NumStates() const {
	//return num_true_states_;
	return 8;
}

bool Grid::Step(State& s, double random_num, int action, double& reward,
		OBS_TYPE& obs) const {
	GridState& state = static_cast<GridState&>(s);
	//cout << "Grid::Step, address of state: " << &state << ", content: " << state.state_idx << endl;
	//cout << "Grid::Step, state: " << state.state_idx << " -> ";
	bool hold_it = world_.Step(state, random_num, action, reward, obs);
	//cout << state.state_idx << ", action=" << action << ", reward=" << reward << ", obs=" << obs << endl;
	return hold_it;
}

double Grid::ObsProb(OBS_TYPE obs, const State& s, int a) const {
	const GridState& state = static_cast<const GridState&>(s);
	//cout << "ObsProb(obs=" << obs << ", state=" << state.state_idx << ", a=" << a << ") is called" << endl;
	/*
	if (a != LISTEN)
		return obs == 2;

	return state.grid_position == obs ? (1 - NOISE) : NOISE;*/
	//return 1;
	return world_.GetProbObservationGivenState(obs, state.state_idx);
}

State* Grid::CreateStartState(string type) const {
	return new GridState(Random::RANDOM.NextInt(NumStates()));
}

Belief* Grid::InitialBelief(const State* start, string type) const {
	/*vector<State*> particles;
	GridState* left = static_cast<GridState*>(Allocate(-1, 0.5));
	//left->grid_position = LEFT;
	particles.push_back(left);
	GridState* right = static_cast<GridState*>(Allocate(-1, 0.5));
	//right->grid_position = RIGHT;
	particles.push_back(right);*/
	
	vector<State*> particles;
	for (int i = 0; i < NumStates(); i++) {
		GridState* state = static_cast<GridState*>(Allocate(-1, 1. / NumStates()));
		state->state_idx = i;
		particles.push_back(state);
	}
	return new ParticleBelief(particles, this);
}

ScenarioLowerBound* Grid::CreateScenarioLowerBound(string name,
	string particle_bound_name) const {
	ScenarioLowerBound* bound = NULL;
	if (name == "TRIVIAL" || name == "DEFAULT") {
		bound = new TrivialParticleLowerBound(this);
	} else if (name == "RANDOM") {
		bound = new RandomPolicy(this,
			CreateParticleLowerBound(particle_bound_name));
	} else {
		cerr << "Unsupported scenario lower bound: " << name << endl;
		exit(1);
	}
	return bound;
}

void Grid::PrintState(const State& state, ostream& out) const {
	const GridState& gridstate = static_cast<const GridState&>(state);
	world_.Print(gridstate.state_idx, out);
}

void Grid::PrintBelief(const Belief& belief, ostream& out) const {
}

void Grid::PrintObs(const State& state, OBS_TYPE obs, ostream& out) const {
	out << "obs=" << obs << " (";
	if (obs & (1 << GridWorld::ACT_UP))
		out << "up,";
	if (obs & (1 << GridWorld::ACT_RIGHT))
		out << "right,";
	if (obs & (1 << GridWorld::ACT_DOWN))
		out << "down,";
	if (obs & (1 << GridWorld::ACT_LEFT))
		out << "left,";
	out << ")";
}

void Grid::PrintAction(int action, ostream& out) const {
	out << "action=" << action << " (";
	switch (action) {
		case GridWorld::ACT_UP:
			out << "up";
			break;
		case GridWorld::ACT_RIGHT:
			out << "right";
			break;
		case GridWorld::ACT_DOWN:
			out << "down";
			break;
		case GridWorld::ACT_LEFT:
			out << "left";
			break;
	}
	out << ")";
}

State* Grid::Allocate(int state_id, double weight) const {
	//cout << "Grid::Allocate(state_id=" << state_id << ", weight=" << weight << ") is triggered" << endl;
	//State* particle = memory_pool_.Allocate();
	GridState* particle = memory_pool_.Allocate();
	particle->state_id = state_id;
	particle->weight = weight;
	return particle;
}

State* Grid::Copy(const State* particle) const {
	//cout << "Grid::Copy(" << *particle << ") is triggered" << endl;
	GridState* new_particle = memory_pool_.Allocate();
	*new_particle = *static_cast<const GridState*>(particle);
	new_particle->SetAllocated();
	return new_particle;
}

void Grid::Free(State* particle) const {
	memory_pool_.Free(static_cast<GridState*>(particle));
}

int Grid::NumActiveParticles() const {
	return memory_pool_.num_allocated();
}

} // namespace despot

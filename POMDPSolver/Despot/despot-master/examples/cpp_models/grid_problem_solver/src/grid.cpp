#include "grid.h"

using namespace std;

namespace despot {


Grid::Grid(string params_files) {
	string delimiter = ";";
	int idx = params_files.find(delimiter);
	if (idx == string::npos) {
		cerr << "Incorrect params_files (expect a `;` to separate world file and agent file" << endl;
		exit(0);
	}

	string world_file = params_files.substr(0, idx);
	string agent_file = params_files.substr(idx+1);
	world_ = GridWorld(world_file);
	agent_ = GridAgent(agent_file);
}

int Grid::NumStates() const {
	return agent_.GetNumStates();
}

bool Grid::Step(State& s, double random_num, int action, double& reward,
		OBS_TYPE& obs) const {
	GridState& state = static_cast<GridState&>(s);
	//cout << "Grid::Step, current state: world=" << state.env_state_idx << ", agent=" << state.agent_state_idx << endl;
	//cout << "Grid::Step, state: " << state.state_idx << " -> ";
	agent_.Step(state.agent_state_idx, action);
	//cout << "please 1" << endl;
	bool hold_it = world_.Step(state.env_state_idx, action, reward, obs);
	//cout << "please 2: world=" << state.env_state_idx << ", agent=" << state.agent_state_idx << ". action=" << action << ", reward=" << reward << ", obs=" << obs << endl;
	return hold_it;
}

double Grid::ObsProb(OBS_TYPE obs, const State& s, int a) const {
	const GridState& state = static_cast<const GridState&>(s);
	//cout << "ObsProb(obs=" << obs << ", state=" << state.state_idx << ", a=" << a << ") is called" << endl;
	//cout << "Grid::ObsProb, observation=" << obs << ". state: world=" << state.env_state_idx << ", agent=" << state.agent_state_idx << endl;
	//cout << "P(o=" << obs << " | s=" << state.agent_state_idx << ") = " << agent_.GetProbObservationGivenState(obs, state.agent_state_idx) << endl;
	return agent_.GetProbObservationGivenState(obs, state.agent_state_idx);
	//return 0;
}

State* Grid::CreateStartState(string type) const {
	int start_env_state_idx = Random::RANDOM.NextInt(world_.get_num_true_states());
	GridState* start_state = new GridState(start_env_state_idx, -1);
	return start_state;
}

Belief* Grid::InitialBelief(const State* start, string type) const {
	const GridState* start_grid_state = static_cast<const GridState*>(start);
	vector<State*> particles;
	for (int i = 0; i < NumStates(); i++) {
		GridState* particle_state = static_cast<GridState*>(Allocate(-1, 1. / NumStates()));
		particle_state->env_state_idx = start_grid_state->env_state_idx;
		particle_state->agent_state_idx = i;
		particles.push_back(particle_state);
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
	world_.PrintState(gridstate.env_state_idx, out);
	out << " ; ";
	agent_.PrintState(gridstate.agent_state_idx, out);
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

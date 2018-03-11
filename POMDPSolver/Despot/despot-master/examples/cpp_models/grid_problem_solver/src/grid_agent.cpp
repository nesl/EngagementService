#include "grid_agent.h"


using namespace std;

namespace despot {

template<class T>
vector< vector< vector<T> > > GridAgent::Init3DVector_(int d1, int d2, int d3) {
	vector< vector< vector<T> > > ret;
	for (int i = 0; i < d1; i++)
		ret.push_back(Init2DVector_<T>(d2, d3));
	return ret;
}

template<class T>
vector< vector<T> > GridAgent::Init2DVector_(int d1, int d2) {
	vector< vector<T> > ret;
	for (int i = 0; i < d1; i++)
		ret.push_back(vector<T>(d2));
	return ret;
}

GridAgent::GridAgent(string filename) {
	/*
	 *  The description is copied from "pomc.py", `dump()` in `PartiallyObservalbeMarkovChain`
	 *  class.
	 *
     *  Format:
     *
     *      num_states num_actions num_obserables
     *      <state transition probability section>
     *      <observation-state probability section>
     *
     *    The format of `state transition probability section` represent (number of actions)
     *    matrices. For example, for the first action_token, the format is presented below:
     *
     *      action_token_0
     *      alist[action_token_0][0, 0]  alist[action_token_0][0, 1]  ...
     *      alist[action_token_0][1, 0]  alist[action_token_0][1, 1]  ...
     *      ...
     *   
     *    The `observation-state probability section` is just a matrix:
     *     
     *      c[0, 0] c[0, 1] ...
     *      c[1, 0] c[1, 1] ...
     *      ...
	 */

	ifstream fin(filename.c_str());
	if (fin.fail()) {
		cerr << "Cannot open agent file\"" << filename << "\". Abort" << endl;
		exit(0);
	}
	
	// get number of states, actions, and observables
	fin >> num_states_ >> num_actions_ >> num_observables_;

	mat_T_ = Init3DVector_<double>(num_actions_, num_states_, num_states_);
	mat_Z_ = Init2DVector_<double>(num_observables_, num_states_);

	for (int i = 0; i < num_actions_; i++) {
		int action_token;
		fin >> action_token;
		for (int j = 0; j < num_states_; j++)
			for (int k = 0; k < num_states_; k++)
				fin >> mat_T_[action_token][j][k];
	}

	//cout << "DEBUG:" << mat_T_[0][4][2] << endl;
	//cout << "DEBUG:" << mat_T_[1][3][3] << endl;
	//cout << "DEBUG:" << mat_T_[2][7][5] << endl;
	//cout << "Address of mat_T=" << mat_T_ << endl;

	for (int i = 0; i < num_observables_; i++)
		for (int j = 0; j < num_states_; j++)
			fin >> mat_Z_[i][j];

	fin.close();
}

int GridAgent::GetNumStates() const {
	return num_states_;
}

void GridAgent::Step(int& state_idx, int action) const {
	//double* prob_arr = mat_T_[action][state_idx];
	//cout << "GridAgent::Step(), original state_idx=" << state_idx << ", action=" << action << endl;
	//cout << "BTW, #states=" << num_states_ << ", #actions=" << num_actions_ << ", #num_observables=" << num_observables_ << endl;
	double sum = 0.;
	double die = Random::RANDOM.NextDouble();
	//cout << "What the hack the address of mat_T=" << mat_T_ << endl;
	//cout << "DEBUG:" << mat_T_[0][4][2] << endl;
	//cout << "DEBUG:" << mat_T_[1][3][3] << endl;
	//cout << "DEBUG:" << mat_T_[2][7][5] << endl;
	for (int i = 0; i < num_states_; i++) {
		//cout << "going to access mat_T_[" << action << "][" << i << "][" << state_idx << "]" << endl;
		sum += mat_T_[action][i][state_idx];
		//cout << "mat_T_[" << action << "][" << i << "][" << state_idx << "]=" << mat_T_[action][i][state_idx] << endl;
		if (sum > die) {
			//cout << "Found " << i << endl;
			state_idx = i;
			break;
		}
	}
	//cout << "Not found!! sum=" << sum << ", die=" << die << endl;
}

double GridAgent::GetProbObservationGivenState(OBS_TYPE& obs, int state_idx) const {
	return mat_Z_[obs][state_idx];
}

void GridAgent::PrintState(int state_idx, ostream& out) const {
	out << "latent_state=" << state_idx;
}

} // namespace despot

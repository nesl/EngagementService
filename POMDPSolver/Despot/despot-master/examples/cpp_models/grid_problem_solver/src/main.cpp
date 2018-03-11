#include <iostream>

#include <despot/simple_tui.h>

#include "grid.h"

using namespace despot;

class TUI: public SimpleTUI {
public:
	TUI() {
	}
 
	DSPOMDP* InitializeModel(option::Option* options) {
		if (!options[E_PARAMS_FILE]) {
			std::cerr << "No input simulation file provided. Abort simulation." << std::endl;
			exit(1);
		}
		DSPOMDP* model = new Grid(options[E_PARAMS_FILE].arg);
		return model;
	}
	
	void InitializeDefaultParameters() {
	}
};

int main(int argc, char* argv[]) {
	return TUI().run(argc, argv);
}

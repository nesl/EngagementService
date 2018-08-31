#
# Copyright (c) 2017 Intel Corporation 
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import datetime

from exploration_policies.exploration_policy import *


DATETIME_FORMAT = "%Y/%b/%d %H:%M:%S.%f"


class Categorical(ExplorationPolicy):
    def __init__(self, tuning_parameters):
        """
        :param tuning_parameters: A Preset class instance with all the running paramaters
        :type tuning_parameters: Preset
        """
        ExplorationPolicy.__init__(self, tuning_parameters)
        try:
            self.action_likelihood_path = tuning_parameters.action_likelihood_path
        except:
            self.action_likelihood_path = None

    def get_action(self, action_values):
        # choose actions according to the probabilities
        print("Categorical", self.action_space_size, action_values)
        if self.action_likelihood_path is not None:
            with open(self.action_likelihood_path, 'a') as fo:
                fo.write("%s\tprob\t%.6f\t%.6f\n" % (
                    datetime.datetime.now().strftime(DATETIME_FORMAT),
                    action_values[0],
                    action_values[1],
                ))
        return np.random.choice(range(self.action_space_size), p=action_values)

    def get_control_param(self):
        return 0

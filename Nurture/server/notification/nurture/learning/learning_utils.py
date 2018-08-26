import itertools
import string
import random
import os
import math

from nurture.learning.state import State


def argmax_dict(d):
    idx = None
    val = -1e100
    for k in d:
        if d[k] > val:
            idx, val = k, d[k]
    return idx


def max_dict_val(d):
    return max([d[k] for k in d])

def clip(val, min_cut, max_cut):
    return min(max(val, min_cut), max_cut)

def one_hot_list(val, array):
    result = [1 if val == v else 0 for v in array]
    assert sum(result) == 1
    return result

def smart_list_concatenation(*arg):
    listified_args = [(a if type(a) is list else [a]) for a in arg]
    return list(itertools.chain(*listified_args))

def random_choices(candidates, length):
    clen = len(candidates)
    return [candidates[random.randint(0, clen -1)] for _ in range(length)]

def make_tmp_folder():
    folder_path = '/tmp/nurture_%s/' % (
            ''.join(random_choices(string.ascii_uppercase + string.digits, length=20)))
    os.makedirs(folder_path, exist_ok=True)
    return folder_path

def load_files_in_memory(folder_path):
    data = {}
    for filename in os.listdir(folder_path):
        with open(os.path.join(folder_path, filename), 'rb') as f:
            data[filename] = f.read()
    return data

def restore_files_to_disk(folder_path, data):
    for filename in data:
        with open(os.path.join(folder_path, filename), 'wb') as fo:
            fo.write(data[filename])
    
def get_feature_vector_one_hot_classic(state):
	return smart_list_concatenation(
			state.timeOfDay,
			state.dayOfWeek,
			one_hot_list(state.motion, State.allMotionValues()),
			one_hot_list(state.location, State.allLocationValues()),
			math.log(clip(state.notificationTimeElapsed, 5.0, 60.0)),
			one_hot_list(state.ringerMode, State.allRingerModeValues()),
			state.screenStatus,
	)

def get_feature_vector_one_hot_no_log(state):
	return smart_list_concatenation(
			state.timeOfDay,
			state.dayOfWeek,
			one_hot_list(state.motion, State.allMotionValues()),
			one_hot_list(state.location, State.allLocationValues()),
			clip(state.notificationTimeElapsed, 0., 120) / 60.0,
			one_hot_list(state.ringerMode, State.allRingerModeValues()),
			state.screenStatus,
	)

import sys
import os
import datetime


DATETIME_FORMAT = "%Y/%b/%d %H:%M:%S.%f"

uid = '48444'
timestamp = datetime.datetime.now()
reward = 1
done = False
state = '0,0,0,0,0,0,0,0,0,0,0,0,0,0,0'

path = os.path.join('checkpoints', uid, 'reward_state.txt')
with open(path, 'w') as fo:
    fo.write(timestamp.strftime(DATETIME_FORMAT) + "\n")
    fo.write(str(reward) + "\n")
    fo.write(str(done) + "\n")
    fo.write(state + "\n")

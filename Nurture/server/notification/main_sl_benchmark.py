import datetime
t1 = datetime.datetime.now()

import dill
from nurture.learning.state import State
import time

agent = dill.load(open("/home/timestring/Dropbox/Nurture/models/user/88520/classification.p", 'rb'))

t2 = datetime.datetime.now()

agent.feed_reward(0)
print(agent.get_action(State.getExampleState()))

t3 = datetime.datetime.now()

dill.dump(agent, open("/tmp/whatsoever", 'wb'))

t4 = datetime.datetime.now()

with open('/tmp/sl_time_load.txt', 'a') as fo:
    fo.write("%f\n" % (t2 - t1).total_seconds())
with open('/tmp/sl_time_compute.txt', 'a') as fo:
    fo.write("%f\n" % (t3 - t2).total_seconds())
with open('/tmp/sl_time_save.txt', 'a') as fo:
    fo.write("%f\n" % (t4 - t3).total_seconds())


time.sleep(1000)

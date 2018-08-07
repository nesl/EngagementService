#!/usr/bin/env python3

import subprocess
import time
import sys
import os
import signal


if len(sys.argv) < 2:
    print("Error: %s IP:Port" % sys.argv[0])
    exit()


while True:
    print()
    print("===== Restart the program ======================")
    proc = subprocess.Popen(['python3', 'manage.py', 'runsslserver', sys.argv[1]])
    time.sleep(10 * 60)  # 10 mins
    proc.terminate()
    os.system('pkill -f runsslserver')
    print("===== Force to start the program ===============")
    time.sleep(2)  # 2 seconds to receive 2nd ctrl-c

import os
import sys
import datetime
import time

sys.path.append('../notification')
from nurture.learning.agents import CoachA3CAgent
from nurture.learning.state import State


UID = '99994'
DAILY_ROUTING_FILE = '01588-54445_actions.csv'

TIME_WAIT_SLEEP = 0.003
TIME_WAIT_REGULAR = 0.01

def time_of_day_2_hms(v):
    v *= 24
    h = int(v)
    v -= h
    v *= 60
    m = int(v)
    v -= m
    s = int(v)
    return h, m, s

def time_of_week_2_day(v):
    idx = int(v * 7)
    return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][idx]

def get_month_name(n):
    return [
            'Jaunary',
            'February',
            'March',
            'April',
            'May',
            'June',
            'July',
            'August',
            'September',
            'October',
            'November',
            'December',
    ][n-1]

def parse_line(line):
    terms = line.split(',')
    start_h, start_m, start_s = time_of_day_2_hms(float(terms[2]))
    current_time = datetime.datetime(
            2018, int(terms[0].strip()), int(terms[1].strip()), start_h, start_m, start_s)

    motionMap = {
            'still': State.MOTION_STATIONARY,
            'walking': State.MOTION_WALKING,
            'running': State.MOTION_RUNNING,
            'driving': State.MOTION_DRIVING,
            'biking': State.MOTION_BIKING,
    }

    locationMap = {
            'home': State.LOCATION_HOME,
            'work': State.LOCATION_WORK,
            'others': State.LOCATION_OTHER,
    }

    ringerModeMap = {
            'silent': State.RINGER_MODE_SILENT,
            'vibrate': State.RINGER_MODE_VIBRATE,
            'normal': State.RINGER_MODE_NORMAL,
    }

    screenStatusMap = {
            'on': State.SCREEN_STATUS_ON,
            'off': State.SCREEN_STATUS_OFF,
    }

    state = State(
            timeOfDay=float(terms[2]),
            dayOfWeek=float(terms[3]),
            motion=motionMap[terms[4]],
            location=locationMap[terms[5]],
            notificationTimeElapsed=float(terms[6]),
            ringerMode=ringerModeMap[terms[7]],
            screenStatus=screenStatusMap[terms[8]],
    )

    return current_time, state

"""
def get_reward():
    response = input("Will you answer this notification? y/n/[i]: ")
    try:
        x = response.lower()[0]
        if x == 'y':
            return 1.
        elif x == 'n':
            return -5.
        else:
            return 0.
    except:
        return 0.
"""


def get_reward_auto(state):
    if state.motion == State.MOTION_DRIVING:
        return -5

    if state.screenStatus == State.SCREEN_STATUS_ON:
        return 1

    if state.location == State.LOCATION_OTHER:
        if state.notificationTimeElapsed > 10:
            return 1
        else:
            return 0
    elif state.location == State.LOCATION_HOME:
        if state.motion != State.MOTION_STATIONARY:
            return 0
        else:
            if state.notificationTimeElapsed > 20:
                return 1
            elif state.notificationTimeElapsed > 10:
                return 0
            else:
                return -5
    else:
        if state.motion != State.MOTION_STATIONARY:
            return 1
        else:
            if state.notificationTimeElapsed > 15:
                return 1
            elif state.notificationTimeElapsed > 10:
                return 0
            else:
                return -5

"""
def get_reward_auto(state):
    if state.motion == State.MOTION_DRIVING:
        return -1.5

    if state.screenStatus == State.SCREEN_STATUS_ON:
        return 1

    bonus = {
            State.RINGER_MODE_SILENT: 0.,
            State.RINGER_MODE_VIBRATE: 0.1,
            State.RINGER_MODE_NORMAL: 0.3,
    }[state.ringerMode]

    if state.notificationTimeElapsed > 60:
        return -0.1 + bonus

    if state.notificationTimeElapsed < 15:
        return (state.notificationTimeElapsed - 15) * 0.03 + bonus

    return (state.notificationTimeElapsed - 15) * 0.01 + bonus
"""

def print_context(current_time, state, reward):
    motionMap = {
            State.MOTION_STATIONARY: 'still',
            State.MOTION_WALKING: 'walking',
            State.MOTION_RUNNING: 'running',
            State.MOTION_DRIVING: 'driving',
            State.MOTION_BIKING: 'biking',
    }

    locationMap = {
            State.LOCATION_HOME: 'home',
            State.LOCATION_WORK: 'work',
            State.LOCATION_OTHER: 'others',
    }

    ringerModeMap = {
            State.RINGER_MODE_SILENT: 'silent',
            State.RINGER_MODE_VIBRATE: 'vibrate',
            State.RINGER_MODE_NORMAL: 'normal',
    }

    screenStatusMap = {
            State.SCREEN_STATUS_ON: 'on',
            State.SCREEN_STATUS_OFF: 'off',
    }

    os.system('clear')
    h, m, _ = time_of_day_2_hms(state.timeOfDay)
    print("==========================    (%.2f)" % reward)
    print("%s/%d %s, %d:%02d" % (
        get_month_name(current_time.month),
        current_time.day,
        time_of_week_2_day(state.dayOfWeek),
        current_time.hour,
        current_time.minute,
    ))
    print("==========================")
    print("           location: %s" % locationMap[state.location])
    print("           activity: %s" % motionMap[state.motion])
    print("        ringer mode: %s" % ringerModeMap[state.ringerMode])
    print("             screen: %s" % screenStatusMap[state.screenStatus])
    print("  last notification: %.1f minutes ago" % state.notificationTimeElapsed)

if __name__ == "__main__":

    agent = CoachA3CAgent()
    agent.set_user_code(UID)

    with open(DAILY_ROUTING_FILE) as f:
        routing_lines = [l.strip() for l in f.readlines()]
   
    now, cur_state = parse_line(routing_lines[0])
    last_notification_time = now - datetime.timedelta(days=1)
    total_rewards = 0.
    cnt = 5

    for l in routing_lines:
        target_time, target_state = parse_line(l)
        while now < target_time:
            get_action = False
            if target_time - now < datetime.timedelta(seconds=120):
                now = target_time
                cur_state = target_state
                get_action = True
            else:
                now += datetime.timedelta(seconds=60)

            # calibrate notification time
            cur_state.notificationTimeElapsed = (now - last_notification_time).total_seconds() / 60. + 2
            #cur_state.notificationTimeElapsed = cnt
            #cnt += 1
            #if cnt > 61:
            #    cnt = 5

            sleeping_time = now.hour < 10 or now.hour >= 22
            if sleeping_time:
                print_context(now, cur_state, total_rewards)
                time.sleep(TIME_WAIT_SLEEP)
                continue

            if not get_action:
                print_context(now, cur_state, total_rewards)
                time.sleep(TIME_WAIT_REGULAR)
                continue

            agent.last_notification_time -= datetime.timedelta(seconds=60)
            send_notification = agent.get_action(target_state)
            if not send_notification:
                agent.feed_reward(0)
                print_context(now, cur_state, total_rewards)
                continue
            else:
                print_context(now, cur_state, total_rewards)
                #reward = get_reward()
                reward = get_reward_auto(cur_state)
                agent.feed_reward(reward)
                total_rewards += reward
                last_notification_time = now

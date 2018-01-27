from constant import *

def getTimeState(hour, minute):
    if hour < 8:
        return STATE_TIME_SLEEPING
    elif hour < 12:
        return STATE_TIME_MORNING
    elif hour < 18:
        return STATE_TIME_AFTERNOON
    else:
        return STATE_TIME_EVENING

def getDayState(day):
    if day == 0 or day == 6:
        return STATE_DAY_WEEKEND
    else:
        return STATE_DAY_WEEKDAY

def getLastNotificationState(last_notification_time):
    if last_notification_time <= 60:
        return STATE_LAST_NOTIFICATION_WITHIN_1HR
    else:
        return STATE_LAST_NOTIFICATION_LONG

def getDeltaMinutes(day1, hour1, minute1, day2, hour2, minute2):
    return (day2 - day1) * 24 * 60 + (hour2 - hour1) * 60 + (minute2 - minute1)

def allTimeStates():
    return [STATE_TIME_MORNING, STATE_TIME_AFTERNOON, STATE_TIME_EVENING, STATE_TIME_SLEEPING]

def allDayStates():
    return [STATE_DAY_WEEKDAY, STATE_DAY_WEEKEND]

def allLocationStates():
    return [STATE_LOCATION_HOME, STATE_LOCATION_WORK, STATE_LOCATION_OTHER]

def allActivityStates():
    return [STATE_ACTIVITY_STATIONARY, STATE_ACTIVITY_WALKING, STATE_ACTIVITY_RUNNING, STATE_ACTIVITY_DRIVING]

def allLastNotificationStates():
    return [STATE_LAST_NOTIFICATION_WITHIN_1HR, STATE_LAST_NOTIFICATION_LONG]

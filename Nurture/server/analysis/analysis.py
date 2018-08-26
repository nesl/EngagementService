import datetime

from notification import settings
from nurture.models import *
from nurture import utils
from nurture.learning import learning_utils
from nurture.learning.state import State


def _to_token(datetime_obj):
    return datetime_obj.strftime("%m%d%H%M")

def sleep_time(datetime_obj):
    return datetime_obj.hour < 10 or datetime_obj.hour >= 22


def get_effective_data_length(user_code, span_min=10):
    """
    To compute the length of the data by the user, based on `ActionLog`. Our system establishes
    a connection (from app to server) every minute. However, since recent Android OS version
    introduces power saving feature, when the phone goes to background, the frequency of the
    connection drops to around every 10 minutes per connection.

    Return: (length, ratio)
      - length: Effective data length in minutes
      - ratio: The ratio compared with expected length
    """
    user = AppUser.objects.get(code=user_code)
    logs = ActionLog.objects.filter(user=user, processing_status=ActionLog.STATUS_OKAY)
    sensor_times = [utils.get_recent_calibrated_sensor_time_in_action_log(l) for l in logs]
    begin_time = min(sensor_times)
    end_time = max(sensor_times)
    
    appeared_times = set()
    for st in sensor_times:
        for i in range(span_min):
            secs = i * 60
            dt = st + datetime.timedelta(seconds=secs)
            if not sleep_time(dt):
                appeared_times.add(_to_token(dt))
    
    expected_times = set()
    while begin_time < end_time:
        if not sleep_time(begin_time):
            expected_times.add(_to_token(begin_time))
        begin_time += datetime.timedelta(seconds=60)
        
    appeared_minutes = len(appeared_times)
    expected_minutes = len(expected_times)
        
    return appeared_minutes, appeared_minutes / expected_minutes


def get_action_response(user_code):
    """
    Return a list of (acted_log, response, delay)
      - acted_log: `ActionLog`, the one which sends notification
      - response: a `str` of either 'answered', 'dismissed`, `ignored`
      - delay: a `float` shows how much time the user takes to respond in minutes
    """
    user = AppUser.objects.get(code=user_code)
    logs = ActionLog.objects.filter(user=user, processing_status=ActionLog.STATUS_OKAY).order_by('id')
    results = []
    for i in range(len(logs)):
        acted_log = logs[i]
        if acted_log.action_message != 'action-1':
            continue
            
        status = 'ignored'
        delay = 60.
        j = i + 1
        while j < len(logs):
            response_log = logs[j]
            j += 1
            
            if response_log.action_message == 'action-1':  # stop when hitting next question
                break

            time_diff = (response_log.query_time - acted_log.query_time).seconds / 60.
            if time_diff > 60.:  # timeout
                break

            if response_log.num_dismissed > 0:
                status = 'dismissed'
                delay = time_diff
                break

            if response_log.num_accepted > 0:
                status = 'accepted'
                delay = time_diff
                reward_msg = response_log.reward_state_message.split(';')[0][1:-1]
                r_terms = reward_msg.split(',')
                for r_term in r_terms:
                    sub_terms = r_term.split(':')
                    if len(sub_terms) == 2:
                        accurate_time = float(sub_terms[1]) / 60.
                        delay = min(delay, accurate_time)
                break
            
        results.append((acted_log, status, delay))
        
    return results


def get_user_response_analysis(code):
    """
    user -> total notifications
            number and rate of accepted notifications
            number and rate of dismissed notifications
            number and rate of ignored notifications
            notification response times
            notification dismiss times
    """
    effective_length_min, _ = get_effective_data_length(code)
    effective_length_hour = effective_length_min / 60.
    
    response_raw = get_action_response(code)
    accept_times = [r[2] for r in response_raw if r[1] == 'accepted']
    dismiss_times = [r[2] for r in response_raw if r[1] == 'dismissed']
    num_ignores = len([r for r in response_raw if r[1] == 'ignored'])
    
    accept_times.sort()
    dismiss_times.sort()
    
    return {
        'num_total': len(response_raw),
        'num_accepted': len(accept_times),
        'num_dismissed': len(dismiss_times),
        'num_ignored': num_ignores,
        'rate_all': len(response_raw) / effective_length_hour,
        'rate_accepted': len(accept_times) / effective_length_hour,
        'rate_dismissed': len(dismiss_times) / effective_length_hour,
        'rate_ignored': num_ignores / effective_length_hour,
        'accept_times': accept_times,
        'dismiss_times': dismiss_times,
    }


def make_feature_vector(state):
    return learning_utils.smart_list_concatenation(
            state.timeOfDay,
            state.dayOfWeek,
            learning_utils.one_hot_list(state.motion, State.allMotionValues()),
            learning_utils.one_hot_list(state.location, State.allLocationValues()),
            state.notificationTimeElapsed / 60.0,
            learning_utils.one_hot_list(state.ringerMode, State.allRingerModeValues()),
            state.screenStatus,
    )

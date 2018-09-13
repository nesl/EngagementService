import dill
import datetime
import os
from collections import defaultdict

import numpy as np

from notification import settings
from nurture.models import *
from nurture import utils
from nurture.learning import learning_utils
from nurture.learning.state import State
from nurture.learning.agents.classification_agent import ClassificationAgent
from nurture.tasks.task_response import TaskResponse

from secret import user_list


def _to_token(datetime_obj):
    return datetime_obj.strftime("%m%d%H%M")

def sleep_time(datetime_obj):
    return datetime_obj.hour < 10 or datetime_obj.hour >= 22


def get_effective_data_length(user_code, span_min=10, best_effort=False, exclude_capped=False,
        start_time=None, end_time=None):
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

    if exclude_capped:
        logs = [l for l in logs if l.action_message != 'action-c']

    sensor_times = [utils.get_recent_calibrated_sensor_time_in_action_log(
        l, best_effort=best_effort) for l in logs]

    if start_time is not None:
        sensor_times = [t for t in sensor_times if t >= start_time]
    if end_time is not None:
        sensor_times = [t for t in sensor_times if t <= end_time]


    if len(sensor_times) == 0:
        return None, None

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


def get_action_response_by_bucket(code, bucket_size_days):
    """
    Return a `list` of lists, each sub-list includes a list of *action response*, which is defined
    in `get_action_response()`.
    """
    study_start_date = user_list.get_user_study_start_date()[code]
    today = datetime.datetime.now(pytz.timezone(settings.TIME_ZONE))
    num_buckets = (today - study_start_date).days // bucket_size_days
    assert num_buckets >= 0
    ret = [[] for _ in range(num_buckets)]
    responses = get_action_response(code)
    for r in responses:
        bucket_id = (r[0].query_time - study_start_date).days // bucket_size_days
        if 0 <= bucket_id < num_buckets:
            ret[bucket_id].append(r)
    return ret


def get_user_response_analysis(code, best_effort=True):
    """
    user -> total notifications
            number and rate of accepted notifications
            number and rate of dismissed notifications
            number and rate of ignored notifications
            notification response times
            notification dismiss times
    """
    effective_length_min, _ = get_effective_data_length(code, best_effort=best_effort)
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


def make_training_dataset(user_code, start_datetime=None, end_datetime=None,
        dismiss_weight=5):
    responses = get_action_response(user_code)

    if start_datetime is not None:
        responses = filter(lambda r: r.query_time >= start_datetime, responses)
    if end_datetime is not None:
        responses = filter(lambda r: r.query_time <= end_datetime, responses)

    action_log_bundle_list = list(map(utils.get_action_log_lazy_bundle, [r[0] for r in responses]))
    states = []
    labels = []
    for bundle, response in zip(action_log_bundle_list, responses):
        state = bundle['states'][-1]
        label = 1 if response[1] == 'accepted' else 0
        repeat = dismiss_weight if response[1] == 'dismissed' else 1
        for _ in range(repeat):
            states.append(state)
            labels.append(label)
    return states, labels


def prepare_supervised_learning_model(user_code):
    print("Make training data for %s" % user_code)
    states, labels = make_training_dataset(user_code)
    print("Total %d instances, %d with positive labels" % (len(labels), sum(labels)))
    print("Picking a model, it will take a few minutes ...")
    agent = ClassificationAgent()
    agent.prepare_classifier(states, labels)
    print("We got a model! The agent choose %s algorithm" % agent.output_classifier_name())

    model_path = os.path.join(
            settings.USER_MODEL_ROOT,
            user_code,
            '%s.p' % agent.get_policy_name(),
    )
    dill.dump(agent, open(model_path, 'wb'))


def _parse_mid_survey_result_line(line):
    terms = line.strip().split("\t")
    try:
        code = terms[1].zfill(5)
        week = int(terms[2][5:])  # e.g., "Week 1"
        appropriate_time_score = int(terms[3][0:1])
        overall_experience_score = int(terms[4][0:1])
        return {
                'code': code,
                'week': week,
                'appropriate_time_score': appropriate_time_score,
                'overall_experience_score': overall_experience_score,
        }
    except:
        return None


def get_mid_survey_results():
    """
    Return a dictionary of
        'appropriate_time' -> a dictionary of
            user_code => a `list` of weekly rating
        'overall_experience'
            user_code => a `list` of weekly rating
    """

    # process mid survey file
    file_path = os.path.join(
            os.path.dirname(os.path.abspath(__file__)),
            'secret',
            'external_data',
            'mid_survey_result.txt',
    )
    
    with open(file_path) as f:
        lines = f.readlines()

    processed = [_parse_mid_survey_result_line(l) for l in lines[1:]]
    processed = [p for p in processed if p is not None]

    # group by user
    processed_by_user = defaultdict(list)
    for p in processed:
        processed_by_user[p['code']].append(p)

    ret = {
            'appropriate_time': {},
            'overall_experience': {},
    }
    for code in processed_by_user:
        processed_list = processed_by_user[code]
        processed_list_by_week = {p['week']: p for p in processed_list}
        appropriate_time_score_list = []
        overall_experience_score_list = []
        for week_id in range(1, 7):
            if week_id not in processed_list_by_week:
                break
            p = processed_list_by_week[week_id]
            appropriate_time_score_list.append(p['appropriate_time_score'])
            overall_experience_score_list.append(p['overall_experience_score'])
        ret['appropriate_time'][code] = appropriate_time_score_list
        ret['overall_experience'][code] = overall_experience_score_list

    return ret


def process_mid_survey_results_filter_users(mid_survey_results, user_keep_list):
    """
    Params:
      - mid_survey_results: return value from `get_mid_survey_results()` 
      - user_keep_list: a `list` of user codes that we want to keep
    """
    ret = {}
    for score_type in mid_survey_results:
        ret[score_type] = {}
        for code in mid_survey_results[score_type]:
            if code in user_keep_list:
                ret[score_type][code] = mid_survey_results[score_type][code]
    return ret


def compute_mid_survey_results_weekly_mean_std(mid_survey_results):
    """
    Params:
      - mid_survey_results: return value from `get_mid_survey_results()`, or processed from
                            `process_mid_survey_results_filter_users()`
    """
    ret = {}
    for score_type in mid_survey_results:
        rating_list_by_week = [[] for _ in range(6)]
        input_data = mid_survey_results[score_type]
        for code in input_data:
            for week_idx, score in enumerate(input_data[code]):
                rating_list_by_week[week_idx].append(score)
        ret[score_type] = {
                'mean': [(np.mean(ratings) if len(ratings) > 0 else 0.0)
                        for ratings in rating_list_by_week],
                'std': [(np.std(ratings) if len(ratings) > 0 else 0.0)
                        for ratings in rating_list_by_week]
        }

    return ret


def get_task_responses_by_bucket(user_code, bucket_size_day):
    study_start_date = user_list.get_user_study_start_date()[user_code]
    today = datetime.datetime.now(pytz.timezone(settings.TIME_ZONE))
    num_buckets = (today - study_start_date).days // bucket_size_day
    assert num_buckets >= 0
    ret = [[] for _ in range(num_buckets)]

    user = AppUser.objects.get(code=user_code)
    file_log = FileLog.objects.filter(user=user, type='task-response').last()
    assert file_log is not None
    responses = TaskResponse.parse_response_file(file_log.get_path())

    for r in responses:
        created_datetime = datetime.datetime.fromtimestamp(
                r.created_time // 1000, tz=pytz.timezone(settings.TIME_ZONE))
        bucket_id = (created_datetime - study_start_date).days // bucket_size_day
        if 0 <= bucket_id < num_buckets:
            ret[bucket_id].append(r)

    return ret

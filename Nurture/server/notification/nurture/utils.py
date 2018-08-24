import os
import pytz
import sys
import traceback
import dill
import shutil
import datetime

from django.contrib.auth.models import User
from django.http import HttpResponse
from django.utils.encoding import smart_str
from django.utils import timezone

from wsgiref.util import FileWrapper

from notification import settings

from nurture.learning.state import State
from nurture.learning.agents import *
from nurture.models import *


def generate_navbar_bundle(request):
    web_user = User.objects.get(username=request.user)
    num_exceptions = ExceptionLog.objects.all().count()
    
    return {
            'user': web_user,
            'num_exceptions': num_exceptions,
    }

def make_http_response_for_file_download(file_path):
    wrapper = FileWrapper(open(file_path, 'rb'))
    response = HttpResponse(wrapper, content_type='application/force-download')
    response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename(file_path))
    response['Content-Length'] = os.path.getsize(file_path)
    response['X-Sendfile'] = smart_str(file_path)
    return response


def convert_to_local_timezone(datetime):
    return datetime.astimezone(pytz.timezone(settings.TIME_ZONE))


def log_last_exception(request, user=None):
    ExceptionLog.objects.create(
            request_path=request.path,
            user=user,
            log_time=timezone.now(),
            content=''.join(traceback.format_exception(*sys.exc_info())),
    )


def convert_request_text_to_state(text):
    terms = text.split(',')

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

    return State(
            timeOfDay=float(terms[0]),
            dayOfWeek=float(terms[1]),
            motion=motionMap[terms[2]],
            location=locationMap[terms[3]],
            notificationTimeElapsed=float(terms[4]),
            ringerMode=ringerModeMap[terms[5]],
            screenStatus=screenStatusMap[terms[6]],
    )


def get_learning_agent_class_for_user(app_user):
    agents = {
            AppUser.LEARNING_AGENT_RANDOM: RandomAgent,
            AppUser.LEARNING_AGENT_ATTELIA2: Attelia2Agent,
            AppUser.LEARNING_AGENT_Q_LEARNING: QLearningAgent,
            AppUser.LEARNING_AGENT_Q_LEARNING_REPLAY: QLearningPrioritizedReplayAgent,
            AppUser.LEARNING_AGENT_DEBUG: DebugAgent,
            AppUser.LEARNING_AGENT_SILENT: SilentAgent,
            AppUser.LEARNING_AGENT_TF_DQN: TensorForceDQNAgent,
            AppUser.LEARNING_AGENT_COACH_A3C: CoachA3CAgent,
    }
    return agents[app_user.learning_agent]


def prepare_learning_agent(app_user):
    LearningAgent = get_learning_agent_class_for_user(app_user)

    # check model source (global v.s. user-dependent)
    if LearningAgent.is_user_dependent():
        model_path = os.path.join(settings.USER_MODEL_ROOT, app_user.code,
                LearningAgent.get_policy_file_name())
    else:
        model_path = os.path.join(settings.GLOBAL_MODEL_ROOT,
                LearningAgent.get_policy_file_name())

    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    if not os.path.isfile(model_path):
        initial_model_path = prepare_initial_model(LearningAgent)
        shutil.copyfile(initial_model_path, model_path)

    return model_path


def prepare_initial_model(LearningAgentClass):
    model_path = os.path.join(settings.INITIAL_MODEL_ROOT,
            LearningAgentClass.get_policy_file_name())
    if not os.path.isfile(model_path):
        learning_agent = LearningAgentClass()
        learning_agent.on_pickle_save()
        dill.dump(learning_agent, open(model_path, "wb"))

    return model_path


def argmax_dict(d):
    idx = None
    val = -1e100
    for k in d:
        if d[k] > val:
            idx, val = k, d[k]
    return idx


def max_dict_val(d):
    return max([d[k] for k in d])


def get_ratio(n, d):
    return 0. if d == 0 else n / d


def is_file_extended(path_from, path_to):
    with open(path_from, 'rb') as f:
        content_small = f.read()
    with open(path_to, 'rb') as f:
        content_big = f.read()
    return content_big.startswith(content_small)


def time_of_day_2_hms(v):
    v *= 24
    h = int(v)
    v -= h
    v *= 60
    m = int(v)
    v -= m
    v *= 60
    s = int(v)
    return h, m, s


def weekday_difference(weekday_ref, weekday_target):
    """
    Return
      1     if `weekday_target` is one day ahead of `weekday_ref`
      0     if `weekday_target` and `weekday_ref` are the same
      -1    if `weekday_target` is one day behind of `weekday_ref`
      None  otherwise
    """
    if weekday_target == weekday_ref:
        return 0
    if (weekday_target + 7 - weekday_ref) % 7 == 1:
        return 1
    if (weekday_target + 7 - weekday_ref) % 7 == 6:
        return -1
    return None


def calibrate_sensor_time(action_log_datetime, sensor_time_of_day, sensor_time_of_week, best_effort=False):
    sensor_weekday = int(sensor_time_of_week * 7.)

    # since Nurture app considers Sunday as 0 and Python considers Monday as 0, we'll align with
    # Python's definition
    sensor_weekday = 6 if sensor_weekday == 0 else sensor_weekday - 1

    weekday_diff = weekday_difference(action_log_datetime.weekday(), sensor_weekday)

    if weekday_diff is None:
        if not best_effort:
            raise Exception("Cannot calibrate sensor time (weekdays differ to much)")
        else:
            weekday_diff = 0

    action_log_datetime += datetime.timedelta(days=weekday_diff)
    h, m, s = time_of_day_2_hms(sensor_time_of_day)
    return action_log_datetime.replace(hour=h, minute=m, second=s)


def get_first_calibrated_sensor_time_in_action_log(action_log, best_effort=False):
    terms = action_log.reward_state_message.split(';')[1][1:-1].split(',')
    return calibrate_sensor_time(
            action_log_datetime=action_log.query_time,
            sensor_time_of_day=float(terms[0]),
            sensor_time_of_week=float(terms[1]),
            best_effort=best_effort,
    )

def get_recent_calibrated_sensor_time_in_action_log(action_log, best_effort=False):
    categories = action_log.reward_state_message.split(';')
    if len(categories) == 3:
        return get_first_calibrated_sensor_time_in_action_log(action_log, best_effort)

    terms = categories[3][1:-1].split(',')
    return calibrate_sensor_time(
            action_log_datetime=action_log.query_time,
            sensor_time_of_day=float(terms[0]),
            sensor_time_of_week=float(terms[1]),
            best_effort=best_effort,
    )

import os
import pytz
import sys
import traceback
import dill
import shutil

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

import os
import pytz

from django.http import HttpResponse
from django.utils.encoding import smart_str

from wsgiref.util import FileWrapper

from notification import settings

from nurture.learning.state import State


def make_http_response_for_file_download(file_path):
    wrapper = FileWrapper(open(file_path, 'rb'))
    response = HttpResponse(wrapper, content_type='application/force-download')
    response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename(file_path))
    response['Content-Length'] = os.path.getsize(file_path)
    response['X-Sendfile'] = smart_str(file_path)
    return response


def convert_to_local_timezone(datetime):
    return datetime.astimezone(pytz.timezone(settings.TIME_ZONE))


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

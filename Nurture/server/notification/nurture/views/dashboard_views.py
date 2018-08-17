import datetime
import pytz

from django.contrib.auth.decorators import login_required

from django.shortcuts import render

from django.core.urlresolvers import reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.db import transaction
from django.utils import timezone
from django.db.models import Sum

from notification import settings

from nurture.models import *
from nurture import utils


def _make_app_user_bundle(user):
    """
    Params:
      - user : An `AppUser` object
    Returns:
      A `dict`
    """
    last_file_log = FileLog.objects.filter(user=user).last()
    last_uploading_time = last_file_log.uploaded_time if last_file_log is not None else None

    last_action = ActionLog.objects.filter(user=user).last()
    last_action_time = last_action.query_time if last_action is not None else None

    now = timezone.now()
    ago_10m = now - datetime.timedelta(minutes=10)
    ago_1h = now - datetime.timedelta(hours=1)
    ago_1d = now - datetime.timedelta(days=1)

    action_logs_10m_ago = ActionLog.objects.filter(user=user, query_time__gt=ago_10m)
    action_logs_1h_ago = ActionLog.objects.filter(user=user, query_time__gt=ago_1h)
    action_logs_1d_ago = ActionLog.objects.filter(user=user, query_time__gt=ago_1d)

    return {
            'user': user,
            'last_uploading_time': last_uploading_time,
            'last_action_request_time': last_action_time,
            'connection_stat': {
                'ago_10m': action_logs_10m_ago.count(),
                'ago_1h': action_logs_1h_ago.count(),
                'ago_1d': action_logs_1d_ago.count(),
            },
            'response_stat': {
                'num_notifications': action_logs_1d_ago.filter(action_message='action-1').count(),
                'num_accepted': action_logs_1d_ago.aggregate(Sum('num_accepted'))['num_accepted__sum'],
                'num_dismissed': action_logs_1d_ago.aggregate(Sum('num_dismissed'))['num_dismissed__sum'],
            }
    }

@login_required(login_url='/login/')
def list_users(request):
    app_users = AppUser.objects.all().order_by('-status', '-created_time')
    app_user_bundles = list(map(_make_app_user_bundle, app_users))

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'user_bundles': app_user_bundles,
    }

    return render(request, 'nurture/list_users.html', template_context)



def _try_parse_organize_users_form(post):
    try:
        user_codes = post['user-list'].split(',')

        with transaction.atomic():
            for code in user_codes:
                user = AppUser.objects.get(code=code)

                user.name = post['%s-name' % code]
                user.status = post['%s-status' % code]
                user.learning_agent = post['%s-agent' % code]
                user.save()
        return True
    except:
        return False

@login_required(login_url='/login/')
def organize_users(request):
    if request.method == 'POST':
        if _try_parse_organize_users_form(request.POST):
            return HttpResponseRedirect(reverse('dashboard-list-users'))

    print("get request 'organize_users'")

    app_users = AppUser.objects.all().order_by('-status', '-created_time')
    user_list = ",".join([u.code for u in app_users])

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'user_list': user_list,
            'users': app_users,
            'status_options': AppUser.STATUS_TYPES,
            'learning_agent_options': AppUser.LEARNING_AGENT_TYPES,
    }

    return render(request, 'nurture/organize_users.html', template_context)


def _get_valid_file_types():
    return [
            'notification-interaction',
            'motion',
            'location',
            'ringer-mode',
            'screen-status',
            'task-response',
    ]

@login_required(login_url='/login/')
def show_latest_upload(request, user_code, file_type=None):
    # check user code
    try:
        app_user = AppUser.objects.get(code=user_code)
    except AppUser.DoesNotExist:
        return HttpResponse("Unrecognized user code \"%s\"" % user_code, status=404)

    # check file type
    valid_file_types = _get_valid_file_types()
    if file_type is None:
        file_type = valid_file_types[0]

    if file_type not in valid_file_types:
        return HttpResponse("Unrecognized file type \"%s\"" % file_type, status=404)

    file_logs = FileLog.objects.filter(user=app_user, type=file_type).order_by('-filename')
    file_log = file_logs[0] if len(file_logs) > 0 else None

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'valid_file_types': valid_file_types,
            'user': app_user,
            'file_type': file_type,
            'file_log': file_log,
    }

    return render(request, 'nurture/show_latest_upload.html', template_context)


@login_required(login_url='/login/')
def show_upload_history(request, user_code, file_type, file_name):
    # check user code
    try:
        app_user = AppUser.objects.get(code=user_code)
    except AppUser.DoesNotExist:
        return HttpResponse("Unrecognized user code \"%s\"" % user_code, status=404)

    # check file type
    valid_file_types = _get_valid_file_types()
    if file_type not in valid_file_types:
        return HttpResponse("Unrecognized file type \"%s\"" % file_type, status=404)

    # retrieve target file log
    try:
        target_file_log = FileLog.objects.get(user=app_user, type=file_type, filename=file_name)
    except FileLog.DoesNotExist:
        return HttpResponse("Cannot retrieve file log", status=404)

    file_log_list = (
            FileLog.objects.filter(user=app_user, type=file_type).order_by('-filename'))

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'user': app_user,
            'file_type': file_type,
            'target_file_log': target_file_log,
            'file_log_list': file_log_list,
    }

    return render(request, 'nurture/show_upload_history.html', template_context)



@login_required(login_url='/login/')
def show_responses(request, user_code):
    # check user code
    try:
        app_user = AppUser.objects.get(code=user_code)
    except AppUser.DoesNotExist:
        return HttpResponse("Unrecognized user code \"%s\"" % user_code, status=404)

    # retrieve action logs
    responses = ActionLog.objects.filter(user=app_user).order_by('-query_time')

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'user': app_user,
            'responses': responses,
    }

    return render(request, 'nurture/show_responses.html', template_context)


@login_required(login_url='/login/')
def show_last_exception(request):
    last_exception = ExceptionLog.objects.last()
    return HttpResponseRedirect(reverse('dashboard-show-exception',
        kwargs={'exception_id': last_exception.id}))


@login_required(login_url='/login/')
def show_exception(request, exception_id):
    try:
        target_exception = ExceptionLog.objects.get(id=exception_id)
    except ExceptionLog.DoesNotExist:
        return HttpResponse("Unrecognized exception ID \"%s\"" % exception_id, status=404)

    all_exceptions = ExceptionLog.objects.all().order_by('-id')

    template_context = {
            'navbar': utils.generate_navbar_bundle(request),
            'target_exception': target_exception,
            'all_exceptions': all_exceptions,
    }

    return render(request, 'nurture/show_exception.html', template_context)

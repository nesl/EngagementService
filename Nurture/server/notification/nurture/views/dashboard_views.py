from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required

from django.shortcuts import render

from django.core.urlresolvers import reverse
from django.http import HttpResponseRedirect

from django.db import transaction

from nurture.models import *


def _make_app_user_bundle(user):
    """
    Params:
      - user : An `AppUser` object
    Returns:
      A `dict`
    """
    return {
            'user': user,
    }

@login_required(login_url='/login/')
def list_users(request):
    web_user = User.objects.get(username=request.user)

    app_users = AppUser.objects.all().order_by('-status', '-created_time')
    app_user_bundles = list(map(_make_app_user_bundle, app_users))

    template_context = {
            'myuser': web_user,
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
                user.save()
        return True
    except:
        return False

@login_required(login_url='/login/')
def organize_users(request):
    web_user = User.objects.get(username=request.user)

    if request.method == 'POST':
        if _try_parse_organize_users_form(request.POST):
            return HttpResponseRedirect(reverse('dashboard-list-users'))

    app_users = AppUser.objects.all().order_by('-status', '-created_time')
    user_list = ",".join([u.code for u in app_users])

    template_context = {
            'myuser': web_user,
            'user_list': user_list,
            'users': app_users,
            'status_options': AppUser.STATUS_TYPES,
    }

    return render(request, 'nurture/organize_users.html', template_context)



def _get_valid_file_types():
    return ['motion', 'notification-interaction', 'ringer-mode']

@login_required(login_url='/login/')
def show_latest_upload(request, user_code, file_type=None):
    web_user = User.objects.get(username=request.user)

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

    file_logs = FileLog.objects.filter(user=app_user, type=file_type).order_by('-uploaded_time')
    file_log = file_logs[0] if len(file_logs) > 0 else None

    if file_log is None:
        file_content = None
    else:
        with open(file_log.get_path(), 'r') as f:
            file_content = f.read()

    template_context = {
            'myuser': web_user,
            'valid_file_types': valid_file_types,
            'user': app_user,
            'file_type': file_type,
            'file_log': file_log,
            'file_content': file_content,
    }

    return render(request, 'nurture/show_latest_upload.html', template_context)


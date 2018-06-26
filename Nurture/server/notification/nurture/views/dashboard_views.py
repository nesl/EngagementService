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

    app_users = AppUser.objects.all().order_by('status', 'created_time')
    user_list = ",".join([u.code for u in app_users])

    template_context = {
            'myuser': web_user,
            'user_list': user_list,
            'users': app_users,
            'status_options': AppUser.STATUS_TYPES,
    }

    return render(request, 'nurture/organize_users.html', template_context)

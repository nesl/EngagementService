from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required

from django.shortcuts import render

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

    app_users = AppUser.objects.all().order_by('status', 'created_time')
    app_user_bundles = list(map(_make_app_user_bundle, app_users))

    template_context = {
            'myuser': web_user,
            'user_bundles': app_user_bundles,
    }

    return render(request, 'nurture/list_users.html', template_context)

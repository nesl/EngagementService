from django.contrib.auth.models import User
from django.contrib.auth.decorators import login_required

from django.shortcuts import render


@login_required(login_url='/login/')
def list_users(request):
    user = User.objects.get(username=request.user)

    template_context = {
            'myuser': user,
    }

    return render(request, 'nurture/list_users.html', template_context)

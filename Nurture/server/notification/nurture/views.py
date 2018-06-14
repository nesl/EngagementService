import random

from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt

from nurture.models import *


@csrf_exempt
def debug_dump_post(request):
    if request.method == 'POST':
        print(request.POST)

    return HttpResponse("Recv", status=200)


@csrf_exempt
def get_user_code(request):
    code = None
    trials = 10
    while trials > 0:
        picked_code = str(random.randint(0, 99999)).zfill(5)
        if not AppUser.objects.filter(code=picked_code).exists():
            code = picked_code
            break

    if code is None:
        return HttpResponse("Bad", status=404)
    
    AppUser.objects.create(code=code)
    return HttpResponse(code, status=200)

import pytz
import random
import base64

from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone

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


@csrf_exempt
def upload_log_file(request):

    # extract user
    if 'code' not in request.POST:
        return HttpResponse("Bad", status=404)

    try:
        user = AppUser.objects.get(code=request.POST['code'])
    except AppUser.DoesNotExist:
        return HttpResponse("Bad", status=404)

    # extract type
    if 'type' not in request.POST:
        return HttpResponse("Bad", status=404)
    type = request.POST['type']

    # extract content
    if 'content' not in request.POST:
        return HttpResponse("Bad", status=404)
    content_base64 = request.POST['content']
    try:
        content_bytes = base64.b64decode(content_base64)
    except:
        return HttpResponse("Wrong encoding", status=404)
    
    # uploaded time
    now = timezone.now().astimezone(pytz.timezone('US/Pacific'))

    # write file
    record = FileLog(user=user, type=type, uploaded_time=now)
    path = record.get_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)

    with open(path, 'wb') as fo:
        fo.write(content_bytes)

    record.save()

    return HttpResponse("Ok", status=200)

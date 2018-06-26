from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def debug_dump_post(request):
    if request.method == 'POST':
        print(request.POST)

    return HttpResponse("Recv", status=200)

import os

from django.views.decorators.csrf import csrf_exempt

from notification import settings

from nurture import utils


@csrf_exempt
def download(request):
    return utils.make_http_response_for_file_download(
            os.path.join(settings.APK_ROOT, 'app-debug.apk'))

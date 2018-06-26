import os

from django.http import HttpResponse
from django.utils.encoding import smart_str

from wsgiref.util import FileWrapper


def make_http_response_for_file_download(file_path):
    wrapper = FileWrapper(open(file_path, 'rb'))
    response = HttpResponse(wrapper, content_type='application/force-download')
    response['Content-Disposition'] = 'attachment; filename=%s' % smart_str(os.path.basename(file_path))
    response['Content-Length'] = os.path.getsize(file_path)
    response['X-Sendfile'] = smart_str(file_path)
    return response

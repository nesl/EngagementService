from django.conf.urls import url

from nurture.views import *

urlpatterns = [

        ## mobile request
        url('^mobile/get-user-code/$', mobile_views.get_user_code, name='mobile-get-user-code'),
        url('^mobile/upload-log-file/$', mobile_views.upload_log_file, name='mobile-upload-log-file'),
        url('^mobile/get-action/$', mobile_views.get_action, name='mobile-get-action'),
       
        ## apk download
        url('^apk/download/$', apk_views.download, name='apk-download'),

        ## debugging purpose
        url('^debug/dump-post/$', mobile_views.debug_dump_post, name='debug-dump-post'),
]

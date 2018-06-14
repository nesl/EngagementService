from django.conf.urls import url

from . import views

urlpatterns = [

        ## mobile request
        url('^mobile/get-user-code/$', views.get_user_code, name='mobile-get-user-code'),
        url('^mobile/upload-log-file/$', views.upload_log_file, name='mobile-upload-log-file'),
        url('^mobile/get-action/$', views.get_action, name='mobile-get-action'),
        
        ## debugging purpose
        url('^debug/dump-post/$', views.debug_dump_post, name='debug-dump-post'),
]

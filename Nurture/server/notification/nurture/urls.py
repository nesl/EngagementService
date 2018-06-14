from django.conf.urls import url

from . import views

urlpatterns = [
        url('^get-user-code/$', views.get_user_code, name='get-user-code'),
        #url('^upload-log-file/$', views.upload_log_file, name='upload-log-file'),
        #url('^get-action/$', views.get_action, name='get-action'),
        url('^debug-dump-post/$', views.debug_dump_post, name='debug-dump-post'),
]

from django.conf.urls import url
from django.contrib.auth import views as auth_views

from nurture.views import *


urlpatterns = [
        ## mobile request
        url(r'^mobile/get-user-code/$', mobile_views.get_user_code, name='mobile-get-user-code'),
        url(r'^mobile/upload-log-file/$', mobile_views.upload_log_file, name='mobile-upload-log-file'),
        url(r'^mobile/get-action/$', mobile_views.get_action, name='mobile-get-action'),
       
        ## apk download
        url(r'^apk/download/$', apk_views.download, name='apk-download'),

        ## Log in / log out
        url(r'^login/$', auth_views.login, {'template_name': 'nurture/login.html'}, name= 'login'),
        url(r'^logout/$', auth_views.logout, {'next_page': '/login/'}, name='logout'),

        ## Dashboard
        url(r'^$', dashboard_views.list_users, name='dashboard-homepage'),
        url(r'^dashboard/list-users/$', dashboard_views.list_users, name='dashboard-list-users'),
        url(r'^dashboard/organize-users/$', dashboard_views.organize_users, name='dashboard-organize-users'),
        url(r'^dashboard/latest-upload/(?P<user_code>[0-9]+)/$', dashboard_views.show_latest_upload, name='dashboard-latest-upload-simplified'),
        url(r'^dashboard/latest-upload/(?P<user_code>[0-9]+)/(?P<file_type>[a-z\-]+)/$', dashboard_views.show_latest_upload, name='dashboard-latest-upload'),

        ## debugging purpose
        url(r'^debug/dump-post/$', debug_views.debug_dump_post, name='debug-dump-post'),
]

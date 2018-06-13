from django.conf.urls import url

from . import views

urlpatterns = [
        url('^debug-dump-post/$', views.debug_dump_post, name='debug-dump-post'),
]

import os

from django.db import models


class AppUser(models.Model):
    code = models.CharField(max_length=25)


class FileLog(models.Model):
    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    type = models.CharField(max_length=25)
    uploaded_time = models.DateTimeField()

    def get_path(self):
        return os.path.join(
                str(user),
                str(type),
                "%s.txt" % uploaded_time.strftime('%Y%m%d-%H%M%S.txt'),
        )

class ActionLog(models.Model):
    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    query_time = models.DateTimeField()
    reward_state_message = models.CharField(max_length=256)
    action_message = models.CharField(max_length=256)

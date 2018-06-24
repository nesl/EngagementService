import os

from django.db import models

from notification import settings

class AppUser(models.Model):
    code = models.CharField(max_length=25)

    def __str__(self):
        return self.code


class FileLog(models.Model):
    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    type = models.CharField(max_length=25)
    uploaded_time = models.DateTimeField()

    def get_path(self):
        return os.path.join(
                settings.LOG_FILE_ROOT,
                str(self.user),
                str(self.type),
                self.uploaded_time.strftime('%Y%m%d-%H%M%S.txt'),
        )


class ActionLog(models.Model):
    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    query_time = models.DateTimeField()
    reward_state_message = models.CharField(max_length=256)
    action_message = models.CharField(max_length=256)

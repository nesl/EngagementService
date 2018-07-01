import os
import pytz

from django.db import models

from notification import settings

from nurture import utils


class AppUser(models.Model):
    STATUS_ACTIVE = 1
    STATUS_EXPERIMENT_DONE = 0
    STATUS_HIDDEN = -1
            
    STATUS_TYPES = (
            (STATUS_ACTIVE, 'Active'),
            (STATUS_EXPERIMENT_DONE, 'Finish experiment'),
            (STATUS_HIDDEN, 'Hide this user'),
    )

    code = models.CharField(max_length=25, unique=True)
    name = models.CharField(max_length=256)
    status = models.IntegerField(choices=STATUS_TYPES)
    created_time = models.DateTimeField()

    def __str__(self):
        return self.code


class FileLog(models.Model):
    class Meta:
        unique_together = ('user', 'type', 'filename')

    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    type = models.CharField(max_length=25)
    uploaded_time = models.DateTimeField()
    filename = models.CharField(max_length=25)  # uploaded time, YYYYMMDD-HHMMSS.txt

    def get_path(self):
        return os.path.join(
                settings.LOG_FILE_ROOT,
                str(self.user),
                self.type,
                self.filename,
        )


class ActionLog(models.Model):
    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    query_time = models.DateTimeField()
    reward_state_message = models.CharField(max_length=256)
    action_message = models.CharField(max_length=256)

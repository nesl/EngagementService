import os
import pytz

from django.db import models

from notification import settings


class AppUser(models.Model):
    STATUS_ACTIVE = 1
    STATUS_EXPERIMENT_DONE = 0
    STATUS_HIDDEN = -1
    
    STATUS_TYPES = (
            (STATUS_ACTIVE, 'Active'),
            (STATUS_EXPERIMENT_DONE, 'Finish experiment'),
            (STATUS_HIDDEN, 'Hide this user'),
    )

    LEARNING_AGENT_RANDOM = 0
    LEARNING_AGENT_ATTELIA2 = 1
    LEARNING_AGENT_Q_LEARNING = 2
    LEARNING_AGENT_Q_LEARNING_REPLAY = 4
    LEARNING_AGENT_DEBUG = 3

    LEARNING_AGENT_TYPES = (
            (LEARNING_AGENT_RANDOM, 'Random'),
            (LEARNING_AGENT_ATTELIA2, 'Attelia2'),
            (LEARNING_AGENT_Q_LEARNING, 'Q-learning'),
            (LEARNING_AGENT_Q_LEARNING_REPLAY, 'Q-learning-replay'),
            (LEARNING_AGENT_DEBUG, 'Debug'),
    )

    code = models.CharField(max_length=25, unique=True)
    name = models.CharField(max_length=256)
    status = models.IntegerField(choices=STATUS_TYPES)
    created_time = models.DateTimeField()
    learning_agent = models.IntegerField(choices=LEARNING_AGENT_TYPES)

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

    def __str__(self):
        return self.filename


class ActionLog(models.Model):
    STATUS_REQUEST_RECEIVED = 0
    STATUS_INVALID_REWARD = 1
    STATUS_INVALID_STATE = 2
    STATUS_POLICY_EXECUTION_FAILURE = 3
    STATUS_OKAY = 4

    PROCESSING_STATUS_TYPES = (
            (STATUS_REQUEST_RECEIVED, 'Request received'),
            (STATUS_INVALID_REWARD, 'Invalid reward'),
            (STATUS_INVALID_STATE, 'Invalid state'),
            (STATUS_POLICY_EXECUTION_FAILURE, 'Policy execution failure'),
            (STATUS_OKAY, 'Okay'),
    )

    user = models.ForeignKey(AppUser, on_delete=models.CASCADE)
    query_time = models.DateTimeField()
    reward_state_message = models.CharField(max_length=256)
    action_message = models.CharField(max_length=256)

    # meta
    reward = models.FloatField()
    processing_status = models.IntegerField(choices=PROCESSING_STATUS_TYPES)


class ExceptionLog(models.Model):
    request_path = models.CharField(max_length=256)
    user = models.ForeignKey(AppUser, on_delete=models.SET_NULL, null=True)
    log_time = models.DateTimeField()
    content = models.TextField()

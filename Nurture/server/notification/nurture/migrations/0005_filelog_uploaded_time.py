# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-06-28 20:21
from __future__ import unicode_literals

import datetime
import pytz

from django.db import migrations, models
from django.utils.timezone import utc
from django.db import transaction


def filename_to_datetime_data_migration(apps, schema_editor):
    FileLog = apps.get_model('nurture', 'FileLog')

    with transaction.atomic():
        for fl in FileLog.objects.all():
            tmp_datetime = datetime.datetime.strptime(fl.filename, '%Y%m%d-%H%M%S.txt')
            fl.uploaded_time = tmp_datetime.astimezone(pytz.timezone('America/Los_Angeles'))
            fl.save()

class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0004_auto_20180627_2148'),
    ]

    operations = [
        migrations.AddField(
            model_name='filelog',
            name='uploaded_time',
            field=models.DateTimeField(default=datetime.datetime(2018, 6, 28, 20, 21, 18, 116137, tzinfo=utc)),
            preserve_default=False,
        ),
        migrations.RunPython(filename_to_datetime_data_migration),
    ]
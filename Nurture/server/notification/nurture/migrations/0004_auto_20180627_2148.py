# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-06-28 04:48
from __future__ import unicode_literals

from django.db import migrations, models

from django.db import transaction


def eliminate_duplicates_data_migration(apps, schema_editor):
    AppUser = apps.get_model('nurture', 'AppUser')
    FileLog = apps.get_model('nurture', 'FileLog')

    with transaction.atomic():
        # eliminate duplicate app users
        code_set = set([u.code for u in AppUser.objects.all()])
        for code in code_set:
            users = AppUser.objects.filter(code=code)
            for u in users[1:]:
                u.delete()
        
        # eliminate duplicate file logs
        file_log_set = set([(fl.user.code, fl.type, fl.filename) for fl in FileLog.objects.all()])
        for fl_tuple in file_log_set:
            user = AppUser.objects.get(code=fl_tuple[0])
            file_logs = FileLog.objects.filter(user=user, type=fl_tuple[1], filename=fl_tuple[2])
            for fl in file_logs[1:]:
                fl.delete()


class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0003_auto_20180627_2120'),
    ]

    operations = [
        migrations.RunPython(eliminate_duplicates_data_migration),
        migrations.AlterField(
            model_name='appuser',
            name='code',
            field=models.CharField(max_length=25, unique=True),
        ),
        migrations.AlterUniqueTogether(
            name='filelog',
            unique_together=set([('user', 'type', 'filename')]),
        ),
    ]

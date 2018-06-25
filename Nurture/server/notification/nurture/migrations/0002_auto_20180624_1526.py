# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-06-24 22:26
from __future__ import unicode_literals

import datetime
from django.db import migrations, models
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='appuser',
            name='created_time',
            field=models.DateTimeField(default=datetime.datetime(2018, 6, 24, 22, 26, 0, 478950, tzinfo=utc)),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='appuser',
            name='name',
            field=models.CharField(default='', max_length=256),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='appuser',
            name='status',
            field=models.IntegerField(choices=[(1, 'Active'), (0, 'Finish experiment'), (-1, 'Hide this user')], default=1),
            preserve_default=False,
        ),
    ]

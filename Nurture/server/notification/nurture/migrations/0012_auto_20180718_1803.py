# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-07-19 01:03
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0011_auto_20180717_1531'),
    ]

    operations = [
        migrations.AlterField(
            model_name='appuser',
            name='learning_agent',
            field=models.IntegerField(choices=[(0, 'Random'), (1, 'Attelia2'), (2, 'Q-learning'), (4, 'Q-learning-replay'), (3, 'Debug')]),
        ),
    ]

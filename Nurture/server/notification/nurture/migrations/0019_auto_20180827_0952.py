# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-08-27 16:52
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0018_appuser_hit_cap'),
    ]

    operations = [
        migrations.AlterField(
            model_name='appuser',
            name='learning_agent',
            field=models.IntegerField(choices=[(0, 'Random'), (1, 'Attelia2'), (2, 'Q-learning'), (4, 'Q-learning-replay'), (6, 'DQN (TF)'), (7, 'A3C (coach)'), (8, 'Classification'), (3, 'Debug'), (5, 'Silent')]),
        ),
    ]

# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2018-07-10 23:03
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('nurture', '0008_appuser_learning_agent'),
    ]

    operations = [
        migrations.AlterField(
            model_name='appuser',
            name='learning_agent',
            field=models.IntegerField(choices=[(0, 'Random'), (1, 'Attelia2')]),
        ),
    ]

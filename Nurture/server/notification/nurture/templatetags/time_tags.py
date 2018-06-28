import datetime

from django import template
from django.template import Context
from django.template.loader import get_template
from django.utils.html import format_html

from django.utils import timezone


register = template.Library()


MINUTE_TO_SECONDS = 60
HOUR_TO_SECONDS = 60 * 60
DAY_TO_SECONDS = 24 * 60 * 60



def _complete_time_elapsed_phrase(num, unit):
    return '%d %s%s ago' % (num, unit, 's' if num != 1 else '')

def _figure_out_granularity(seconds_since):
    if seconds_since < 0:
        return 'In future'
    elif seconds_since < HOUR_TO_SECONDS:
        return _complete_time_elapsed_phrase(seconds_since // MINUTE_TO_SECONDS, 'minute')
    elif seconds_since < DAY_TO_SECONDS:
        return _complete_time_elapsed_phrase(seconds_since // HOUR_TO_SECONDS, 'hour')
    else:
        return _complete_time_elapsed_phrase(seconds_since // DAY_TO_SECONDS, 'day')


@register.simple_tag
def time_since(previous_time):
    delta = timezone.now() - previous_time
    seconds_since = delta.days * DAY_TO_SECONDS + delta.seconds
    return format_html(_figure_out_granularity(seconds_since))


@register.simple_tag
def time_since_flag_anomaly(previous_time, tolerance):
    delta = timezone.now() - previous_time
    seconds_since = delta.days * DAY_TO_SECONDS + delta.seconds
    text = _figure_out_granularity(seconds_since)

    if seconds_since < 0 or seconds_since > tolerance:
        extra_style = "color:red; font-weight:bold"
    else:
        extra_style = 'color:#777'

    return format_html(
            "<span style='font-size:13px;font-style:italic;%s'>%s</span>" % (extra_style, text))

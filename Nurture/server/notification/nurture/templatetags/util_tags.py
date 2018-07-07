from django import template
from django.template import Context
from django.template.loader import get_template
from django.utils.html import format_html


register = template.Library()


@register.filter
def ljust(string, length):
    return s.ljust(length)


@register.simple_tag
def str_repeat(char, length):
    return char * length


@register.filter
def flag_anomaly_if_value(value, blocked_value):
    span_class = 'highlight-flag' if value == blocked_value else 'highlight-okay'
    return format_html("<span class='%s'>%s</span>" % (span_class, str(value)))


@register.filter
def flag_anomaly_if_gte(value, threshold):
    span_class = 'highlight-flag' if value >= theshold else 'highlight-okay'
    return format_html("<span class='%s'>%s</span>" % (span_class, str(value)))


@register.filter
def flag_anomaly_if_lte(value, threshold):
    span_class = 'highlight-flag' if value <= theshold else 'highlight-okay'
    return format_html("<span class='%s'>%s</span>" % (span_class, str(value)))

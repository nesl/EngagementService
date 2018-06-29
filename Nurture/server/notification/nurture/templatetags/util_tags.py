from django import template
from django.template import Context
from django.template.loader import get_template


register = template.Library()


@register.filter
def ljust(string, length):
    return s.ljust(length)


@register.simple_tag
def str_repeat(char, length):
    return char * length

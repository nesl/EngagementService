import time

from django import template
from django.template import Context
from django.template.loader import get_template


register = template.Library()


def extract_timestamp(line):
    if len(line) < 13:
        return '-'
    
    prefix = line[:10]  # just to get the second part
    if not prefix.isdigit():
        return '-'

    return time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(int(prefix)))


@register.simple_tag
def show_content_with_datetime(filelog):
    with open(filelog.get_path(), 'r') as f:
        lines = f.readlines()

    timestamps = '\n'.join([extract_timestamp(line) for line in lines])
    content = ''.join(lines)

    template = get_template("filelog/show_filelog_content.html")

    context = Context({
        'timestamps': timestamps,
        'content': content,
    })

    return template.render(context)

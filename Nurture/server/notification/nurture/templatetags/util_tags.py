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


@register.simple_tag
def format_user_name_code(app_user):
    if app_user.name == '':
        return format_html(app_user.code)
    else:
        return format_html('%s (%s)' % (app_user.name, app_user.code))


@register.simple_tag
def select_html_tag(tag_name, options, default_value):
    template = get_template("util_tags/select.html")

    context = Context({
        'name': tag_name,
        'options': options,
        'default_value': default_value,
    })

    return template.render(context)


@register.simple_tag
def select_html_tag_user_specific(user, tag_name_suffix, options, default_value):
    tag_name = "%s-%s" % (user.code, tag_name_suffix)
    return select_html_tag(tag_name, options, default_value)

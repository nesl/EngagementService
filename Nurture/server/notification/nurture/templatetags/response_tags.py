import time

from django import template
from django.template import Context
from django.template.loader import get_template

from nurture import utils


register = template.Library()


@register.simple_tag
def show_response_raw_content(responses):
    timestamp_width = 19
    reward_state_width = 20
    action_width = 20

    for r in responses:
        reward_state_width = max(reward_state_width, len(r.reward_state_message))
        action_width = max(action_width, len(r.action_message))

    lines = []
    lines.append('    '.join([
        'Time'.ljust(timestamp_width),
        'Action Message'.ljust(action_width),
        'State-Reward Message'.ljust(reward_state_width),
    ]))
    lines.append('-----------------------------------------------------------------------------')
    for r in responses:
        lines.append('    '.join([
            utils.convert_to_local_timezone(r.query_time).strftime('%Y-%m-%d %H:%M:%S'),
            r.action_message.ljust(action_width),
            r.reward_state_message.ljust(reward_state_width),
        ]))

    return "\n".join(lines)

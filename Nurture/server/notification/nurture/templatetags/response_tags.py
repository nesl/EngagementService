import math
import datetime

from django import template
from django.template import Context
from django.template.loader import get_template

from django.utils.html import format_html

from django.utils import timezone

from nurture import utils


register = template.Library()


NUM_BUCKETS = 24
CHUNK_SIZE = 6
NUM_CHUNKS = math.ceil(NUM_BUCKETS // CHUNK_SIZE)


LIGHT_GREEN = (185, 255, 171)
DARK_GREEN = (0, 128, 128)
LIGHT_RED = (255, 178, 178)
DARK_RED = (170, 0, 0)

WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
LIGHT_GREY = (240, 240, 240)

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


def _retrieve_rewards(response):
    try:
        return float(response.reward_state_message.split(';')[0][1:-1])
    except:
        return 0.

def _interpolate_int(v1, v2, r):
    return int(v1 + (v2 - v1) * r)

def _pick_color(c1, c2, r1, r2, v):
    if r1 == r2:
        return c1

    ratio = (v - r1) / (r2 - r1)
    return (
            _interpolate_int(c1[0], c2[0], ratio),
            _interpolate_int(c1[1], c2[1], ratio),
            _interpolate_int(c1[2], c2[2], ratio),
    )

def _assign_connection_background_color(v, threshold):
    if v is None:
        return None

    if v > threshold:
        return DARK_RED
    return _pick_color(LIGHT_GREEN, DARK_GREEN, 0, threshold, v)

def _assign_reward_background_color(v, max_reward, min_reward):
    if v is None:
        return None

    if v <= 0.:
        return _pick_color(LIGHT_RED, DARK_RED, 0, min_reward, v)
    else:
        return _pick_color(LIGHT_GREEN, DARK_GREEN, 0, max_reward, v)

def _fill_buckets(responses, interval_sec):
    buckets = [[] for _ in range(NUM_BUCKETS)]
    now = timezone.now()
    for r in responses:
        delta = now - r.query_time
        seconds_since = delta.days * 60 * 60 * 24 + delta.seconds
        bucket_idx = seconds_since // interval_sec
        if 0 <= bucket_idx and bucket_idx < NUM_BUCKETS:
            buckets[bucket_idx].append(r)
    return buckets

def _get_history_labels(interval_sec, label_format):
    now = timezone.now()
    offsets = [i * CHUNK_SIZE * interval_sec for i in range(NUM_CHUNKS)]
    times = [now - datetime.timedelta(seconds=offset) for offset in offsets]
    return [utils.convert_to_local_timezone(tp).strftime(label_format) for tp in times]

@register.simple_tag
def visualize_connection_history_div(responses, interval_sec, label_format):
    
    buckets = _fill_buckets(responses, interval_sec)

    # process connections
    expected_num_connections = int(interval_sec / 60. * 1.1 + 2)  # give 10% tolerance
    num_connections = [len(batch) for batch in buckets]
    num_connections = [(v if v > 0 else None) for v in num_connections]
    connection_bk_colors = [_assign_connection_background_color(v, expected_num_connections)
            for v in num_connections]

    # process rewards
    rewards = [[res.reward for res in batch] for batch in buckets]
    rewards = [(round(sum(batch), 1) if len(batch) > 0 else None) for batch in rewards]
    valid_rewards = [r for r in rewards if r is not None]
    if len(valid_rewards) == 0:
        max_reward = 1.
        min_reward = -1.
    else:
        max_reward = max(1., max(valid_rewards))
        min_reward = min(-1., min(valid_rewards))
    reward_bk_colors = [_assign_reward_background_color(v, max_reward, min_reward)
            for v in rewards]

    labels = _get_history_labels(interval_sec, label_format)

    template = get_template("response/history_div.html")

    context = Context({
        'rows': [
            {
                'title': 'Requests',
                'values': num_connections,
                'bk_colors': connection_bk_colors,
            },
            {
                'title': 'Rewards',
                'values': rewards,
                'bk_colors': reward_bk_colors,
            },
        ],
        'labels': labels,
    })

    return template.render(context)


def _assign_color_by_max(v, threshold, zero_color, low_color, high_color):
    if v is None:
        return None
    if v == 0:
        return zero_color
    if v > threshold:
        return high_color
    return _pick_color(low_color, high_color, 0, threshold, v)

def _assign_notification_background_color(v, threshold):
    return _assign_color_by_max(v, threshold, LIGHT_GREY, LIGHT_GREEN, DARK_GREEN)

def _assign_accept_background_color(v, threshold):
    return _assign_color_by_max(v, threshold, LIGHT_GREY, LIGHT_GREEN, DARK_GREEN)
    
def _assign_dismiss_background_color(v, threshold):
    return _assign_color_by_max(v, threshold, LIGHT_GREY, LIGHT_RED, DARK_RED)

@register.simple_tag
def visualize_response_history_div(responses, interval_sec, label_format):
    
    buckets = _fill_buckets(responses, interval_sec)

    # process total notifications
    num_notifications = [(len(list(filter(lambda b: b.action_message == 'action-1', batch)))
            if len(batch) > 0 else None) for batch in buckets]
    max_num_notifications = max(1, max([v if v is not None else 0 for v in num_notifications]))
    notification_bk_colors = [_assign_notification_background_color(v, max_num_notifications)
            for v in num_notifications]

    # process accepted notifications
    num_accepts = [(sum([b.num_accepted for b in batch]) if len(batch) > 0 else None)
            for batch in buckets]
    #max_num_accepts = max(1, max([v if v is not None else 0 for v in num_accepts]))
    accept_bk_colors = [_assign_accept_background_color(v, max_num_notifications)
            for v in num_accepts]

    # process dismissed notifications
    num_dismisses = [(sum([b.num_dismissed for b in batch]) if len(batch) > 0 else None)
            for batch in buckets]
    #max_num_dismisses = max(1, max([v if v is not None else 0 for v in num_dismisses]))
    dismiss_bk_colors = [_assign_dismiss_background_color(v, max_num_notifications)
            for v in num_dismisses]

    labels = _get_history_labels(interval_sec, label_format)

    template = get_template("response/history_div.html")

    context = Context({
        'rows': [
            {
                'title': '# notifications',
                'values': num_notifications,
                'bk_colors': notification_bk_colors,
            },
            {
                'title': 'Answer amount',
                'values': num_accepts,
                'bk_colors': accept_bk_colors,
            },
            {
                'title': 'Dismiss amount',
                'values': num_dismisses,
                'bk_colors': dismiss_bk_colors,
            },
        ],
        'labels': labels,
    })

    return template.render(context)


@register.simple_tag
def cell_bar_div(title, values, bk_colors):
    if len(values) != len(bk_colors):
        raise Exception("Lengths are not equal")

    bundles = [({'text': v, 'bk_color': bk_color} if v is not None else None)
            for v, bk_color in zip(values, bk_colors)]

    template = get_template("response/cell_bar_div.html")

    context = Context({
        'title': title,
        'bundles': bundles,
    })

    return template.render(context)


@register.simple_tag
def cell_bar_label_div(labels):
    context = Context({
        'labels': labels,
    })
    return get_template("response/cell_bar_label_div.html").render(context)


@register.filter
def rgbhtml(color, arg=None):
    if type(color) is not tuple or len(color) != 3:
        raise Exception("invalid color variable")
    return format_html("rgb(%d, %d, %d)" % color)


@register.filter
def background_to_font_color(color, arg=None):
    if type(color) is not tuple or len(color) != 3:
        raise Exception("invalid color variable")

    background_intensity = color[0] * 0.299 + color[1] * 0.587 + color[2] * 0.114
    return WHITE if background_intensity < 156. else BLACK

import time

from django import template
from django.template import Context
from django.template.loader import get_template

from nurture import utils
from nurture.tasks.task_response import TaskResponse


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


def _quick_analysis_process_task_response(filelog):
    responses = TaskResponse.parse_response_file(filelog.get_path())
    if len(responses) == 0:
        return None

    num_total_notifications = len(responses)
    answered_responses = [r for r in responses if r.is_answered()]
    num_answered_responses = len(answered_responses)
    answered_ratio = utils.get_ratio(num_answered_responses, num_total_notifications)
    
    question_types = [
            (6, 'Arithmetic Task'),
            (8, 'Image Task'),
    ]

    distribution = []
    for question_type_id, question_name in question_types:
        within_type = [r for r in answered_responses if r.question_type == question_type_id]
        within_type_correct = [r for r in within_type if r.is_correct_answer()]
        num_answered = len(within_type)
        num_correct = len(within_type_correct)
        ratio = utils.get_ratio(num_correct, num_answered)
        distribution.append({
            'name': question_name,
            'answered': num_answered,
            'correct': num_correct,
            'correct_perc': round(ratio * 100., 1),
        })

    template = get_template("filelog/quick_analysis/task_responses.html")
    context = Context({
        'num_total_notifications': num_total_notifications,
        'num_answered_responses': num_answered_responses,
        'answered_perc': round(answered_ratio * 100., 1),
        'distribution': distribution,
    })
    return template.render(context)
    

@register.simple_tag
def try_show_quick_analysis(filelog):
    analysis_proxy = {
            'task-response': _quick_analysis_process_task_response,
    }

    if filelog.type not in analysis_proxy:
        return ""
    content = analysis_proxy[filelog.type](filelog)
    if content is None:
        return ""
    
    template = get_template("filelog/quick_analysis_div.html")
    context = Context({
        'content': content,
    })
    return template.render(context)

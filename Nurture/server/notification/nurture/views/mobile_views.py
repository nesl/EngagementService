import pytz
import random
import base64
import dill
import datetime

from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone

from nurture.models import *
from nurture import utils


NOTIFICATION_DAILY_VOLUME_CAP = 55


@csrf_exempt
def get_user_code(request):
    print("get request 'get_user_code'")

    code = None
    trials = 10
    while trials > 0:
        picked_code = str(random.randint(0, 99999)).zfill(5)
        if not AppUser.objects.filter(code=picked_code).exists():
            code = picked_code
            break
        trials -= 1

    if code is None:
        return HttpResponse("Bad", status=404)
    
    AppUser.objects.create(
            code=code,
            name="",
            status=AppUser.STATUS_ACTIVE,
            created_time=timezone.now(),
            learning_agent=AppUser.LEARNING_AGENT_RANDOM,
            hit_cap=False,
    )
    return HttpResponse(code, status=200)


@csrf_exempt
def upload_log_file(request):

    # extract user
    if 'code' not in request.POST:
        return HttpResponse("Bad", status=404)

    try:
        user = AppUser.objects.get(code=request.POST['code'])
    except AppUser.DoesNotExist:
        return HttpResponse("Bad", status=404)

    # extract type
    if 'type' not in request.POST:
        return HttpResponse("Bad", status=404)
    type = request.POST['type']

    # extract content
    if 'content' not in request.POST:
        return HttpResponse("Bad", status=404)
    content_base64 = request.POST['content']
    try:
        content_bytes = base64.b64decode(content_base64)
    except:
        return HttpResponse("Wrong encoding", status=404)
    
    # uploaded time
    now = timezone.now().astimezone(pytz.timezone('US/Pacific'))
    filename = utils.convert_to_local_timezone(timezone.now()).strftime('%Y%m%d-%H%M%S.txt')

    # write file
    try:
        record = FileLog.objects.get(user=user, type=type, filename=filename)
    except FileLog.DoesNotExist:
        record = FileLog(user=user, type=type, filename=filename)
    record.uploaded_time = now

    path = record.get_path()
    os.makedirs(os.path.dirname(path), exist_ok=True)

    with open(path, 'wb') as fo:
        fo.write(content_bytes)

    record.save()

    # delete previous file if the latest version covers everything of the previous version
    # (it should always be the case unless a user reinstall the app)
    files = FileLog.objects.filter(user=user, type=type).order_by('-id')
    if len(files) >= 2:
        prev_file_log = files[1]
        if utils.is_file_extended(prev_file_log.get_path(), record.get_path()):
            os.remove(prev_file_log.get_path())
            prev_file_log.delete()

    return HttpResponse("Ok", status=200)


def _process_reward_sub_term(sub_term):
    elements = sub_term.split(':')
    original_reward = float(elements[0])

    if len(elements) == 1:
        return original_reward

    # don't change punishment
    if original_reward <= 0.:
        return original_reward

    # reward changes based on response time: 0.9 ^ minutes
    elapsed_time_min = float(elements[1]) / 60.
    print("elasped_time_min", elapsed_time_min, 0.9 ** elapsed_time_min)
    return 0.9 ** elapsed_time_min

def _is_night(state):
    morning_threshold = 10. / 24.  # 10am
    evening_threshold = 22. / 24.  # 10pm
    return state.timeOfDay < morning_threshold or state.timeOfDay > evening_threshold

def _reach_notification_quota(user):
    # to address the problem of different timezones, we first select the actions in the past
    # 24 hours, and then figure out when is the start of the day
    now = timezone.now()
    ago_1d = now - datetime.timedelta(days=1)
    action_logs = ActionLog.objects.filter(
            user=user, action_message='action-1', query_time__gt=ago_1d).order_by('id')
    
    if len(action_logs) > NOTIFICATION_DAILY_VOLUME_CAP:
        # let's figure out when is the start day only when we need, i.e., the number of selected
        # records is more than the threshold. we choose the first day switch (i.e., the timestamp
        # of predecessor is larger than the current record
        time_of_day = [float(a.reward_state_message.split(';')[1][1:-1].split(',')[0])
                for a in action_logs]
        total = len(action_logs)
        for i in range(len(time_of_day) - 1):
            if time_of_day[i] > time_of_day[i+1]:
                action_logs = action_logs[i+1:total]
                break

    return len(action_logs) >= NOTIFICATION_DAILY_VOLUME_CAP


@csrf_exempt
def get_action(request):
    
    # extract user
    if 'code' not in request.POST:
        return HttpResponse("Bad", status=404)

    try:
        user = AppUser.objects.get(code=request.POST['code'])
    except AppUser.DoesNotExist:
        return HttpResponse("Bad", status=404)

    # extract reward-state message
    if 'observation' not in request.POST:
        return HttpResponse("Bad", status=404)
    raw_reward_state_message = request.POST['observation']

    print("get request 'get_action' and identifies user", request.POST['code'])

    # query time
    now = timezone.now()

    log = ActionLog.objects.create(
            user=user,
            query_time=now,
            reward_state_message=raw_reward_state_message,
            action_message='?',
            reward=0.,
            num_accepted=0,
            num_dismissed=0,
            processing_status=ActionLog.STATUS_REQUEST_RECEIVED,
    )

    # the format of the request format can be found in this Google Doc:
    # https://docs.google.com/document/d/1YUBMl02jqsc6ChYNuLhf8mVD3tM-GUGq_BIwp8bkx4A/edit

    # get reward
    try:
        terms = raw_reward_state_message.split(';')
        for term in terms:
            assert term[0] == '[' and term[-1] == ']'
        terms = [t[1:-1] for t in terms]
        if terms[0] == '':
            reward = 0.
            num_accepted = 0
            num_dismissed = 0
        else:
            reward_list = list(map(_process_reward_sub_term, terms[0].split(',')))
            reward = sum(reward_list)
            num_accepted = len([r for r in reward_list if r > 0.])
            num_dismissed = len([r for r in reward_list if r < 0.])
    except:
        utils.log_last_exception(request, user)
        log.processing_status = ActionLog.STATUS_INVALID_REWARD
        log.save()
        return HttpResponse("Bad", status=404)

    # extract states
    try:
        state1 = utils.convert_request_text_to_state(terms[1])
        assert terms[2] in ['continue', 'discontinue']
        state2 = (utils.convert_request_text_to_state(terms[3])
                if terms[2] == 'discontinue' else None)
    except:
        utils.log_last_exception(request, user)
        log.processing_status = ActionLog.STATUS_INVALID_REWARD
        log.save()
        return HttpResponse("Bad", status=404)

    # execute policy
    try:
        LearningAgent = utils.get_learning_agent_class_for_user(user)

        if user.hit_cap and _reach_notification_quota(user):
            # unfortunately, cannot send anymore notifications
            action = 'c'
        elif LearningAgent.non_disturb_mode_during_night() and _is_night(state1):
            # non disturb mode
            action = 'z'
        else:
            # regular mode
            model_path = utils.prepare_learning_agent(user)
            agent = dill.load(open(model_path, 'rb'))
            agent.on_pickle_load()
            agent.set_user_code(user.code)

            agent.feed_reward(reward)
            send_notification = agent.get_action(state1)
            if state2 is not None:
                agent.restart_episode()
                send_notification = agent.get_action(state2)

            agent.on_pickle_save()
            dill.dump(agent, open(model_path, 'wb'))

            action = '1' if send_notification else '0'
    except:
        utils.log_last_exception(request, user)
        log.processing_status = ActionLog.STATUS_POLICY_EXECUTION_FAILURE
        log.save()
        return HttpResponse("Bad", status=404)

    action_message = "action-%s" % action

    log.reward = reward
    log.num_accepted = num_accepted
    log.num_dismissed = num_dismissed
    log.action_message = action_message
    log.processing_status = ActionLog.STATUS_OKAY
    log.save()

    return HttpResponse(action_message, status=200)

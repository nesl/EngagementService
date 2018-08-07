import pytz
import random
import base64
import dill

from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone

from nurture.models import *
from nurture import utils


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
        else:
            reward = sum(list(map(_process_reward_sub_term, terms[0].split(','))))
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

        if LearningAgent.non_disturb_mode_during_night() and _is_night(state1):
            # non disturb mode
            action = 0
        else:
            # regular mode
            model_path = utils.prepare_learning_agent(user)
            agent = dill.load(open(model_path, 'rb'))
            agent.on_pickle_load()

            agent.feed_reward(reward)
            send_notification = agent.get_action(state1)
            if state2 is not None:
                agent.restart_episode()
                send_notification = agent.get_action(state2)

            agent.on_pickle_save()
            dill.dump(agent, open(model_path, 'wb'))

            action = 1 if send_notification else 0
    except:
        utils.log_last_exception(request, user)
        log.processing_status = ActionLog.STATUS_POLICY_EXECUTION_FAILURE
        log.save()
        return HttpResponse("Bad", status=404)

    action_message = "action-%d" % action

    log.reward = reward
    log.action_message = action_message
    log.processing_status = ActionLog.STATUS_OKAY
    log.save()

    return HttpResponse(action_message, status=200)

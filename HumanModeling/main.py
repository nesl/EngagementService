from agent import *
from environment import *
from controller import Controller

def main():
    #agent = AlwaysSendNotificationAgent()
    agent = QLearningAgent()
    #agent = ContextualBanditSVMAgent()
    
    #environment = AlwaysSayOKUser()
    #environment = StubbornUser()
    #environment = LessStubbornUser()
    #environment = SurveyUser('survey/ver1_pilot/data/02.txt')
    environment = MTurkSurveyUser([
            'survey/ver2_mturk/results/01_1st_Batch_3137574_batch_results.csv',
    ])
    
    controller = Controller(agent, environment)
    results = controller.execute()

    notificationEvents = [r for r in results if r['decision']]
    numNotifications = len(notificationEvents)
    numAcceptedNotifications = len([r for r in notificationEvents if r['reward'] > 0])
    answerRate = numAcceptedNotifications / numNotifications

    expectedNumDeliveredNotifications = sum([r['probOfAnswering'] for r in results])

    print("%d decision points" % len(results))
    print("%d notifications are sent, %d are answered (%.2f%%)"
            % (numNotifications, numAcceptedNotifications, answerRate * 100.))
    print("Expectation of total delivered notifications is %.2f" % expectedNumDeliveredNotifications)

if __name__ == "__main__":
    main()

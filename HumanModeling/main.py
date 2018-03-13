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
    environment = MTurkSurveyUser(filePaths=[
            'survey/ver2_mturk/results/01_1st_Batch_3137574_batch_results.csv',
            'survey/ver2_mturk/results/02_Batch_3148398_batch_results.csv',
            'survey/ver2_mturk/results/03_Batch_3149214_batch_results.csv',
    ], filterFunc=(lambda r: ord(r['rawWorkerID'][-1]) % 3 == 2))
    
    controller = Controller(agent, environment)
    results = controller.execute()

    notificationEvents = [r for r in results if r['decision']]
    numNotifications = len(notificationEvents)
    numAcceptedNotifications = len([r for r in notificationEvents if r['reward'] > 0])
    answerRate = numAcceptedNotifications / numNotifications
    numDismissedNotifications = len([r for r in notificationEvents if r['reward'] < 0])
    dismissRate = numDismissedNotifications / numNotifications

    expectedNumDeliveredNotifications = sum([r['probOfAnswering'] for r in results])

    print("%d decision points" % len(results))
    print("%d notifications are sent:" % numNotifications)
    print("  - %d are answered (%.2f%%)"  % (numAcceptedNotifications, answerRate * 100.))
    print("  - %d are dismissed (%.2f%%)"  % (numDismissedNotifications, dismissRate * 100.))
    print("Expectation of total delivered notifications is %.2f" % expectedNumDeliveredNotifications)

if __name__ == "__main__":
    main()

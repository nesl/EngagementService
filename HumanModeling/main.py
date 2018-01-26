from agent import *
from environment import *
from controller import Controller

def main():
    agent = AlwaysSendNotificationAgent()
    
    environment = AlwaysSayOKUser()
    
    controller = Controller(agent, environment)
    results = controller.execute()

    notificationEvents = [r for r in results if r['decision']]
    numNotifications = len(notificationEvents)
    numAcceptedNotifications = len([r for r in notificationEvents if r['reward'] > 0])
    answerRate = numAcceptedNotifications / numNotifications

    print("%d decision points" % len(results))
    print("%d notifications are sent, %d are answered (%.2f%%)"
            % (numNotifications, numAcceptedNotifications, answerRate * 100.))

if __name__ == "__main__":
    main()

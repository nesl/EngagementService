class BaseEnvironment:
    
    def getUserContext(self, hour, minute, day, lastNotificationTime):
        """
        This function returns user context including the following two parts: User's physical
        states like location and activity as part of the observation, and user's internal
        perception of notifications (i.e., at this given moment, how likely the user is going to
        answer the notification.)

        Returns:
          (stateLocation, stateActivity, probAnsweringNotification, probIgnoringNotification, probDismissingNotification)
            - stateLocation: An enum of location
            - stateActivity: An enum of activity
            - probAnsweringNotification: A float number between 0. to 1.
            - probIgnoringNotification: A float number between 0. to 1.
            - probDismissingNotification: A float number between 0. to 1.

          The sum of `probAnsweringNotification`, `probIgnoringNotification`, and
          `probDismissingNotification` is expected to be 1.0.
        """
        pass

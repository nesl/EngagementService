class State:

    MOTION_STATIONARY = 0
    MOTION_WALKING = 1
    MOTION_RUNNING = 2
    MOTION_DRIVING = 3
    MOTION_BIKING = 4

    LOCATION_HOME = 0
    LOCATION_WORK = 1
    LOCATION_OTHER = 2

    RINGER_MODE_SILENT = 0
    RINGER_MODE_VIBRATE = 1
    RINGER_MODE_NORMAL = 2

    SCREEN_STATUS_ON = 0
    SCREEN_STATUS_OFF = 1


    def __init__(self, timeOfDay, dayOfWeek, motion, location,
            notificationTimeElapsed, ringerMode, screenStatus):

        assert 0.0 <= timeOfDay and timeOfDay <= 1.0
        assert 0.0 <= dayOfWeek and dayOfWeek <= 1.0
        assert motion in State.allMotionValues()
        assert location in State.allLocationValues()
        assert 0.0 <= notificationTimeElapsed
        assert ringerMode in State.allRingerModeValues()
        assert screenStatus in State.allScreenStatusValues()

        self.timeOfDay = timeOfDay
        self.dayOfWeek = dayOfWeek
        self.motion = motion
        self.location = location
        self.notificationTimeElapsed = notificationTimeElapsed
        self.ringerMode = ringerMode
        self.screenStatus = screenStatus

    @staticmethod
    def allMotionValues():
        return [
            State.MOTION_STATIONARY,
            State.MOTION_WALKING,
            State.MOTION_RUNNING,
            State.MOTION_DRIVING,
            State.MOTION_BIKING,
        ]

    @staticmethod
    def allLocationValues():
        return [
            State.LOCATION_HOME,
            State.LOCATION_WORK,
            State.LOCATION_OTHER,
        ]
    
    @staticmethod
    def allRingerModeValues():
        return [
            State.RINGER_MODE_SILENT,
            State.RINGER_MODE_VIBRATE,
            State.RINGER_MODE_NORMAL,
        ]

    @staticmethod
    def allScreenStatusValues():
        return [
            State.SCREEN_STATUS_ON,
            State.SCREEN_STATUS_OFF,
        ]

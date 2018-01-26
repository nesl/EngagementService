package ucla.nesl.emubloodpressuredemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final int NOTI_BP_ID = 0;
    private static final int NOTI_R_ID = 1;

    private NotificationManager notificationManager;
    private Notification notiBP;
    private Notification notiR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationHandler.sendEmptyMessage(0);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notiR = new Notification.Builder(MainActivity.this)
                .setContentTitle("My Health Assistant")
                .setContentText("Don't forget your daily exercise!")
                .build();

        notiBP = new Notification.Builder(MainActivity.this)
                .setContentTitle("My Health Assistant")
                .setContentText("Please keep a record of your blood pressure!")
                .build();

        scheduleNextRunningNotification();
    }

    private void scheduleNextRunningNotification() {
        // get timestamp of 10am of today
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 10);

        long runningTimeThreshold = cal.getTimeInMillis();
        if (runningTimeThreshold < now)
            runningTimeThreshold += 86400;

        notificationHandler.scheduleNotificationAtTime(notiR, runningTimeThreshold, NOTI_R_ID);
    }

    private void scheduleNextBloodPressureNotification() {
        long measurementTimeThreshold = System.currentTimeMillis() + 15 * 60 * 1000L;
        notificationHandler.scheduleNotificationAtTime(notiBP, measurementTimeThreshold, NOTI_BP_ID);
    }


    private void onBloodPressureMeasured() {
        notificationManager.cancel(NOTI_BP_ID);
        scheduleNextRunningNotification();
    }

    private void onRunning() {
        notificationManager.cancel(NOTI_R_ID);
        scheduleNextBloodPressureNotification();
    }

    private SendNotificationHandler notificationHandler = new SendNotificationHandler();

    private class SendNotificationHandler extends Handler {
        public void scheduleNotificationAtTime(Notification noti, long time, int id) {
            Message msg = new Message();
            msg.what = id;
            msg.obj = noti;
            sendMessageAtTime(msg, time);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            // hide the notification after its selected
            Notification noti = (Notification) inputMessage.obj;
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(NOTI_BP_ID, noti);
        }
    }
}

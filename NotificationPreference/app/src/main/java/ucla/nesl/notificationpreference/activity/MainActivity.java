package ucla.nesl.notificationpreference.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class MainActivity extends AppCompatActivity {

    // Notification related
    private NotificationHelper notificationHelper;
    private ToastShortcut toastHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationHelper = new NotificationHelper(this);

        // UI
        Button button;

        button = findViewById(R.id.button1);
        button.setOnClickListener(sendNotificationEvent);

        button = findViewById(R.id.button2);
        button.setOnClickListener(cancelNotificationEvent);
    }

    View.OnClickListener sendNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            notificationHandler.sendEmptyMessageDelayed(0, 5000L);
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(300);
        }
    };

    View.OnClickListener cancelNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            notificationHelper.cancelNotification(NotificationHelper.Type.LOCATION_CHANGED);
        }
    };

    Handler notificationHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            notificationHelper.sendNotification(NotificationHelper.Type.LOCATION_CHANGED);
        }
    };
}

package ucla.nesl.notificationpreference.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.service.TaskSchedulingService;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class MainActivity extends AppCompatActivity {

    // permissions
    private static final int ACTIVITY_EDITOR_RESULT_REQUEST_CODE = 0;

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private static final String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    // notification related
    private ToastShortcut toastHelper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE);

        // UI
        Button button;

        button = findViewById(R.id.button1);
        button.setOnClickListener(sendNotificationEvent);

        button = findViewById(R.id.button2);
        button.setOnClickListener(cancelNotificationEvent);

        //AlarmEventManager alarmEventManager = new AlarmEventManager(this);
        //alarmEventManager.registerWorker(new TestAlarmWorker("B", 3000L, 5000L));
        //alarmEventManager.registerWorker(new TestAlarmWorker("A", 5000L, 10000L));

        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    View.OnClickListener sendNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //notificationHandler.sendEmptyMessageDelayed(0, 5000L);
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(300);
        }
    };

    View.OnClickListener cancelNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //notificationHelper.cancelNotification(NotificationHelper.Type.LOCATION_CHANGED);
        }
    };

}

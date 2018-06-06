package ucla.nesl.notificationpreference.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.history.ResponseHistoryActivity;
import ucla.nesl.notificationpreference.service.TaskSchedulingService;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;

    // permissions
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

        Intent serviceIntent = new Intent(this, TaskSchedulingService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //region Section: UI Option menu
    // =============================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_debug: {
                Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    View.OnClickListener sendNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //notificationHandler.sendEmptyMessageDelayed(0, 5000L);
            //Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            //vibrator.vibrate(300);

            /*
            LatLngBounds bound = LatLngBounds.builder()
                    //.include(new LatLng(34.069627, -118.454081))
                    .include(new LatLng(34.071627, -118.454081))
                    .include(new LatLng(34.067627, -118.454081))
                    .build();

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            builder.setLatLngBounds(bound);

            try {
                startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
            } catch (Exception e) {
                Log.i("MainActivity", "Get exception", e);
            }*/

            startActivity(ConfigurePlaceActivity.getIntentForStartActivity(
                    MainActivity.this, ConfigurePlaceActivity.MODE_INITIALIZE));
        }
    };

    View.OnClickListener cancelNotificationEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //notificationHelper.cancelNotification(NotificationHelper.Type.LOCATION_CHANGED);
            Intent intent = new Intent(getApplicationContext(), ResponseHistoryActivity.class);
            startActivity(intent);
        }
    };


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                String toastMsg = String.format("Place: %s %s", place.getName(), place.getLatLng());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
}

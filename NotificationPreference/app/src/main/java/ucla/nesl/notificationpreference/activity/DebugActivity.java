package ucla.nesl.notificationpreference.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.io.File;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;
import ucla.nesl.notificationpreference.utils.ToastShortcut;

public class DebugActivity extends AppCompatActivity {

    private ToastShortcut toastHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button dumpDBButton = findViewById(R.id.buttonDBDump);
        dumpDBButton.setOnClickListener(dumpDBEvent);

        toastHelper = new ToastShortcut(this);
    }

    View.OnClickListener dumpDBEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NotificationResponseRecordDatabase database =
                    NotificationResponseRecordDatabase.getAppDatabase(DebugActivity.this);
            File file = new File(Environment.getExternalStorageDirectory(),
                    "notification_response.dump.csv");
            boolean successful = database.dump(file);
            if (successful) {
                toastHelper.showShort("Database is successfully dumped");
            } else {
                toastHelper.showLong("Error occurs when dumping DB!!");
            }
        }
    };
}

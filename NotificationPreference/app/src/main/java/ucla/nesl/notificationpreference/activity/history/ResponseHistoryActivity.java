package ucla.nesl.notificationpreference.activity.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.INotificationEventListener;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;

public class ResponseHistoryActivity extends AppCompatActivity
        implements INotificationEventListener {

    // database
    NotificationResponseRecordDatabase database;

    // notification helper to receive events
    NotificationHelper notificationHelper;

    // UI Widgets
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // state
    private boolean firstTimeSetAdapter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_history);

        database = NotificationResponseRecordDatabase.getAppDatabase(this);

        notificationHelper = new NotificationHelper(this, false, this);

        mRecyclerView = findViewById(R.id.listViewResponses);
        //mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateList();
    }

    private void updateList() {
        mAdapter = new ResponseAdapter(database.getAllRecordsReverseOrder(), this);
        if (firstTimeSetAdapter) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.swapAdapter(mAdapter, false);
        }

        firstTimeSetAdapter = false;
    }

    public View.OnClickListener getOnClickEventListenerToCompleteTask(final int notificationID) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResponseHistoryActivity.this, TaskActivity.class);
                NotificationHelper.overloadInfoOnIntentForActivity(intent, notificationID);
                startActivity(intent);
            }
        };
    }

    @Override
    public void onNotificationEvent(int notificationID, int eventID) {
        Log.i("ResponseHistoryA", "Receive " + notificationID + " " + eventID);
        updateList();
    }
}

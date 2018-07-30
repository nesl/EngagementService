package ucla.nesl.notificationpreference.activity.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ViewAnimator;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.INotificationEventListener;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.notification.enums.NotificationEventType;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;

public class ResponseHistoryActivity extends AppCompatActivity
        implements INotificationEventListener {

    private static final int MAX_NUM_NOTIFICATIONS_IN_VIEW = 100;

    // database
    private NotificationResponseRecordDatabase database;

    // notification helper to receive events
    private NotificationHelper notificationHelper;

    // UI Widgets
    private ViewAnimator layoutContainer;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // state
    private boolean firstTimeSetAdapter = true;
    private boolean isInListView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_history);

        database = NotificationResponseRecordDatabase.getAppDatabase(this);

        notificationHelper = new NotificationHelper(this, false, this);

        layoutContainer = findViewById(R.id.container);

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
        mAdapter = new ResponseAdapter(
                database.getLastNRecords(MAX_NUM_NOTIFICATIONS_IN_VIEW), this);
        if (firstTimeSetAdapter) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.swapAdapter(mAdapter, false);
        }

        if (mAdapter.getItemCount() > 0) {
            trySwitchToListView();
        }
        firstTimeSetAdapter = false;
    }

    private void trySwitchToListView() {
        if (isInListView) {
            return;
        }
        layoutContainer.showNext();
        isInListView = true;
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
    public void onNotificationEvent(int notificationID, NotificationEventType event) {
        Log.i("ResponseHistoryA", "Receive " + notificationID + " " + event);
        updateList();
    }
}

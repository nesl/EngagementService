package ucla.nesl.notificationpreference.activity.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.storage.NotificationResponseRecordDatabase;

public class ResponseHistoryActivity extends AppCompatActivity {

    // database
    NotificationResponseRecordDatabase database;

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
        mAdapter = new ResponseAdapter(database.getAllRecordsReverseOrder());
        if (firstTimeSetAdapter) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mRecyclerView.swapAdapter(mAdapter, false);
        }

        firstTimeSetAdapter = false;
    }
}

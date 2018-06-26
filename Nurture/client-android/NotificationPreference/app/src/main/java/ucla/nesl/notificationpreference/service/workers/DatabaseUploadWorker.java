package ucla.nesl.notificationpreference.service.workers;

import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import ucla.nesl.notificationpreference.alarm.NextTrigger;
import ucla.nesl.notificationpreference.storage.SharedPreferenceHelper;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecordDatabase;

/**
 * Created by timestring on 6/26/18.
 *
 * Upload database content back to server
 */

public class DatabaseUploadWorker extends FileUploadWorker {

    private NotificationResponseRecordDatabase database;

    public DatabaseUploadWorker(
            ConnectivityManager connectivityManager,
            SharedPreferenceHelper keyValueStore,
            String type,
            NotificationResponseRecordDatabase _database
    ) {
        super(connectivityManager, keyValueStore, type,
                NotificationResponseRecordDatabase.DEFAULT_FILE);
        database = _database;
    }

    @NonNull
    @Override
    protected NextTrigger onPlan() {
        database.dump(NotificationResponseRecordDatabase.DEFAULT_FILE);
        return super.onPlan();
    }
}

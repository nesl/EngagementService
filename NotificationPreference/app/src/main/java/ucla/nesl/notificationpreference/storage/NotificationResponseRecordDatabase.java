package ucla.nesl.notificationpreference.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by timestring on 5/25/18.
 */

@Database(entities = {NotificationResponseRecord.class}, version = 1)
public abstract class NotificationResponseRecordDatabase extends RoomDatabase {
    private static NotificationResponseRecordDatabase INSTANCE;

    public abstract NotificationResponseRecordDao getDao();

    public static NotificationResponseRecordDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            // Since the database scale of this app will be small, we do the computation on the
            // main thread to simplify the development process. The impact of the app fluency should
            // be negligible.
            INSTANCE = Room.databaseBuilder(context,
                                            NotificationResponseRecordDatabase.class,
                                            "user-activity-database")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public int createResponseRecord(int questionType, int questionSubType) {
        NotificationResponseRecord record = new NotificationResponseRecord();
        record.createdTime = System.currentTimeMillis();
        record.questionType = questionType;
        record.questionSubType = questionSubType;
        return (int) getDao().insert(record);
    }

    public NotificationResponseRecord getRecordByID(int ID) {
        return getDao().getRecordByID(ID);
    }
}




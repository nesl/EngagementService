package ucla.nesl.notificationpreference.storage;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by timestring on 5/25/18.
 *
 * `NotificationResponseRecordDatabase` provides a couple operations and communicates with low-level
 * database system. This class implements the singleton pattern.
 */

@Database(entities = {NotificationResponseRecord.class}, version = 1)
public abstract class NotificationResponseRecordDatabase extends RoomDatabase {

    private static final String TAG = "NotiResponseDB";

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
        record.subQuestionType = questionSubType;
        return (int) getDao().insert(record);
    }

    public NotificationResponseRecord getRecordByID(int ID) {
        return getDao().getRecordByID(ID);
    }

    public void fillAnswer(int notificationID, @NonNull String answer) {
        getDao().updateAnswer(notificationID, System.currentTimeMillis(), answer);
    }

    public boolean dump(File file) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file));

            // header
            writer.writeNext(new String[]{
                    "ID",
                    "created_time",
                    "question_type",
                    "sub_question_type",
                    "answer_time",
                    "answer",
                    "expired_time"
            });

            // data
            for (NotificationResponseRecord record : getDao().getAllRecords()) {
                writer.writeNext(new String[]{
                        String.valueOf(record.getID()),
                        String.valueOf(record.createdTime),
                        String.valueOf(record.questionType),
                        String.valueOf(record.subQuestionType),
                        String.valueOf(record.answerTime),
                        String.valueOf(record.answer),
                        String.valueOf(record.expiredTime)
                });
            }

            writer.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception when dumping DB", e);
            return false;
        }

        return true;
    }
}




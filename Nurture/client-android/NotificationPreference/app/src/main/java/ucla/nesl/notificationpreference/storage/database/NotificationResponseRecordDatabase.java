package ucla.nesl.notificationpreference.storage.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Created by timestring on 5/25/18.
 *
 * `NotificationResponseRecordDatabase` provides a couple operations and communicates with low-level
 * database system. This class implements the singleton pattern.
 */

@Database(entities = {NotificationResponseRecord.class}, version = 1)
public abstract class NotificationResponseRecordDatabase extends RoomDatabase {

    private static final String TAG = "NotiResponseDB";

    public static final File DEFAULT_FILE = new File(
            Environment.getExternalStorageDirectory(), "notification_response.dump.csv");


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

    public int createResponseRecord(int questionType, int subQuestionType) {
        NotificationResponseRecord record = new NotificationResponseRecord();
        record.createdTime = System.currentTimeMillis();
        record.questionType = questionType;
        record.subQuestionType = subQuestionType;
        record.status = NotificationResponseRecord.STATUS_APPEAR;
        return (int) getDao().insert(record);
    }

    public NotificationResponseRecord getRecordByID(int ID) {
        return getDao().getRecordByID(ID);
    }

    public ArrayList<NotificationResponseRecord> getAllRecordsReverseOrder() {
        return new ArrayList<>(getDao().getAllRecordsReverseOrder());
    }

    public ArrayList<NotificationResponseRecord> getLastFiveRecords() {
        return new ArrayList<>(getDao().getLastFiveRecords());
    }

    public ArrayList<NotificationResponseRecord> getLastNRecords(int num) {
        return new ArrayList<>(getDao().getLastNRecords(num));
    }

    public NotificationResponseRecord getLastRecordByType(int questionType) {
        return getDao().getLastRecordByType(questionType);
    }

    public void fillAnswer(int notificationID, @NonNull String answer, int optionID) {
        getDao().updateAnswer(notificationID, System.currentTimeMillis(), answer, optionID);
    }

    public void recordDismissedNotification(int notificationID) {
        getDao().setDismiss(notificationID, System.currentTimeMillis());
    }

    public void expireOneNotification(int notificationID) {
        getDao().tryExpireNotification(notificationID, System.currentTimeMillis());
    }

    public void expireOutDatedNotifications(long validTimeSpan) {
        long now = System.currentTimeMillis();
        long createTimeThreshold = now - validTimeSpan;
        getDao().expireOutDatedNotifications(createTimeThreshold, now);
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
                    "status",
                    "answer_time",
                    "answer",
                    "option_ID",
                    "is_dismissed",
                    "dismiss_time",
                    "expired_time"
            });

            // data
            for (NotificationResponseRecord record : getDao().getAllRecords()) {
                writer.writeNext(new String[]{
                        String.valueOf(record.getID()),
                        String.valueOf(record.createdTime),
                        String.valueOf(record.questionType),
                        String.valueOf(record.subQuestionType),
                        String.valueOf(record.status),
                        String.valueOf(record.answerTime),
                        String.valueOf(record.answer),
                        String.valueOf(record.optionID),
                        String.valueOf(record.isDismissed),
                        String.valueOf(record.dismissTime),
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

package ucla.nesl.notificationpreference.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by timestring on 5/25/18.
 *
 * This class defines the common database queries when updating `notification_response_record`
 * table. Please see `NotificationResponseRecord` class for more details.
 */

@Dao
public interface NotificationResponseRecordDao {

    @Insert
    long insert(NotificationResponseRecord record);

    @Query("SELECT * FROM notification_response_record WHERE ID = :ID")
    NotificationResponseRecord getRecordByID(int ID);

    @Query("SELECT * FROM notification_response_record ORDER BY ID")
    List<NotificationResponseRecord> getAllRecords();

    @Query("SELECT * FROM notification_response_record ORDER BY ID DESC")
    List<NotificationResponseRecord> getAllRecordsReverseOrder();

    //@Query("SELECT * FROM user_activity WHERE end_time_ms = " + Utils.INVALID_TIME + " ORDER BY start_time_ms DESC")
    //List<UserActivity> getAllWithInvalidEndTimeStartTimeDesc();

    //@Query("SELECT * FROM user_activity WHERE end_time_ms >= :threshold ORDER BY start_time_ms DESC")
    //List<UserActivity> getAllWithEndTimeGreaterThanStartTimeDesc(long threshold);

    @Query("SELECT COUNT(*) from notification_response_record")
    int countAll();

    @Query("UPDATE notification_response_record " +
           "SET answer_time = :answerTime, answer = :answer, " +
           "    status = " + NotificationResponseRecord.STATUS_RESPONDED + " " +
           "WHERE ID = :notificationID")
    void updateAnswer(int notificationID, long answerTime, @NonNull String answer);
}

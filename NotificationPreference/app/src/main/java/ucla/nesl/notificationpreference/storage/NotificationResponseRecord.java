package ucla.nesl.notificationpreference.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by timestring on 5/25/18.
 *
 * This class is a data structure for the record of notification responses. Each object records
 * the status of one notification. This class also declares the database table schema.
 */

@Entity(tableName = "notification_response_record")
public class NotificationResponseRecord {

    public static final int STATUS_APPEAR = 0;
    public static final int STATUS_SEEN = 1;
    public static final int STATUS_RESPONDED = 2;
    public static final int STATUS_EXPIRED = 3;


    public static final long NOT_SET = 0L;

    @PrimaryKey(autoGenerate = true)
    private int ID;

    @ColumnInfo(name = "created_time")
    public long createdTime;

    @ColumnInfo(name = "question_type")
    public int questionType;

    @ColumnInfo(name = "sub_question_type")
    public int subQuestionType;

    @ColumnInfo(name = "status")
    public int status;

    @ColumnInfo(name = "answer_time")
    public long answerTime = NOT_SET;

    @ColumnInfo(name = "answer")
    public @NonNull String answer = "";

    @ColumnInfo(name = "expired_time")
    public long expiredTime = NOT_SET;


    // ==== Getters and setters for Room Database Library ==========================================

    /**
     * It is declared for Room Database Library, as well as a getter for the rest of the app.
     */
    public int getID() {
        return ID;
    }

    /**
     * Please do not use `setID()` directly in the code. It is declared for Room Database Library.
     */
    public void setID(int ID) {
        this.ID = ID;
    }
}

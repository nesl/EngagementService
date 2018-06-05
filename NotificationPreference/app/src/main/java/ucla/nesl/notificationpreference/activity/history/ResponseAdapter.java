package ucla.nesl.notificationpreference.activity.history;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ucla.nesl.notificationpreference.R;
import ucla.nesl.notificationpreference.storage.database.NotificationResponseRecord;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;
import ucla.nesl.notificationpreference.task.TaskFactory;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 5/29/18.
 *
 * An adapter of `RecyclerView` to help `ResponseHistoryActivity` to list all the existing tasks
 */

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ViewHolder> {

    private static final String DATE_FORMAT = "MMM dd, H:mm";

    private ArrayList<NotificationResponseRecord> records;
    private ResponseHistoryActivity historyActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public ResponseAdapter(
            @NonNull ArrayList<NotificationResponseRecord> _records,
            ResponseHistoryActivity _activity
    ) {
        records = _records;
        historyActivity = _activity;
    }

    @Override
    public ResponseAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notification_reponse, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.i("ResponseAdapter", "Get view in position " + position);
        View rowView = holder.view;
        final NotificationResponseRecord record = records.get(position);
        ShortQuestionTask task = TaskFactory.retrieveExistingTask(record);

        TextView questionText = rowView.findViewById(R.id.textQuestionStat);
        questionText.setText(task.getPrimaryQuestionStatement());

        TextView notifiedTimeText = rowView.findViewById(R.id.textAppearTimeStat);
        notifiedTimeText.setText(Utils.formatDate(DATE_FORMAT, record.createdTime));

        TextView statusText = rowView.findViewById(R.id.textStatusStat);
        statusText.setText(getStatusText(record.status));

        RelativeLayout answerLayout = rowView.findViewById(R.id.layoutAnswer);
        TextView answerText = rowView.findViewById(R.id.textAnswerStat);
        if (record.status != NotificationResponseRecord.STATUS_RESPONDED) {
            answerLayout.setVisibility(View.GONE);
        } else {
            answerLayout.setVisibility(View.VISIBLE);
            answerText.setText(record.answer);
        }

        Button buttonCompleteQuestion = rowView.findViewById(R.id.buttonComplete);
        if (shouldShowCompleteTaskButton(record.status)) {
            buttonCompleteQuestion.setVisibility(View.VISIBLE);
        } else {
            buttonCompleteQuestion.setVisibility(View.GONE);
        }
        buttonCompleteQuestion.setOnClickListener(
                historyActivity.getOnClickEventListenerToCompleteTask(record.getID()));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    private String getStatusText(int status) {
        switch (status) {
            case NotificationResponseRecord.STATUS_APPEAR:
            case NotificationResponseRecord.STATUS_SEEN:
                return "Not answered yet";
            case NotificationResponseRecord.STATUS_RESPONDED:
                return "Answered";
            case NotificationResponseRecord.STATUS_EXPIRED:
                return "Expired";
        }
        throw new IllegalArgumentException("Unrecognized status");
    }

    private boolean shouldShowCompleteTaskButton(int status) {
        return (status == NotificationResponseRecord.STATUS_APPEAR ||
                status == NotificationResponseRecord.STATUS_SEEN);
    }
}
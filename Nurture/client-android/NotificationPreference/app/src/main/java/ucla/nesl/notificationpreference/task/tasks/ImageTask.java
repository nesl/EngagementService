package ucla.nesl.notificationpreference.task.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;
import ucla.nesl.notificationpreference.utils.DP;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 7/17/18.
 *
 * Task that shows an image and the user need to select the correct tag.
 */

public class ImageTask extends ShortQuestionTask {

    public static final int TASK_ID = 8;

    private Context context;

    private int questionSeed;


    public ImageTask(int notificationID, int seed, Context _context) {
        super(notificationID);
        questionSeed = seed;
        context = _context;
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return new Random().nextInt(1000000);
    }

    public static long getCoolDownTime() {
        //return TimeUnit.MINUTES.toMillis(30);
        return 0L;
    }

    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        super.fillNotificationLayout(notificationHelper, builder);

        // primary problem statement
        Bitmap originalBitmap = Utils.getImageFromAsset(context, "lisa.jpg");
        Bitmap displayBitmap = Utils.addMarginForNotification(originalBitmap);
        Log.i("ImageTask", "size: " + displayBitmap.getWidth() + "x" + displayBitmap.getHeight());
        builder.setContentText(getPrimaryQuestionStatement())
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(displayBitmap)
                        .bigLargeIcon(null))
                .setLargeIcon(originalBitmap);

        // choices
        String[] options = getOptions();
        for (int i = 0; i < options.length; i++) {
            builder.addAction(android.R.drawable.checkbox_on_background, options[i],
                    getButtonActionPendingIndent(i + 1, options[i]));
        }
    }

    public ViewGroup getViewLayoutInActivity(TaskActivity taskActivity) {

        LinearLayout layout = new LinearLayout(taskActivity);
        layout.setOrientation(LinearLayout.VERTICAL);

        // blank space
        TextView topBlankSpace = new TextView(taskActivity);
        LinearLayout.LayoutParams topBlankSpaceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, DP.toPX(20));
        topBlankSpace.setLayoutParams(topBlankSpaceLayoutParams);
        topBlankSpace.setVisibility(View.INVISIBLE);
        layout.addView(topBlankSpace);

        // image
        ImageView image = new ImageView(taskActivity);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Bitmap originalBitmap = Utils.getImageFromAsset(context, "lisa.jpg");
        image.setImageBitmap(originalBitmap);
        image.setLayoutParams(imageLayoutParams);
        layout.addView(image);

        // blank space
        TextView bottomBlankSpace = new TextView(taskActivity);
        LinearLayout.LayoutParams bottomBlankSpaceLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, DP.toPX(20));
        bottomBlankSpace.setLayoutParams(bottomBlankSpaceLayoutParams);
        bottomBlankSpace.setVisibility(View.INVISIBLE);
        layout.addView(bottomBlankSpace);

        // buttons
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                DP.toPX(200), LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.gravity = Gravity.CENTER;

        String[] options = getOptions();
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int optionID = i + 1;  // optionID is 1-index to be compatible
            Button button = new Button(taskActivity);
            button.setText(option);
            button.setLayoutParams(buttonLayoutParams);
            button.setOnClickListener(taskActivity.getOnClickEventListenerForResponse(
                    getNotificationID(), option, optionID));
            button.setGravity(Gravity.CENTER);
            layout.addView(button);
        }

        return layout;
    }

    @NonNull
    @Override
    public String getPrimaryQuestionStatement() {
        return "What is the object in the image?";
    }

    protected String[] getOptions() {
        //TODO
        return new String[] {"A", "B", "C"};
    }

    private Bitmap getDummyBitmap() {
        return null;
    }
}

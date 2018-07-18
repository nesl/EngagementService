package ucla.nesl.notificationpreference.task.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ucla.nesl.notificationpreference.activity.TaskActivity;
import ucla.nesl.notificationpreference.notification.NotificationHelper;
import ucla.nesl.notificationpreference.task.tasks.template.ShortQuestionTask;
import ucla.nesl.notificationpreference.utils.DP;
import ucla.nesl.notificationpreference.utils.Utils;

/**
 * Created by timestring on 7/17/18.
 *
 * Task that shows an image and the user need to select the correct tag.
 *
 * To check if the question is answered correctly, see buttonID = seed % 3 + 1
 */

public class ImageTask extends ShortQuestionTask {

    public static final int TASK_ID = 8;

    private static final String[] imageCategories = new String[] {
            "bear",
            "bird",
            "butterfly",
            "cat",
            "crab",
            "dog",
            "elephant",
            "fish",
            "gorilla",
            "lizard",
            "turtle",
    };
    private static final int NUM_IMAGES_IN_EACH_CATEGORY = 90;


    private Context context;

    private int questionSeed;

    private int correctAnswerPos;
    private int correctLabelIdx;
    private int imageIdx;
    private String imageFileName;


    public ImageTask(int notificationID, int seed, Context _context) {
        super(notificationID);
        questionSeed = seed;
        context = _context;

        int tSeed = questionSeed;
        correctAnswerPos = tSeed % 3;
        tSeed /= 3;
        correctLabelIdx = tSeed % imageCategories.length;
        tSeed /= imageCategories.length;
        imageIdx = tSeed % NUM_IMAGES_IN_EACH_CATEGORY;

        imageFileName = String.format(Locale.getDefault(),
                "%s/%03d.jpg", imageCategories[correctLabelIdx], imageIdx);
    }

    @Override
    public int getTypeID() {
        return TASK_ID;
    }

    public static int sampleQuestionSeedIfCreatedNow() {
        return new Random().nextInt(1000000);
    }

    public static long getCoolDownTime() {
        return TimeUnit.MINUTES.toMillis(30);
    }

    @Override
    public void fillNotificationLayout(NotificationHelper notificationHelper,
                                       NotificationCompat.Builder builder) {

        super.fillNotificationLayout(notificationHelper, builder);

        // primary problem statement
        Bitmap originalBitmap = Utils.getImageFromAsset(context, imageFileName);
        Bitmap displayBitmap = Utils.addMarginForNotification(originalBitmap);
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
        Bitmap originalBitmap = Utils.getImageFromAsset(context, imageFileName);
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
        Random random = new Random(questionSeed);

        String[] options = new String[3];
        HashSet<Integer> usedLabelIndexes = new HashSet<Integer>();

        options[correctAnswerPos] = imageCategories[correctLabelIdx];
        usedLabelIndexes.add(correctLabelIdx);
        for (int i = 0; i < 3; i++) {
            if (i == correctAnswerPos) {
                continue;
            }

            while (true) {
                int die = random.nextInt(imageCategories.length);
                if (!usedLabelIndexes.contains(die)) {
                    options[i] = imageCategories[die];
                    usedLabelIndexes.add(die);
                    break;
                }
            }
        }

        return options;
    }

}

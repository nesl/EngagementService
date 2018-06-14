package ucla.nesl.notificationpreference.activity.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * Created by timestring on 6/14/18.
 */

class ChangeUserCodeDialogHelper {

    static void createAndShowDialog(final MainActivity activity) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        alert.setTitle("User code");
        alert.setMessage("Change user code (please don't do it unless you're a study coordinator");

        final EditText input = new EditText(activity);
        alert.setView(input);

        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String code = input.getText().toString();
                activity.tryUpdateUserCode(code);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }

    // disable instantiation
    private ChangeUserCodeDialogHelper() {}
}

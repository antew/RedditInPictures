package com.antew.redditinpictures.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.antew.redditinpictures.pro.R;

public class UpdateToFullVersionDialogFragment extends DialogFragment {

    public interface UpdateToFullVersionDialogListener {
        void onFinishUpgradeDialog();
    }

    // Empty constructor required for DialogFragment
    public UpdateToFullVersionDialogFragment() {}

    ;

    public static UpdateToFullVersionDialogFragment newInstance() {
        return new UpdateToFullVersionDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.upgrade_to_full_version)
                                                              .setMessage(
                                                                  "Upgrading to the pro version will allow you to log in, cast votes, and save images to the gallery.")
                                                              .setPositiveButton(R.string.upgrade, new DialogInterface.OnClickListener() {
                                                                  public void onClick(DialogInterface dialog, int whichButton) {
                                                                      UpdateToFullVersionDialogListener activity = (UpdateToFullVersionDialogListener) getActivity();
                                                                      activity.onFinishUpgradeDialog();
                                                                  }
                                                              })
                                                              .setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                                                                  public void onClick(DialogInterface dialog, int whichButton) {
                                                                      dialog.cancel();
                                                                  }
                                                              })
                                                              .create();

        return dialog;
    }
}
package com.antew.redditinpictures.library.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.antew.redditinpictures.pro.R;

public class SetDefaultSubredditsDialogFragment extends DialogFragment {

    public static SetDefaultSubredditsDialogFragment newInstance() {
        return new SetDefaultSubredditsDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.default_subreddits_title)
                                                                           .setMessage(R.string.default_subreddits_message)
                                                                           .setPositiveButton(R.string.yes,
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                      ((SetDefaultSubredditsDialogListener) getActivity())
                                                                                                          .onSetDefaultSubreddits();
                                                                                                  }
                                                                                              })
                                                                           .setNegativeButton(R.string.no,
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                      dialog.cancel();
                                                                                                  }
                                                                                              });
        return dialog.create();
    }

    public interface SetDefaultSubredditsDialogListener {
        void onSetDefaultSubreddits();
    }
}
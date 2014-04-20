/*
 * Copyright (C) 2014 Antew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;

public class ExitDialogFragment extends DialogFragment {
    public ExitDialogFragment() {
    }

    public static ExitDialogFragment newInstance() {
        return new ExitDialogFragment();
    }

    ;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.exit)
                                                                           .setMessage(R.string.are_you_sure_you_want_to_exit)
                                                                           .setPositiveButton(R.string.yes,
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                      ExitDialogListener activity = (ExitDialogListener) getActivity();
                                                                                                      activity.onFinishExitDialog();
                                                                                                  }
                                                                                              }
                                                                                             )
                                                                           .setNegativeButton(R.string.no,
                                                                                              new DialogInterface.OnClickListener() {
                                                                                                  public void onClick(
                                                                                                      DialogInterface dialog,
                                                                                                      int whichButton) {
                                                                                                      dialog.cancel();
                                                                                                  }
                                                                                              }
                                                                                             );

        String username = BundleUtil.getString(getArguments(), Constants.Extra.EXTRA_USERNAME, null);
        if (!Strings.isEmpty(username)) {
            dialog.setMessage(String.format(getString(R.string.are_you_sure_you_want_to_log_out_with_username), username));
        }

        return dialog.create();
    }

    public interface ExitDialogListener {
        void onFinishExitDialog();
    }
}
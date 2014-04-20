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

        Dialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.feature_not_available)
                                                              .setMessage(R.string.upgrade_value_prop)
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
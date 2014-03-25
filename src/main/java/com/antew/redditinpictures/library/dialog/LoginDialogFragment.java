/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.pro.R;

public class LoginDialogFragment extends DialogFragment implements OnEditorActionListener, TextWatcher {

    private TextView username;
    private TextView password;

    public interface LoginDialogListener {
        void onFinishLoginDialog(String username, String password);
    }

    // Empty constructor required for DialogFragment
    public LoginDialogFragment() {};

    public static LoginDialogFragment newInstance() {
        return new LoginDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater lf = LayoutInflater.from(getActivity());
        View dialogView = lf.inflate(R.layout.login_dialog, null);

        username = (TextView) dialogView.findViewById(R.id.login_username);
        password = (TextView) dialogView.findViewById(R.id.login_password);
        password.setOnEditorActionListener(this);
        username.requestFocus();
        //@formatter:off
        final AlertDialog dialog 
                = new AlertDialog.Builder(getActivity())
                                 .setView(dialogView)
                                 .setTitle(R.string.log_in_to_reddit)
                                 .setPositiveButton(R.string.log_in, null)
                                 .setNegativeButton(R.string.cancel, null)
                                 .create();
        //@formatter:on
        // We have to override setOnShowListener here (min API level 8) in order to validate
        // the inputs before closing the dialog. Just overriding setPositiveButton closes the
        // automatically when the button is pressed
        dialog.setOnShowListener(getDialogOnShowListener());
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        username.addTextChangedListener(this);
        password.addTextChangedListener(this);
        return dialog;
    }

    /**
     * We have to override setOnShowListener here (min API level 8) in order to validate
     * the inputs before closing the dialog. Just overriding setPositiveButton closes the
     * automatically when the button is pressed
     * @return The onShowListener for the AlertDialog
     */
    private OnShowListener getDialogOnShowListener() {
        return new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button login = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                login.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        doLogin();
                    }
                });

            }
        };
    }

    /**
     * Perform the login if the user presses the IME key from the password field
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            doLogin();
            return true;
        }
        return false;
    }

    /**
     * Returns true if the username and password pass validation.
     * 
     * @return False if the username or password is blank
     */
    private boolean hasErrors() {
        boolean hasErrors = false;
        if (username.getText().length() == 0) {
            username.setError("Enter a username", ImageUtil.getErrorDrawable(getActivity()));
            hasErrors = true;
        }

        if (password.getText().length() == 0) {
            password.setError("Enter a password", ImageUtil.getErrorDrawable(getActivity()));
            hasErrors = true;
        }

        return hasErrors;
    }

    /**
     * Perform the login, provided {@link #hasErrors()} returns false
     */
    private void doLogin() {
        if (!hasErrors()) {
            LoginDialogListener activity = (LoginDialogListener) getActivity();
            activity.onFinishLoginDialog(username.getText().toString(), password.getText().toString());
            this.dismiss();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    /**
     * If the user receives an error message due to a blank field, this automatically
     * clears the error on the field once they remedy it.
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (username.getText().length() > 0 && username.getError() != null)
            username.setError(null);
        
        if (password.getText().length() > 0 && password.getError() != null)
            password.setError(null);
    }
}
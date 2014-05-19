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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
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
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.ImageUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;

public class LoginDialogFragment extends DialogFragment implements OnEditorActionListener, TextWatcher {

    private String   mUsername;
    private TextView mUsernameText;
    private TextView mPasswordText;

    // Empty constructor required for DialogFragment
    public LoginDialogFragment() {}

    public static LoginDialogFragment newInstance() {
        return new LoginDialogFragment();
    }

    public static LoginDialogFragment newInstance(String username) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.Extra.EXTRA_USERNAME, username);

        LoginDialogFragment fragment = new LoginDialogFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater lf = LayoutInflater.from(getActivity());
        View dialogView = lf.inflate(R.layout.login_dialog, null);

        if (getArguments() != null) {
            mUsername = BundleUtil.getString(getArguments(), Constants.Extra.EXTRA_USERNAME, null);
        }

        mUsernameText = (TextView) dialogView.findViewById(R.id.login_username);
        mPasswordText = (TextView) dialogView.findViewById(R.id.login_password);
        mPasswordText.setOnEditorActionListener(this);
        if (Strings.notEmpty(mUsername)) {
            mUsernameText.setText(mUsername);
            mPasswordText.requestFocus();
        } else {
            mUsernameText.requestFocus();
        }
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                                                                         .setTitle(R.string.log_in_to_reddit)
                                                                         .setPositiveButton(R.string.log_in, null)
                                                                         .setNegativeButton(R.string.cancel, null)
                                                                         .create();
        // We have to override setOnShowListener here (min API level 8) in order to validate
        // the inputs before closing the dialog. Just overriding setPositiveButton closes the
        // automatically when the button is pressed
        dialog.setOnShowListener(getDialogOnShowListener());
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mUsernameText.addTextChangedListener(this);
        mPasswordText.addTextChangedListener(this);
        return dialog;
    }

    /**
     * We have to override setOnShowListener here (min API level 8) in order to validate
     * the inputs before closing the dialog. Just overriding setPositiveButton closes the
     * automatically when the button is pressed
     *
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
     * Perform the login, provided {@link #hasErrors()} returns false
     */
    private void doLogin() {
        if (!hasErrors()) {
            LoginDialogListener activity = (LoginDialogListener) getActivity();
            activity.onFinishLoginDialog(mUsernameText.getText().toString(), mPasswordText.getText().toString());
            this.dismiss();
        }
    }

    /**
     * Returns true if the username and password pass validation.
     *
     * @return False if the username or password is blank
     */
    private boolean hasErrors() {
        boolean hasErrors = false;
        if (mUsernameText.getText().length() == 0) {
            mUsernameText.setError("Enter a username", ImageUtil.getErrorDrawable(getActivity()));
            hasErrors = true;
        }

        if (mPasswordText.getText().length() == 0) {
            mPasswordText.setError("Enter a password", ImageUtil.getErrorDrawable(getActivity()));
            hasErrors = true;
        }

        return hasErrors;
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
        if (mUsernameText.getText().length() > 0 && mUsernameText.getError() != null) {
            mUsernameText.setError(null);
        }

        if (mPasswordText.getText().length() > 0 && mPasswordText.getError() != null) {
            mPasswordText.setError(null);
        }
    }

    public interface LoginDialogListener {
        void onFinishLoginDialog(String username, String password);
    }
}
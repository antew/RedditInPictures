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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.antew.redditinpictures.library.util.BundleUtil;
import com.antew.redditinpictures.library.util.ImageUtil;
import com.antew.redditinpictures.pro.R;

public class SaveImageDialogFragment extends DialogFragment implements OnEditorActionListener, TextWatcher {
    public static final String FILENAME = "fileName";
    private EditText filename;

    // Empty constructor required for DialogFragment
    public SaveImageDialogFragment() {}

    public static SaveImageDialogFragment newInstance(String defaultFilename) {
        SaveImageDialogFragment frag = new SaveImageDialogFragment();
        Bundle b = new Bundle();
        b.putString(FILENAME, defaultFilename);
        frag.setArguments(b);

        return frag;
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String initialFilename = BundleUtil.getString(getArguments(), FILENAME, null);

        LayoutInflater lf = LayoutInflater.from(getActivity());
        View dialogView = lf.inflate(R.layout.save_image_dialog, null);

        filename = (EditText) dialogView.findViewById(R.id.save_image_name);
        filename.setText(initialFilename);
        filename.selectAll();
        filename.setOnEditorActionListener(this);
        filename.requestFocus();
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                                                                         .setTitle(R.string.save_image)
                                                                         .setPositiveButton(R.string.save_image, null)
                                                                         .setNegativeButton(R.string.cancel, null)
                                                                         .create();
        // We have to override setOnShowListener here (min API level 8) in order to validate
        // the inputs before closing the dialog. Just overriding setPositiveButton closes the
        // automatically when the button is pressed
        dialog.setOnShowListener(getDialogOnShowListener());
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        filename.addTextChangedListener(this);
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
                Button save = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                save.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        saveImage();
                    }
                });
            }
        };
    }

    /**
     * Perform the callback, provided {@link #hasErrors()} returns false
     */
    private void saveImage() {
        if (!hasErrors()) {
            SaveImageDialogListener activity = (SaveImageDialogListener) getActivity();
            activity.onFinishSaveImageDialog(filename.getText().toString());
            this.dismiss();
        }
    }

    /**
     * Returns true if the filename passes validation.
     *
     * @return True if the filename is valid, false if the the filename is invalid.
     */
    private boolean hasErrors() {
        boolean hasErrors = false;
        if (filename.getText().length() == 0) {
            filename.setError("Enter a filename", ImageUtil.getErrorDrawable(getActivity()));
            hasErrors = true;
        }

        return hasErrors;
    }

    /**
     * Perform the callback if the user presses the IME key from the filename
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            saveImage();
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
        if (filename.getText().length() > 0 && filename.getError() != null) {
            filename.setError(null);
        }
    }

    public interface SaveImageDialogListener {
        void onFinishSaveImageDialog(String filename);
    }
}
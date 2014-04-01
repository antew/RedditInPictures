package com.antew.redditinpictures.library.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.pro.R;
import java.util.ArrayList;

public class AddSubredditDialogFragment extends DialogFragment {
    /**
     * Poor mans check to reduce network calls. Based on the the Reddit source code the minimum length for a subreddit is 3 characters. See
     * https://github.com/reddit/reddit/blob/master/r2/r2/lib/validator/validator.py#L512
     */
    private static final int MIN_SEARCH_LENGTH = 3;
    private TextWatcher mSubredditWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            return;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s == null) {
                return;
            } else if (s.length() >= MIN_SEARCH_LENGTH) {
                searchForSubreddits(s.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            return;
        }
    };
    @InjectView(R.id.et_subreddit)
    protected AutoCompleteTextView mSubreddit;
    private TextView.OnEditorActionListener mSubredditSearchEditorActionListener = new TextView.OnEditorActionListener() {

        /**
         * Called when an action is being performed.
         *
         * @param v The view that was clicked.
         * @param actionId Identifier of the action.  This will be either the
         * identifier you supplied, or {@link android.view.inputmethod.EditorInfo#IME_NULL
         * EditorInfo.IME_NULL} if being called due to the enter key
         * being pressed.
         * @param event If triggered by an enter key, this is the event;
         * otherwise, this is null.
         * @return Return true if you have consumed the action, else false.
         */
        @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_GO && mSubreddit != null) {
                ((AddSubredditDialogListener) getActivity()).onAddSubreddit(mSubreddit.getText().toString());
                dismiss();
            }
            return false;
        }
    };
    protected ArrayAdapter<String> mSubredditSearchResultsAdapter;
    private AdapterView.OnItemClickListener mSubredditSearchResponseListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mSubredditSearchResultsAdapter != null) {
                ((AddSubredditDialogListener) getActivity()).onAddSubreddit(mSubredditSearchResultsAdapter.getItem(position));
                dismiss();
            }
        }
    };
    private DialogInterface.OnClickListener mAddSubredditListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            ((AddSubredditDialogListener) getActivity()).onAddSubreddit(mSubreddit.getText().toString());
        }
    };
    private DialogInterface.OnClickListener mDialogCancelListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.cancel();
        }
    };
    private BroadcastReceiver mSubredditsSearch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.EXTRA_SUBREDDIT_NAMES)) {
                Ln.d("Got Back Subreddit Search Result");
                final ArrayList<String> subredditNames = intent.getStringArrayListExtra(Constants.EXTRA_SUBREDDIT_NAMES);

                if (subredditNames != null) {
                    handleSubredditSearchResults(subredditNames);
                }
            }
        }
    };

    public static AddSubredditDialogFragment newInstance() {
        return new AddSubredditDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater lf = LayoutInflater.from(getActivity());
        View dialogView = lf.inflate(R.layout.add_subreddit_dialog, null);
        ButterKnife.inject(this, dialogView);

        mSubreddit.addTextChangedListener(mSubredditWatcher);
        mSubreddit.setOnEditorActionListener(mSubredditSearchEditorActionListener);
        mSubreddit.setOnItemClickListener(mSubredditSearchResponseListener);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                                                                         .setTitle(R.string.add_subreddit)
                                                                         .setPositiveButton(R.string.add_subreddit, mAddSubredditListener)
                                                                         .setNegativeButton(R.string.cancel, mDialogCancelListener)
                                                                         .create();

        LocalBroadcastManager.getInstance(getActivity())
                             .registerReceiver(mSubredditsSearch, new IntentFilter(Constants.BROADCAST_SUBREDDIT_SEARCH));
        return dialog;
    }

    @Override public void onDismiss(DialogInterface dialog) {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSubredditsSearch);
        super.onDismiss(dialog);
    }

    protected void searchForSubreddits(String queryText) {
        if (Strings.notEmpty(queryText)) {
            RedditService.searchSubreddits(getActivity(), queryText, SharedPreferencesHelper.getShowNsfwImages(getActivity()));
        }
    }

    private void handleSubredditSearchResults(ArrayList<String> subredditNameList) {
        if (mSubreddit != null && subredditNameList != null && subredditNameList.size() > 0) {
            mSubredditSearchResultsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line,
                                                                      subredditNameList);
            mSubreddit.setAdapter(mSubredditSearchResultsAdapter);
            mSubreddit.showDropDown();
        }
    }

    public interface AddSubredditDialogListener {
        void onAddSubreddit(String subreddit);
    }
}
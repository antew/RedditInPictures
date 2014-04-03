package com.antew.redditinpictures.library.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;

public class AboutSubredditDialogFragment extends DialogFragment {
    SubredditData mSubredditData;
    @InjectView(R.id.iv_header)            ImageView header;
    @InjectView(R.id.tv_name)              TextView  name;
    @InjectView(R.id.tv_short_description) TextView  shortDescription;
    @InjectView(R.id.tv_description)       TextView  description;

    public static AboutSubredditDialogFragment newInstance(SubredditData subredditData) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Extra.EXTRA_SUBREDDIT_DATA, subredditData);

        AboutSubredditDialogFragment aboutDialogFragment = new AboutSubredditDialogFragment();
        aboutDialogFragment.setArguments(bundle);

        return aboutDialogFragment;
    }

    ;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            if (getArguments().containsKey(Constants.Extra.EXTRA_SUBREDDIT_DATA)) {
                mSubredditData = getArguments().getParcelable(Constants.Extra.EXTRA_SUBREDDIT_DATA);
            }
        }

        LayoutInflater lf = LayoutInflater.from(getActivity());
        View dialogView = lf.inflate(R.layout.about_subreddit_dialog, null);

        ButterKnife.inject(this, dialogView);

        if (mSubredditData != null) {
            int size = Util.dpToPx(getActivity(), 100);
            Picasso.with(getActivity())
                   .load(mSubredditData.getHeader_img())
                   .placeholder(R.drawable.loading_spinner_76)
                   .resize(size, size)
                   .error(R.drawable.empty_photo)
                   .into(header);
            name.setText(mSubredditData.getDisplay_name());
            shortDescription.setText(mSubredditData.getPublic_description());
            description.setText(mSubredditData.getDescription());
        }

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(dialogView)
                                                                         .setTitle(R.string.about_subreddit)
                                                                         .setNegativeButton(R.string.close,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                public void onClick(DialogInterface dialog,
                                                                                                                    int whichButton) {
                                                                                                    dialog.cancel();
                                                                                                }
                                                                                            }
                                                                                           )
                                                                         .create();
        return dialog;
    }
}
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
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.model.reddit.SubredditData;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.MarkdownUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;

public class AboutSubredditDialogFragment extends DialogFragment {
    SubredditData mSubredditData;
    @InjectView(R.id.iv_header)
    ImageView header;
    @InjectView(R.id.tv_name)
    TextView  name;
    @InjectView(R.id.tv_info)
    TextView  info;
    @InjectView(R.id.tv_short_description)
    TextView  shortDescription;
    @InjectView(R.id.tv_description)
    TextView  description;

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
            String separator = " " + "\u2022" + " ";
            String infoText = "";

            //If we have a NSFW subreddit.
            if (mSubredditData.isOver18()) {
                infoText = "<font color='#AC3939'>" + getActivity().getString(R.string.nsfw) + "</font>" + separator;
            }

            infoText += mSubredditData.getSubscribers() + getActivity().getString(R.string._subscribers);

            if (Strings.notEmpty(mSubredditData.getHeader_img())) {
                try {
                    Picasso.with(getActivity())
                           .load(Uri.parse(mSubredditData.getHeader_img()))
                           .placeholder(R.drawable.empty_photo)
                           .error(R.drawable.error_photo)
                           .into(header);
                } catch (Exception e) {
                    Ln.e(e, "Failed to load image");
                    Picasso.with(getActivity()).load(R.drawable.error_photo).into(header);
                }
            } else {
                Picasso.with(getActivity()).load(R.drawable.error_photo).into(header);
            }
            name.setText(mSubredditData.getDisplay_name());
            info.setText(Html.fromHtml(infoText));
            MarkdownUtil.setMarkdownText(shortDescription, mSubredditData.getPublic_description());
            MarkdownUtil.setMarkdownText(description, mSubredditData.getDescription());
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
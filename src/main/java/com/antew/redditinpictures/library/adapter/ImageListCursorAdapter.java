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
package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.enums.Vote;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;
import java.util.regex.Pattern;

/**
 * This is used as the backing adapter for the {@link android.widget.GridView} in {@link
 * com.antew.redditinpictures.library.ui.ImageGridFragment}
 *
 * @author Antew
 */
public class ImageListCursorAdapter extends CursorAdapter {
    public static final String TAG = ImageListCursorAdapter.class.getSimpleName();
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private Cursor mCursor;
    private LayoutInflater mInflater;
    private Pattern mImgurNonAlbumPattern = Pattern.compile("^https?://imgur.com/[^/]*$");

    /**
     * @param context The context
     * @param cursor Cursor to a database containing PostData information
     */
    public ImageListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
        mCursor = cursor;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);
        return mCursor;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder;
        if (view != null && view.getTag() != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final PostData postData = PostData.fromListViewProjection(cursor);

        // If we have a thumbnail from Reddit use that, otherwise use the full URL
        // Reddit will send 'default' for one of the default alien icons, which we want to avoid using
        String url = postData.getUrl();
        String thumbnail = postData.getThumbnail();
        if (Strings.notEmpty(thumbnail) && !thumbnail.equals("default")) {
            url = thumbnail;
        } else {
            // If the url is not pointing directly to the image. (Normally at i.imgur.com not imgur.com
            if (postData.getDomain() != null && postData.getDomain().equals("imgur.com")) {
                // If the url is not an album but is just using a shortlink to an image append .jpg to the end and hope for the best.
                if (mImgurNonAlbumPattern.matcher(url).matches()) {
                    url += ".jpg";
                    Ln.d("Updating Url To: %s", url);
                }
            }
        }

        Picasso.with(mContext).load(url).placeholder(R.drawable.loading_spinner_48).error(
            R.drawable.empty_photo).into(holder.imageView);

        String separator = " " + "\u2022" + " ";
        String titleText =
            postData.getTitle() + " <font color='#BEBEBE'>(" + postData.getDomain() + ")</font>";
        holder.postTitle.setText(Html.fromHtml(titleText));
        holder.postInformation.setText(postData.getSubreddit()
            + separator
            + postData.getNum_comments()
            + " "
            + mContext.getString(R.string.comments));
        holder.postVotes.setText("" + postData.getScore());

        if (postData.getVote() == Vote.UP) {
            holder.upVote.setImageResource(R.drawable.arrow_up_highlighted);
        } else if (postData.getVote() == Vote.DOWN) {
            holder.downVote.setImageResource(R.drawable.arrow_down_highlighted);
        }

        holder.upVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote(Vote.UP, postData, holder);
            }
        });

        holder.downVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote(Vote.DOWN, postData, holder);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.image_list_item, parent, false);
    }

    /**
     * Handles updating the vote based on the action bar vote icon that was clicked, broadcasts a
     * message to have the fragment update the score.
     * <p>
     * If the user is not logged in, we return immediately.
     * </p>
     * <p>
     * If the current vote is UP and the new vote is UP, the vote is changed to NEUTRAL.<br>
     * If the current vote is UP and the new vote is DOWN, the vote is changed to DOWN.
     * </p>
     * <p>
     * If the current vote is DOWN and the new vote is DOWN, the vote is changed to NEUTRAL<br>
     * If the current vote is DOWN and the new vote is UP, the vote is changed to UP.
     * </p>
     *
     * @param whichVoteButton The vote representing the menu item which was clicked
     * @param p The post this vote is for
     */
    private void vote(Vote whichVoteButton, PostData p, ViewHolder holder) {
        if (!RedditLoginInformation.isLoggedIn()) {
            if (mContext instanceof ImageGridActivity) {
                ((ImageGridActivity) mContext).handleLoginAndLogout();
            }
            return;
        }

        Intent intent = new Intent(Consts.BROADCAST_UPDATE_SCORE);
        intent.putExtra(Consts.EXTRA_PERMALINK, p.getPermalink());

        Ln.d("Vote is: %s", p.getVote());
        Ln.d("Vote Button is: %s", whichVoteButton);

        // If the user hasn't voted on this post yet, put it through no matter what.
        if (p.getVote() == null || p.getVote() == Vote.NEUTRAL) {
            switch (whichVoteButton) {
                case UP:
                    Ln.d("Voting Up Post");
                    RedditService.vote(mContext, p.getName(), Vote.UP);
                    p.setVote(Vote.UP);
                    p.setScore(p.getScore() + 1);
                    holder.postVotes.setText("" + p.getScore());
                    holder.upVote.setImageResource(R.drawable.arrow_up_highlighted);
                    break;
                case DOWN:
                    Ln.d("Voting Down Post");
                    RedditService.vote(mContext, p.getName(), Vote.DOWN);
                    p.setVote(Vote.DOWN);
                    p.setScore(p.getScore() - 1);
                    holder.postVotes.setText("" + p.getScore());
                    holder.downVote.setImageResource(R.drawable.arrow_down_highlighted);
                    break;
            }
        } else if (p.getVote() == Vote.UP) {
            switch (whichVoteButton) {
                case UP:
                    Ln.d("Voting Neutral Post");
                    RedditService.vote(mContext, p.getName(), Vote.NEUTRAL);
                    p.setVote(Vote.NEUTRAL);
                    p.setScore(p.getScore() - 1);
                    holder.postVotes.setText("" + p.getScore());
                    holder.upVote.setImageResource(R.drawable.arrow_up);
                    break;
                case DOWN:
                    Ln.d("Voting Down Post");
                    RedditService.vote(mContext, p.getName(), Vote.DOWN);
                    p.setVote(Vote.DOWN);
                    p.setScore(p.getScore() - 2);
                    holder.postVotes.setText("" + p.getScore());
                    holder.upVote.setImageResource(R.drawable.arrow_up);
                    holder.downVote.setImageResource(R.drawable.arrow_down_highlighted);
                    break;
            }
        } else if (p.getVote() == Vote.DOWN) {
            switch (whichVoteButton) {
                case UP:
                    Ln.d("Voting Up Post");
                    RedditService.vote(mContext, p.getName(), Vote.UP);
                    p.setVote(Vote.UP);
                    p.setScore(p.getScore() + 2);
                    holder.postVotes.setText("" + p.getScore());
                    holder.downVote.setImageResource(R.drawable.arrow_down);
                    holder.upVote.setImageResource(R.drawable.arrow_up_highlighted);
                    break;
                case DOWN:
                    Ln.d("Voting Neutral Post");
                    RedditService.vote(mContext, p.getName(), Vote.NEUTRAL);
                    p.setVote(Vote.NEUTRAL);
                    p.setScore(p.getScore() + 1);
                    holder.postVotes.setText("" + p.getScore());
                    holder.downVote.setImageResource(R.drawable.arrow_down);
                    break;
            }
        }

        // Broadcast the intent to update the score in the ImageDetailFragment
        intent.putExtra(Consts.EXTRA_SCORE, p.getScore());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    static class ViewHolder {
        @InjectView(R.id.iv_image) ImageView imageView;
        @InjectView(R.id.tv_title) TextView postTitle;
        @InjectView(R.id.tv_post_information) TextView postInformation;
        @InjectView(R.id.tv_votes) TextView postVotes;
        @InjectView(R.id.ib_upVote) ImageButton upVote;
        @InjectView(R.id.ib_downVote) ImageButton downVote;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

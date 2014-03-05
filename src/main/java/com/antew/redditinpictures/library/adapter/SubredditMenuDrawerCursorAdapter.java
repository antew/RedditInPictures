package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.listener.OnSubredditActionListener;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.utils.ViewUtils;
import com.antew.redditinpictures.pro.R;

/**
 * This is the Adapter for the drop down in the Action Bar of {@link
 * com.antew.redditinpictures.library.ui.ImageGridActivity}. It displays
 * the current Subreddit along with the selected Category and Age
 *
 * @author Antew
 */
public class SubredditMenuDrawerCursorAdapter extends CursorAdapter {

    private static final String TAG = SubredditMenuDrawerCursorAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private int mActivePosition = -1;
    private OnSubredditActionListener mSubredditActionListener;

    /**
     * Create a new Adapter for the Subreddit/Category/Age combo
     *
     * @param context
     *            The context
     * @param subredditActionListener
     *            Listener for changes to the subreddit (subscribed, unsubscribed, etc)
     */
    public SubredditMenuDrawerCursorAdapter(Context context, OnSubredditActionListener subredditActionListener) {
        super(context, null, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mSubredditActionListener = subredditActionListener;
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

            // If we recycled this row, make sure the menu is not shown.
            holder.back.setVisibility(View.GONE);
        } else {
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        final SubredditData subredditData = SubredditData.fromProjection(cursor);
        holder.subreddit.setText(subredditData.getDisplay_name());

        // TODO: Make this less of a worse hack...
        // If we don't have a name, then we are assuming this a system generated record. So hide all options for it.
        if (subredditData.getName() == null) {
            holder.more.setVisibility(View.GONE);
            holder.back.setVisibility(View.GONE);
        } else {
            holder.more.setVisibility(View.VISIBLE);
            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ViewUtils.toggleVisibility(holder.back);
                }
            });

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSubredditActionListener != null) {
                        mSubredditActionListener.onAction(subredditData,
                            OnSubredditActionListener.SubredditAction.View);
                    }
                }
            });

            if (subredditData.getUserIsSubscriber()) {
                showUnsubscribe(holder, subredditData);
            } else {
                showSubscribe(holder, subredditData);
            }

            holder.info.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSubredditActionListener != null) {
                        mSubredditActionListener.onAction(subredditData,
                            OnSubredditActionListener.SubredditAction.Info);
                    }
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSubredditActionListener != null) {
                        mSubredditActionListener.onAction(subredditData,
                            OnSubredditActionListener.SubredditAction.Delete);
                    }
                }
            });
        }
    }

    private void showSubscribe(final ViewHolder holder, final SubredditData subredditData) {
        holder.unsubscribe.setVisibility(View.GONE);
        holder.subscribe.setVisibility(View.VISIBLE);
        holder.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mSubredditActionListener != null) {
                    mSubredditActionListener.onAction(subredditData,
                        OnSubredditActionListener.SubredditAction.Subscribe);
                }
                // If the user is logged in, make the assumption that the request went through. If the request was bad, it will just show up on the next MySubreddits reload.
                if (RedditLoginInformation.isLoggedIn()) {
                    showUnsubscribe(holder, subredditData);
                }
            }
        });
    }

    private void showUnsubscribe(final ViewHolder holder, final SubredditData subredditData) {
        holder.subscribe.setVisibility(View.GONE);
        holder.unsubscribe.setVisibility(View.VISIBLE);
        holder.unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mSubredditActionListener != null) {
                    mSubredditActionListener.onAction(subredditData,
                        OnSubredditActionListener.SubredditAction.Unsubscribe);
                }

                // If the user is logged in, make the assumption that the request went through. If the request was bad, it will just show up on the next MySubreddits reload.
                if (RedditLoginInformation.isLoggedIn()) {
                    showSubscribe(holder, subredditData);
                }
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.subreddit_menudrawer_item, parent, false);
    }

    public void setActivePosition(int activePosition) {
        this.mActivePosition = activePosition;
    }

    static class ViewHolder {
        @InjectView(R.id.tv_subreddit) TextView subreddit;
        @InjectView(R.id.ib_more) ImageButton more;
        @InjectView(R.id.back) LinearLayout back;
        @InjectView(R.id.b_view) Button view;
        @InjectView(R.id.b_subscribe) Button subscribe;
        @InjectView(R.id.b_unsubscribe) Button unsubscribe;
        @InjectView(R.id.b_info) Button info;
        @InjectView(R.id.b_delete) Button delete;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
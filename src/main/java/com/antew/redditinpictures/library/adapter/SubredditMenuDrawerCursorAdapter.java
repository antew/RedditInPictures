package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.antew.redditinpictures.library.listener.OnSubredditActionListener;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.utils.Strings;
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
    private LayoutInflater inflater;
    private int mActivePosition = -1;
    private OnSubredditActionListener mSubredditActionListener;

    /**
     * Create a new Adapter for the Subreddit/Category/Age combo
     *
     * @param context
     *     The context
     * @param subredditActionListener
     *     Listener for changes to the subreddit (subscribed, unsubscribed, etc)
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
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.subreddit_menudrawer_item, parent, false);
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

        // Always make sure the menu is not shown initially.
        holder.back.setVisibility(View.GONE);

        final SubredditData subredditData = SubredditData.fromProjection(cursor);
        holder.subreddit.setText(subredditData.getDisplay_name());

        // TODO: Make this less of a worse hack...
        // If we don't have a name, then we are assuming this a system generated record. So hide all options for it.
        if (subredditData.getName() == null) {
            holder.more.setVisibility(View.GONE);
            holder.back.setVisibility(View.GONE);
        } else {
            // Even though the view is visible by default since this could be recycled, we need to make sure to make it visible if it wasn't before.
            holder.more.setVisibility(View.VISIBLE);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSubredditActionListener != null) {
                        mSubredditActionListener.onAction(subredditData, OnSubredditActionListener.SubredditAction.View);
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
                        mSubredditActionListener.onAction(subredditData, OnSubredditActionListener.SubredditAction.Info);
                    }
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSubredditActionListener != null) {
                        mSubredditActionListener.onAction(subredditData, OnSubredditActionListener.SubredditAction.Delete);
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
                    mSubredditActionListener.onAction(subredditData, OnSubredditActionListener.SubredditAction.Subscribe);
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
                    mSubredditActionListener.onAction(subredditData, OnSubredditActionListener.SubredditAction.Unsubscribe);
                }

                // If the user is logged in, make the assumption that the request went through. If the request was bad, it will just show up on the next MySubreddits reload.
                if (RedditLoginInformation.isLoggedIn()) {
                    showSubscribe(holder, subredditData);
                }
            }
        });
    }

    public void setActivePosition(int activePosition) {
        this.mActivePosition = activePosition;
    }

    protected class ViewHolder {
        @InjectView(R.id.tv_subreddit)   TextView     subreddit;
        @InjectView(R.id.ib_more)        ImageButton  more;
        @InjectView(R.id.back)           LinearLayout back;
        @InjectView(R.id.ib_view)        ImageButton  view;
        @InjectView(R.id.ib_subscribe)   ImageButton  subscribe;
        @InjectView(R.id.ib_unsubscribe) ImageButton  unsubscribe;
        @InjectView(R.id.ib_info)        ImageButton  info;
        @InjectView(R.id.ib_delete)      ImageButton  delete;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }

        @OnClick(R.id.ib_more)
        protected void onMore() {
            ViewUtils.toggleVisibility(back);
        }

        @OnLongClick({ R.id.ib_more, R.id.ib_view, R.id.ib_subscribe, R.id.ib_unsubscribe, R.id.ib_info, R.id.ib_delete })
        protected boolean onLongClickMenuOption(View view) {
            if (view != null) {
                String description = Strings.toString(view.getContentDescription());
                if (Strings.notEmpty(description)) {
                    Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            return false;
        }
    }
}
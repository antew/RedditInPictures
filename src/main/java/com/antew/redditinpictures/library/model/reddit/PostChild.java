package com.antew.redditinpictures.library.model.reddit;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.commonsware.cwac.anddown.AndDown;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PostChild implements Child<PostData> {
    private PostData data;
    public int depth;
    private String   kind;

    private int[] colors = new int[] {
        R.color.comment_depth_1,
        R.color.comment_depth_2,
        R.color.comment_depth_3,
        R.color.comment_depth_4,
        R.color.comment_depth_5,
        R.color.comment_depth_6,
        R.color.comment_depth_7,
    };

    @Inject
    AndDown andDown;

    public PostChild() {
        Injector.inject(this);
    }

    public PostChild(Parcel source) {
        this();
        kind = source.readString();
        data = source.readParcelable(PostData.class.getClassLoader());
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View v = convertView;
        ViewHolder holder = null;
        if (v == null) {
            v = inflater.inflate(R.layout.reddit_comment, null);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.username.setText(data.getAuthor());
        holder.votes.setText(Integer.toString(data.getScore()) + " points");
        holder.date.setText(
                DateUtils.getRelativeTimeSpanString(
                        (long) data.getCreated_utc() * 1000, // startTime
                        System.currentTimeMillis(),          // endTime
                        0L,                                  // minResolution
                        DateUtils.FORMAT_ABBREV_RELATIVE     // format
                )
        );
        if (Strings.notEmpty(data.getBody())) {
            holder.comment.setText(Strings.trimTrailingWhitespace(Html.fromHtml(andDown.markdownToHtml(data.getBody()))));
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.commentWrapper.getLayoutParams();
        params.setMargins(50 * depth, params.topMargin, params.rightMargin, params.bottomMargin);

        // Due to view recycling the indicator needs to be hidden if we aren't at depth zero
        if (depth > 0) {
            holder.commentDepthIndicator.setVisibility(View.VISIBLE);
            holder.commentDepthIndicator.setBackgroundResource(colors[depth % colors.length]);
        } else {
            holder.commentDepthIndicator.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public Type getType() {
        return Type.POST;
    }

    public void setData(PostData data) {
        this.data = data;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getKind() {
        return "t1";
    }

    public PostData getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kind);
        dest.writeParcelable(data, flags);
    }

    public static final Parcelable.Creator<PostChild> CREATOR = new Parcelable.Creator<PostChild>() {

        @Override
        public PostChild createFromParcel(Parcel source) {
            return new PostChild(source);
        }

        @Override
        public PostChild[] newArray(int size) {
            return new PostChild[size];
        }
    };

    static class ViewHolder {
        @InjectView(R.id.tv_username)
        TextView username;
        @InjectView(R.id.tv_date)            TextView  date;
        @InjectView(R.id.tv_comment_votes)   TextView  votes;
        @InjectView(R.id.tv_comment)         TextView  comment;
        @InjectView(R.id.comment_depth_indicator) View commentDepthIndicator;
        @InjectView(R.id.rl_comment_wrapper)
        RelativeLayout commentWrapper;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

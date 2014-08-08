package com.antew.redditinpictures.library.model.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.CharacterPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antew.redditinpictures.library.util.ColoredString;
import com.antew.redditinpictures.pro.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MoreChild implements Child<MoreData> {
    private static final String COMMENT = " comment";
    private static final String COMMENTS = " comments";

    MoreData data;
    public int depth;
    private int[] colors = new int[] {
            R.color.comment_depth_1,
            R.color.comment_depth_2,
            R.color.comment_depth_3,
            R.color.comment_depth_4,
            R.color.comment_depth_5,
            R.color.comment_depth_6,
            R.color.comment_depth_7,
    };

    public static final Parcelable.Creator<MoreChild> CREATOR = new Parcelable.Creator<MoreChild>() {

        @Override
        public MoreChild createFromParcel(Parcel source) {
            return new MoreChild(source);
        }

        @Override
        public MoreChild[] newArray(int size) {
            return new MoreChild[size];
        }
    };

    public MoreChild(Parcel source) {
        data = source.readParcelable(MoreData.class.getClassLoader());
        depth = source.readInt();
    }

    public String getKind() {
        return "more";
    }

    public MoreData getData() {
        return data;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View v = convertView;
        ViewHolder holder = null;
        if (v == null) {
            v = inflater.inflate(R.layout.more_comments_list_item, null);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.moreComments.setText(data.count + (data.count == 1 ? COMMENT : COMMENTS) + " hidden");
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.moreComments.getLayoutParams();
        params.setMargins(50 * depth, params.topMargin, params.rightMargin, params.bottomMargin);

        return v;
    }

    @Override
    public Type getType() {
        return Type.MORE;
    }

    @Override
    public String getParent() {
        return data == null ? null : data.getParentId();
    }

    @Override
    public String getName() {
        return data == null ? null : data.getName();
    }

    public void setData(MoreData data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, flags);
        dest.writeInt(depth);

    }

    static class ViewHolder {
        @InjectView(R.id.tv_more_comments)
        TextView moreComments;

        @InjectView(R.id.more_comment_depth_indicator)
        View commentDepthIndicator;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

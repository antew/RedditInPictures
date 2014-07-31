package com.antew.redditinpictures.library.model.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antew.redditinpictures.pro.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MoreChild implements Child<MoreData> {
    MoreData data;
    public int depth;

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

        holder.moreComments.setText(data.count + " comments hidden");
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.moreComments.getLayoutParams();
        params.setMargins(50 * depth, params.topMargin, params.rightMargin, params.bottomMargin);
        return v;
    }

    @Override
    public Type getType() {
        return Type.MORE;
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

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

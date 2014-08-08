package com.antew.redditinpictures.library.model.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MoreData implements Parcelable {
    int count;

    @SerializedName("parent_id")
    String parentId;

    String id;
    String name;
    List<String> children;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoreData moreData = (MoreData) o;

        if (count != moreData.count) return false;
        if (children != null ? !children.equals(moreData.children) : moreData.children != null)
            return false;
        if (id != null ? !id.equals(moreData.id) : moreData.id != null) return false;
        if (name != null ? !name.equals(moreData.name) : moreData.name != null) return false;
        if (parentId != null ? !parentId.equals(moreData.parentId) : moreData.parentId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = count;
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MoreData{" +
                "count=" + count +
                ", parentId='" + parentId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", children=" + children +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.count);
        dest.writeString(this.parentId);
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeList(this.children);
    }

    public MoreData() {
    }

    private MoreData(Parcel in) {
        this.count = in.readInt();
        this.parentId = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.children = new ArrayList<String>();
        in.readList(this.children, String.class.getClassLoader());
    }

    public static final Parcelable.Creator<MoreData> CREATOR = new Parcelable.Creator<MoreData>() {
        public MoreData createFromParcel(Parcel source) {
            return new MoreData(source);
        }

        public MoreData[] newArray(int size) {
            return new MoreData[size];
        }
    };
}

package com.antew.redditinpictures.library.model.gfycat;

import android.os.Parcel;
import android.os.Parcelable;

public class GfycatImage implements Parcelable {
    public static final Parcelable.Creator<GfycatImage> CREATOR = new Parcelable.Creator<GfycatImage>() {

        @Override
        public GfycatImage createFromParcel(Parcel source) {
            return new GfycatImage(source);
        }

        public GfycatImage[] newArray(int size) {
            return new GfycatImage[size];
        }

        ;
    };
    private String gfyname;
    private String gfyName;
    private long   gfysize;
    private long   gifSize;
    private int    gifWidth;
    private String mp4Url;
    private String webmUrl;
    private int    frameRate;
    private String gifUrl;
    private String error;

    public GfycatImage(Parcel in) {
        gfyname = in.readString();
        gfyName = in.readString();
        gfysize = in.readLong();
        gifSize = in.readLong();
        gifWidth = in.readInt();
        mp4Url = in.readString();
        webmUrl = in.readString();
        frameRate = in.readInt();
        gifUrl = in.readString();
        error = in.readString();
    }

    public String getGfyname() {
        return gfyname;
    }

    public String getGfyName() {
        return gfyName;
    }

    public long getGfysize() {
        return gfysize;
    }

    public long getGifSize() {
        return gifSize;
    }

    public int getGifWidth() {
        return gifWidth;
    }

    public String getMp4Url() {
        return mp4Url;
    }

    public String getWebmUrl() {
        return webmUrl;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public String getError() {
        return error;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest
     *     The Parcel in which the object should be written.
     * @param flags
     *     Additional flags about how the object should be written.
     *     May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gfyname);
        dest.writeString(gfyName);
        dest.writeLong(gfysize);
        dest.writeLong(gifSize);
        dest.writeInt(gifWidth);
        dest.writeString(mp4Url);
        dest.writeString(webmUrl);
        dest.writeInt(frameRate);
        dest.writeString(gifUrl);
        dest.writeString(error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GfycatImage that = (GfycatImage) o;

        if (frameRate != that.frameRate) {
            return false;
        }
        if (gfysize != that.gfysize) {
            return false;
        }
        if (gifSize != that.gifSize) {
            return false;
        }
        if (gifWidth != that.gifWidth) {
            return false;
        }
        if (gfyName != null ? !gfyName.equals(that.gfyName) : that.gfyName != null) {
            return false;
        }
        if (gfyname != null ? !gfyname.equals(that.gfyname) : that.gfyname != null) {
            return false;
        }
        if (gifUrl != null ? !gifUrl.equals(that.gifUrl) : that.gifUrl != null) {
            return false;
        }
        if (mp4Url != null ? !mp4Url.equals(that.mp4Url) : that.mp4Url != null) {
            return false;
        }
        if (webmUrl != null ? !webmUrl.equals(that.webmUrl) : that.webmUrl != null) {
            return false;
        }
        if (error != null ? !error.equals(that.error) : that.error != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = gfyname != null ? gfyname.hashCode() : 0;
        result = 31 * result + (gfyName != null ? gfyName.hashCode() : 0);
        result = 31 * result + (int) (gfysize ^ (gfysize >>> 32));
        result = 31 * result + (int) (gifSize ^ (gifSize >>> 32));
        result = 31 * result + gifWidth;
        result = 31 * result + (mp4Url != null ? mp4Url.hashCode() : 0);
        result = 31 * result + (webmUrl != null ? webmUrl.hashCode() : 0);
        result = 31 * result + frameRate;
        result = 31 * result + (gifUrl != null ? gifUrl.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GfycatImage{" +
               "gfyname='" + gfyname + '\'' +
               ", gfyName='" + gfyName + '\'' +
               ", gfysize=" + gfysize +
               ", gifSize=" + gifSize +
               ", gifWidth=" + gifWidth +
               ", mp4Url='" + mp4Url + '\'' +
               ", webmUrl='" + webmUrl + '\'' +
               ", frameRate=" + frameRate +
               ", gifUrl='" + gifUrl + '\'' +
               ", error='" + error + '\'' +
               '}';
    }
}

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
package com.antew.redditinpictures.library.model.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import com.antew.redditinpictures.library.util.AndroidUtil;

public class MediaEmbed implements Parcelable {
    public static final Parcelable.Creator<MediaEmbed> CREATOR = new Parcelable.Creator<MediaEmbed>() {
        public MediaEmbed createFromParcel(Parcel in) {
            return new MediaEmbed(in);
        }

        public MediaEmbed[] newArray(int size) {
            return new MediaEmbed[size];
        }
    };
    private String  content;
    private int     width;
    private int     height;
    private boolean scrolling;

    public MediaEmbed(Parcel in) {
        content = in.readString();
        width = in.readInt();
        height = in.readInt();
        scrolling = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeByte(AndroidUtil.parcelBoolean(scrolling));
    }
}
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
package com.antew.redditinpictures.library.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.utils.Util;

public class RedditUrl implements Parcelable {
    private static final String REDDIT_URL       = "http://www.reddit.com";
    private static final String BASE_URL         = REDDIT_URL + "/r/";
    private static final String URL_SEPARATOR    = "/";
    private static final String QS_SEPARATOR     = "?";
    private static final String SORT             = "sort=%s";
    private static final String COUNT            = "limit=%d";
    private static final String TIME             = "t=%s";
    private static final String AFTER            = "after=%s";
    private static final String BEFORE           = "before=%s";
    private static final String JSON             = ".json";
    private static final String PARAM_SEPARATOR  = "&";
    public static final String  REDDIT_FRONTPAGE = "REDDIT_FRONTPAGE";

    public final String         subreddit;
    public final Age            age;
    public final Category       category;
    public final int            count;
    public final String         after;
    public final String         before;
    public final boolean        isLoggedIn;

    public String getUrl() {

        StringBuffer url = new StringBuffer();
        if (subreddit.equals(REDDIT_FRONTPAGE)) {
            url.append(REDDIT_URL);
            url.append(URL_SEPARATOR);
        } else {
            url.append(BASE_URL);
            url.append(subreddit);
            url.append(URL_SEPARATOR);
        }

        // The "Rising" category uses www.reddit.com/new/?sort=rising
        if (category.equals(Category.RISING))
            url.append(Category.NEW.getName());
        else
            url.append(category.getName());

        url.append(URL_SEPARATOR);

        url.append(JSON);

        url.append(QS_SEPARATOR);

        url.append(String.format(SORT, category.getName()));
        url.append(PARAM_SEPARATOR);

        url.append(String.format(COUNT, count));
        url.append(PARAM_SEPARATOR);

        url.append(String.format(TIME, age.getAge()));

        if (after != null && after.length() > 0) {
            url.append(PARAM_SEPARATOR);
            url.append(String.format(AFTER, after));
        }

        if (before != null && before.length() > 0) {
            url.append(PARAM_SEPARATOR);
            url.append(String.format(BEFORE, before));
        }

        Log.i("Returning URL", url.toString());

        return url.toString();
    }

    public enum Category {
        //@formatter:off
        HOT("hot", "Hot"), NEW("new", "New"), RISING("rising", "Rising"), CONTROVERSIAL("controversial", "Controversial"), TOP("top", "Top");

        private String name;
        private String simpleName;

        Category(String name, String simpleName) {
            this.name = name;
            this.simpleName = simpleName;
        }

        public String getName() {
            return this.name;
        
        }
        public String getSimpleName() {
            return this.simpleName;
        }

        public static Category fromString(String name) {

            if (name != null) {
                for (Category c : Category.values()) {
                    if (name.equalsIgnoreCase(c.name))
                        return c;
                }

            }

            return null;
        }

        //@formatter:on
    }

    public enum Age {
        //@formatter:off
        TODAY("today", "Today"),
        THIS_HOUR("hour", "This Hour"),
        THIS_WEEK("week", "This Week"),
        THIS_MONTH("month", "This Month"),
        THIS_YEAR("year", "This Year"),
        ALL_TIME("all", "All Time");

        private String age;
        private String simpleName;

        Age(String age, String simpleName) {
            this.age = age;
            this.simpleName = simpleName;
        }

        public String getAge() {
            return this.age;
        }
        
        public String getSimpleName() {
            return this.simpleName;
        }

        public static Age fromString(String age) {
            if (age != null) {
                for (Age a : Age.values()) {
                    if (age.equalsIgnoreCase(a.age))
                        return a;
                }
            }

            return null;
        }
        
        //@formatter:off
    }
    

    public static String getCategorySimpleName(Category category, Age age) {
        String simpleName = null;
        switch (category) {
                
            case HOT:
            case NEW:
            case RISING:
                simpleName = category.getSimpleName();
                break;
                
            case CONTROVERSIAL:
            case TOP:
                simpleName = (category.getSimpleName() + " - " + age.getSimpleName());
                break;
            
        }
        
        return simpleName;
    }

    private RedditUrl(Builder builder) {
        subreddit = builder.subreddit;
        age = builder.age;
        category = builder.category;
        count = builder.count;
        after = builder.after;
        before = builder.before;
        isLoggedIn = builder.isLoggedIn;
    }

    public RedditUrl(Parcel source) {
        subreddit = source.readString();
        age = Age.valueOf(source.readString());
        category = Category.valueOf(source.readString());
        count = source.readInt();
        after = source.readString();
        before = source.readString();
        isLoggedIn = source.readByte() == 1;
    }

    public static class Builder {
        private final String subreddit;

        private Category     category   = Category.HOT;
        private Age          age        = Age.TODAY;
        private int          count      = 25;
        private String       after      = "";
        private String       before     = "";
        private boolean      isLoggedIn = false;

        public Builder(String subreddit) {
            if (subreddit == null) {
                throw new NullPointerException("Subreddit must not be null");
            }
            this.subreddit = subreddit;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder isLoggedIn(boolean isLoggedIn) {
            this.isLoggedIn = isLoggedIn;
            return this;
        }

        public Builder age(Age age) {
            this.age = age;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder after(String after) {
            this.after = after;
            return this;
        }

        public Builder before(String before) {
            this.before = before;
            return this;
        }

        public RedditUrl build() {
            return new RedditUrl(this);
        }
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subreddit);
        dest.writeString(age.name());
        dest.writeString(category.name());
        dest.writeInt(count);
        dest.writeString(after);
        dest.writeString(before);
        dest.writeByte(Util.parcelBoolean(isLoggedIn));
    }
    
    //@formatter:off
    public static final Parcelable.Creator<RedditUrl> CREATOR
        = new Parcelable.Creator<RedditUrl>() {

            @Override
            public RedditUrl createFromParcel(Parcel source) {
                return new RedditUrl(source);
            }

            @Override
            public RedditUrl[] newArray(int size) {
                return new RedditUrl[size];
            }
            
        
    };
    //@formatter:on
}

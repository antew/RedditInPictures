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
package com.antew.redditinpictures.library.event;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public class LoadSubredditEvent {
    private String   mSubreddit;
    private Age      mAge;
    private Category mCategory;

    public LoadSubredditEvent(String mSubreddit, Category mCategory, Age mAge) {
        this.mSubreddit = mSubreddit;
        this.mCategory = mCategory;
        this.mAge = mAge;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public Category getCategory() {
        return mCategory;
    }

    public Age getAge() {
        return mAge;
    }
}

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
package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public interface RedditDataProvider {
    public Age getAge();

    public void setAge(Age age);

    public Category getCategory();

    public void setCategory(Category category);

    public String getSubreddit();

    public void setSubreddit(String subreddit);
}

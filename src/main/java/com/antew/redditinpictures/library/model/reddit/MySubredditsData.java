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

import java.util.ArrayList;
import java.util.List;

public class MySubredditsData {
    private String                  modhash;
    private List<SubredditChildren> children;
    private String                  after;
    private String                  before;

    public void addChildren(List<SubredditChildren> children) {
        if (this.children == null) {
            this.children = new ArrayList<SubredditChildren>();
        }

        this.children.addAll(children);
    }

    public String getModhash() {
        return modhash;
    }

    public List<SubredditChildren> getChildren() {
        return children;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }
}
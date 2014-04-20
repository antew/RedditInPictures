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

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.pro.R;
import java.util.HashMap;
import java.util.Map;

public class RedditSort {
    private static Map<Integer, SortCriteria> menuItemSorts = new HashMap<Integer, SortCriteria>();

    static {
        menuItemSorts.put(R.id.category_hot, new SortCriteria(Category.HOT));
        menuItemSorts.put(R.id.category_new, new SortCriteria(Category.NEW));
        menuItemSorts.put(R.id.category_rising, new SortCriteria(Category.RISING));
        menuItemSorts.put(R.id.category_top_hour, new SortCriteria(Category.TOP, Age.THIS_HOUR));
        menuItemSorts.put(R.id.category_top_today, new SortCriteria(Category.TOP, Age.TODAY));
        menuItemSorts.put(R.id.category_top_week, new SortCriteria(Category.TOP, Age.THIS_WEEK));
        menuItemSorts.put(R.id.category_top_month, new SortCriteria(Category.TOP, Age.THIS_MONTH));
        menuItemSorts.put(R.id.category_top_year, new SortCriteria(Category.TOP, Age.THIS_YEAR));
        menuItemSorts.put(R.id.category_top_all_time, new SortCriteria(Category.TOP, Age.ALL_TIME));
        menuItemSorts.put(R.id.category_controversial_hour, new SortCriteria(Category.CONTROVERSIAL, Age.THIS_HOUR));
        menuItemSorts.put(R.id.category_controversial_today, new SortCriteria(Category.CONTROVERSIAL, Age.TODAY));
        menuItemSorts.put(R.id.category_controversial_week, new SortCriteria(Category.CONTROVERSIAL, Age.THIS_WEEK));
        menuItemSorts.put(R.id.category_controversial_month, new SortCriteria(Category.CONTROVERSIAL, Age.THIS_MONTH));
        menuItemSorts.put(R.id.category_controversial_year, new SortCriteria(Category.CONTROVERSIAL, Age.THIS_YEAR));
        menuItemSorts.put(R.id.category_controversial_all_time, new SortCriteria(Category.CONTROVERSIAL, Age.ALL_TIME));
    }

    public static SortCriteria get(Integer menuItemId) {
        return menuItemSorts.get(menuItemId);
    }

    public static boolean contains(Integer menuItemId) {
        return menuItemSorts.containsKey(menuItemId);
    }

    public static class SortCriteria {

        private final Age      age;
        private final Category category;

        public SortCriteria(Category category) {
            this(category, null);
        }

        public SortCriteria(Category category, Age age) {
            this.age = age;
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public Age getAge() {
            return age;
        }
    }
}

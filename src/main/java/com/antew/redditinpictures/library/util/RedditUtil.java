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
package com.antew.redditinpictures.library.util;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public class RedditUtil {
    /**
     * Get the sort string to display in the ActionBar subtitle, e.g. 'Top: All Time"
     * @param category The current {@link com.antew.redditinpictures.library.model.Category}
     * @param age The current {@link com.antew.redditinpictures.library.model.Age}
     * @return A simple description of the category + age combination.
     */
    public static String getSortDisplayString(Category category, Age age) {
        // If we're sorting by 'Hot', 'New', or 'Rising' we don't need to display age, it is implied by the name.
        if (category.equals(Category.HOT) || category.equals(Category.NEW) || category.equals(Category.RISING)) {
            return category.getSimpleName();
        } else {
            return category.getSimpleName() + (age == null ? "" : ": " + age.getSimpleName());
        }
    }
}

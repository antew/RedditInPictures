package com.antew.redditinpictures.library.utils;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public class RedditUtils {
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

package com.antew.redditinpictures.library.enums;

public enum Age {
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

    public static Age fromString(String age) {
        if (age != null) {
            for (Age a : Age.values()) {
                if (age.equalsIgnoreCase(a.age)) {
                    return a;
                }
            }
        }

        return null;
    }

    public String getAge() {
        return this.age;
    }

    public String getSimpleName() {
        return this.simpleName;
    }

}
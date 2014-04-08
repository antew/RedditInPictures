package com.antew.redditinpictures.library.enums;

public enum Category {
    HOT("hot", "Hot"), NEW("new", "New"), RISING("rising", "Rising"), CONTROVERSIAL("controversial", "Controversial"), TOP("top", "Top");

    private String name;
    private String simpleName;

    Category(String name, String simpleName) {
        this.name = name;
        this.simpleName = simpleName;
    }

    public static Category fromString(String name) {

        if (name != null) {
            for (Category c : Category.values()) {
                if (name.equalsIgnoreCase(c.name)) {
                    return c;
                }
            }
        }

        return null;
    }

    public String getName() {
        return this.name;
    }

    public String getSimpleName() {
        return this.simpleName;
    }

}
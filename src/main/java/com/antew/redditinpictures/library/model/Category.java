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
package com.antew.redditinpictures.library.model;

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
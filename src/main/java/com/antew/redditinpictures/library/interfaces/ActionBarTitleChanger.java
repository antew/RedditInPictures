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

public interface ActionBarTitleChanger {
    /**
     * Set the {@link android.app.ActionBar} title
     *
     * @param title
     *     The title
     * @param subtitle
     *     The subtitle.  If the subtitle is null or empty it will not be set.
     */
    public void setActionBarTitle(String title, String subtitle);
}

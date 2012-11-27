/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
package com.antew.redditinpictures.library.reddit;

import java.util.List;

public class RedditLoginResponse {
    private LoginResponse json;

    public LoginResponse getLoginResponse() {
        return json;
    }

    public static class LoginResponse {
        private List<String[]> errors;
        private LoginData      data;

        public List<String[]> getErrors() {
            return errors;
        }

        public LoginData getData() {
            return data;
        }
    }

    public static class LoginData {
        private String modhash;
        private String cookie;

        public String getModhash() {
            return modhash;
        }

        public String getCookie() {
            return cookie;
        }
    }

}

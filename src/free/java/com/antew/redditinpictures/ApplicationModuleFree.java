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
package com.antew.redditinpictures;

import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.util.ImageDownloader;
import com.antew.redditinpictures.ui.ImageDetailActivityFree;
import com.antew.redditinpictures.ui.ImageDetailFragmentFree;
import com.antew.redditinpictures.ui.ImgurAlbumActivityFree;
import com.antew.redditinpictures.ui.ImgurAlbumFragmentFree;
import com.antew.redditinpictures.ui.RedditFragmentActivityFree;
import com.antew.redditinpictures.ui.RedditImageGridFragmentFree;
import com.antew.redditinpictures.ui.RedditImageListFragmentFree;
import dagger.Module;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module(
    complete = false,
    overrides = true,
    injects = {
        RedditImageGridFragmentFree.class, RedditImageListFragmentFree.class, RedditFragmentActivityFree.class,
        ImageDetailActivityFree.class, ImageDetailFragmentFree.class, ImgurAlbumActivityFree.class, ImgurAlbumFragmentFree.class,
        RedditService.GetNewPostsIfNeededTask.class, ImageDownloader.class, RedditService.NotifyRequestCompleted.class
    }, library = true)
public class ApplicationModuleFree {

}
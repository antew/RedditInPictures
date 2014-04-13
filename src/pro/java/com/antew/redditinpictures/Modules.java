package com.antew.redditinpictures;

import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.modules.RootModule;
import com.antew.redditinpictures.library.util.Ln;

public class Modules {

    public static Object get(RedditInPicturesApplication app) {
        Ln.e("Called free 'Modules' class");
        return new ApplicationModulePro();
    }
}

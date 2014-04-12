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

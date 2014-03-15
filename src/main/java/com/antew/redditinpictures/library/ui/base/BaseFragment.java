package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.antew.redditinpictures.library.RedditInPicturesApplication;

public class BaseFragment extends SherlockFragment {
    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseFragmentActivity) getSherlockActivity()).inject(this);
    }
}
package android.support.v4.app;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Temporary fix until a new version of the Support Library is released.
 *
 * @see <a href="https://code.google.com/p/android/issues/detail?id=37484">Issue #37484</a> and <a
 * href="https://github.com/antew/RedditInPictures/issues/162">Reddit In Pictures issue #162</a>
 */
public abstract class FixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    public FixedFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        Bundle savedFragmentState = f.mSavedFragmentState;
        if (savedFragmentState != null) {
            savedFragmentState.setClassLoader(f.getClass().getClassLoader());
        }
        return f;
    }
}

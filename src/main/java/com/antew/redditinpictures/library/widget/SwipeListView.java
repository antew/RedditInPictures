package com.antew.redditinpictures.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ListView;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.pro.R;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import java.util.ArrayList;
import java.util.List;

public class SwipeListView extends ListView {
    private State mState     = State.IDLE;
    private int   mViewWidth = 1;

    private int               mFrontViewId;
    private int               mBackViewId;
    private SwipeableViewPair mViewPair;

    private float           mDownX;
    private VelocityTracker mVelocityTracker;

    private int  mTouchSlop;
    private int  mMinFlingVelocity;
    private int  mMaxFlingVelocity;
    private long mAnimationTime;

    private OnScrollListener mOnScrollListener;

    private List<SwipeableViewPair> mSwipedViews = new ArrayList<SwipeableViewPair>();

    private AbsListView.OnScrollListener mInternalOnScrollListener = new AbsListView.OnScrollListener() {
        /**
         * Callback method to be invoked while the list view or grid view is being scrolled. If the
         * view is being scrolled, this method will be called before the next frame of the scroll is
         * rendered. In particular, it will be called before any calls to
         * {@link android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)}.
         *
         * @param view
         *     The view whose scroll state is being reported
         * @param scrollState
         *     The current scroll state. One of {@link #SCROLL_STATE_IDLE},
         *     {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                case SCROLL_STATE_FLING:
                    changeState(State.IDLE);
                    break;
                case SCROLL_STATE_TOUCH_SCROLL:
                    changeState(State.SCROLLING);
                    // Close any open views.
                    for (SwipeableViewPair viewPair : mSwipedViews) {
                        viewPair.mBackView.setVisibility(View.GONE);
                        ViewPropertyAnimator.animate(viewPair.mFrontView).translationX(0).alpha(1).setDuration(mAnimationTime);
                    }
                    mSwipedViews.clear();
                    break;
            }

            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        /**
         * Callback method to be invoked when the list or grid has been scrolled. This will be
         * called after the scroll has completed
         *
         * @param view
         *     The view whose scroll state is being reported
         * @param firstVisibleItem
         *     the index of the first visible cell (ignore if
         *     visibleItemCount == 0)
         * @param visibleItemCount
         *     the number of visible cells
         * @param totalItemCount
         *     the number of items in the list adaptor
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    private enum State {
        IDLE, SWIPING, SCROLLING;
    }

    /**
     * If you create a View programmatically you need send the front and back identifiers
     *
     * @param context
     *     Context
     * @param frontViewId
     *     Front View Identifier
     * @param backViewId
     *     Back View Identifier
     */
    public SwipeListView(Context context, int frontViewId, int backViewId) {
        super(context);
        this.mFrontViewId = frontViewId;
        this.mBackViewId = backViewId;
        initialize(null);
    }

    /**
     * @see android.widget.ListView#ListView(android.content.Context, android.util.AttributeSet)
     */
    public SwipeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    /**
     * @see android.widget.ListView#ListView(android.content.Context, android.util.AttributeSet, int)
     */
    public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {

        // If we are in an IDE Preview, don't initialize.
        if (isInEditMode()) {
            return;
        }

        if (attrs != null) {
            TypedArray styled = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeListView);
            mFrontViewId = styled.getResourceId(R.styleable.SwipeListView_swipeFrontView, 0);
            mBackViewId = styled.getResourceId(R.styleable.SwipeListView_swipeBackView, 0);
        }

        if (mFrontViewId == 0 || mBackViewId == 0) {
            throw new RuntimeException("You must specify a Front View and Back View");
        }

        ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfig.getScaledTouchSlop();
        mMinFlingVelocity = viewConfig.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = viewConfig.getScaledMaximumFlingVelocity();
        mAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        super.setOnScrollListener(mInternalOnScrollListener);
    }

    private void changeState(State state) {
        if (state != null && mState != state) {
            mState = state;
            Ln.d("State Changed To %s", mState);
        }
    }

    public SwipeListView setFrontViewId(int frontViewId) {
        mFrontViewId = frontViewId;
        return this;
    }

    public SwipeListView setBackViewId(int backViewId) {
        mBackViewId = backViewId;
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Store width of this list for usage of swipe distance detection
        if (mViewWidth < 2) {
            mViewWidth = getWidth();
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                int[] viewCoords = new int[2];
                getLocationOnScreen(viewCoords);

                int touchX = (int) event.getRawX() - viewCoords[0];
                int touchY = (int) event.getRawY() - viewCoords[1];

                //TODO: Account for headers/footers.
                int firstVisibleChild = getFirstVisiblePosition();
                int lastVisibleChild = getLastVisiblePosition();

                Rect rect = new Rect();
                View child;

                for (int i = firstVisibleChild; i <= lastVisibleChild; i++) {
                    child = getChildAt(i);

                    if (child != null) {
                        //Get the child hit rectangle.
                        child.getHitRect(rect);
                        //If the child would be hit by this press.
                        if (rect.contains(touchX, touchY)) {
                            //Grab the front and back views.
                            View frontView = child.findViewById(mFrontViewId);
                            View backView = child.findViewById(mBackViewId);
                            mViewPair = new SwipeableViewPair(frontView, backView);
                            break;
                        }
                    }
                }

                if (mViewPair != null) {
                    mDownX = event.getRawX();
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(event);
                } else {
                    Ln.e("Failed to Find Children");
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);

                    float deltaX = event.getRawX() - mDownX;
                    float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                    float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                    if (mViewPair != null) {
                        if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityX > velocityY) {
                            ViewPropertyAnimator.animate(mViewPair.mFrontView)
                                                .translationX(deltaX >= 0 ? mViewWidth : -mViewWidth)
                                                .alpha(0)
                                                .setDuration(mAnimationTime);
                            ViewPropertyAnimator.animate(mViewPair.mBackView).alpha(1).setDuration(mAnimationTime);
                            mSwipedViews.add(mViewPair);
                            resetState();
                        } else if (mState == State.SWIPING) {
                            //If the user stopped swiping but we don't think we should hide the view (it was cancelled basically) reset the views.
                            mViewPair.mBackView.setVisibility(View.GONE);
                            ViewPropertyAnimator.animate(mViewPair.mFrontView).translationX(0).alpha(1).setDuration(mAnimationTime);
                            resetState();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker != null && mState != State.SCROLLING) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);

                    float deltaX = event.getRawX() - mDownX;
                    float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                    float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                    if (Math.abs(deltaX) > mTouchSlop && velocityX > velocityY) {
                        ViewParent parent = getParent();
                        if (parent != null) {
                            // Don't allow parent to intercept touch (e.g. like NavigationDrawer does)
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                        changeState(State.SWIPING);
                        requestDisallowInterceptTouchEvent(true);

                        // Cancel ListView's touch (un-highlighting the item)
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(
                            MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        super.onTouchEvent(cancelEvent);
                    }

                    if (mState == State.SWIPING && mViewPair != null) {
                        mViewPair.mBackView.setVisibility(View.VISIBLE);
                        //Fade the back in and front out.
                        ViewHelper.setAlpha(mViewPair.mBackView, Math.min(1f, 2f * Math.abs(deltaX) / mViewWidth));
                        ViewHelper.setTranslationX(mViewPair.mFrontView, deltaX);
                        ViewHelper.setAlpha(mViewPair.mFrontView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
                        return true;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void resetState() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mDownX = 0;
        mViewPair = null;
        changeState(State.IDLE);
    }

    /**
     * Set the listener that will receive notifications every time the list scrolls.
     *
     * @param onScrollListener
     *     the scroll listener
     */
    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    private class SwipeableViewPair {
        View mFrontView;
        View mBackView;

        private SwipeableViewPair(View mFrontView, View mBackView) {
            this.mFrontView = mFrontView;
            this.mBackView = mBackView;
        }
    }
}

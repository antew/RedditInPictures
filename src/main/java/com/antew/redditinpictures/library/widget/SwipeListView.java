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
import android.widget.AdapterView;
import android.widget.ListView;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.pro.R;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import java.util.WeakHashMap;

public class SwipeListView extends ListView {
    /**
     * Defines the direction in which list items can be swiped. Use {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
     * to change the behavior. This can also
     * be
     * set via a Styleable Attribute called swipeDirection.
     */
    public enum SwipeDirection {
        /**
         * Setting the swipe direction via {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
         * to {@link
         * #BOTH} will allow the user to swipe list items to
         * either the left or the right.
         */
        BOTH,
        /**
         * Setting the swipe direction via {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
         * to {@link
         * #LEFT} will only allow the user to swipe list items to
         * the left.
         */
        LEFT,
        /**
         * Setting the swipe direction via {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
         * to {@link
         * #LEFT} will only allow the user to swipe list items to
         * the right.
         */
        RIGHT
    }

    /**
     * Defines the direction in which list items can be swiped. Use {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
     * to change the behavior. This can also
     * be
     * set via a Styleable Attribute called swipeDirection. Setting the value to {@link #SWIPE_DIRECTION_BOTH} will allow list items to be
     * swiped either left or right.
     */
    public static final int   SWIPE_DIRECTION_BOTH  = 0;
    /**
     * Defines the direction in which list items can be swiped. Use {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
     * to change the behavior. This can also
     * be
     * set via a Styleable Attribute called swipeDirection. Setting the value to {@link #SWIPE_DIRECTION_LEFT} will only allow list items
     * to
     * be
     * swiped to the left.
     */
    public static final int   SWIPE_DIRECTION_LEFT  = 1;
    /**
     * Defines the direction in which list items can be swiped. Use {@link #setSwipeDirection(com.antew.redditinpictures.library.widget.SwipeListView.SwipeDirection)}
     * to change the behavior. This can also
     * be
     * set via a Styleable Attribute called swipeDirection. Setting the value to {@link #SWIPE_DIRECTION_LEFT} will only allow list items
     * to be swiped to the left.
     */
    public static final int   SWIPE_DIRECTION_RIGHT = 2;
    /**
     * Used to hold information about the current state that the list view is operating in. This is used mainly to prevent what looks like
     * a
     * swipe action from occuring while a user is scrolling.
     */
    private             State mState                = State.IDLE;
    /**
     * Used to store the current view width. This potentially could not properly return so it is initialized as 1 to avoid divide by zero
     * errors.
     */
    private             int   mViewWidth            = 1;
    /**
     * Holds the generated Id for the Front View, can be passed via XML with R.styleable.SwipeListView_frontViewId or set via the
     * constructor if creating it in code. This value is required.
     */
    private int     mFrontViewId;
    /**
     * Holds the generated Id for the Back View, can be passed via XML with R.styleable.SwipeListView_backViewId or set via the constructor
     * if creating it in code. This value is required.
     */
    private int     mBackViewId;
    /**
     * Holds the selection for the SwipeDirection that applies to all list items, can be passed via XML with
     * R.styleable.SwipeListView_swipeDirection or set via #setSwipeDirection. Defaults to SWIPE_DIRECTION_BOTH.
     */
    private int     mSwipeDirection;
    /**
     * Holds the selection for whether or not scrolling closes all currently open list items, can be passed via XML with
     * R.styleable.SwipeListView_closeAllWhenScrolling or set via #setCloseAllWhenScrolling. Defaults to true.
     */
    private boolean mCloseAllWhenScrolling;
    /**
     * Holds the selection for whether or not long pressing on an item causes it to swipe open or not, can be passed via XML with
     * R.styleable.SwipeListView_openOnLongPress or set via #setOpenOnLongPress. Defaults to true.
     */
    private boolean mOpenOnLongPress;

    /**
     * Used to hold the location from which the initial touch began.
     */
    private float             mDownX;
    /**
     * Used to track movements after the initial touch has been done and determine velocity to handle flinging items.
     */
    private VelocityTracker   mVelocityTracker;
    /**
     * Used to hold the current pair of views that are being manipulated. The pair consists of a Front and Back view.
     */
    private SwipeableViewPair mViewPair;
    /**
     * The touch slop as defined in ViewConfiguration for this device. Used to calculate if enough movement has occured in order to start
     * capturing a swipe.
     */
    private int               mTouchSlop;
    /**
     * The minimum fling velocity as defined in ViewConfiguration. Used to determine if the velocity at which a movement has taken place is
     * significant enough to consider it a fling.
     */
    private int               mMinFlingVelocity;
    /**
     * The maximum fling velocity as defined in ViewConfiguration. Used to determine if the velocity at which a movement has taken place is
     * within the correct range to consider it a fling.
     */
    private int               mMaxFlingVelocity;
    /**
     * The animation times as defined in ViewConfiguration. Used to specify the animation time for tranformations.
     */
    private long              mAnimationTime;
    /**
     * A weak hash-map containing all of the views which are currently in a open position (back is showing). This is used to close these
     * views when scrolling occurs if configured for that functionality.
     */
    private WeakHashMap<Integer, SwipeableViewPair> mSwipedViews = new WeakHashMap<Integer, SwipeableViewPair>();
    /**
     * The OnItemLongClickListener that can be optionally passed via #setOnItemLongClickListener.
     */
    private OnItemLongClickListener mOnItemLongClickListener;
    /**
     * The internal OnItemLongClickListener used to provide the ability to open and close items on long click. Also will propagate the call
     * to any OnItemLongClickListener set by #setOnItemLongClickListener.
     */
    private OnItemLongClickListener mInternalOnItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mOpenOnLongPress) {
                // If long clicking opening a menu is enabled.

                // Grab the views.
                View frontView = view.findViewById(mFrontViewId);
                View backView = view.findViewById(mBackViewId);

                if (frontView != null && backView != null) {
                    // If we don't have the views there isn't anything that we can do, abort!
                    mViewPair = new SwipeableViewPair(frontView, backView);

                    // If the view we are looking at has alrady been swiped, reset it.
                    if (mSwipedViews.containsKey(mViewPair.hashCode())) {
                        mViewPair.mBackView.setVisibility(View.GONE);
                        ViewPropertyAnimator.animate(mViewPair.mFrontView).translationX(0).alpha(1).setDuration(mAnimationTime);
                    } else {
                        // Otherwise let's open it up.
                        ViewPropertyAnimator.animate(mViewPair.mFrontView)
                                            .translationX(mSwipeDirection == SWIPE_DIRECTION_LEFT ? -mViewWidth : mViewWidth)
                                            .alpha(0)
                                            .setDuration(mAnimationTime);
                        mViewPair.mBackView.setVisibility(View.VISIBLE);
                        ViewPropertyAnimator.animate(mViewPair.mBackView).alpha(1).setDuration(mAnimationTime);
                        mSwipedViews.put(mViewPair.hashCode(), mViewPair);
                        resetState();
                    }

                    if (mOnItemLongClickListener != null) {
                        // If we have a listener for Long Clicks that was passed, call it.
                        mOnItemLongClickListener.onItemLongClick(parent, view, position, id);
                    }

                    return true;
                }
            }

            if (mOnItemLongClickListener != null) {
                // If long clicking to open views is not enabled and we have a listener that was passed, call it.
                return mOnItemLongClickListener.onItemLongClick(parent, view, position, id);
            }

            // Otherwise we don't want to consume the long click.
            return false;
        }
    };
    /**
     * The OnScrollListener that can be optionally passed via #setOnScrollListener.
     */
    private OnScrollListener mOnScrollListener;
    /**
     * The internal OnScrollListener used to provide the ability to open and close items on long click. Also will propagate the call
     * to any OnScrollListener set by #setOnScrollListener.
     */
    private OnScrollListener mInternalOnScrollListener = new OnScrollListener() {
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
                    // If scrolling is not occuring we consider ourselves idle.
                    changeState(State.IDLE);
                    break;
                case SCROLL_STATE_TOUCH_SCROLL:
                    // If we are scrolling, change the state to prevent accidental intercepts of touches while scrolling.
                    changeState(State.SCROLLING);
                    if (mCloseAllWhenScrolling) {
                        // If we are supposed to close all views when the user scrolls, do it.
                        for (SwipeableViewPair viewPair : mSwipedViews.values()) {
                            viewPair.mBackView.setVisibility(View.GONE);
                            ViewPropertyAnimator.animate(viewPair.mFrontView).translationX(0).alpha(1).setDuration(mAnimationTime);
                        }
                        mSwipedViews.clear();
                    }
                    break;
            }

            if (mOnScrollListener != null) {
                // If a scroll listener was set, call it.
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
                // If a scroll listener was set, call it.
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    };

    /**
     * If you create a View in code you have to send the front and back identifiers
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

    private void initialize(AttributeSet attrs) {

        // If we are in an IDE Preview, don't initialize.
        if (isInEditMode()) {
            return;
        }

        if (attrs != null) {
            TypedArray styled = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeListView);
            mFrontViewId = styled.getResourceId(R.styleable.SwipeListView_frontViewId, 0);
            mBackViewId = styled.getResourceId(R.styleable.SwipeListView_backViewId, 0);
            mCloseAllWhenScrolling = styled.getBoolean(R.styleable.SwipeListView_closeAllWhenScrolling, true);
            mOpenOnLongPress = styled.getBoolean(R.styleable.SwipeListView_openOnLongPress, true);
            setSwipeDirection(styled.getInt(R.styleable.SwipeListView_swipeDirection, SWIPE_DIRECTION_BOTH));
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
        super.setOnItemLongClickListener(mInternalOnItemLongClickListener);
    }

    /**
     * @param closeAllWhenScrolling
     *
     * @return This {@link SwipeListView}
     */
    public SwipeListView setCloseAllWhenScrolling(boolean closeAllWhenScrolling) {
        mCloseAllWhenScrolling = closeAllWhenScrolling;
        return this;
    }

    /**
     * @param swipeDirection
     *
     * @return This {@link SwipeListView}
     */
    public SwipeListView setSwipeDirection(SwipeDirection swipeDirection) {
        switch (swipeDirection) {
            case LEFT:
                setSwipeDirection(SWIPE_DIRECTION_LEFT);
                break;
            case RIGHT:
                setSwipeDirection(SWIPE_DIRECTION_RIGHT);
                break;
            // fall through
            case BOTH:
            default:
                setSwipeDirection(SWIPE_DIRECTION_BOTH);
                break;
        }
        return this;
    }

    /**
     * Used to set the SwipeDirection for items in the List View. Can also be set in the layout XML using
     * R.styleable.SwipeListView_swipeDirection.
     *
     * @param swipeDirection
     *     The direction that swiping should be allowed to occur in. Defaults to SWIPE_DRECTION_BOTH. Use SWIPE_DIRECTION_LEFT to only
     *     allow
     *     swiping items to the left, and SWIPE_DIRECTION_RIGHT to only allow swiping items to the right.
     */
    private void setSwipeDirection(int swipeDirection) {
        switch (swipeDirection) {
            case SWIPE_DIRECTION_LEFT:
                mSwipeDirection = SWIPE_DIRECTION_LEFT;
                break;
            case SWIPE_DIRECTION_RIGHT:
                mSwipeDirection = SWIPE_DIRECTION_RIGHT;
                break;
            // Fall through to default to Both.
            case SWIPE_DIRECTION_BOTH:
            default:
                mSwipeDirection = SWIPE_DIRECTION_BOTH;
                break;
        }
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

    /**
     * @param openOnLongPress
     *
     * @return This {@link SwipeListView}
     */
    public SwipeListView setOpenOnLongPress(boolean openOnLongPress) {
        mOpenOnLongPress = openOnLongPress;
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
                // Figure out where the touch occurred.
                getLocationOnScreen(viewCoords);

                int touchX = (int) event.getRawX() - viewCoords[0];
                int touchY = (int) event.getRawY() - viewCoords[1];

                Rect rect = new Rect();
                View child;

                int childCount = getChildCount();
                for (int i = getHeaderViewsCount(); i <= childCount; i++) {
                    // Go through each child view (excluding headers) and see if our touch pressed it.
                    child = getChildAt(i);

                    if (child != null) {
                        //Get the child hit rectangle.
                        child.getHitRect(rect);
                        //If the child would be hit by this press.
                        if (rect.contains(touchX, touchY)) {
                            // DIRECT HIT! You sunk my battleship. Now that we know which view was touched, store it off for use if a move occurs.
                            View frontView = child.findViewById(mFrontViewId);
                            View backView = child.findViewById(mBackViewId);
                            // Create our view pair.
                            mViewPair = new SwipeableViewPair(frontView, backView);
                            break;
                        }
                    }
                }

                if (mViewPair != null) {
                    // If we have a view pair, record details about the inital touch for use later.
                    mDownX = event.getRawX();
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    // Add the movement so we can calculate velocity.
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);

                    float deltaX = event.getRawX() - mDownX;
                    float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                    float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                    if (mViewPair != null) {
                        boolean shouldSwipe = false;

                        // If the view has been moved a significant enough distance or if the view was flung, check to see if we should swipe it.
                        if ((Math.abs(deltaX) > mViewWidth / 2 && mState == State.SWIPING) || (mMinFlingVelocity <= velocityX
                                                                                               && velocityX <= mMaxFlingVelocity
                                                                                               && velocityX > velocityY)) {
                            if (mSwipeDirection == SWIPE_DIRECTION_BOTH) {
                                // If the list is setup to swipe in either direction, just let it go.
                                shouldSwipe = true;
                            } else if (mSwipeDirection == SWIPE_DIRECTION_LEFT && deltaX < 0) {
                                // If the list is only setup to swipe left, then only allow swiping to the left.
                                shouldSwipe = true;
                            } else if (mSwipeDirection == SWIPE_DIRECTION_RIGHT && deltaX > 0) {
                                // If the list is only setup to swipe right, then only allow swiping to the right.
                                shouldSwipe = true;
                            }
                        }

                        if (shouldSwipe) {
                            // If a swipe should occur meaning someone has let go of a view they were moving and it was far/fast enough for us to consider it a swipe start the animations.
                            ViewPropertyAnimator.animate(mViewPair.mFrontView)
                                                .translationX(deltaX >= 0 ? mViewWidth : -mViewWidth)
                                                .alpha(0)
                                                .setDuration(mAnimationTime);
                            ViewPropertyAnimator.animate(mViewPair.mBackView).alpha(1).setDuration(mAnimationTime);
                            // Now that the item is open, store it off so we can close it when we scroll if needed.
                            mSwipedViews.put(mViewPair.hashCode(), mViewPair);
                            // Clear out current variables as they are no longer needed and recycle the velocity tracker.
                            resetState();
                        } else {
                            // If the user let go of the view and we don't think the swipe was intended to occur (it was cancelled basically) reset the views.
                            // Make sure the back disappears, since if it has buttons these can intercept touches from the front view.
                            mViewPair.mBackView.setVisibility(View.GONE);
                            ViewPropertyAnimator.animate(mViewPair.mFrontView).translationX(0).alpha(1).setDuration(mAnimationTime);
                            // Clear out current variables as they are no longer needed and recycle the velocity tracker.
                            resetState();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mVelocityTracker != null && mState != State.SCROLLING) {
                    // If this is an initial movement and we aren't already swiping.
                    // Add the movement so we can calculate velocity.
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);

                    float deltaX = event.getRawX() - mDownX;
                    float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                    float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                    // If the movement has been more than what is considered slop, and they are clearing moving horizontal not vertical.
                    if (Math.abs(deltaX) > mTouchSlop && velocityX > velocityY) {
                        boolean initiateSwiping = false;

                        if (mSwipeDirection == SWIPE_DIRECTION_BOTH) {
                            // If the list is setup to swipe in either direction, just let it go.
                            initiateSwiping = true;
                        } else if (mSwipeDirection == SWIPE_DIRECTION_LEFT && deltaX < 0) {
                            // If the list is only setup to swipe left, then only allow swiping to the left.
                            initiateSwiping = true;
                        } else if (mSwipeDirection == SWIPE_DIRECTION_RIGHT && deltaX > 0) {
                            // If the list is only setup to swipe right, then only allow swiping to the right.
                            initiateSwiping = true;
                        }

                        if (initiateSwiping) {
                            ViewParent parent = getParent();
                            if (parent != null) {
                                // Don't allow parent to intercept touch (prevents NavigationDrawers from intercepting when near the bezel).
                                parent.requestDisallowInterceptTouchEvent(true);
                            }
                            // Change our state to swiping to start tranforming the item.
                            changeState(State.SWIPING);
                            // Make sure that touches aren't intercepted.
                            requestDisallowInterceptTouchEvent(true);

                            // Cancel ListView's touch to prevent it from being focused.
                            MotionEvent cancelEvent = MotionEvent.obtain(event);
                            cancelEvent.setAction(
                                MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                            super.onTouchEvent(cancelEvent);
                        } else {
                            // Otherwise we need to cancel the touch event to prevent accidentally selecting the item and also preventing the swipe in the wrong direction or an incomplete touch from moving the view.
                            MotionEvent cancelEvent = MotionEvent.obtain(event);
                            cancelEvent.setAction(
                                MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                            super.onTouchEvent(cancelEvent);
                        }
                    }

                    if (mState == State.SWIPING && mViewPair != null) {
                        // Make sure the back is visible.
                        mViewPair.mBackView.setVisibility(View.VISIBLE);
                        //Fade the back in and front out as they move.
                        ViewHelper.setAlpha(mViewPair.mBackView, Math.min(1f, 2f * Math.abs(deltaX) / mViewWidth));
                        ViewHelper.setTranslationX(mViewPair.mFrontView, deltaX);
                        ViewHelper.setAlpha(mViewPair.mFrontView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
                        return true;
                    }
                }
                break;
        }
        /**
         * In older versions of Android MotionEvent will cause an ArrayIndexOutOfBoundsException when it
         * attempts to access the Y coordinateunchecked if there hasn't been any motion in the Y direction.
         * Can't extend MotionEvent since it is final, so catching the error is the best we can do.
         */
        if (!AndroidUtil.hasHoneycomb()) {
            try {
                return super.onTouchEvent(event);
            } catch (ArrayIndexOutOfBoundsException e) {
                return true;
            }
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * Reset variables used to handle swiping and clear out any resources no longer needed.
     */
    private void resetState() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mDownX = 0;
        mViewPair = null;
        changeState(State.IDLE);
    }

    /**
     * Change the state of the ListView.
     *
     * @param state
     *     The new {@link State}
     */
    private void changeState(State state) {
        if (state != null && mState != state) {
            mState = state;
        }
    }

    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked and held
     *
     * @param listener
     *     The callback that will run
     */
    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    /**
     * Used to identify the current state the list view is in.
     */
    private enum State {
        IDLE, SWIPING, SCROLLING;
    }

    /**
     * Helper class which simply serves as a wrapper for holding a Front and Back View.
     */
    private class SwipeableViewPair {
        View mFrontView;
        View mBackView;

        private SwipeableViewPair(View mFrontView, View mBackView) {
            this.mFrontView = mFrontView;
            this.mBackView = mBackView;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SwipeableViewPair that = (SwipeableViewPair) o;

            if (!mBackView.equals(that.mBackView)) {
                return false;
            }
            if (!mFrontView.equals(that.mFrontView)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = mFrontView.hashCode();
            result = 31 * result + mBackView.hashCode();
            return result;
        }
    }
}


package com.antew.redditinpictures.library.ui.base;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.QueryCriteria;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.squareup.otto.Bus;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Fragment with convenience methods for displaying images
 *
 * @param <T> The type of view the fragment is using, e.g. GridView, ListView
 * @param <V> The type of the cursor adapter backing the view
 */
public abstract class BaseImageFragment<T extends AdapterView, V extends CursorAdapter>
    extends BaseFragment
    implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    protected V mAdapter;
    @Inject
    protected Bus mBus;
    @InjectView(R.id.no_images)
    protected TextView mNoImages;
    private String mAppendTo;
    private boolean mFullRefresh = true;

    private String mCurrentSubreddit = RedditUrl.REDDIT_FRONTPAGE;
    private Category mCategory = Category.HOT;
    private Age mAge = Age.TODAY;

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(android.app.Activity)} and before
     * {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup,
     * android.os.Bundle)}.
     *
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(android.os.Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Initialize the adapter to null, the adapter will be populated in onLoadFinished
        mAdapter = getNewAdapter();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(android.os.Bundle)} and {@link #onActivityCreated(android.os.Bundle)}.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        final View v = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleArguments();
        getActivity().getSupportLoaderManager().initLoader(Consts.LOADER_POSTS, null, this);
        fetchImagesFromReddit(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1) public void handleArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (Util.hasHoneycombMR1()) {
                mCurrentSubreddit = arguments.getString(Consts.EXTRA_SELECTED_SUBREDDIT,
                    RedditUrl.REDDIT_FRONTPAGE);
                mCategory = Category.valueOf(
                    arguments.getString(Consts.EXTRA_CATEGORY, Category.HOT.toString()));
                mAge = Age.valueOf(arguments.getString(Consts.EXTRA_AGE, Age.TODAY.toString()));
            } else {
                if (arguments.containsKey(Consts.EXTRA_SELECTED_SUBREDDIT)) {
                    mCurrentSubreddit = arguments.getString(Consts.EXTRA_SELECTED_SUBREDDIT);
                }

                if (arguments.containsKey(Consts.EXTRA_CATEGORY)) {
                    mCategory = Category.valueOf(arguments.getString(Consts.EXTRA_CATEGORY));
                }

                if (arguments.containsKey(Consts.EXTRA_AGE)) {
                    mAge = Age.valueOf(arguments.getString(Consts.EXTRA_CATEGORY));
                }
            }
        }
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Consts.LOADER_POSTS:
                QueryCriteria queryCriteria = getPostsQueryCriteria();
                String selection = null;
                List<String> selectionArgsList = new ArrayList<String>();
                String[] selectionArgs = null;

                // If we aren't on one of the default subreddits (Frontpage/All), filter the selection to only the subreddit we are concerned with.
                if (!SubredditUtils.isDefaultSubreddit(mCurrentSubreddit)) {
                    selection = RedditContract.PostColumns.SUBREDDIT + " = ?";
                    selectionArgsList.add(mCurrentSubreddit);
                }

                // If the user doesn't want to see NSFW images, filter them out. Otherwise do nothing.
                if (!SharedPreferencesHelper.getShowNsfwImages(getActivity())) {
                    if (Strings.isEmpty(selection)) {
                        selection = RedditContract.PostColumns.OVER_18 + " = ?";
                    } else {
                        selection += " and " + RedditContract.PostColumns.OVER_18 + " = ?";
                    }
                    selectionArgsList.add("0");
                }

                if (selectionArgsList != null && selectionArgsList.size() > 0) {
                    selectionArgs = selectionArgsList.toArray(selectionArgs);
                }

                return new CursorLoader(getActivity(), RedditContract.Posts.CONTENT_URI,  // uri
                    queryCriteria.getProjection(),     // projection
                    selection,                         // selection
                    selectionArgs,                     // selectionArgs[]
                    queryCriteria.getSort());          // sort
            default:
                return null;
        }
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link android.support.v4.app.FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     *
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     *
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link android.database.Cursor}
     * and you place it in a {@link android.widget.CursorAdapter}, use
     * the {@link android.widget.CursorAdapter#CursorAdapter(android.content.Context,
     * android.database.Cursor, int)} constructor <em>without</em> passing
     * in either {@link android.widget.CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link android.widget.CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link android.database.Cursor} from a {@link android.content.CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link android.widget.CursorAdapter}, you should use the
     * {@link android.widget.CursorAdapter#swapCursor(android.database.Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case Consts.LOADER_POSTS:
                mAdapter.swapCursor(data);

                if (data.getCount() == 0) {
                    fetchImagesFromReddit(true);
                    mNoImages.setVisibility(View.VISIBLE);
                } else if (mNoImages.getVisibility() == View.VISIBLE) {
                    mNoImages.setVisibility(View.GONE);
                }

                // If a full refresh was initiated, scroll to the top.
                if (mFullRefresh) {
                    getAdapterView().setSelection(0);
                    mFullRefresh = false;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Consts.LOADER_POSTS:
                mAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this
     * will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     */
    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
        final Intent i = new Intent(getActivity(), getImageDetailActivityClass());
        i.putExtra(Consts.EXTRA_IMAGE, (int) position);
        Bundle b = new Bundle();
        b.putString(Consts.EXTRA_AGE, mRedditDataProvider.getAge().name());
        b.putString(Consts.EXTRA_CATEGORY, mRedditDataProvider.getCategory().name());
        b.putString(Consts.EXTRA_SELECTED_SUBREDDIT, mRedditDataProvider.getSubreddit());
        i.putExtras(b);

        if (Util.hasJellyBean()) {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }*/
    }

    protected void fetchImagesFromReddit(boolean replaceAll) {
        mFullRefresh = replaceAll;
        RedditService.getPosts(getActivity(), mCurrentSubreddit, mAge, mCategory, mAppendTo,
            mFullRefresh);
    }

    protected abstract int getLayoutId();

    protected abstract T getAdapterView();

    protected abstract V getNewAdapter();

    protected abstract QueryCriteria getPostsQueryCriteria();
}

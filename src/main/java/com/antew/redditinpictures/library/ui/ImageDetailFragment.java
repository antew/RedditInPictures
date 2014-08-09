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
package com.antew.redditinpictures.library.ui;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.animation.ArgbEvaluator;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.adapter.RedditCommentAdapter;
import com.antew.redditinpictures.library.animation.PeekInterpolator;
import com.antew.redditinpictures.library.animation.Rotate3dAnimation;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageEvent;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.image.ImgurGalleryType;
import com.antew.redditinpictures.library.interfaces.OnBackPressedListener;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.Child;
import com.antew.redditinpictures.library.model.reddit.Comment;
import com.antew.redditinpictures.library.model.reddit.MoreChild;
import com.antew.redditinpictures.library.model.reddit.MoreData;
import com.antew.redditinpictures.library.model.reddit.PostChild;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RedditServiceRetrofit;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.PostUtil;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Optional;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.antew.redditinpictures.library.Constants.Analytics.Action;
import static com.antew.redditinpictures.library.Constants.Analytics.Category;
import static com.antew.redditinpictures.library.Constants.Analytics.Label;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends ImageViewerFragment implements SaveImageDialogFragment.SaveImageDialogListener, Observer<List<Child>>, OnBackPressedListener {

    @InjectView(R.id.lv_post_comments)
    ListView mPostComments;

    @InjectView(R.id.next_comment) ImageButton mNextComment;
    @InjectView(R.id.previous_comment) ImageButton mPreviousComment;
    @InjectView(R.id.save_post)    ImageButton mSaveImageMenuItem;
    @InjectView(R.id.share_post)   ImageButton mSharePostMenuItem;
    @InjectView(R.id.upvote)       ImageButton mUpvoteMenuItem;
    @InjectView(R.id.downvote)     ImageButton mDownvoteMenuItem;
    @InjectView(R.id.view_post)    ImageButton mViewPostMenuItem;
    @InjectView(R.id.report_image) ImageButton mReportImageMenuItem;

    @InjectView(R.id.pb_post_comments)
    GoogleProgressBar mPostCommentsProgressBar;

    @Inject
    RedditServiceRetrofit mRedditService;

    /**
     * Used to animate smoothly between colors
     */
    @Inject
    ArgbEvaluator mArgbEvaluator;

    private AlphaInAnimationAdapter mAlphaInAnimationAdapter;

    private RedditCommentAdapter mCommentAdapter;

    private List<PostData> mAllPosts = new LinkedList<PostData>();

    /**
     * Observer for new comments being loaded
     */
    private Observable<List<Child>> mCommentsObservable;

    private boolean mMoreCommentsRequestInProgress;

    /**
     * Whether the sliding drawer with comments has been opened
     */
    private boolean mCommentsPanelHasBeenOpened;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    private SlidingUpPanelLayout.PanelSlideListener slidingUpPanelListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View view, float v) {
            int newColor = (int) mArgbEvaluator.evaluate(v, R.color.post_information_background, 0xFF000000);
            mCommentsPanel.setBackgroundColor(newColor);
        }

        @Override public void onPanelCollapsed(View view) { Ln.i("onPanelCollapsed"); }
        @Override public void onPanelAnchored(View view) { Ln.i("onPanelAnchored"); }
        @Override public void onPanelHidden(View view) { Ln.i("onPanelHidden"); }

        @Override
        public void onPanelExpanded(View view) {
            // Don't reload comments
            if (!mCommentsPanelHasBeenOpened) {
                loadPostComments();
            }
        }
    };

    private void loadPostComments() {
        mCommentsPanelHasBeenOpened = true;
        SharedPreferencesHelper.setHasOpenedCommentsSlidingPanel(getActivity());
        mPostCommentsProgressBar.setVisibility(View.VISIBLE);

        mCommentsObservable = AndroidObservable.bindFragment(
                ImageDetailFragment.this,
                mRedditService.getComments(mImage.getSubreddit(), mImage.getId())
                        .flatMap(redditApis -> Observable.from(redditApis.get(1).getData().getChildren()))
                        .flatMap(child -> Observable.just(flattenList(child)))
                        .filter(child -> (child instanceof MoreChild) ? ((MoreChild) child).hasChildren() : true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .cache());

        mCommentsObservable.subscribe(postList -> onNext(postList));
    }

    @OnClick(R.id.save_post)
    public void saveImage() {
        SharedPreferencesHelper.setHasNotOpenedCommentsSlidingPanel(getActivity());
        track(Category.ACTION_BAR_ACTION, Action.SAVE_POST, mImage.getSubreddit());
        handleSaveImage();
    }

    @OnClick(R.id.share_post)
    public void sharePost() {
        track(Category.ACTION_BAR_ACTION, Action.SHARE_POST, mImage.getSubreddit());
        String subject = getString(R.string.check_out_this_image);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, subject + " " + mImage.getUrl());
        startActivity(Intent.createChooser(intent, getString(R.string.share_using_)));
    }

    @OnClick(R.id.view_post)
    public void viewPost() {
        track(Category.ACTION_BAR_ACTION, Action.OPEN_POST_EXTERNAL, mImage.getSubreddit());
        Ln.i("View Post URL = " + mImage.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity())));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mImage.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity()))));
        startActivity(browserIntent);
    }

    @OnClick(R.id.upvote)
    public void upVote() {
        track(Category.ACTION_BAR_ACTION, Action.POST_VOTE, Label.UP);
        handleVote(Vote.UP);
    }

    @OnClick(R.id.downvote)
    public void downVote() {
        track(Category.ACTION_BAR_ACTION, Action.POST_VOTE, Label.DOWN);
        handleVote(Vote.DOWN);
    }

    @OnClick(R.id.report_image)
    public void reportImage() {
        track(Category.ACTION_BAR_ACTION, Action.REPORT_POST, mImage.getSubreddit());
        new Thread(new Runnable() {
            @Override
            public void run() {
                reportCurrentItem();
            }
        }).start();
        Toast.makeText(getActivity(), R.string.image_display_issue_reported, Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.next_comment)
    public void nextComment() {
        Integer nextTopLevelComment = mCommentAdapter.getNextTopLevelComment(mPostComments.getFirstVisiblePosition());
        if (nextTopLevelComment != null) {
            mPostComments.setSelection(nextTopLevelComment);
        }
    }

    @OnClick(R.id.previous_comment)
    public void previousComment() {
        Integer previousTopLevelComment = mCommentAdapter.getPreviousTopLevelComment(mPostComments.getFirstVisiblePosition());
        if (previousTopLevelComment != null) {
            mPostComments.setSelection(previousTopLevelComment);
        }
    }

    public void track(String category, String action, String label) {
        track(category, action, label, null);
    }

    public void track(String category, String action, String label, Long value) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(category, action, label, value)
                           .build());
    }

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param image
     *     The post to load
     *
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static Fragment newInstance(PostData image) {
        final ImageDetailFragment fragment = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putParcelable(IMAGE_DATA_EXTRA, image);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onDestroyView() {
        if (mCommentsObservable != null) {
            mCommentsObservable.unsubscribeOn(AndroidSchedulers.mainThread());
        }

        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mAlphaInAnimationAdapter.setAbsListView(mPostComments);
        mPostComments.setAdapter(mAlphaInAnimationAdapter);
        mSlidingUpPanel.setPanelSlideListener(slidingUpPanelListener);

        return v;
    }

    @OnItemClick(R.id.lv_post_comments)
    public void handleCommentsClick(AdapterView<?> parent, View view, int position, long id) {
        Child c = (Child) mCommentAdapter.getItem(position);

        if (c instanceof MoreChild) {
            if (!mMoreCommentsRequestInProgress) {
                handleMoreCommentsClick((MoreChild) c, position);
            }
        } else if (c instanceof PostChild) {
            mCommentAdapter.collapseChildCommentsAtPosition(position, c.getDepth());
        }
    }

    private void handleMoreCommentsClick(MoreChild child, int clickedPosition) {
        mMoreCommentsRequestInProgress = true;
        child.setRequestInProgress(true);

        // Update the adapter to show the ProgressBar
        // TODO: Refactor to only update the current ListItem
        mCommentAdapter.notifyDataSetChanged();

        MoreData data = child.getData();
        mRedditService.getMoreComments("json", Strings.join(",", data.getChildren()), data.getName(), mImage.getName())
                .flatMap(moreComments -> Observable.from(moreComments.getThings()))
                .filter(child1 -> (child1 instanceof MoreChild) ? ((MoreChild) child1).hasChildren() : true)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Child>>() {
                    @Override
                    public void onCompleted() {
                        Ln.i("onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mMoreCommentsRequestInProgress = false;
                        Ln.e(e, "onError");
                    }

                    @Override
                    public void onNext(List<Child> children) {
                        Ln.i("Got more comments back! size = " + children.size());
                        mCommentAdapter.replaceAtPosition(clickedPosition, children);
                        mMoreCommentsRequestInProgress = false;
                    }
                });
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(com.antew.redditinpictures.library.model.reddit.PostData)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        loadExtras();
        displayVote();

        // Need this here rather than in onCreateView so that the adapter isn't recreated on orientation change
        mCommentAdapter = new RedditCommentAdapter(getActivity(), new ArrayList<Child>());

        // Animation is turned off after loading a couple of items (in onNext()), if we were to
        // recreate the animation adapter on orientation change the animation would start again...which we don't want.
        mAlphaInAnimationAdapter = new AlphaInAnimationAdapter(mCommentAdapter);
        mAlphaInAnimationAdapter.setAnimationDurationMillis(10);
        mAlphaInAnimationAdapter.setInitialDelayMillis(0);
        mAlphaInAnimationAdapter.setAnimationDelayMillis(0);
    }


    public static List<Child> flattenList(Child child) {
        List<Child> posts = new LinkedList<Child>();
        posts.add(child);

        if (child instanceof PostChild || child instanceof Comment) {
            PostChild p = (PostChild) child;

            // TODO: Switch this to use Optional<T>, it's hideous
            if (p.getData() != null && p.getData().getReplies() != null && p.getData().getReplies().getData() != null) {
                List<Child> children = p.getData().getReplies().getData().getChildren();
                for (Child c : children) {
                    posts.addAll(flattenList(c));
                }
            }
        }

        return posts;
    }

    public void loadExtras() {
        mImage = getArguments() != null ? (PostData) getArguments().getParcelable(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    protected void resolveImage() {
        mResolveImageTask = new ResolveImageTask();
        mResolveImageTask.execute(mImage.getUrl());
    }

    @Override
    protected boolean shouldShowPostInformation() {
        return true;
    }

    public void populatePostData(View v) {
        String separator = " " + "\u2022" + " ";
        mPostVotes.setText("" + mImage.getScore());
        String titleText = mImage.getTitle() + " <font color='#BEBEBE'>(" + mImage.getDomain() + ")</font>";
        mPostTitle.setText(Html.fromHtml(titleText));
        mPostInformation.setText(
            mImage.getSubreddit() + separator + mImage.getNum_comments() + " comments" + separator + mImage.getAuthor());
    }

    @Override
    public void loadImage(final Image image) {
        super.loadImage(image);

        if (image == null) {
            Ln.e("Received null ImageContainer in loadImage(ImageContainer image)");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ImageType.IMGUR_ALBUM.equals(image.getImageType())) {
                    mAlbum = ((ImgurAlbumType) image).getAlbum();
                } else if (ImageType.IMGUR_GALLERY.equals(image.getImageType())) {
                    //@formatter:off
                    /*
                     There are two types of galleries, one wraps a single image in
                     the gallery response, while the other wraps an album.


                     For a single image the JSON format is like:
                     {
                         "data": {
                             "image": {
                               "hash": "X74W0",
                               "album_cover": null,
                               ...
                             }
                         }
                     }

                     While the album wrapping kind has an array of images under
                     'album_images'.

                     The cover image refers to one of the images in the set.

                     {
                       "data": {
                           "image": {
                             "hash": "X74W0",
                             "album_cover": "li3gH5A",
                             "album_images": {
                               "count": 2,
                               "images": [
                                   {
                                     "hash": "li3gH5A",
                                     "title": "",
                                     "description": "",
                                     "width": 350,
                                     "height": 350,
                                     "size": 1875308,
                                     "ext": ".gif",
                                     "animated": 1,
                                     "datetime": "2014-04-15 14:25:18",
                                     "ip": "1195885755"
                                   },
                                   {
                                     "hash": "SegdXoM",
                                     "title": "",
                                     "description": "",
                                     "width": 350,
                                     "height": 350,
                                     "size": 1069755,
                                     "ext": ".gif",
                                     "animated": 1,
                                     "datetime": "2014-04-15 14:25:26",
                                     "ip": "1195885755"
                                   }
                               ]
                             }
                           }
                       }
                     }
                     */
                    //@formatter:on
                    ImgurGalleryType gallery = (ImgurGalleryType) image;
                    if (gallery.isSingleImage()) {
                        mResolvedImage = image;
                    } else {
                        mAlbum = gallery.getAlbum();
                    }

                    if (mAlbum == null) {
                        Ln.e("Received imgur gallery without an album or image!");
                    }
                }

                if (mAlbum != null && mAlbum.getImages() != null && mAlbum.getImages().size() > 1) {
                    mBtnViewGallery.setVisibility(View.VISIBLE);
                }
            }
        }).start();
    }

    @Optional
    @OnClick(R.id.btn_view_gallery)
    protected void getViewGalleryOnClickListener() {
        track(Category.UI_ACTION, Action.OPEN_GALLERY, Label.IMGUR, (long) mAlbum.getImages().size());
        Intent intent = new Intent(getActivity(), getImgurAlbumActivity());
        intent.putExtra(ImgurAlbumActivity.EXTRA_ALBUM, mAlbum);
        startActivity(intent);
    }

    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivity.class;
    }

    @Override
    public void onCompleted() {
        Ln.i("onComplete! PostData size = " + mAllPosts.size());
    }

    @Override
    public void onError(Throwable e) {
        Ln.e(e, "onError");
    }

    @Override
    public void onNext(List<Child> postData) {
        mCommentAdapter.addAll(postData);
        mPostCommentsProgressBar.setVisibility(View.GONE);

        // Disable the ListView animation after 200ms
        Observable.from(mAlphaInAnimationAdapter)
                  .delay(200, TimeUnit.MILLISECONDS)
                  .subscribe(animator -> animator.setShouldAnimate(false));
    }

    /**
     * Save the current image in the ViewPager.
     *
     * @see com.antew.redditinpictures.library.dialog.SaveImageDialogFragment
     */
    public void handleSaveImage() {
        SaveImageDialogFragment saveImageDialog = SaveImageDialogFragment.newInstance(StringUtil.sanitizeFileName(mImage.getTitle()));
        saveImageDialog.setTargetFragment(this, 0);
        saveImageDialog.show(getFragmentManager(), Constants.Dialog.DIALOG_GET_FILENAME);
    }

    protected Uri getPostUri() {
        return Uri.parse(mImage.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity())));
    }

    @Override
    public void onFinishSaveImageDialog(String filename) {
        mImageDownloader.downloadImage(mResolvedImageUrl, filename);
    }

    public void handleVote(Vote vote) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            PostUtil.votePost(getActivity(), mImage, vote);
        }

        Rotate3dAnimation rotate3dAnimation = new Rotate3dAnimation(0, 180, mUpvoteMenuItem.getWidth() / 2.0f, mUpvoteMenuItem.getHeight() / 2.0f, -150.0f, false);
        rotate3dAnimation.setInterpolator(new AccelerateInterpolator());
        rotate3dAnimation.setDuration(500);
        rotate3dAnimation.setRepeatCount(0);
        ImageButton viewToAnimate = vote.equals(Vote.UP) ? mUpvoteMenuItem : mDownvoteMenuItem;
        viewToAnimate.startAnimation(rotate3dAnimation);
        rotate3dAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation)  { }
            @Override public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (vote == Vote.UP) {
                    viewToAnimate.setImageResource(R.drawable.ic_action_upvote_highlighted);
                } else {
                    viewToAnimate.setImageResource(R.drawable.ic_action_downvote_highlighted);
                }
            }

        });
    }

    protected void showLogin() {
        // Only needs to be shown if they aren't currently logged in.
        if (!RedditLoginInformation.isLoggedIn()) {
            LoginDialogFragment loginFragment = LoginDialogFragment.newInstance();
            loginFragment.show(getFragmentManager(), Constants.Dialog.DIALOG_LOGIN);
        }
    }

    /**
     * Get the JSON representation of the current image/post in the ViewPager to report an error.
     *
     * @return The JSON representation of the currently viewed object.
     */
    protected void reportCurrentItem() {
        RedditService.reportPost(getActivity(), mImage);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayVote();

        if (!SharedPreferencesHelper.getHasOpenedCommentsSlidingPanel(getActivity())) {
            ObjectAnimator animY = ObjectAnimator.ofFloat(mCommentsPanel, "translationY", 0, -100f);
            animY.setDuration(1000);
            animY.setInterpolator(new PeekInterpolator());
            animY.setRepeatCount(3);
            animY.start();
        }
    }

    public void displayVote() {
        displayVote(mImage.getVote());
    }

    public void displayVote(Vote vote) {
        if (mUpvoteMenuItem == null || mDownvoteMenuItem == null) {
            return;
        }

        switch (vote) {
            case DOWN:
                mUpvoteMenuItem.setImageResource(R.drawable.ic_action_upvote);
                mDownvoteMenuItem.setImageResource(R.drawable.ic_action_downvote_highlighted);
                break;

            case UP:
                mUpvoteMenuItem.setImageResource(R.drawable.ic_action_upvote_highlighted);
                mDownvoteMenuItem.setImageResource(R.drawable.ic_action_downvote);
                break;

            case NEUTRAL:
                mUpvoteMenuItem.setImageResource(R.drawable.ic_action_upvote);
                mDownvoteMenuItem.setImageResource(R.drawable.ic_action_downvote);
                break;
        }
    }

    @Override
    public boolean shouldRespondToBackPress() {
        if (mSlidingUpPanel != null && (mSlidingUpPanel.isPanelExpanded() || mSlidingUpPanel.isPanelAnchored())) {
            mSlidingUpPanel.collapsePanel();
            return false;
        }

        return true;
    }
}

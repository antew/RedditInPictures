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

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.adapter.RedditCommentAdapter;
import com.antew.redditinpictures.library.dialog.LoginDialogFragment;
import com.antew.redditinpictures.library.dialog.SaveImageDialogFragment;
import com.antew.redditinpictures.library.event.DownloadImageEvent;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImgurAlbumType;
import com.antew.redditinpictures.library.image.ImgurGalleryType;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.Child;
import com.antew.redditinpictures.library.model.reddit.Comment;
import com.antew.redditinpictures.library.model.reddit.PostChild;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditApi;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.service.RedditServiceRetrofit;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.PostUtil;
import com.antew.redditinpictures.library.util.StringUtil;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends ImageViewerFragment implements SaveImageDialogFragment.SaveImageDialogListener, Observer<List<Child>> {

    @InjectView(R.id.lv_post_comments)
    ListView mPostComments;

    @InjectView(R.id.save_post)    ImageButton mSaveImageMenuItem;
    @InjectView(R.id.share_post)   ImageButton mSharePostMenuItem;
    @InjectView(R.id.upvote)       ImageButton mUpvoteMenuItem;
    @InjectView(R.id.downvote)     ImageButton mDownvoteMenuItem;
    @InjectView(R.id.view_post)    ImageButton mViewPostMenuItem;
    @InjectView(R.id.report_image) ImageButton mReportImageMenuItem;

    @Inject
    RedditServiceRetrofit mRedditService;

    private RedditCommentAdapter mCommentAdapter;

    private Observable<List<Child>> mCommentsObservable;

    private List<PostData> mAllPosts = new LinkedList<PostData>();
    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    @OnClick(R.id.save_post)
    public void saveImage() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.SAVE_POST, mImage.getSubreddit());
        handleSaveImage();
    }

    @OnClick(R.id.share_post)
    public void sharePost() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.SHARE_POST, mImage.getSubreddit());
        String subject = getString(R.string.check_out_this_image);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, subject + " " + mImage.getUrl());
        startActivity(Intent.createChooser(intent, getString(R.string.share_using_)));
    }

    @OnClick(R.id.view_post)
    public void viewPost() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.OPEN_POST_EXTERNAL, mImage.getSubreddit());
        Ln.i("View Post URL = " + mImage.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity())));
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mImage.getFullPermalink(SharedPreferencesHelper.getUseMobileInterface(getActivity()))));
        startActivity(browserIntent);
    }

    @OnClick(R.id.upvote)
    public void upVote() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.POST_VOTE, Constants.Analytics.Label.UP);
        handleVote(Vote.UP);
    }

    @OnClick(R.id.downvote)
    public void downVote() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.POST_VOTE, Constants.Analytics.Label.DOWN);
        handleVote(Vote.DOWN);
    }

    @OnClick(R.id.report_image)
    public void reportImage() {
        track(Constants.Analytics.Category.ACTION_BAR_ACTION, Constants.Analytics.Action.REPORT_POST, mImage.getSubreddit());
        new Thread(new Runnable() {
            @Override
            public void run() {
                reportCurrentItem();
            }
        }).start();
        Toast.makeText(getActivity(), R.string.image_display_issue_reported, Toast.LENGTH_LONG).show();
    }

    public void track(String category, String action, String label) {
        EasyTracker.getInstance(getActivity())
                   .send(MapBuilder.createEvent(category, action, label, null)
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mPostComments.setAdapter(mCommentAdapter);

        mSlidingUpPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override public void onPanelSlide(View view, float v) {}
            @Override public void onPanelCollapsed(View view) { }
            @Override public void onPanelAnchored(View view) { }
            @Override public void onPanelHidden(View view) { }

            @Override
            public void onPanelExpanded(View view) {
                Observable<List<Child>> postDataObservable;

                //  The can probably be greatly simplified
                postDataObservable = mRedditService.getComments(mImage.getSubreddit(), mImage.getId())
                        .map(redditApis -> redditApis.get(1).getData().getChildren())
                        .flatMap(children -> {
                                List<Child> mapped = new ArrayList<Child>();
                                Observable<List<Child>> map = Observable.from(children).map(child -> flattenList(child, 0));
                                map.subscribe(children1 -> mapped.addAll(children1));
                                return Observable.just(mapped);
                            }
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());

                postDataObservable.subscribe(postList -> onNext(postList));
            }

        });
        return v;
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

        mCommentAdapter = new RedditCommentAdapter(getActivity(), new ArrayList<Child>());
        displayVote();
    }

    public static List<Child> flattenList(Child child, int depth) {
        List<Child> posts = new LinkedList<Child>();

        child.setDepth(depth);
        posts.add(child);

        if (child instanceof PostChild || child instanceof Comment) {
            PostChild p = (PostChild) child;
            if (p.getData() != null && p.getData().getReplies() != null && p.getData().getReplies().getData() != null) {
                List<Child> children = p.getData().getReplies().getData().getChildren();
                for (Child c : children) {
                    posts.addAll(flattenList(c, depth + 1));
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

                if (mAlbum != null && mAlbum.getImages().size() > 1) {
                    mBtnViewGallery.post(new Runnable() {
                        @Override
                        public void run() {
                            mBtnViewGallery.setVisibility(View.VISIBLE);
                            mBtnViewGallery.setOnClickListener(getViewGalleryOnClickListener());
                        }
                    });
                }
            }
        }).start();
    }

    private OnClickListener getViewGalleryOnClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Ln.i("View Gallery");
                EasyTracker.getInstance(getActivity())
                           .send(MapBuilder.createEvent(Constants.Analytics.Category.UI_ACTION, Constants.Analytics.Action.OPEN_GALLERY,
                                                        Constants.Analytics.Label.IMGUR, (long) mAlbum.getImages().size()).build()
                                );
                Intent intent = new Intent(getActivity(), getImgurAlbumActivity());
                intent.putExtra(ImgurAlbumActivity.EXTRA_ALBUM, mAlbum);
                startActivity(intent);
            }
        };
    }

    public Class<? extends ImgurAlbumActivity> getImgurAlbumActivity() {
        return ImgurAlbumActivity.class;
    }

    @Subscribe
    public void downloadImage(DownloadImageEvent event) {
        Ln.e("Received downloadImage event in ImageDetailFragment!");
        if (!mImage.getPermalink().equals(event.getUniqueId())) {
            // One of the other fragments has been notified to download
            // their image, ignore.
            return;
        }

        mImageDownloader.downloadImage(mResolvedImageUrl, event.getFilename());
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
        downloadImage(new DownloadImageEvent(mImage.getPermalink(), filename));
    }

    public void handleVote(Vote vote) {
        if (!RedditLoginInformation.isLoggedIn()) {
            showLogin();
        } else {
            PostUtil.votePost(getActivity(), mImage, vote);
        }
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
}

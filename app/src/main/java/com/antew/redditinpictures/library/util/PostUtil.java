package com.antew.redditinpictures.library.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.model.Vote;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.service.RedditService;

public class PostUtil {
    public static void votePost(Context context, PostData postData, Vote vote) {
        new PostVoteTask(context, postData, vote).execute();
    }

    private static class PostVoteTask extends SafeAsyncTask<Void> {
        Context  mContext;
        PostData mPostData;
        Vote     mVote;

        public PostVoteTask(Context context, PostData postData, Vote vote) {
            mContext = context;
            mPostData = postData;
            mVote = vote;
        }

        @Override
        public Void call() throws Exception {
            if (!RedditLoginInformation.isLoggedIn()) {
                return null;
            }

            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = resolver.query(RedditContract.Posts.CONTENT_URI, null, "name = ?", new String[] {mPostData.getName()}, RedditContract.Posts.DEFAULT_SORT);
            cursor.moveToFirst();
            PostData entirePostData = new PostData(cursor);
            if (entirePostData.getVote() == null || entirePostData.getVote() == Vote.NEUTRAL) {
                switch (mVote) {
                    case UP:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.UP);
                        entirePostData.setVote(Vote.UP);
                        entirePostData.setScore(entirePostData.getScore() + 1);
                        break;
                    case DOWN:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.DOWN);
                        entirePostData.setVote(Vote.DOWN);
                        entirePostData.setScore(entirePostData.getScore() - 1);
                        break;
                }
            } else if (entirePostData.getVote() == Vote.UP) {
                switch (mVote) {
                    case UP:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.NEUTRAL);
                        entirePostData.setVote(Vote.NEUTRAL);
                        entirePostData.setScore(entirePostData.getScore() - 1);
                        break;
                    case DOWN:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.DOWN);
                        entirePostData.setVote(Vote.DOWN);
                        entirePostData.setScore(entirePostData.getScore() - 2);
                        break;
                }
            } else if (entirePostData.getVote() == Vote.DOWN) {
                switch (mVote) {
                    case UP:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.UP);
                        entirePostData.setVote(Vote.UP);
                        entirePostData.setScore(entirePostData.getScore() + 2);
                        break;
                    case DOWN:
                        RedditService.vote(mContext, entirePostData.getName(), Vote.NEUTRAL);
                        entirePostData.setVote(Vote.NEUTRAL);
                        entirePostData.setScore(entirePostData.getScore() + 1);
                        break;
                }
            }

            resolver.update(RedditContract.Posts.CONTENT_URI, entirePostData.getContentValues(), "name = ?", new String[] {entirePostData.getName()});

            return null;
        }
    }
}

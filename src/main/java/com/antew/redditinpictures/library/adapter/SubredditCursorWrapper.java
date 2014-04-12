package com.antew.redditinpictures.library.adapter;

import android.database.Cursor;
import android.database.CursorWrapper;

public class SubredditCursorWrapper extends CursorWrapper {

    public SubredditCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public boolean moveToLast() {
        return super.moveToLast();
    }

    @Override
    public boolean moveToPosition(int position) {
        // TODO Auto-generated method stub
        return super.moveToPosition(position);
    }

    @Override
    public int getPosition() {
        return super.getPosition();
    }
}

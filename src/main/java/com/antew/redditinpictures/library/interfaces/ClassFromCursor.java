package com.antew.redditinpictures.library.interfaces;

import android.database.Cursor;

public interface ClassFromCursor<T> {
    public T fromCursor(Cursor cursor);
}

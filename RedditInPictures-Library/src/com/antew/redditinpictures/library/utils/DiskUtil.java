package com.antew.redditinpictures.library.utils;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.antew.redditinpictures.library.logging.Log;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DiskUtil {

    public static final String TAG = DiskUtil.class.getSimpleName();

    /*
     * Suppressing lint for this function since it uses reflection to find out if the method exists
     */
    @SuppressLint("NewApi")
    public static File getPicturesDirectory() {
        File picturesDirectory = null;
        try {
            Method getPublicDir = Environment.class.getMethod("getExternalStoragePublicDirectory", new Class[] { String.class });
            getPublicDir.invoke(null, Environment.DIRECTORY_PICTURES);
            picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        } catch (NoSuchMethodException e) {
            picturesDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "redditinpictures" + File.separator);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getExternalStoragePublicDirectory", e);
        }

        if (picturesDirectory != null)
            picturesDirectory.mkdirs();

        return picturesDirectory;
    }
}

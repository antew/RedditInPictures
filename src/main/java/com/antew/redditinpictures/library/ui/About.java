package com.antew.redditinpictures.library.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.ui.base.BaseActivity;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.R;

public class About extends BaseActivity {
    private ImageView mImageView;
    private static final int MAX_TRIES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageView = (ImageView) findViewById(R.id.about_image);
        int size = Util.dpToPx(this, 100);
        mImageView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.market_icon, size, size));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap b = null;
        int i = 0;
        while (b == null && i < MAX_TRIES) {
            try {
                b = BitmapFactory.decodeResource(res, resId, options); 
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            }
            i++;
        }
        
        return b;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
}

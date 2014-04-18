package com.antew.redditinpictures.library.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.InjectView;
import com.actionbarsherlock.view.MenuItem;
import com.antew.redditinpictures.library.ui.base.BaseActivity;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.pro.R;
import com.squareup.picasso.Picasso;
import java.util.Calendar;

public class About extends BaseActivity {
    @InjectView(R.id.tv_version)
    protected TextView  mVersion;
    @InjectView(R.id.tv_copyright)
    protected TextView  mCopyright;
    @InjectView(R.id.about_image)
    protected ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int size = AndroidUtil.dpToPx(this, 100);

        Picasso.with(this)
               .load(R.drawable.market_icon)
               .resize(size, size)
               .placeholder(R.drawable.empty_photo)
               .error(R.drawable.error_photo)
               .into(mImageView);

        String version = getString(R.string.version_);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (packageInfo != null) {
                version += packageInfo.versionName + " (" + packageInfo.versionCode + ")";
            }
        } catch (PackageManager.NameNotFoundException e) {
            Ln.e(e, "Failed to get Package Info");
        }

        mVersion.setText(version);
        mCopyright.setText(getString(R.string.copyright_) + Calendar.getInstance().get(Calendar.YEAR));
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
}

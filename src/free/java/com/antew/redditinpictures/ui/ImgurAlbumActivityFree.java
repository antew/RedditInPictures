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
package com.antew.redditinpictures.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.antew.redditinpictures.adapter.ImgurAlbumPagerAdapterFree;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.util.ConstsFree;

public class ImgurAlbumActivityFree extends ImgurAlbumActivity implements UpdateToFullVersionDialogListener{

    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImgurAlbumPagerAdapterFree(getSupportFragmentManager(), getImages());   
    }
    
    @Override
    public void handleSaveImage() {
        DialogFragment upgrade = UpdateToFullVersionDialogFragment.newInstance();
        upgrade.show(getSupportFragmentManager(), ConstsFree.DIALOG_UPGRADE);
    }

    @Override
    public void onFinishUpgradeDialog() {
        if (!AndroidUtil.isUserAMonkey()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ConstsFree.MARKET_INTENT + ConstsFree.PRO_VERSION_PACKAGE));
            startActivity(intent);        
        }
    }
}

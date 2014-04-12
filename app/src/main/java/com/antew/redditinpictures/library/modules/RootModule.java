package com.antew.redditinpictures.library.modules;

import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import com.antew.redditinpictures.device.ScreenSize;
import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.annotation.ForApplication;
import com.antew.redditinpictures.library.util.MainThreadBus;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Module for all Android related provisions
 */
@Module(library = true)
public class RootModule {
    private final RedditInPicturesApplication application;

    public RootModule(RedditInPicturesApplication application) {
        this.application = application;
    }

    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides @ForApplication SharedPreferences provideDefaultSharedPreferences(@ForApplication final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides @ForApplication PackageInfo providePackageInfo(@ForApplication Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides @ForApplication TelephonyManager provideTelephonyManager(@ForApplication Context context) {
        return getSystemService(context, Context.TELEPHONY_SERVICE);
    }

    @SuppressWarnings("unchecked")
    public <T> T getSystemService(Context context, String serviceConstant) {
        return (T) context.getSystemService(serviceConstant);
    }

    @Provides @ForApplication InputMethodManager provideInputMethodManager(@ForApplication Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides @ForApplication ApplicationInfo provideApplicationInfo(@ForApplication final Context context) {
        return context.getApplicationInfo();
    }

    @Provides @ForApplication AccountManager provideAccountManager(@ForApplication final Context context) {
        return AccountManager.get(context);
    }

    @Provides @ForApplication ClassLoader provideClassLoader(@ForApplication final Context context) {
        return context.getClassLoader();
    }

    @Provides @ForApplication NotificationManager provideNotificationManager(@ForApplication final Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides @Singleton Bus provideOttoBus() {
        return new MainThreadBus(new Bus());
    }

    @Provides @Singleton ScreenSize provideScreenSize(@ForApplication final Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new ScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }
}

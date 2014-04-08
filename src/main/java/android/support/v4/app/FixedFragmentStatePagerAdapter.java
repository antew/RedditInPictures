package android.support.v4.app;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Fix this very annoying crash:
<br>E/AndroidRuntime(28989): FATAL EXCEPTION: main
<br>E/AndroidRuntime(28989): android.os.BadParcelableException: ClassNotFoundException when unmarshalling: com.antew.redditinpictures.library.model.reddit.RedditApi$PostData
<br>E/AndroidRuntime(28989):    at android.os.Parcel.readParcelable(Parcel.java:2077)
<br>E/AndroidRuntime(28989):    at android.os.Parcel.readValue(Parcel.java:1965)
<br>E/AndroidRuntime(28989):    at android.os.Parcel.readMapInternal(Parcel.java:2226)
<br>E/AndroidRuntime(28989):    at android.os.Bundle.unparcel(Bundle.java:223)
<br>E/AndroidRuntime(28989):    at android.os.Bundle.getSparseParcelableArray(Bundle.java:1232)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.FragmentManagerImpl.moveToState(SourceFile:805)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.FragmentManagerImpl.moveToState(SourceFile:1080)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.BackStackRecord.run(SourceFile:622)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.FragmentManagerImpl.execPendingActions(SourceFile:1416)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.FragmentManagerImpl.executePendingTransactions(SourceFile:431)
<br>E/AndroidRuntime(28989):    at android.support.v4.app.FragmentStatePagerAdapter.finishUpdate(SourceFile:160)
<br>E/AndroidRuntime(28989):    at android.support.v4.view.ViewPager.d(SourceFile:804)
<br>E/AndroidRuntime(28989):    at android.support.v4.view.ViewPager.e(SourceFile:1280)
<br>E/AndroidRuntime(28989):    at android.support.v4.view.ViewPager.computeScroll(SourceFile:1176)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12397)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12453)
<br>E/AndroidRuntime(28989):    at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:2911)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12345)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12453)
<br>E/AndroidRuntime(28989):    at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:2911)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12345)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12453)
<br>E/AndroidRuntime(28989):    at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:2911)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12345)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12453)
<br>E/AndroidRuntime(28989):    at android.view.ViewGroup.dispatchGetDisplayList(ViewGroup.java:2911)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12345)
<br>E/AndroidRuntime(28989):    at android.view.View.getDisplayList(View.java:12453)
<br>E/AndroidRuntime(28989):    at android.view.HardwareRenderer$GlRenderer.draw(HardwareRenderer.java:1168)
<br>E/AndroidRuntime(28989):    at android.view.ViewRootImpl.draw(ViewRootImpl.java:2149)
<br>E/AndroidRuntime(28989):    at android.view.ViewRootImpl.performDraw(ViewRootImpl.java:2021)
<br>E/AndroidRuntime(28989):    at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:1832)
<br>E/AndroidRuntime(28989):    at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1000)
<br>E/AndroidRuntime(28989):    at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:4214)
<br>E/AndroidRuntime(28989):    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:725)
<br>E/AndroidRuntime(28989):    at android.view.Choreographer.doCallbacks(Choreographer.java:555)
<br>E/AndroidRuntime(28989):    at android.view.Choreographer.doFrame(Choreographer.java:525)
<br>E/AndroidRuntime(28989):    at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:711)
<br>E/AndroidRuntime(28989):    at android.os.Handler.handleCallback(Handler.java:615)
<br>E/AndroidRuntime(28989):    at android.os.Handler.dispatchMessage(Handler.java:92)
<br>E/AndroidRuntime(28989):    at android.os.Looper.loop(Looper.java:137)
<br>E/AndroidRuntime(28989):    at android.app.ActivityThread.main(ActivityThread.java:4899)
<br>E/AndroidRuntime(28989):    at java.lang.reflect.Method.invokeNative(Native Method)
<br>E/AndroidRuntime(28989):    at java.lang.reflect.Method.invoke(Method.java:511)
<br>E/AndroidRuntime(28989):    at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:791)
<br>E/AndroidRuntime(28989):    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:558)
<br>E/AndroidRuntime(28989):    at dalvik.system.NativeStart.main(Native Method)

 * @see <a href="http://code.google.com/p/android/issues/detail?id=37484">Google code issue #37484</a>
 * @author Antew
 *
 */
public abstract class FixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    public FixedFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        Bundle savedFragmentState = f.mSavedFragmentState;
        if (savedFragmentState != null) {
            savedFragmentState.setClassLoader(f.getClass().getClassLoader());
        }
        return f;
    }

}

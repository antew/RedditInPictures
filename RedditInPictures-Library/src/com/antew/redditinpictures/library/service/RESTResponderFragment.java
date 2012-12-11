package com.antew.redditinpictures.library.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * This is from an article by Neil Goodman
 * @author Neil Goodman
 * @see <a href="http://neilgoodman.net/2012/01/01/modern-techniques-for-implementing-rest-clients-on-android-4-0-and-below-part-2/">The article</a>
 */
public abstract class RESTResponderFragment extends SherlockFragment {
    
    private ResultReceiver mReceiver;
    
    // We are going to use a constructor here to make our ResultReceiver,
    // but be careful because Fragments are required to have only zero-arg
    // constructors. Normally you don't want to use constructors at all
    // with Fragments.
    public RESTResponderFragment() {
        mReceiver = new ResultReceiver(new Handler()) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                int requestCode = resultData.getInt(RESTService.EXTRA_REQUEST_CODE, 0);
                
                if (resultData != null && resultData.containsKey(RESTService.REST_RESULT)) {
                    onRESTResult(resultCode, requestCode, resultData.getString(RESTService.REST_RESULT));
                }
                else {
                    onRESTResult(resultCode, requestCode, null);
                }
            }
            
        };
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This tells our Activity to keep the same instance of this
        // Fragment when the Activity is re-created during lifecycle
        // events. This is what we want because this Fragment should
        // be available to receive results from our RESTService no
        // matter what the Activity is doing.
        setRetainInstance(true);
    }
    
    public ResultReceiver getResultReceiver() {
        return mReceiver;
    }

    // Implementers of this Fragment will handle the result here.
    abstract public void onRESTResult(int code, int requestCode, String result);
}

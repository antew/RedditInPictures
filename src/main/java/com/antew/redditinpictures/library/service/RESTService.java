package com.antew.redditinpictures.library.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.antew.redditinpictures.library.utils.Consts;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * This is based an article by Neil Goodman, I've added the EXTRA_REQUEST_CODE so that callers can
 * have multiple request types
 * 
 * @author Neil Goodman
 * @see <a
 *      href="http://neilgoodman.net/2012/01/01/modern-techniques-for-implementing-rest-clients-on-android-4-0-and-below-part-2/">The
 *      article</a>
 */
public class RESTService extends IntentService {
    private static final String TAG                = RESTService.class.getSimpleName();

    public static final int     GET                = 0x1;
    public static final int     POST               = 0x2;
    public static final int     PUT                = 0x3;
    public static final int     DELETE             = 0x4;

    public static final String  EXTRA_BUNDLE       = "EXTRA_BUNDLE";
    public static final String  EXTRA_HTTP_VERB    = "EXTRA_HTTP_VERB";
    public static final String  EXTRA_PARAMS       = "EXTRA_PARAMS";
    public static final String  EXTRA_COOKIE       = "EXTRA_COOKIE";
    public static final String  EXTRA_REPLACE_ALL  = "EXTRA_REPLACE_ALL";
    public static final String  EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE";
    public static final String  EXTRA_RESULT       = "EXTRA_RESULT";
    public static final String  EXTRA_STATUS_CODE  = "EXTRA_STATUS_CODE";
    public static final String  EXTRA_USER_AGENT   = "EXTRA_USER_AGENT";
    public static final String  EXTRA_PASS_THROUGH = "EXTRA_PASS_THROUGH";

    public static final String  REST_RESULT        = "REST_RESULT";

    public RESTService() {
        super(TAG);
        Log.i(TAG, "constructor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");

        Uri action = intent.getData();
        Bundle extras = intent.getExtras();

        if (extras == null || action == null) {
            Log.e(TAG, "You did not pass extras or data with the Intent.");
            return;
        }

        // We default to GET if no verb was specified.
        int verb = extras.getInt(EXTRA_HTTP_VERB, GET);
        RequestCode requestCode = (RequestCode) extras.getSerializable(EXTRA_REQUEST_CODE);
        Bundle params = extras.getParcelable(EXTRA_PARAMS);
        String cookie = extras.getString(EXTRA_COOKIE);
        String userAgent = extras.getString(EXTRA_USER_AGENT);
        boolean replaceAll = extras.getBoolean(EXTRA_REPLACE_ALL, false);

        // Items in this bundle are simply passed on in onRequestComplete
        Bundle passThrough = extras.getBundle(EXTRA_PASS_THROUGH);

        HttpEntity responseEntity = null;
        Intent result = new Intent(Consts.BROADCAST_HTTP_FINISHED);
        result.putExtra(EXTRA_PASS_THROUGH, passThrough);
        Bundle resultData = new Bundle();

        try {
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request = null;

            // Let's build our request based on the HTTP verb we were
            // given.
            switch (verb) {
                case GET: {
                    request = new HttpGet();
                    attachUriWithQuery(request, action, params);
                }
                    break;

                case DELETE: {
                    request = new HttpDelete();
                    attachUriWithQuery(request, action, params);
                }
                    break;

                case POST: {
                    request = new HttpPost();
                    request.setURI(new URI(action.toString()));

                    // Attach form entity if necessary. Note: some REST APIs
                    // require you to POST JSON. This is easy to do, simply use
                    // postRequest.setHeader('Content-Type', 'application/json')
                    // and StringEntity instead. Same thing for the PUT case
                    // below.
                    HttpPost postRequest = (HttpPost) request;

                    if (params != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
                        postRequest.setEntity(formEntity);
                    }
                }
                    break;

                case PUT: {
                    request = new HttpPut();
                    request.setURI(new URI(action.toString()));

                    // Attach form entity if necessary.
                    HttpPut putRequest = (HttpPut) request;

                    if (params != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
                        putRequest.setEntity(formEntity);
                    }
                }
                    break;
            }

            if (request != null) {
                DefaultHttpClient client = new DefaultHttpClient();

                // GZip requests
                // antew on 3/12/2014 - Disabling GZIP for now, need to figure this CloseGuard error:
                // 03-12 21:02:09.248    9674-9683/com.antew.redditinpictures.pro E/StrictMode﹕ A resource was acquired at attached stack trace but never released. See java.io.Closeable for information on avoiding resource leaks.
                //         java.lang.Throwable: Explicit termination method 'end' not called
                // at dalvik.system.CloseGuard.open(CloseGuard.java:184)
                // at java.util.zip.Inflater.<init>(Inflater.java:82)
                // at java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:96)
                // at java.util.zip.GZIPInputStream.<init>(GZIPInputStream.java:81)
                // at com.antew.redditinpictures.library.service.RESTService$GzipDecompressingEntity.getContent(RESTService.java:346)
                client.addRequestInterceptor(getGzipRequestInterceptor());
                client.addResponseInterceptor(getGzipResponseInterceptor());

                if (cookie != null)
                    request.addHeader("Cookie", cookie);

                if (userAgent != null)
                    request.addHeader("User-Agent", RedditService.USER_AGENT);

                // Let's send some useful debug information so we can monitor things
                // in LogCat.
                Log.d(TAG, "Executing request: " + verbToString(verb) + ": " + action.toString());

                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this thread.
                HttpResponse response = client.execute(request);

                responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int statusCode = responseStatus != null ? responseStatus.getStatusCode() : 0;

                if (responseEntity != null) {
                    resultData.putString(REST_RESULT, EntityUtils.toString(responseEntity));
                    resultData.putSerializable(EXTRA_REQUEST_CODE, requestCode);
                    resultData.putInt(EXTRA_STATUS_CODE, statusCode);
                    resultData.putBoolean(EXTRA_REPLACE_ALL, replaceAll);

                    result.putExtra(EXTRA_BUNDLE, resultData);

                    onRequestComplete(result);

                } else {
                    onRequestFailed(result, statusCode);
                }
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. " + verbToString(verb) + ": " + action.toString(), e);
            onRequestFailed(result, 0);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            onRequestFailed(result, 0);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            onRequestFailed(result, 0);
        } catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            onRequestFailed(result, 0);
        } finally {
            if (responseEntity != null)
                try {
                    responseEntity.consumeContent();
                } catch (IOException ignored) {
                }
        }
    }

    public void onRequestFailed(Intent result, int statusCode) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_STATUS_CODE, statusCode);
        result.putExtra(EXTRA_BUNDLE, args);

        onRequestComplete(result);
    }

    public void onRequestComplete(Intent result) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(result);
    }

    private static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            } else {
                Uri.Builder uriBuilder = uri.buildUpon();

                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : paramsToList(params)) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }

                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: " + uri.toString(), e);
        }
    }

    private static String verbToString(int verb) {
        switch (verb) {
            case GET:
                return "GET";

            case POST:
                return "POST";

            case PUT:
                return "PUT";

            case DELETE:
                return "DELETE";
        }

        return "";
    }

    private static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());

        for (String key : params.keySet()) {
            Object value = params.get(key);

            // We can only put Strings in a form entity, so we call the toString()
            // method to enforce. We also probably don't need to check for null here
            // but we do anyway because Bundle.get() can return null.
            if (value != null)
                formList.add(new BasicNameValuePair(key, value.toString()));
        }

        return formList;
    }

    /**
     * From apache examples
     * 
     * @see <a
     *      href="http://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientGZipContentCompression.java">Apache
     *      examples</a>
     * @return
     */
    private HttpRequestInterceptor getGzipRequestInterceptor() {
        return new HttpRequestInterceptor() {

            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        };
    }

    /**
     * From apache examples
     * 
     * @see <a
     *      href="http://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientGZipContentCompression.java">Apache
     *      examples</a>
     * @return
     */
    private HttpResponseInterceptor getGzipResponseInterceptor() {
        return new HttpResponseInterceptor() {

            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++) {
                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * @see <a
     *      href="http://svn.apache.org/repos/asf/httpcomponents/httpcore/branches/4.2.x/httpcore-contrib/src/main/java/org/apache/http/contrib/compress/GzipDecompressingEntity.java">Apache
     *      project</a>
     * 
     */
    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

}

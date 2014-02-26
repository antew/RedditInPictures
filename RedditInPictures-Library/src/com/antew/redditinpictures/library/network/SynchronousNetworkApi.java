package com.antew.redditinpictures.library.network;

import android.os.Build;

import com.antew.redditinpictures.library.logging.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SynchronousNetworkApi {
    private static final int   IO_BUFFER_SIZE = 8 * 1024;
    public static final String TAG            = SynchronousNetworkApi.class.getSimpleName();

    /**
     * Download the input URL and return the output as a String
     * 
     * @param urlString
     *            The URL to download
     * @return A String with the downloaded contents
     * @throws IOException
     */
    public static String downloadUrl(String urlString) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try {

            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);

                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder builder = new StringBuilder(in.available());
                String line;
                while ((line = reader.readLine()) != null)
                    builder.append(line);

                Log.i("Url = " + urlString, " result = " + builder.toString());
                return builder.toString();
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadUrl - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return null;
    }

    public static byte[] downloadUrlToByteArray(String urlString) {
        disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
                
                return streamToBytes(in);
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadUrl - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // Ignored
                }
            }

        }
        return null;
    }

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

}

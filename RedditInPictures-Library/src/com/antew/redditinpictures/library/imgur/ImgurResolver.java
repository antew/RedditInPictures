package com.antew.redditinpictures.library.imgur;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;

import com.antew.redditinpictures.library.BuildConfig;
import com.antew.redditinpictures.library.gson.BooleanDeserializer;
import com.antew.redditinpictures.library.imageapi.Flickr;
import com.antew.redditinpictures.library.imageapi.Flickr.FlickrSize;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.utils.ImageContainer;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.ImageUtil.ImageType;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class ImgurResolver {
    public static final String  TAG                   = "ImgurResolver";
    private static final String JSON                  = ".json";
    private static final String URL_IMGUR_ALBUM_API   = "http://api.imgur.com/2/album/";
    private static final String URL_IMGUR_IMAGE_API   = "http://api.imgur.com/2/image/";
    private static final String URL_IMGUR_GALLERY_API = "http://imgur.com/gallery/";
    private static final String FLICKR_URL            = "http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key=d71a06b84331ac6428ba90d1b72a9b6f&photo_id=%s&format=json&nojsoncallback=1";
    private static final String EHOST_URL             = "http://i.eho.st/%s.jpg";
    private static final String PICSARUS_URL          = "http://www.picsarus.com/%s.jpg";

    public enum ImageSize {
        ORIGINAL, SMALL_SQUARE, LARGE_THUMBNAIL;
    };

    /**
     * Resolve the image given, this method should only be called outside of the main thread!
     * 
     * @param url
     *            The URL to resolve.
     * @return An ImageContainer with the decoded image.
     */
    public static ImageContainer resolve(String url) {
        Log.i(TAG, "resolve - URL = " + url);
        ImageContainer container = null;
        switch (ImageUtil.getImageType(url)) {

            case IMGUR_IMAGE:
                container = resolveImgurImage(url);
                break;

            case IMGUR_ALBUM:
                container = resolveImgurAlbum(url);
                break;

            case OTHER_SUPPORTED_IMAGE:
                container = new ImageContainer(url);
                break;

            case IMGUR_GALLERY:
                container = resolveImgurGallery(url);
                break;

            case UNSUPPORTED_IMAGE:
                break;

            case DEVIANTART_IMAGE:
                break;

            case EHOST_IMAGE:
                String hash = getHash(url, ImageType.EHOST_IMAGE);
                if (hash != null) {
                    container = new ImageContainer(String.format(EHOST_URL, hash));
                    Log.i("HASH = ", hash + "url = " + url);
                }
                break;

            case FLICKR_IMAGE:
                container = resolveFlickrImage(url);
                break;

            case LIVEMEME_IMAGE:
                break;

            case MEMECRUNCH_IMAGE:
                break;

            case MEMEFIVE_IMAGE:
                break;

            case MINUS_IMAGE:
                break;

            case PICASARUS_IMAGE:
                
                break;

            case PICSHD_IMAGE:
                break;

            case QUICKMEME_IMAGE:
                break;

            case SNAGGY_IMAGE:
                break;

            case STEAM_IMAGE:
                break;

            case TUMBLR_IMAGE:
                break;

            default:
                break;
        }

        return container;
    }

    /**
     * Return the URL for the requested image size.
     * 
     * @param image
     *            The image container from which the link to the specified size image will be
     *            returned
     * @param size
     *            The size of the image to return
     * @return The url to the image of the requested size
     */
    public static String getSize(ImageContainer image, ImageSize size) {
        if (image == null) {
            if (BuildConfig.DEBUG)
                throw new NullPointerException();
            else
            return null;
        }

        ImgurImage decoded = null;
        switch (image.getImageType()) {
            case IMGUR_ALBUM:
                decoded = image.getImgurAlbum().getImages().get(0);
                break;

            case IMGUR_IMAGE:
                decoded = image.getImgurImage();
                break;

            case FLICKR_IMAGE:
                FlickrSize flickrSize = FlickrSize.ORIGINAL;
                if (ImageSize.SMALL_SQUARE.equals(size))
                    flickrSize = FlickrSize.THUMBNAIL;

                return image.getFlickr().getSize(flickrSize).getSource();

            case OTHER_SUPPORTED_IMAGE:
            default:
                return image.getUrl();
        }

        switch (size) {
            case SMALL_SQUARE:
                return decoded.getLinks().getSmall_square();
            case LARGE_THUMBNAIL:
                return decoded.getLinks().getLarge_thumbnail();
            case ORIGINAL:
                return decoded.getLinks().getOriginal();
        }

        return null;
    }

    public static ImageContainer resolveImgurImage(String url) {
        ImageContainer container = null;
        ImgurImageApi image = null;
        String hash = getHash(url, ImageType.IMGUR_IMAGE);
        if (hash != null)
            image = resolveImgurImageFromHash(hash);

        if (image != null && image.getImage() != null)
            container = new ImageContainer(image.getImage());

        return container;
    }

    /**
     * 
     * @param url
     *            The url to extract the hash from
     * @param type
     *            The {@link ImageType} of image the URL represents
     * @return
     */
    public static String getHash(String url, ImageType type) {
        String hash = null;
        Pattern pattern = null;

        switch (type) {
            case IMGUR_IMAGE:
                pattern = Pattern.compile("imgur.com/(?:gallery/)?([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);

                break;

            case IMGUR_ALBUM:
                pattern = Pattern.compile("imgur.com/a/([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
                break;

            case IMGUR_GALLERY:
                pattern = Pattern.compile("imgur.com/gallery/([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
                break;

            case EHOST_IMAGE:
                pattern = Pattern.compile("^http://(?:i\\.)?(?:\\d+.)?eho.st/(\\w+)/?");
                break;

            case OTHER_SUPPORTED_IMAGE:
                if (BuildConfig.DEBUG)
                    throw new UnsupportedOperationException("Unable to get a hash from a non-imgur image URL!");

                break;

            case UNSUPPORTED_IMAGE:
                if (BuildConfig.DEBUG)
                    throw new UnsupportedOperationException("Unable to get a hash from an unsupported image URL!");
                break;

            case DEVIANTART_IMAGE:
                break;

            case FLICKR_IMAGE:
                pattern = Pattern.compile("^http://(?:\\w+).?flickr.com/(?:.*)/([\\d]{10})/?(?:.*)?$");
                break;

            case LIVEMEME_IMAGE:
                break;

            case MEMECRUNCH_IMAGE:
                break;

            case MEMEFIVE_IMAGE:
                break;

            case MINUS_IMAGE:
                break;

            case PICASARUS_IMAGE:
                pattern = Pattern.compile("^https?://(?:[i.]|[edge.]|[www.])*picsarus.com/(?:r/[\\w]+/)?([\\w]{6,})(\\..+)?$");
                break;

            case PICSHD_IMAGE:
                break;

            case QUICKMEME_IMAGE:
                break;

            case SNAGGY_IMAGE:
                break;

            case STEAM_IMAGE:
                break;

            case TUMBLR_IMAGE:
                break;

            default:
                break;
        }

        if (pattern != null) {
            Matcher m = pattern.matcher(url);
            while (m.find())
                hash = m.group(1);
        }

        Log.i(TAG, "getHash(" + url + ", " + type.name() + ") returning " + hash);
        return hash;
    }

    public static ImgurImageApi resolveImgurImageFromHash(String hash) {
        Gson gson = new Gson();
        String json = null;
        ImgurImageApi image = null;

        String newUrl = URL_IMGUR_IMAGE_API + hash + JSON;
        if (ImgurApiCache.getInstance().containsImgurImage(hash)) {
            Log.i(TAG, "cache - resolveImgurImageFromHash - " + hash + " found in cache");
            image = ImgurApiCache.getInstance().getImgurImage(hash);
        } else {
            Log.i(TAG, "cache - resolveImgurImageFromHash - " + hash + " NOT found in cache");
            try {
                json = downloadUrl(newUrl);

                if (json == null)
                    return null;

                image = gson.fromJson(json, ImgurImageApi.class);
                ImgurApiCache.getInstance().addImgurImage(hash, image);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "resolveImgurImageFromHash", e);
            } catch (IOException e) {
                Log.e(TAG, "resolveImgurImageFromHash", e);
            }
        }

        return image;
    }

    public static ImageContainer resolveImgurAlbum(String url) {
        ImageContainer container = null;
        ImgurAlbumApi album = null;
        String hash = getHash(url, ImageType.IMGUR_ALBUM);
        if (hash != null)
            album = resolveImgurAlbumFromHash(hash);

        if (album != null && album.getAlbum() != null)
            container = new ImageContainer(album.getAlbum());

        return container;
    }

    public static ImgurAlbumApi resolveImgurAlbumFromHash(String hash) {
        if (hash == null)
            Log.e(TAG, "resolveImgurAlbumFromHash - hash was null");

        Log.i(TAG, "resolveImgurAlbumFromHash, hash = " + hash);
        ImgurAlbumApi album = null;
        Gson gson = new Gson();
        String json = null;
        String newUrl = URL_IMGUR_ALBUM_API + hash + JSON;

        if (ImgurApiCache.getInstance().containsImgurAlbum(hash)) {
            Log.i(TAG, "cache - resolveImgurAlbumFromHash - " + hash + " found in cache");
            album = ImgurApiCache.getInstance().getImgurAlbum(hash);
        } else {
            Log.i(TAG, "cache - resolveImgurAlbumFromHash - " + hash + " NOT found in cache");
            try {
                json = downloadUrl(newUrl);

                if (json == null)
                    return null;

                album = gson.fromJson(json, ImgurAlbumApi.class);
                ImgurApiCache.getInstance().addImgurAlbum(hash, album);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "resolveImgurAlbumFromHash", e);
            } catch (IOException e) {
                Log.e(TAG, "resolveImgurAlbumFromHash", e);
            }
        }

        return album;
    }

    private static ImageContainer resolveImgurGallery(String url) {
        ImgurGallery gallery = null;
        ImageContainer container = null;

        String hash = getHash(url, ImageType.IMGUR_GALLERY);
        if (hash != null)
            gallery = getImgurGalleryFromHash(hash);

        if (gallery != null && gallery.getImageContainer() != null)
            container = gallery.getImageContainer();

        return container;

    }

    private static ImgurGallery getImgurGalleryFromHash(String hash) {
        ImgurGallery gallery = null;
        ImgurAlbumApi album = null;
        ImgurImageApi image = null;
        ImgurApiCache cache = ImgurApiCache.getInstance();
        String newUrl = URL_IMGUR_GALLERY_API + hash + JSON;

        if (cache.containsImgurGallery(hash)) {
            Log.i(TAG, "cache - getImgurGalleryFromHash - " + hash + " found in cache");
            gallery = cache.getImgurGallery(hash);
        } else {
            Log.i(TAG, "cache - getImgurGalleryFromHash - " + hash + " NOT found in cache");

            try {
                Gson gson = new Gson();
                String json = downloadUrl(newUrl);

                if (json == null)
                    return null;

                gallery = gson.fromJson(json, ImgurGallery.class);

                // Resolve the image as a standard ImgurImage or an Album
                if (gallery.isSuccess()) {
                    String imageHash = gallery.getData().getImage().getHash();

                    if (gallery.getData().getImage().isAlbum()) {
                        album = resolveImgurAlbumFromHash(imageHash);
                        if (album != null && album.getAlbum() != null)
                            gallery.setImageContainer(new ImageContainer(album.getAlbum()));

                    } else {
                        image = resolveImgurImageFromHash(imageHash);
                        if (image != null && image.getImage() != null)
                            gallery.setImageContainer(new ImageContainer(image.getImage()));
                    }

                    cache.addImgurGallery(hash, gallery);
                } else {
                    Log.e(TAG, "resolveImgurGallery, error resolving image");
                }

            } catch (JsonSyntaxException e) {
                Log.e(TAG, "resolveImgurGallery", e);
            } catch (IOException e) {
                Log.e(TAG, "resolveImgurGallery", e);
            }
        }

        return gallery;
    }

    public static ImageContainer resolveFlickrImage(String url) {
        ImageContainer container = null;
        Flickr flickr = null;

        String hash = getHash(url, ImageType.FLICKR_IMAGE);
        if (hash != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Boolean.class, new BooleanDeserializer()).create();
            try {
                String json = downloadUrl(String.format(FLICKR_URL, hash));
                flickr = gson.fromJson(json, Flickr.class);
            } catch (IOException e) {
                Log.e(TAG, "Error parsing JSON in resolveFlickrImage", e);
            }

        }

        if (flickr != null)
            container = new ImageContainer(flickr);

        return container;
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private static String downloadUrl(String urlString) throws IOException {
        disableConnectionReuseIfNecessary();
        InputStream stream = null;
        String json = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            stream = new BufferedInputStream(conn.getInputStream());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                json = StringUtil.convertStreamToString(stream);
        } catch (IOException e) {
            String status = "null";
            if (conn != null)
                status = Integer.toString(conn.getResponseCode());
            Log.e(TAG, "Error in downloadUrl, connection returned status code = " + status, e);
        } finally {
            if (conn != null)
                conn.disconnect();

            if (stream != null)
                stream.close();
        }

        return json;
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

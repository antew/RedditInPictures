package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;
import com.antew.redditinpictures.library.imgur.ImgurApiCache;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.utils.Ln;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ImgurImageType extends Image {
    private static final String     URL_IMGUR_IMAGE_API = "http://api.imgur.com/2/image/";
    private static final String     URL_REGEX           = "imgur.com/(?:gallery/)?([A-Za-z0-9]+)";
    private              ImgurImage mImgurImage         = null;

    public ImgurImageType(String url) {
        super(url);
        mImgurImage = resolve();
    }

    /**
     * Resolve the imgur image for the input URL
     *
     * @return An {@link ImgurImage}
     */
    private ImgurImage resolve() {
        ImgurImage imgurImage = resolveImgurImageFromHash(getHash());

        return imgurImage;
    }

    /**
     * Resolve an {@link ImgurImage} from an Imgur hash
     *
     * @param hash
     *     The hash to resolve an image from (e.g. u9PWV)
     *
     * @return An {@link ImgurImage} representing the image
     */
    public static ImgurImage resolveImgurImageFromHash(String hash) {
        if (hash == null) {
            Ln.e("Received null hash in resolveImgurImageFromHash");
            return null;
        }

        Gson gson = new Gson();
        String json = null;
        ImgurImageApi imgurImageApi = null;
        ImgurImage imgurImage = null;

        String apiUrl = URL_IMGUR_IMAGE_API + hash + Constants.JSON;
        if (ImgurApiCache.getInstance().containsImgurImage(hash)) {
            Ln.i("cache - resolveImgurImageFromHash - %s found in cache", hash);
            imgurImageApi = ImgurApiCache.getInstance().getImgurImage(hash);
        } else {
            Ln.i("cache - resolveImgurImageFromHash - %s NOT found in cache", hash);
            try {
                json = SynchronousNetworkApi.downloadUrl(apiUrl);

                if (json == null) {
                    return null;
                }

                imgurImageApi = gson.fromJson(json, ImgurImageApi.class);
                ImgurApiCache.getInstance().addImgurImage(hash, imgurImageApi);
            } catch (JsonSyntaxException e) {
                Ln.e(e, "resolveImgurImageFromHash");
            }
        }

        if (imgurImageApi != null) {
            imgurImage = imgurImageApi.getImage();
        }

        return imgurImage;
    }

    @Override
    public String getSize(ImageSize size) {
        assert size != null : "ImageSize must not be null";

        if (mImgurImage == null) {
            mImgurImage = resolve();
        }

        String decodedUrl = null;

        if (mImgurImage != null) {
            decodedUrl = mImgurImage.getSize(size);
        }

        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.IMGUR_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}

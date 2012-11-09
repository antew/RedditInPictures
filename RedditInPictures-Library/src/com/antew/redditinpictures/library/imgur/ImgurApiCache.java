package com.antew.redditinpictures.library.imgur;

import java.util.HashMap;

public class ImgurApiCache {
    private static ImgurApiCache                  instance    = null;
    private static HashMap<String, ImgurImageApi> imgurImages = null;
    private static HashMap<String, ImgurAlbumApi> imgurAlbums = null;
    private static HashMap<String, ImgurGallery>  imgurGalleries = null;

    private ImgurApiCache() {
    }

    public boolean containsImgurImage(String url) {
        return imgurImages != null && imgurImages.containsKey(url);
    }

    public boolean containsImgurAlbum(String url) {
        return imgurAlbums != null && imgurAlbums.containsKey(url);
    }
    
    public boolean containsImgurGallery(String url) {
        return imgurGalleries != null && imgurGalleries.containsKey(url);
    }

    public void addImgurImage(String url, ImgurImageApi image) {
        if (imgurImages == null) {
            imgurImages = new HashMap<String, ImgurImageApi>();
        }

        imgurImages.put(url, image);
    }

    public void addImgurAlbum(String url, ImgurAlbumApi album) {
        if (imgurAlbums == null) {
            imgurAlbums = new HashMap<String, ImgurAlbumApi>();
        }

        imgurAlbums.put(url, album);
    }
    
    public void addImgurGallery(String url, ImgurGallery album) {
        if (imgurGalleries == null) {
            imgurGalleries = new HashMap<String, ImgurGallery>();
        }
        
        imgurGalleries.put(url, album);
    }

    public ImgurImageApi getImgurImage(String url) {
        ImgurImageApi retVal = null;

        if (imgurImages != null)
            retVal = imgurImages.get(url);

        return retVal;
    }

    public ImgurAlbumApi getImgurAlbum(String url) {
        ImgurAlbumApi retVal = null;

        if (imgurAlbums != null)
            retVal = imgurAlbums.get(url);

        return retVal;
    }
    
    public ImgurGallery getImgurGallery(String url) {
        ImgurGallery retVal = null;
        
        if (imgurGalleries != null)
            retVal = imgurGalleries.get(url);
        
        return retVal;
    }

    public static synchronized ImgurApiCache getInstance() {
        if (instance == null) {
            instance = new ImgurApiCache();
        }

        return instance;
    }

}

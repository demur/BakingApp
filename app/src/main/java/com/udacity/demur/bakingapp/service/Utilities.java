package com.udacity.demur.bakingapp.service;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

public class Utilities {
    private VideoRequestHandler videoRequestHandler;
    private static final Object LOCK = new Object();
    private static Picasso picassoInstance;

    /*
     * This function is the product of gar at https://stackoverflow.com/a/4009133
     * suggested to use by Udacity to implement network connection check
     * */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static int getGridLayoutColumnCount(Context context) {
        boolean isLandscape = false;
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            isLandscape = true;
        switch (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                if (isLandscape)
                    return 3;
                else
                    return 2;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
            default:
                if (isLandscape)
                    return 2;
                else
                    return 1;
        }
    }

    public static class VideoRequestHandler extends RequestHandler {
        public static String SCHEME_VIDEO = "video";

        @Override
        public boolean canHandleRequest(Request data) {
            String scheme = data.uri.getScheme();
            return (SCHEME_VIDEO.equals(scheme));
        }

        @Override
        public Result load(Request data, int arg1) throws IOException {
            Bitmap bm = ThumbnailUtils.createVideoThumbnail(data.uri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            return new Result(bm, Picasso.LoadedFrom.DISK);
        }
    }

    public static Picasso getPicassoThumbnailFetcherInstance(Context context) {
        if (null == picassoInstance) {
            synchronized (LOCK) {
                picassoInstance = new Picasso.Builder(context.getApplicationContext())
                        .addRequestHandler(new VideoRequestHandler())
                        .build();
            }
        }
        return picassoInstance;
    }
}
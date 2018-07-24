package com.udacity.demur.bakingapp.service;

import android.content.Context;
import android.util.Log;

import com.udacity.demur.bakingapp.R;

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = RetrofitClient.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static BakingJsonClient sInstance;
    private static final Object OKHTTP_LOCK = new Object();
    private static OkHttpClient sOkHttpInstance;

    public static BakingJsonClient getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new RetrofitClient instance");
                try {
                    URL url = new URL(context.getResources().getString(R.string.json_recipe_source));
                    String baseUrl = url.getProtocol() + "://" + url.getHost();
                    sInstance = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(getOkHttpInstance())
                            .build().create(BakingJsonClient.class);
                } catch (MalformedURLException e) {
                    Log.e(TAG, "getInstance: encountered problem with URL for recipes data");
                }
            }
        }
        Log.d(TAG, "Getting the RetrofitClient instance");
        return sInstance;
    }

    public static OkHttpClient getOkHttpInstance() {
        if (null == sOkHttpInstance) {
            synchronized (OKHTTP_LOCK) {
                sOkHttpInstance = new OkHttpClient.Builder().build();
            }
        }
        return sOkHttpInstance;
    }
}
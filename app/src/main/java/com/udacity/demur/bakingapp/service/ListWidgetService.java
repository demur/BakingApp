package com.udacity.demur.bakingapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.demur.bakingapp.R;
import com.udacity.demur.bakingapp.model.RecipeIngredient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_LAST_SEEN_INGREDIENTS_KEY;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_NAME;
import static com.udacity.demur.bakingapp.service.Utilities.getBulletItem;

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private List<RecipeIngredient> mList;
    private SharedPreferences mSharedPrefs;
    private Type listIngredientType = new TypeToken<List<RecipeIngredient>>() {}.getType();

    ListRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
        mSharedPrefs = applicationContext.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        String jsonString = mSharedPrefs.getString(SHARED_PREFS_LAST_SEEN_INGREDIENTS_KEY, "");
        if (!TextUtils.isEmpty(jsonString)) {
            mList = new Gson().fromJson(jsonString, listIngredientType);
        } else {
            mList = null;
        }
    }

    @Override
    public void onDestroy() {
        mList = null;
    }

    @Override
    public int getCount() {
        return null == mList ? 0 : mList.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        if (null == mList || mList.size() == 0 || i == AdapterView.INVALID_POSITION || i < 0 || i >= mList.size()) {
            return null;
        }
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
        RecipeIngredient ingredient = mList.get(i);
        remoteViews.setTextViewText(android.R.id.text1, getBulletItem(
                mContext.getString(R.string.ingredient_item,
                        ingredient.getIngredient(),
                        ingredient.getQuantity() % 1.0 == 0 ? String.format(Locale.getDefault(), "%.0f", ingredient.getQuantity()) : String.format("%s", ingredient.getQuantity()),
                        ingredient.getMeasure())
        ));
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
package com.udacity.demur.bakingapp.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.SpannableString;
import android.text.style.BulletSpan;
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

public class ListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    List<RecipeIngredient> mList;
    SharedPreferences mSharedPrefs;
    private Type listIngredientType = new TypeToken<List<RecipeIngredient>>() {
    }.getType();

    public ListRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
        mSharedPrefs = applicationContext.getSharedPreferences("Settings", MODE_PRIVATE);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        String jsonString = mSharedPrefs.getString("last_seen_recipe_ingredients_json", "");
        if (!jsonString.isEmpty()) {
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
        if (null == mList || mList.size() == 0 || i == AdapterView.INVALID_POSITION || i < 0 || i >= mList.size())
            return null;
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), android.R.layout.simple_list_item_1);
        RecipeIngredient ingredient = mList.get(i);
        CharSequence ingredientItem = mContext.getString(R.string.ingredient_item,
                ingredient.getIngredient(),
                ingredient.getQuantity() % 1.0 == 0 ? String.format(Locale.getDefault(), "%.0f", ingredient.getQuantity()) : String.format("%s", ingredient.getQuantity()),
                ingredient.getMeasure());
        SpannableString spannableString = new SpannableString(ingredientItem);
        spannableString.setSpan(new BulletSpan(15), 0, ingredientItem.length(), 0);
        remoteViews.setTextViewText(android.R.id.text1, spannableString);
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
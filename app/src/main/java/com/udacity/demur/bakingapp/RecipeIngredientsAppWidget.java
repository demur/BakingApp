package com.udacity.demur.bakingapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.udacity.demur.bakingapp.service.ListWidgetService;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_LAST_SEEN_RECIPE_KEY;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_NAME;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeIngredientsAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = getListViewRemoteView(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    private static RemoteViews getListViewRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_ingredients_app_widget);
        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.appwidget_list_view, intent);
        views.setEmptyView(R.id.appwidget_list_view, R.id.empty_list);
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        String recipeName = sharedPrefs.getString(SHARED_PREFS_LAST_SEEN_RECIPE_KEY, "");
        if (!TextUtils.isEmpty(recipeName)) {
            views.setTextViewText(R.id.appwidget_title, context.getString(R.string.appwidget_title, recipeName));
            views.setViewVisibility(R.id.appwidget_title, VISIBLE);
        } else {
            views.setViewVisibility(R.id.appwidget_title, INVISIBLE);
        }
        return views;
    }
}
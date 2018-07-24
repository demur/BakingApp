package com.udacity.demur.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.demur.bakingapp.adapter.RecipeAdapter;
import com.udacity.demur.bakingapp.model.Recipe;
import com.udacity.demur.bakingapp.service.BakingJsonClient;
import com.udacity.demur.bakingapp.service.RetrofitClient;
import com.udacity.demur.bakingapp.service.Utilities;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class RecipeListActivity extends AppCompatActivity implements
        RecipeAdapter.RecipeAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = RecipeListActivity.class.getSimpleName();


    private RecyclerView mRecyclerView;
    private final String LAYOUT_MANAGER_STATE_KEY = "layout_manager_state";
    private final String RECIPE_LIST_JSON_KEY = "recipe_list_json";
    private Parcelable mLayoutManagerState;
    private List<Recipe> mRecipeList;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecipeAdapter mRecipeAdapter;
    private SharedPreferences mSharedPrefs;
    private BakingJsonClient mClient;
    private ProgressBar mLoadingIndicator;
    private ImageView mStatusIcon;
    private TextView mStatusMessage;

    private Type listRecipeType = new TypeToken<List<Recipe>>() {}.getType();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        mRecyclerView = findViewById(R.id.rv_recipe_list);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mStatusIcon = findViewById(R.id.iv_message_icon);
        mStatusMessage = findViewById(R.id.tv_message);
        mRecyclerView.setHasFixedSize(true);
        mSharedPrefs = getApplicationContext().getSharedPreferences("Settings", MODE_PRIVATE);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);

        mLayoutManager = new GridLayoutManager(this, Utilities.getGridLayoutColumnCount(this));
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.addItemDecoration(new RecipeAdapter.ItemOffsetDecoration(this, R.dimen.recipe_list_item_offset));
        mRecipeAdapter = new RecipeAdapter(this, this);
        mRecyclerView.setAdapter(mRecipeAdapter);

        mClient = RetrofitClient.getInstance(getApplicationContext());

        if (null != mRecipeList || (null != savedInstanceState
                && savedInstanceState.containsKey(RECIPE_LIST_JSON_KEY)
                && null != savedInstanceState.getString(RECIPE_LIST_JSON_KEY))) {
            if (null == mRecipeList)
                mRecipeList = new Gson().fromJson(savedInstanceState.getString(RECIPE_LIST_JSON_KEY), listRecipeType);
            mRecipeAdapter.swapRecipeList(mRecipeList);
        } else {
            requestBakingJson(mClient.listRecipes(getResources().getString(R.string.json_recipe_source)));
        }
    }

    public void requestBakingJson(Call<List<Recipe>> call) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mStatusIcon.setVisibility(View.INVISIBLE);
        mStatusMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull retrofit2.Response<List<Recipe>> response) {
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                if (response.code() == 200) {
                    mRecipeList = response.body();
                    mRecipeAdapter.swapRecipeList(mRecipeList);
                    mRecyclerView.scrollToPosition(0);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mStatusIcon.setImageResource(R.drawable.ic_error_outline);
                    mStatusMessage.setText(R.string.error_unexpected_response);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    mStatusMessage.setVisibility(View.VISIBLE);
                    mStatusIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                if (Utilities.isOnline(getApplicationContext())) {
                    mStatusIcon.setImageResource(R.drawable.ic_error);
                    mStatusMessage.setText(R.string.error_problem_connecting);
                } else {
                    mStatusIcon.setImageResource(R.drawable.ic_warning);
                    mStatusMessage.setText(R.string.error_no_connection);
                }
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
                mStatusMessage.setVisibility(View.VISIBLE);
                mStatusIcon.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(Recipe recipe) {
        Intent recipeDetailIntent = new Intent(this, RecipeDetailActivity.class);
        recipeDetailIntent.putExtra("recipe", new Gson().toJson(recipe));
        recipeDetailIntent.putExtra("recipe_name", recipe.getName());
        mSharedPrefs
                .edit()
                .putString("last_seen_recipe_name", recipe.getName())
                .putString("last_seen_recipe_ingredients_json", new Gson().toJson(recipe.getIngredients()))
                .apply();

        startActivity(recipeDetailIntent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LAYOUT_MANAGER_STATE_KEY, mLayoutManager.onSaveInstanceState());
        outState.putString(RECIPE_LIST_JSON_KEY, new Gson().toJson(mRecipeList));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (null != savedInstanceState) {
            if (savedInstanceState.containsKey(LAYOUT_MANAGER_STATE_KEY)
                    && null != savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_KEY)) {
                mLayoutManagerState = savedInstanceState.getParcelable(LAYOUT_MANAGER_STATE_KEY);
            }
            if (savedInstanceState.containsKey(RECIPE_LIST_JSON_KEY)
                    && null != savedInstanceState.getString(RECIPE_LIST_JSON_KEY)) {
                mRecipeList = new Gson().fromJson(savedInstanceState.getString(RECIPE_LIST_JSON_KEY), listRecipeType);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mLayoutManagerState) {
            mLayoutManager.onRestoreInstanceState(mLayoutManagerState);
            // Preventing recurring use of this state on the rest of onResume single calls (without onRestoreInstanceState)
            mLayoutManagerState = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // updating all the AppWidgets to display ingredients for just visited recipe
        Intent intent = new Intent(this, RecipeIngredientsAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), RecipeIngredientsAppWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
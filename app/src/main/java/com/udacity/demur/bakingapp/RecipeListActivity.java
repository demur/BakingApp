package com.udacity.demur.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.demur.bakingapp.adapter.RecipeAdapter;
import com.udacity.demur.bakingapp.databinding.ActivityRecipeListBinding;
import com.udacity.demur.bakingapp.model.Recipe;
import com.udacity.demur.bakingapp.service.BakingJsonClient;
import com.udacity.demur.bakingapp.service.RecyclerViewStateHelper;
import com.udacity.demur.bakingapp.service.RetrofitClient;
import com.udacity.demur.bakingapp.service.Utilities;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static com.udacity.demur.bakingapp.service.Constant.EXTRA_JSON_RECIPE_KEY;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_RECIPE_NAME_KEY;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_LAST_SEEN_INGREDIENTS_KEY;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_LAST_SEEN_RECIPE_KEY;
import static com.udacity.demur.bakingapp.service.Constant.SHARED_PREFS_NAME;

public class RecipeListActivity extends AppCompatActivity implements
        RecipeAdapter.RecipeAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = RecipeListActivity.class.getSimpleName();

    private final String LAYOUT_MANAGER_STATE_KEY = "layout_manager_state";
    private final String RECIPE_LIST_JSON_KEY = "recipe_list_json";
    private Parcelable mLayoutManagerState;
    private List<Recipe> mRecipeList;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecipeAdapter mRecipeAdapter;
    private SharedPreferences mSharedPrefs;
    private BakingJsonClient mClient;

    private Type listRecipeType = new TypeToken<List<Recipe>>() {}.getType();
    private ActivityRecipeListBinding mBinding;
    private RecyclerViewStateHelper rvHelper = new RecyclerViewStateHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_list);
        mBinding.setRvHelper(rvHelper);

        mBinding.rvRecipeList.setHasFixedSize(true);
        mSharedPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);

        mLayoutManager = new GridLayoutManager(this, Utilities.getGridLayoutColumnCount(this));
        mBinding.rvRecipeList.setLayoutManager(mLayoutManager);
        //mBinding.rvRecipeList.addItemDecoration(new RecipeAdapter.ItemOffsetDecoration(this, R.dimen.recipe_list_item_offset));
        mRecipeAdapter = new RecipeAdapter(this, this);
        mBinding.rvRecipeList.setAdapter(mRecipeAdapter);

        mClient = RetrofitClient.getInstance(getApplicationContext());

        if (null != mRecipeList ||
                (null != savedInstanceState && savedInstanceState.containsKey(RECIPE_LIST_JSON_KEY)
                        && null != savedInstanceState.getString(RECIPE_LIST_JSON_KEY))) {
            if (null == mRecipeList) {
                mRecipeList = new Gson().fromJson(savedInstanceState.getString(RECIPE_LIST_JSON_KEY), listRecipeType);
            }
            mRecipeAdapter.swapRecipeList(mRecipeList);
        } else {
            requestBakingJson(mClient.listRecipes(getResources().getString(R.string.json_recipe_source)));
        }
    }

    public void requestBakingJson(Call<List<Recipe>> call) {
        rvHelper.setLoadingState(true);
        rvHelper.setErrorState(false);
        call.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull retrofit2.Response<List<Recipe>> response) {
                rvHelper.setLoadingState(false);
                if (response.code() == 200) {
                    mRecipeList = response.body();
                    mRecipeAdapter.swapRecipeList(mRecipeList);
                    mBinding.rvRecipeList.scrollToPosition(0);
                    rvHelper.setErrorState(false);
                } else {
                    mBinding.ivMessageIcon.setImageResource(R.drawable.ic_error_outline);
                    mBinding.tvMessage.setText(R.string.error_unexpected_response);
                    rvHelper.setErrorState(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                if (Utilities.isOnline()) {
                    mBinding.ivMessageIcon.setImageResource(R.drawable.ic_error);
                    mBinding.tvMessage.setText(R.string.error_problem_connecting);
                } else {
                    mBinding.ivMessageIcon.setImageResource(R.drawable.ic_warning);
                    mBinding.tvMessage.setText(R.string.error_no_connection);
                }
                rvHelper.setLoadingState(false);
                rvHelper.setErrorState(true);
            }
        });
    }

    @Override
    public void onClick(Recipe recipe) {
        Intent recipeDetailIntent = new Intent(this, RecipeDetailActivity.class);
        recipeDetailIntent.putExtra(EXTRA_JSON_RECIPE_KEY, new Gson().toJson(recipe));
        recipeDetailIntent.putExtra(EXTRA_RECIPE_NAME_KEY, recipe.getName());
        mSharedPrefs
                .edit()
                .putString(SHARED_PREFS_LAST_SEEN_RECIPE_KEY, recipe.getName())
                .putString(SHARED_PREFS_LAST_SEEN_INGREDIENTS_KEY, new Gson().toJson(recipe.getIngredients()))
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
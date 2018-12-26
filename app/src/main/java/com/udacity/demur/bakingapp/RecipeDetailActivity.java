package com.udacity.demur.bakingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.udacity.demur.bakingapp.model.Recipe;

import static com.udacity.demur.bakingapp.service.Constant.EXTRA_JSON_RECIPE_KEY;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_RECIPE_NAME_KEY;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_STEP_NUMBER_KEY;

public class RecipeDetailActivity extends AppCompatActivity implements
        RecipeDetailFragment.OnRecyclerViewDetailFragmentClickListener,
        RecipeStepsFragment.OnStepTabFragmentChangeListener {

    private static final String TAG = RecipeDetailActivity.class.getSimpleName();

    private boolean mTwoPane;
    private String jsonRecipe;
    private String recipeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getIntent().getExtras() && getIntent().hasExtra(EXTRA_JSON_RECIPE_KEY)) {
            setContentView(R.layout.activity_recipe_detail);
            if (null != getSupportActionBar()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            jsonRecipe = getIntent().getStringExtra(EXTRA_JSON_RECIPE_KEY);
            if (getIntent().hasExtra(EXTRA_RECIPE_NAME_KEY)) {
                recipeName = getIntent().getStringExtra(EXTRA_RECIPE_NAME_KEY);
            }
            if (null == recipeName) {
                recipeName = (new Gson().fromJson(jsonRecipe, Recipe.class)).getName();
            }
            setTitle(recipeName);

            mTwoPane = null != findViewById(R.id.recipe_steps_fragment_holder);

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (null == fragmentManager.findFragmentById(R.id.recipe_detail_fragment_holder)) {
                RecipeDetailFragment recipeDetailFragment = RecipeDetailFragment.newInstance(jsonRecipe);
                fragmentManager
                        .beginTransaction()
                        .add(R.id.recipe_detail_fragment_holder, recipeDetailFragment)
                        .commit();
            }

            if (mTwoPane && null == fragmentManager.findFragmentById(R.id.recipe_steps_fragment_holder)) {
                RecipeStepsFragment recipeStepsFragment = RecipeStepsFragment.newInstance(jsonRecipe);
                fragmentManager
                        .beginTransaction()
                        .add(R.id.recipe_steps_fragment_holder, recipeStepsFragment)
                        .commit();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.error_no_data_for_detail_activity, Toast.LENGTH_LONG);
            TextView v = toast.getView().findViewById(android.R.id.message);
            if (null != v) {
                v.setGravity(Gravity.CENTER);
            }
            toast.show();
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public void onStepTabFragmentChange(int position) {
        RecipeDetailFragment recipeDetailFragment = (RecipeDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.recipe_detail_fragment_holder);
        if (null != recipeDetailFragment) {
            recipeDetailFragment.setSelectedStep(position);
        }
    }

    @Override
    public void onRecyclerViewFragmentInteraction(int position) {
        if (!mTwoPane) {
            Intent recipeStepsIntent = new Intent(this, RecipeStepsActivity.class);
            recipeStepsIntent.putExtra(EXTRA_JSON_RECIPE_KEY, jsonRecipe);
            recipeStepsIntent.putExtra(EXTRA_RECIPE_NAME_KEY, recipeName);
            recipeStepsIntent.putExtra(EXTRA_STEP_NUMBER_KEY, position);

            startActivity(recipeStepsIntent);
        } else {
            RecipeStepsFragment recipeStepsFragment = (RecipeStepsFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.recipe_steps_fragment_holder);
            if (null != recipeStepsFragment) {
                recipeStepsFragment.setCurrentTab(position);
            }
        }
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }
}
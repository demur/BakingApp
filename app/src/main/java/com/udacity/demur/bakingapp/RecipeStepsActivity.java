package com.udacity.demur.bakingapp;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.udacity.demur.bakingapp.model.Recipe;

import static com.udacity.demur.bakingapp.service.Constant.EXTRA_JSON_RECIPE_KEY;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_RECIPE_NAME_KEY;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_STEP_NUMBER_KEY;

public class RecipeStepsActivity extends AppCompatActivity implements
        RecipeStepsFragment.OnStepTabFragmentChangeListener {

    private String jsonRecipe;
    private String recipeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getIntent().getExtras() && getIntent().hasExtra(EXTRA_JSON_RECIPE_KEY)) {
            setContentView(R.layout.activity_recipe_steps);
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

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (null == fragmentManager.findFragmentById(R.id.recipe_steps_fragment_holder)) {
                RecipeStepsFragment recipeStepsFragment;
                if (getIntent().hasExtra(EXTRA_STEP_NUMBER_KEY)) {
                    recipeStepsFragment = RecipeStepsFragment.newInstance(jsonRecipe,
                            getIntent().getIntExtra(EXTRA_STEP_NUMBER_KEY, 0));
                } else {
                    recipeStepsFragment = RecipeStepsFragment.newInstance(jsonRecipe);
                }
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
    }
}
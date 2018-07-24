package com.udacity.demur.bakingapp;


import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jakewharton.espresso.OkHttp3IdlingResource;
import com.udacity.demur.bakingapp.service.RetrofitClient;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class IdlingResourceRecipeListActivityTest {

    @Rule
    public ActivityTestRule<RecipeListActivity> mActivityTestRule = new ActivityTestRule<>(RecipeListActivity.class);

    @Test
    public void recipeListActivityTest() {
        IdlingResource idlingResource = OkHttp3IdlingResource.create(
                "retrofit_client", RetrofitClient.getOkHttpInstance());
        IdlingRegistry.getInstance().register(idlingResource);

        onView(withId(R.id.rv_recipe_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));

        onView(withId(R.id.rv_recipe_details))
                .check(matches(hasDescendant(withText(containsString("Ingredients")))));

        // In case first item on RecyclerView is "enormous" we need to scroll to desired position to get it on the screen thus load
        onView(withId(R.id.rv_recipe_details)).perform(scrollToPosition(3));

        onView(withRecyclerView(R.id.rv_recipe_details).atPosition(2))
                .check(matches(hasDescendant(withText(startsWith("1. ")))));

        onView(withRecyclerView(R.id.rv_recipe_details).atPosition(3))
                .check(matches(hasDescendant(withText(containsString("2")))));

        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }
}
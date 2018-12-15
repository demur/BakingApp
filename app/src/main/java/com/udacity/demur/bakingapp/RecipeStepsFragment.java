package com.udacity.demur.bakingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.udacity.demur.bakingapp.adapter.StepsPagerAdapter;
import com.udacity.demur.bakingapp.model.Recipe;
import com.udacity.demur.bakingapp.model.RecipeStep;

import static com.udacity.demur.bakingapp.RecipeListActivity.EXTRA_JSON_RECIPE_KEY;
import static com.udacity.demur.bakingapp.RecipeListActivity.EXTRA_OPENED_TAB_KEY;

public class RecipeStepsFragment extends Fragment {

    private String jsonRecipe;
    private Recipe theRecipe;
    private int mOpenedTab;
    private ViewPager mViewPager;
    private StepsPagerAdapter mStepsPagerAdapter;

    private OnStepTabFragmentChangeListener mListener;

    public RecipeStepsFragment() {
        // Required empty public constructor
    }

    public static RecipeStepsFragment newInstance(String jsonRecipe) {
        RecipeStepsFragment fragment = new RecipeStepsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_JSON_RECIPE_KEY, jsonRecipe);
        args.putInt(EXTRA_OPENED_TAB_KEY, 0);
        fragment.setArguments(args);
        return fragment;
    }

    public static RecipeStepsFragment newInstance(String jsonRecipe, int openedTab) {
        RecipeStepsFragment fragment = new RecipeStepsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_JSON_RECIPE_KEY, jsonRecipe);
        args.putInt(EXTRA_OPENED_TAB_KEY, openedTab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            jsonRecipe = getArguments().getString(EXTRA_JSON_RECIPE_KEY);
            theRecipe = new Gson().fromJson(jsonRecipe, Recipe.class);
            mOpenedTab = getArguments().getInt(EXTRA_OPENED_TAB_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_steps, container, false);

        if (null == mViewPager) {
            mViewPager = view.findViewById(R.id.vp_container);
            setupViewPager(mViewPager);

            TabLayout tabLayout = view.findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
            if (mOpenedTab >= 0 && mOpenedTab < tabLayout.getTabCount()) {
                mViewPager.setCurrentItem(mOpenedTab);
                if (null != mListener) {
                    mListener.onStepTabFragmentChange(mOpenedTab);
                }
            }
        }
        return view;
    }

    public void setCurrentTab(int position) {
        if (position != mOpenedTab && position >= 0 && position < mViewPager.getAdapter().getCount()) {
            mOpenedTab = position;
            mViewPager.setCurrentItem(position);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        if (null == mStepsPagerAdapter) {
            mStepsPagerAdapter = new StepsPagerAdapter(getFragmentManager());
            if (null != theRecipe.getSteps()) {
                for (RecipeStep step : theRecipe.getSteps()) {
                    if (step.getId() == 0 && step.getShortDescription().contains(getString(R.string.tab_name_intro))) {
                        mStepsPagerAdapter.addFragment(
                                RecipeSingleStepFragment.newInstance(new Gson().toJson(step)),
                                getString(R.string.tab_name_intro));
                    } else {
                        mStepsPagerAdapter.addFragment(
                                RecipeSingleStepFragment.newInstance(new Gson().toJson(step)),
                                getString(R.string.tab_name_step, step.getId()));
                    }
                }
            }
        }
        viewPager.setAdapter(mStepsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position != mOpenedTab) {
                    mOpenedTab = position;
                    if (null != mListener) {
                        mListener.onStepTabFragmentChange(position);
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStepTabFragmentChangeListener) {
            mListener = (OnStepTabFragmentChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStepTabFragmentChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnStepTabFragmentChangeListener {
        void onStepTabFragmentChange(int position);
    }
}
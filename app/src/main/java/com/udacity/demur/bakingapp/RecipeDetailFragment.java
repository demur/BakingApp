package com.udacity.demur.bakingapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.udacity.demur.bakingapp.adapter.RecipeDetailAdapter;
import com.udacity.demur.bakingapp.model.Recipe;

import static com.udacity.demur.bakingapp.service.Constant.EXTRA_JSON_RECIPE_KEY;

public class RecipeDetailFragment extends Fragment {

    private String jsonRecipe;
    private Recipe theRecipe;
    private RecipeDetailAdapter mAdapter;

    private OnRecyclerViewDetailFragmentClickListener mListener;

    public RecipeDetailFragment() {
        // Required empty public constructor
    }

    public static RecipeDetailFragment newInstance(String jsonRecipe) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_JSON_RECIPE_KEY, jsonRecipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            jsonRecipe = getArguments().getString(EXTRA_JSON_RECIPE_KEY);
            theRecipe = new Gson().fromJson(jsonRecipe, Recipe.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            if (getActivity() instanceof RecipeDetailActivity) {
                mAdapter = new RecipeDetailAdapter(theRecipe, mListener, ((RecipeDetailActivity) getActivity()).isTwoPane());
            } else {
                mAdapter = new RecipeDetailAdapter(theRecipe, mListener);
            }
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    public void setSelectedStep(int position) {
        mAdapter.setSelectedStep(position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecyclerViewDetailFragmentClickListener) {
            mListener = (OnRecyclerViewDetailFragmentClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRecyclerViewDetailFragmentClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnRecyclerViewDetailFragmentClickListener {
        void onRecyclerViewFragmentInteraction(int position);
    }
}
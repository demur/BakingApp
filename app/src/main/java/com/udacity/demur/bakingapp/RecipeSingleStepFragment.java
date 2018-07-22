package com.udacity.demur.bakingapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.udacity.demur.bakingapp.model.RecipeStep;

public class RecipeSingleStepFragment extends Fragment {

    private static final String JSON_RECIPE_STEP_PARAM_KEY = "json_recipe_step_key";

    private String jsonRecipeStep;
    private RecipeStep theRecipeStep;
    private SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;

    public RecipeSingleStepFragment() {
        // Required empty public constructor
    }

    public static RecipeSingleStepFragment newInstance(String jsonRecipeStep) {
        RecipeSingleStepFragment fragment = new RecipeSingleStepFragment();
        Bundle args = new Bundle();
        args.putString(JSON_RECIPE_STEP_PARAM_KEY, jsonRecipeStep);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            jsonRecipeStep = getArguments().getString(JSON_RECIPE_STEP_PARAM_KEY);
            theRecipeStep = new Gson().fromJson(jsonRecipeStep, RecipeStep.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_single_step, container, false);
        TextView tvRecipeStep = view.findViewById(R.id.recipe_step_desc);
        tvRecipeStep.setText(theRecipeStep.getDescription());
        mPlayerView = view.findViewById(R.id.recipe_step_video);
        if (null != theRecipeStep.getVideoURL() && !theRecipeStep.getVideoURL().isEmpty()) {
            mPlayerView.setVisibility(View.VISIBLE);
            initPlayer(theRecipeStep.getVideoURL());
        } else {
            mPlayerView.setVisibility(View.GONE);
        }
        return view;
    }

    private void initPlayer(String videoURL) {

        //Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();

        mPlayerView.setPlayer(mPlayer);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "Baking_App"),
                defaultBandwidthMeter);

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(videoURL));

        mPlayer.prepare(videoSource);

        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (mPlayer != null && mPlayer.getPlayWhenReady()) {
                mPlayer.setPlayWhenReady(false);
            }
        }
    }
}
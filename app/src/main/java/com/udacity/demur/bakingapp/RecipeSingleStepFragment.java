package com.udacity.demur.bakingapp;

import android.app.Dialog;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
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

import static android.support.constraint.ConstraintSet.END;
import static android.support.constraint.ConstraintSet.MATCH_CONSTRAINT;
import static android.support.constraint.ConstraintSet.START;
import static android.support.constraint.ConstraintSet.TOP;

/*
 * The idea how to use Dialog to open ExoPlayer in fullscreen mode was published by GeoffLedak at
 * https://github.com/GeoffLedak/ExoplayerFullscreen/blob/master/app/src/main/java/com/geoffledak/exoplayerfullscreen/MainActivity.java
 * */

public class RecipeSingleStepFragment extends Fragment {
    private static final String TAG = RecipeSingleStepFragment.class.getSimpleName();

    private static final String JSON_RECIPE_STEP_PARAM_KEY = "json_recipe_step_key";

    private static final String STATE_RESUME_WINDOW = "resumeWindow";
    private static final String STATE_RESUME_POSITION = "resumePosition";
    private static final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private static final String STATE_PLAY_WHEN_READY = "playWhenReadyState";

    private String jsonRecipeStep;
    private RecipeStep theRecipeStep;
    private SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private boolean mExoPlayerFullscreen = false;
    private ImageView mFullScreenIcon;
    private FrameLayout mFullScreenButton;
    private Dialog mFullScreenDialog;

    private int mResumeWindow;
    private long mResumePosition;
    private ConstraintLayout mPlayerViewParentView;
    private boolean mPlayWhenReadyState = false;
    private boolean mFragmentVisibilityState;
    private boolean mFailedResumeAttempt = false;

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
        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            mPlayWhenReadyState = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_single_step, container, false);
        TextView tvRecipeStep = view.findViewById(R.id.recipe_step_desc);
        tvRecipeStep.setText(theRecipeStep.getDescription());
        mPlayerView = view.findViewById(R.id.recipe_step_video);
        mPlayerViewParentView = view.findViewById(R.id.cl_step_container);
        if (null != theRecipeStep.getVideoURL() && !theRecipeStep.getVideoURL().isEmpty()) {
            mPlayerView.setVisibility(View.VISIBLE);
            initPlayer(theRecipeStep.getVideoURL());
        } else
            mPlayerView.setVisibility(View.GONE);
        return view;
    }

    private void initPlayer(String videoURL) {
        if (null == mPlayer) {
            //Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

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

            initFullscreenDialog();
            initFullscreenButton();
        }
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
            if (mPlayer != null && mPlayer.getPlayWhenReady())
                mPlayer.setPlayWhenReady(false);
            mFragmentVisibilityState = false;
        } else {
            mFragmentVisibilityState = true;
            if (mFailedResumeAttempt) {
                resumePlayer();
                mFailedResumeAttempt = false;
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        outState.putBoolean(STATE_PLAY_WHEN_READY, mPlayWhenReadyState);
        super.onSaveInstanceState(outState);
    }

    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {
        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        mFullScreenDialog.addContentView(mPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {
        ((ViewGroup) mPlayerView.getParent()).removeView(mPlayerView);
        mPlayerViewParentView.addView(mPlayerView);
        mPlayerView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        mPlayerView.getLayoutParams().height = 0;
        ConstraintSet set = new ConstraintSet();
        set.setDimensionRatio(mPlayerView.getId(), "W,9:16");
        set.constrainWidth(mPlayerView.getId(), MATCH_CONSTRAINT);
        int pixelMargin = getResources().getDimensionPixelSize(R.dimen.player_view_margin);
        set.connect(mPlayerView.getId(), START, mPlayerViewParentView.getId(), START, pixelMargin);
        set.connect(mPlayerView.getId(), END, mPlayerViewParentView.getId(), END, pixelMargin);
        set.constrainHeight(mPlayerView.getId(), MATCH_CONSTRAINT);
        set.connect(mPlayerView.getId(), TOP, mPlayerViewParentView.getId(), TOP, pixelMargin);
        set.applyTo(mPlayerViewParentView);

        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {
        mFullScreenIcon = mPlayerView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = mPlayerView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }

    private void resumePlayer() {
        if (mFragmentVisibilityState && null != theRecipeStep) {
            if (null != mPlayer) {
                boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
                if (haveResumePosition && (mResumeWindow != 0 || mResumePosition != 0))
                    mPlayer.seekTo(mResumeWindow, mResumePosition);

                mPlayer.setPlayWhenReady(mPlayWhenReadyState);

                if (mExoPlayerFullscreen || (getActivity() instanceof RecipeStepsActivity && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                    openFullscreenDialog();
                }
            }
        } else
            mFailedResumeAttempt = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        resumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayerView != null && mPlayer != null) {
            mResumeWindow = mPlayer.getCurrentWindowIndex();
            mResumePosition = Math.max(0, mPlayer.getContentPosition());
            mPlayWhenReadyState = mPlayer.getPlayWhenReady();

            //mPlayer.release();
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss();
    }
}
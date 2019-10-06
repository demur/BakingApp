package com.udacity.demur.bakingapp;

import android.app.Dialog;
import android.content.res.Configuration;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.udacity.demur.bakingapp.databinding.FragmentRecipeSingleStepBinding;
import com.udacity.demur.bakingapp.model.RecipeStep;

import static androidx.constraintlayout.widget.ConstraintSet.END;
import static androidx.constraintlayout.widget.ConstraintSet.MATCH_CONSTRAINT;
import static androidx.constraintlayout.widget.ConstraintSet.START;
import static androidx.constraintlayout.widget.ConstraintSet.TOP;
import static com.udacity.demur.bakingapp.service.Constant.EXTRA_JSON_STEP_KEY;

/*
 * The idea how to use Dialog to open ExoPlayer in fullscreen mode was published by GeoffLedak at
 * https://github.com/GeoffLedak/ExoplayerFullscreen/blob/master/app/src/main/java/com/geoffledak/exoplayerfullscreen/MainActivity.java
 * */

public class RecipeSingleStepFragment extends Fragment {
    private static final String TAG = RecipeSingleStepFragment.class.getSimpleName();

    private static final String STATE_RESUME_WINDOW = "resumeWindow";
    private static final String STATE_RESUME_POSITION = "resumePosition";
    private static final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private static final String STATE_PLAY_WHEN_READY = "playWhenReadyState";

    private String jsonRecipeStep;
    private RecipeStep theRecipeStep;
    private SimpleExoPlayer mPlayer;
    private boolean mExoPlayerFullscreen = false;
    private ImageView mFullScreenIcon;
    private FrameLayout mFullScreenButton;
    private Dialog mFullScreenDialog;

    private int mResumeWindow;
    private long mResumePosition;
    private boolean mPlayWhenReadyState = false;
    private boolean mFragmentVisibilityState;
    private boolean mFailedResumeAttempt = false;

    private FragmentRecipeSingleStepBinding mBinding;

    public RecipeSingleStepFragment() {
        // Required empty public constructor
    }

    public static RecipeSingleStepFragment newInstance(String jsonRecipeStep) {
        RecipeSingleStepFragment fragment = new RecipeSingleStepFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_JSON_STEP_KEY, jsonRecipeStep);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getArguments()) {
            jsonRecipeStep = getArguments().getString(EXTRA_JSON_STEP_KEY);
            theRecipeStep = new Gson().fromJson(jsonRecipeStep, RecipeStep.class);
        }
        if (null != savedInstanceState) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
            mPlayWhenReadyState = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_single_step, container, false);
        mBinding.recipeStepDesc.setText(theRecipeStep.getDescription());
        if (null != theRecipeStep.getVideoURL() && !theRecipeStep.getVideoURL().isEmpty()) {
            mBinding.recipeStepVideo.setVisibility(View.VISIBLE);
            //initPlayer(theRecipeStep.getVideoURL());
        } else {
            mBinding.recipeStepVideo.setVisibility(View.GONE);
        }
        return mBinding.getRoot();
    }

    private void initPlayer(String videoURL) {
        if (null == mPlayer && null != mBinding && null != mBinding.recipeStepVideo
                && mBinding.recipeStepVideo.getVisibility() != View.GONE) {
            //Handler mainHandler = new Handler();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

            mPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

            mBinding.recipeStepVideo.setUseController(true);
            mBinding.recipeStepVideo.requestFocus();

            mBinding.recipeStepVideo.setPlayer(mPlayer);

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

        if (null != mPlayer) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            mFragmentVisibilityState = false;
            if (null != mPlayer && mPlayer.getPlayWhenReady())
                mPlayer.setPlayWhenReady(false);
            releasePlayer();
        } else {
            mFragmentVisibilityState = true;
            if (mFailedResumeAttempt) {
                mFailedResumeAttempt = false;
                resumePlayer();
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
        ((ViewGroup) mBinding.recipeStepVideo.getParent()).removeView(mBinding.recipeStepVideo);
        mFullScreenDialog.addContentView(mBinding.recipeStepVideo,
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    private void closeFullscreenDialog() {
        ((ViewGroup) mBinding.recipeStepVideo.getParent()).removeView(mBinding.recipeStepVideo);
        mBinding.clStepContainer.addView(mBinding.recipeStepVideo);
        mBinding.recipeStepVideo.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        mBinding.recipeStepVideo.getLayoutParams().height = 0;
        ConstraintSet set = new ConstraintSet();
        set.setDimensionRatio(mBinding.recipeStepVideo.getId(), "W,9:16");
        set.constrainWidth(mBinding.recipeStepVideo.getId(), MATCH_CONSTRAINT);
        int pixelMargin = getResources().getDimensionPixelSize(R.dimen.player_view_margin);
        set.connect(mBinding.recipeStepVideo.getId(), START, mBinding.clStepContainer.getId(), START, pixelMargin);
        set.connect(mBinding.recipeStepVideo.getId(), END, mBinding.clStepContainer.getId(), END, pixelMargin);
        set.constrainHeight(mBinding.recipeStepVideo.getId(), MATCH_CONSTRAINT);
        set.connect(mBinding.recipeStepVideo.getId(), TOP, mBinding.clStepContainer.getId(), TOP, pixelMargin);
        set.applyTo(mBinding.clStepContainer);

        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_fullscreen_expand));
    }

    private void initFullscreenButton() {
        mFullScreenIcon = mBinding.recipeStepVideo.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = mBinding.recipeStepVideo.findViewById(R.id.exo_fullscreen_button);
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
        if (mFragmentVisibilityState && null != theRecipeStep
                && null != mBinding && null != mBinding.recipeStepVideo
                && mBinding.recipeStepVideo.getVisibility() != View.GONE) {
            if (null == mPlayer)
                initPlayer(theRecipeStep.getVideoURL());
            boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;
            if (haveResumePosition && (mResumeWindow != 0 || mResumePosition != 0))
                mPlayer.seekTo(mResumeWindow, mResumePosition);

            mPlayer.setPlayWhenReady(mPlayWhenReadyState);

            if (mExoPlayerFullscreen || (getActivity() instanceof RecipeStepsActivity
                    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
                openFullscreenDialog();
        } else {
            mFailedResumeAttempt = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        resumePlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    private void releasePlayer() {
        if (null != mPlayer && null != mBinding && null != mBinding.recipeStepVideo
                && mBinding.recipeStepVideo.getVisibility() != View.GONE) {
            mResumeWindow = mPlayer.getCurrentWindowIndex();
            mResumePosition = Math.max(0, mPlayer.getContentPosition());
            mPlayWhenReadyState = mPlayer.getPlayWhenReady();

            mPlayer.release();
        }

        if (null != mFullScreenDialog)
            mFullScreenDialog.dismiss();
    }
}
package com.udacity.demur.bakingapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.demur.bakingapp.R;
import com.udacity.demur.bakingapp.RecipeDetailFragment;
import com.udacity.demur.bakingapp.model.Recipe;
import com.udacity.demur.bakingapp.model.RecipeIngredient;
import com.udacity.demur.bakingapp.model.RecipeStep;

import java.util.Locale;

import static com.udacity.demur.bakingapp.service.Utilities.getBulletItem;
import static com.udacity.demur.bakingapp.service.Utilities.getLargeBoldItalic;

public class RecipeDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = RecipeDetailAdapter.class.getSimpleName();
    private final Recipe theRecipe;
    private final RecipeDetailFragment.OnRecyclerViewDetailFragmentClickListener mListener;
    private final boolean isIngredientsCard;
    private final static int COMMON_VIEW_TYPE = 0;
    private final static int INGREDIENTS_VIEW_TYPE = 1;
    private int selectedItemPos = RecyclerView.NO_POSITION;
    private boolean mTwoPane = false;

    public RecipeDetailAdapter(@NonNull Recipe recipe,
                               RecipeDetailFragment.OnRecyclerViewDetailFragmentClickListener listener) {
        theRecipe = recipe;
        mListener = listener;
        isIngredientsCard = null != theRecipe.getIngredients() && theRecipe.getIngredients().size() > 0;
    }

    public RecipeDetailAdapter(Recipe recipe,
                               RecipeDetailFragment.OnRecyclerViewDetailFragmentClickListener listener,
                               boolean isTwoPane) {
        theRecipe = recipe;
        mListener = listener;
        isIngredientsCard = null != theRecipe.getIngredients() && theRecipe.getIngredients().size() > 0;
        mTwoPane = isTwoPane;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case INGREDIENTS_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recipe_ingredients_rv_item, parent, false);
                return new IngredientsViewHolder(view);
            case COMMON_VIEW_TYPE:
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recipe_detail_rv_item, parent, false);
                view.setFocusable(true);
                return new RecipeDetailAdapterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (mTwoPane) {
            holder.itemView.setSelected(selectedItemPos == position);
        }
        final Context context = holder.itemView.getContext();
        switch (holder.getItemViewType()) {
            case INGREDIENTS_VIEW_TYPE:
                IngredientsViewHolder ingredientsHolder = (IngredientsViewHolder) holder;
                CharSequence ingredientList = TextUtils.concat("",
                        getLargeBoldItalic(context.getString(R.string.ingredients_title)));
                for (RecipeIngredient ingredient : theRecipe.getIngredients()) {
                    ingredientList = TextUtils.concat(ingredientList, "\n",
                            getBulletItem(context.getString(
                                    R.string.ingredient_item,
                                    ingredient.getIngredient(),
                                    ingredient.getQuantity() % 1.0 == 0
                                            ? String.format(Locale.getDefault(), "%.0f", ingredient.getQuantity())
                                            : String.format("%s", ingredient.getQuantity()),
                                    ingredient.getMeasure()
                            ))
                    );
                }
                ingredientsHolder.tvIngredients.setText(ingredientList);
                break;
            case COMMON_VIEW_TYPE:
            default:
                final RecipeStep step = theRecipe.getSteps().get(isIngredientsCard ? position - 1 : position);
                RecipeDetailAdapterViewHolder commonHolder = (RecipeDetailAdapterViewHolder) holder;
                if ((position == 0 && !isIngredientsCard) || (position == 1 && isIngredientsCard)) {
                    commonHolder.tvStepName.setText(step.getShortDescription().replaceAll("\\.+$", ""));
                } else {
                    commonHolder.tvStepName.setText(context.getString(R.string.step_name_rv_item, step.getId(), step.getShortDescription().replaceAll("\\.+$", "")));
                }
                commonHolder.ivPlayIcon.setVisibility(TextUtils.isEmpty(step.getVideoURL()) ? View.INVISIBLE : View.VISIBLE);
                if (!TextUtils.isEmpty(step.getThumbnailURL())) {
                    Picasso.get().load(step.getThumbnailURL())
                            .noPlaceholder().error(R.drawable.ic_broken_image)
                            .fit().centerInside().into(commonHolder.ivThumb);
                } else if (position % 2 == 0) {
                    commonHolder.ivThumb.setScaleX(-1);
                }
        }
    }

    @Override
    public int getItemCount() {
        if (null == theRecipe || null == theRecipe.getSteps()) {
            return isIngredientsCard ? 1 : 0;
        }
        return theRecipe.getSteps().size() + (isIngredientsCard ? 1 : 0);
    }

    public void setSelectedStep(int position) {
        setSelectedItemPos(isIngredientsCard ? position + 1 : position);
    }

    private void setSelectedItemPos(int position) {
        notifyItemChanged(selectedItemPos);
        selectedItemPos = position;
        notifyItemChanged(selectedItemPos);
    }

    class RecipeDetailAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView ivPlayIcon;
        final TextView tvStepName;
        final ImageView ivThumb;

        RecipeDetailAdapterViewHolder(View itemView) {
            super(itemView);

            ivPlayIcon = itemView.findViewById(R.id.iv_play_icon);
            tvStepName = itemView.findViewById(R.id.tv_step_name);
            ivThumb = itemView.findViewById(R.id.iv_recipe_thumb);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            setSelectedItemPos(position);
            if (position != RecyclerView.NO_POSITION) {
                if (null != mListener) {
                    if (isIngredientsCard) {
                        position--;
                    }
                    mListener.onRecyclerViewFragmentInteraction(position);
                }
            }
        }
    }

    class IngredientsViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIngredients;

        IngredientsViewHolder(View itemView) {
            super(itemView);

            tvIngredients = itemView.findViewById(R.id.tv_ingredients);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && isIngredientsCard)
                ? INGREDIENTS_VIEW_TYPE
                : COMMON_VIEW_TYPE;
    }
}
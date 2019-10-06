package com.udacity.demur.bakingapp.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.udacity.demur.bakingapp.R;
import com.udacity.demur.bakingapp.RecipeDetailFragment;
import com.udacity.demur.bakingapp.databinding.RecipeDetailRvItemBinding;
import com.udacity.demur.bakingapp.databinding.RecipeIngredientsRvItemBinding;
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
        switch (viewType) {
            case INGREDIENTS_VIEW_TYPE:
                RecipeIngredientsRvItemBinding ingredientsBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext()),
                        R.layout.recipe_ingredients_rv_item,
                        parent,
                        false
                );
                return new IngredientsViewHolder(ingredientsBinding);
            case COMMON_VIEW_TYPE:
            default:
                RecipeDetailRvItemBinding detailBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.getContext()),
                        R.layout.recipe_detail_rv_item,
                        parent,
                        false
                );
                detailBinding.getRoot().setFocusable(true);
                return new RecipeDetailAdapterViewHolder(detailBinding);
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
                ingredientsHolder.binding.tvIngredients.setText(ingredientList);
                break;
            case COMMON_VIEW_TYPE:
            default:
                final RecipeStep step = theRecipe.getSteps().get(isIngredientsCard ? position - 1 : position);
                RecipeDetailAdapterViewHolder commonHolder = (RecipeDetailAdapterViewHolder) holder;
                if ((position == 0 && !isIngredientsCard) || (position == 1 && isIngredientsCard)) {
                    commonHolder.binding.tvStepName.setText(step.getShortDescription().replaceAll("\\.+$", ""));
                } else {
                    commonHolder.binding.tvStepName.setText(context.getString(R.string.step_name_rv_item, step.getId(), step.getShortDescription().replaceAll("\\.+$", "")));
                }
                commonHolder.binding.ivPlayIcon.setVisibility(TextUtils.isEmpty(step.getVideoURL()) ? View.INVISIBLE : View.VISIBLE);
                if (!TextUtils.isEmpty(step.getThumbnailURL())) {
                    Picasso.get().load(step.getThumbnailURL())
                            .noPlaceholder().error(R.drawable.ic_broken_image)
                            .fit().centerInside().into(commonHolder.binding.ivRecipeThumb);
                } else if (position % 2 == 0) {
                    commonHolder.binding.ivRecipeThumb.setScaleX(-1);
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
        private final RecipeDetailRvItemBinding binding;

        RecipeDetailAdapterViewHolder(RecipeDetailRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            this.binding.getRoot().setOnClickListener(this);
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
        private final RecipeIngredientsRvItemBinding binding;

        IngredientsViewHolder(RecipeIngredientsRvItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && isIngredientsCard)
                ? INGREDIENTS_VIEW_TYPE
                : COMMON_VIEW_TYPE;
    }
}
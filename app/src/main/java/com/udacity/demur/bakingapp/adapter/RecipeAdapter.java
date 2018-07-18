package com.udacity.demur.bakingapp.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.demur.bakingapp.R;
import com.udacity.demur.bakingapp.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterViewHolder> {
    private final Context mContext;
    private final RecipeAdapterOnClickHandler mClickHandler;
    private List<Recipe> recipeList;

    public RecipeAdapter(@NonNull Context context, RecipeAdapterOnClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
        setHasStableIds(false);
    }

    @NonNull
    @Override
    public RecipeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recipe_rv_item, parent, false);
        view.setFocusable(true);
        return new RecipeAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecipeAdapterViewHolder holder, int position) {
        final Recipe theRecipe = recipeList.get(position);
        //holder.tvTitle.setText(theRecipe.getTitle());
        //holder.ivPoster.setContentDescription("\"" + theRecipe.getTitle() + "\" movie poster");
        holder.tvName.setText(theRecipe.getName());
        holder.tvServings.setText(String.valueOf(theRecipe.getServings()));
    }

    @Override
    public int getItemCount() {
        if (null == recipeList)
            return 0;
        return recipeList.size();
    }

    @Override
    public long getItemId(int position) {
        return recipeList.get(position).getId();
    }

    public void swapRecipeList(List<Recipe> recipeList) {
        this.recipeList = recipeList;
        notifyDataSetChanged();
    }

    public interface RecipeAdapterOnClickHandler {
        void onClick(Recipe recipe);
    }

    class RecipeAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //final ImageView ivPoster;
        //final TextView tvTitle;
        final TextView tvName;
        final TextView tvServings;

        RecipeAdapterViewHolder(View itemView) {
            super(itemView);

            //ivPoster = itemView.findViewById(R.id.iv_poster);
            //tvTitle = itemView.findViewById(R.id.tv_title);
            tvName = itemView.findViewById(R.id.tv_name);
            tvServings = itemView.findViewById(R.id.tv_servings);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (null != mClickHandler) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mClickHandler.onClick(recipeList.get(position));
                }
            }
        }
    }

    /*
    * This class is product of yqritc at https://gist.github.com/yqritc/ccca77dc42f2364777e1
    * */
    public static final class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }
}
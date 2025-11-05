package com.kitchenkompanion.features.recipes;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kitchenkompanion.R;
import com.kitchenkompanion.data.remote.dto.RecipeSearchResponse;
import com.kitchenkompanion.databinding.ItemRecipeBinding;

/**
 * Adapter for displaying recipe search results.
 */
public class RecipeAdapter extends ListAdapter<RecipeSearchResponse, RecipeAdapter.ViewHolder> {
    
    private OnItemClickListener listener;
    
    public RecipeAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<RecipeSearchResponse> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<RecipeSearchResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecipeSearchResponse oldItem, 
                                       @NonNull RecipeSearchResponse newItem) {
            return oldItem.id == newItem.id;
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull RecipeSearchResponse oldItem, 
                                          @NonNull RecipeSearchResponse newItem) {
            return oldItem.title.equals(newItem.title);
        }
    };
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public interface OnItemClickListener {
        void onItemClick(RecipeSearchResponse recipe);
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecipeBinding binding;
        
        ViewHolder(ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(RecipeSearchResponse recipe) {
            binding.recipeTitle.setText(recipe.title);
            
            // Load image with Glide
            if (recipe.image != null && !recipe.image.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(recipe.image)
                        .placeholder(R.drawable.ic_recipes)
                        .error(R.drawable.ic_recipes)
                        .centerCrop()
                        .into(binding.recipeImage);
            } else {
                binding.recipeImage.setImageResource(R.drawable.ic_recipes);
            }
            
            // Display ingredient match info
            if (recipe.usedIngredientCount > 0) {
                String matchText = recipe.usedIngredientCount + " of your ingredients";
                binding.ingredientMatch.setText(matchText);
                binding.ingredientMatch.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.ingredientMatch.setVisibility(android.view.View.GONE);
            }
            
            // Display missing ingredients count
            if (recipe.missedIngredientCount > 0) {
                String missingText = "+" + recipe.missedIngredientCount + " more ingredients";
                binding.missingIngredients.setText(missingText);
                binding.missingIngredients.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.missingIngredients.setVisibility(android.view.View.GONE);
            }
            
            // Display time and servings if available
            if (recipe.readyInMinutes > 0) {
                String timeText = recipe.readyInMinutes + " min";
                binding.recipeTime.setText(timeText);
                binding.recipeTime.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.recipeTime.setVisibility(android.view.View.GONE);
            }
            
            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(recipe);
                }
            });
        }
    }
}







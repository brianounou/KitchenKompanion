package com.kitchenkompanion.features.grocery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.databinding.ItemGroceryBinding;

/**
 * Adapter for displaying grocery list items.
 */
public class GroceryAdapter extends ListAdapter<GroceryEntryEntity, GroceryAdapter.ViewHolder> {
    
    private OnItemClickListener listener;
    
    public GroceryAdapter() {
        super(DIFF_CALLBACK);
    }
    
    private static final DiffUtil.ItemCallback<GroceryEntryEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<GroceryEntryEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull GroceryEntryEntity oldItem, 
                                       @NonNull GroceryEntryEntity newItem) {
            return oldItem.id == newItem.id;
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull GroceryEntryEntity oldItem, 
                                          @NonNull GroceryEntryEntity newItem) {
            return oldItem.name.equals(newItem.name) 
                    && oldItem.checked == newItem.checked
                    && oldItem.quantity == newItem.quantity
                    && (oldItem.unit == null ? newItem.unit == null : oldItem.unit.equals(newItem.unit));
        }
    };
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroceryBinding binding = ItemGroceryBinding.inflate(
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
        void onCheckedChanged(GroceryEntryEntity entry, boolean isChecked);
        void onItemLongClick(GroceryEntryEntity entry);
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroceryBinding binding;
        
        ViewHolder(ItemGroceryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(GroceryEntryEntity entry) {
            binding.itemName.setText(entry.name);
            
            // Format quantity
            String quantityText = "";
            if (entry.quantity > 0) {
                if (entry.quantity == Math.floor(entry.quantity)) {
                    quantityText = String.valueOf((int) entry.quantity);
                } else {
                    quantityText = String.valueOf(entry.quantity);
                }
                
                if (entry.unit != null && !entry.unit.isEmpty()) {
                    quantityText += " " + entry.unit;
                }
            }
            
            if (!quantityText.isEmpty()) {
                binding.itemQuantity.setText(quantityText);
                binding.itemQuantity.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.itemQuantity.setVisibility(android.view.View.GONE);
            }
            
            // Source badge
            if (entry.source != null && !entry.source.equals("manual")) {
                binding.sourceBadge.setText(entry.source);
                binding.sourceBadge.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.sourceBadge.setVisibility(android.view.View.GONE);
            }
            
            // Checkbox
            binding.checkbox.setOnCheckedChangeListener(null);
            binding.checkbox.setChecked(entry.checked);
            binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onCheckedChanged(entry, isChecked);
                }
            });
            
            // Long click for delete
            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(entry);
                }
                return true;
            });
            
            // Strike-through when checked
            if (entry.checked) {
                binding.itemName.setPaintFlags(binding.itemName.getPaintFlags() | 
                        android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.itemName.setAlpha(0.5f);
                binding.itemQuantity.setAlpha(0.5f);
            } else {
                binding.itemName.setPaintFlags(binding.itemName.getPaintFlags() & 
                        ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.itemName.setAlpha(1.0f);
                binding.itemQuantity.setAlpha(1.0f);
            }
        }
    }
}







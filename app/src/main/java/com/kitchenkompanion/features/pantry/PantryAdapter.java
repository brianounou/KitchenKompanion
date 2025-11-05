package com.kitchenkompanion.features.pantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kitchenkompanion.R;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.utils.DateUtils;

/**
 * RecyclerView adapter for pantry items.
 */
public class PantryAdapter extends ListAdapter<ItemEntity, PantryAdapter.ItemViewHolder> {
    
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(ItemEntity item);
        void onItemLongClick(ItemEntity item);
    }
    
    public PantryAdapter() {
        super(DIFF_CALLBACK);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    private static final DiffUtil.ItemCallback<ItemEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<ItemEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemEntity oldItem, @NonNull ItemEntity newItem) {
            return oldItem.id.equals(newItem.id);
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull ItemEntity oldItem, @NonNull ItemEntity newItem) {
            return oldItem.name.equals(newItem.name) &&
                   oldItem.quantity == newItem.quantity &&
                   oldItem.expiryDate != null && oldItem.expiryDate.equals(newItem.expiryDate);
        }
    };
    
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemEntity item = getItem(position);
        holder.bind(item, listener);
    }
    
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView quantityText;
        private final TextView expiryText;
        private final TextView locationText;
        private final ImageView photoImage;
        private final View expiryIndicator;
        
        ItemViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.item_name);
            quantityText = itemView.findViewById(R.id.item_quantity);
            expiryText = itemView.findViewById(R.id.item_expiry);
            locationText = itemView.findViewById(R.id.item_location);
            photoImage = itemView.findViewById(R.id.item_photo);
            expiryIndicator = itemView.findViewById(R.id.expiry_indicator);
        }
        
        void bind(ItemEntity item, OnItemClickListener listener) {
            nameText.setText(item.name);
            
            // Quantity
            String quantityStr = String.format("%.1f %s", item.quantity, 
                    item.unit != null ? item.unit : "");
            quantityText.setText(quantityStr);
            
            // Location
            if (item.location != null && !item.location.isEmpty()) {
                locationText.setText(item.location);
                locationText.setVisibility(View.VISIBLE);
            } else {
                locationText.setVisibility(View.GONE);
            }
            
            // Expiry date and status
            if (item.expiryDate != null) {
                expiryText.setText(DateUtils.formatForDisplay(item.expiryDate));
                expiryText.setVisibility(View.VISIBLE);
                
                // Set expiry status color
                if (DateUtils.isExpired(item.expiryDate)) {
                    expiryIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_expired));
                    expiryText.setTextColor(
                            itemView.getContext().getColor(R.color.status_expired));
                } else if (DateUtils.isExpiringSoon(item.expiryDate, 3)) {
                    expiryIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_expiring_soon));
                    expiryText.setTextColor(
                            itemView.getContext().getColor(R.color.status_expiring_soon));
                } else {
                    expiryIndicator.setBackgroundColor(
                            itemView.getContext().getColor(R.color.status_fresh));
                    expiryText.setTextColor(
                            itemView.getContext().getColor(R.color.status_fresh));
                }
            } else {
                expiryText.setVisibility(View.GONE);
                expiryIndicator.setBackgroundColor(Color.TRANSPARENT);
            }
            
            // Photo
            if (item.photoUrl != null && !item.photoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.photoUrl)
                        .placeholder(R.drawable.ic_pantry)
                        .into(photoImage);
            } else {
                photoImage.setImageResource(R.drawable.ic_pantry);
            }
            
            // Click listeners
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onItemClick(item));
                itemView.setOnLongClickListener(v -> {
                    listener.onItemLongClick(item);
                    return true;
                });
            }
        }
    }
}








package com.kitchenkompanion.features.pantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.kitchenkompanion.R;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.databinding.FragmentPantryBinding;

/**
 * Fragment displaying pantry items with swipe-to-delete and click-to-edit functionality.
 * 
 * Features:
 * - List all items in the current household
 * - Color-coded expiry status
 * - Swipe left to delete
 * - Tap to view/edit item
 * - FAB to add new item
 */
public class PantryFragment extends Fragment {
    
    private FragmentPantryBinding binding;
    private PantryViewModel viewModel;
    private PantryAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        
        setupRecyclerView();
        setupFab();
        observeData();
    }
    
    private void setupRecyclerView() {
        adapter = new PantryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        
        // Set up item click listeners
        adapter.setOnItemClickListener(new PantryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemEntity item) {
                openEditItem(item.id);
            }
            
            @Override
            public void onItemLongClick(ItemEntity item) {
                showItemOptions(item);
            }
        });
        
        // Set up swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ItemEntity item = adapter.getCurrentList().get(position);
                deleteItem(item);
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }
    
    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
            startActivity(intent);
        });
    }
    
    private void observeData() {
        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                adapter.submitList(items);
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
    }
    
    private void openEditItem(String itemId) {
        Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, itemId);
        startActivity(intent);
    }
    
    private void showItemOptions(ItemEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.name)
                .setItems(new String[]{
                        getString(R.string.edit),
                        getString(R.string.delete)
                }, (dialog, which) -> {
                    if (which == 0) {
                        openEditItem(item.id);
                    } else {
                        confirmDelete(item);
                    }
                })
                .show();
    }
    
    private void confirmDelete(ItemEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_item)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void deleteItem(ItemEntity item) {
        viewModel.deleteItem(item.id);
        Snackbar.make(binding.getRoot(), R.string.item_deleted, Snackbar.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



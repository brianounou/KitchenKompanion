package com.kitchenkompanion.features.grocery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kitchenkompanion.R;
import com.kitchenkompanion.data.local.GroceryEntryEntity;
import com.kitchenkompanion.databinding.FragmentGroceryBinding;

/**
 * Fragment for managing shared grocery lists.
 * 
 * Features:
 * - View all grocery items
 * - Add items manually
 * - Auto-generate from expiring items
 * - Auto-generate from low stock items
 * - Check/uncheck items
 * - Delete items
 * - Clear checked items
 * - Real-time sync across devices
 */
public class GroceryFragment extends Fragment {
    
    private FragmentGroceryBinding binding;
    private GroceryViewModel viewModel;
    private GroceryAdapter adapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGroceryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(GroceryViewModel.class);
        
        setupRecyclerView();
        setupFab();
        observeData();
    }
    
    private void setupRecyclerView() {
        adapter = new GroceryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        
        // Set up item listeners
        adapter.setOnItemClickListener(new GroceryAdapter.OnItemClickListener() {
            @Override
            public void onCheckedChanged(GroceryEntryEntity entry, boolean isChecked) {
                viewModel.updateCheckedStatus(entry.id, isChecked);
            }
            
            @Override
            public void onItemLongClick(GroceryEntryEntity entry) {
                showDeleteConfirmation(entry);
            }
        });
    }
    
    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> showAddItemDialog());
    }
    
    private void observeData() {
        viewModel.getEntries().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                adapter.submitList(entries);
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
    }
    
    private void showAddItemDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_grocery_item, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.item_name_input);
        TextInputEditText quantityInput = dialogView.findViewById(R.id.quantity_input);
        TextInputEditText unitInput = dialogView.findViewById(R.id.unit_input);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_grocery_item)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        GroceryEntryEntity entry = new GroceryEntryEntity();
                        entry.name = name;
                        entry.source = "manual";
                        
                        String quantityStr = quantityInput.getText().toString().trim();
                        if (!quantityStr.isEmpty()) {
                            try {
                                entry.quantity = Double.parseDouble(quantityStr);
                            } catch (NumberFormatException e) {
                                entry.quantity = 1.0;
                            }
                        } else {
                            entry.quantity = 1.0;
                        }
                        
                        entry.unit = unitInput.getText().toString().trim();
                        viewModel.addEntry(entry);
                        
                        Snackbar.make(binding.getRoot(), 
                                R.string.grocery_item_added, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void showDeleteConfirmation(GroceryEntryEntity entry) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_item)
                .setMessage(getString(R.string.confirm_delete_grocery, entry.name))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteEntry(entry.id);
                    Snackbar.make(binding.getRoot(), 
                            R.string.item_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.grocery_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_generate_from_expiring) {
            viewModel.generateFromExpiringItems();
            Snackbar.make(binding.getRoot(), 
                    R.string.generated_from_expiring, Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_generate_from_low_stock) {
            viewModel.generateFromLowStock();
            Snackbar.make(binding.getRoot(), 
                    R.string.generated_from_low_stock, Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_clear_checked) {
            viewModel.deleteCheckedItems();
            Snackbar.make(binding.getRoot(), 
                    R.string.cleared_checked_items, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



package com.kitchenkompanion.features.pantry;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kitchenkompanion.R;
import com.kitchenkompanion.data.local.ItemEntity;
import com.kitchenkompanion.databinding.ActivityAddEditItemBinding;
import com.kitchenkompanion.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Activity for adding a new item or editing an existing one.
 * 
 * Usage:
 * - For new item: startActivity(new Intent(context, AddEditItemActivity.class))
 * - For editing: intent.putExtra(EXTRA_ITEM_ID, itemId)
 * - For barcode prefill: intent.putExtra(EXTRA_BARCODE, barcode)
 *                        intent.putExtra(EXTRA_PRODUCT_NAME, name)
 */
public class AddEditItemActivity extends AppCompatActivity {
    
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_BARCODE = "barcode";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_NUTRITION_JSON = "nutrition_json";
    
    private ActivityAddEditItemBinding binding;
    private PantryViewModel viewModel;
    private String itemId;
    private Date selectedExpiryDate;
    
    private static final String[] LOCATIONS = {"Fridge", "Freezer", "Pantry", "Other"};
    private static final String[] UNITS = {"", "kg", "g", "L", "ml", "pcs", "cups", "tbsp", "tsp"};
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        viewModel = new ViewModelProvider(this).get(PantryViewModel.class);
        
        setupSpinners();
        setupListeners();
        loadItemIfEditing();
    }
    
    private void setupSpinners() {
        // Location spinner
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, LOCATIONS);
        binding.locationInput.setAdapter(locationAdapter);
        
        // Unit spinner
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, UNITS);
        binding.unitInput.setAdapter(unitAdapter);
    }
    
    private void setupListeners() {
        // Expiry date picker
        binding.expiryDateInput.setOnClickListener(v -> showDatePicker());
        binding.expiryDateLayout.setEndIconOnClickListener(v -> showDatePicker());
        
        // Clear expiry date
        binding.expiryDateLayout.setStartIconOnClickListener(v -> {
            selectedExpiryDate = null;
            binding.expiryDateInput.setText("");
        });
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedExpiryDate != null) {
            calendar.setTime(selectedExpiryDate);
        }
        
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedExpiryDate = calendar.getTime();
                    binding.expiryDateInput.setText(DateUtils.formatForDisplay(selectedExpiryDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }
    
    private void loadItemIfEditing() {
        Intent intent = getIntent();
        itemId = intent.getStringExtra(EXTRA_ITEM_ID);
        
        if (itemId != null) {
            // Editing existing item
            setTitle(R.string.edit_item);
            viewModel.getItemById(itemId).observe(this, item -> {
                if (item != null) {
                    populateFields(item);
                }
            });
        } else {
            // New item
            setTitle(R.string.add_item);
            
            // Check for barcode prefill
            String barcode = intent.getStringExtra(EXTRA_BARCODE);
            String productName = intent.getStringExtra(EXTRA_PRODUCT_NAME);
            String nutritionJson = intent.getStringExtra(EXTRA_NUTRITION_JSON);
            
            if (barcode != null) {
                binding.barcodeInput.setText(barcode);
            }
            if (productName != null) {
                binding.nameInput.setText(productName);
            }
            // nutritionJson will be saved with the item
        }
    }
    
    private void populateFields(ItemEntity item) {
        binding.nameInput.setText(item.name);
        binding.barcodeInput.setText(item.barcode);
        binding.quantityInput.setText(String.valueOf(item.quantity));
        binding.unitInput.setText(item.unit, false);
        binding.locationInput.setText(item.location, false);
        binding.notesInput.setText(item.notes);
        binding.lowStockThresholdInput.setText(String.valueOf(item.lowStockThreshold));
        
        if (item.expiryDate != null) {
            selectedExpiryDate = item.expiryDate;
            binding.expiryDateInput.setText(DateUtils.formatForDisplay(item.expiryDate));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_item_menu, menu);
        
        // Hide delete if adding new item
        if (itemId == null) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveItem();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void saveItem() {
        // Validate required fields
        String name = binding.nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            binding.nameLayout.setError(getString(R.string.error_field_required));
            return;
        }
        
        String quantityStr = binding.quantityInput.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            binding.quantityLayout.setError(getString(R.string.error_field_required));
            return;
        }
        
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
            if (quantity <= 0) {
                binding.quantityLayout.setError(getString(R.string.error_must_be_positive));
                return;
            }
        } catch (NumberFormatException e) {
            binding.quantityLayout.setError(getString(R.string.error_invalid_number));
            return;
        }
        
        // Parse low stock threshold
        double lowStockThreshold = 0;
        String thresholdStr = binding.lowStockThresholdInput.getText().toString().trim();
        if (!thresholdStr.isEmpty()) {
            try {
                lowStockThreshold = Double.parseDouble(thresholdStr);
            } catch (NumberFormatException e) {
                // Use default 0
            }
        }
        
        // Create or update item
        ItemEntity item = new ItemEntity();
        if (itemId != null) {
            item.id = itemId;
        }
        
        item.name = name;
        item.barcode = binding.barcodeInput.getText().toString().trim();
        item.quantity = quantity;
        item.unit = binding.unitInput.getText().toString().trim();
        item.location = binding.locationInput.getText().toString().trim();
        item.expiryDate = selectedExpiryDate;
        item.notes = binding.notesInput.getText().toString().trim();
        item.lowStockThreshold = lowStockThreshold;
        
        // Save nutrition JSON if provided from barcode scan
        String nutritionJson = getIntent().getStringExtra(EXTRA_NUTRITION_JSON);
        if (nutritionJson != null) {
            item.nutritionJson = nutritionJson;
        }
        
        // Save to database
        if (itemId != null) {
            viewModel.updateItem(item);
            Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.addItem(item);
            Toast.makeText(this, R.string.item_added, Toast.LENGTH_SHORT).show();
        }
        
        finish();
    }
    
    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_item)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteItem(itemId);
                    Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}







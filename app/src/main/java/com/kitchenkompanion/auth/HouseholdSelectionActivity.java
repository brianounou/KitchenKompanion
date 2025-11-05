package com.kitchenkompanion.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.kitchenkompanion.MainActivity;
import com.kitchenkompanion.R;
import com.kitchenkompanion.databinding.ActivityHouseholdSelectionBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for selecting or creating a household.
 */
public class HouseholdSelectionActivity extends AppCompatActivity {
    
    private static final String PREFS_NAME = "KitchenKompanionPrefs";
    private static final String PREF_HOUSEHOLD_ID = "householdId";
    
    private ActivityHouseholdSelectionBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private HouseholdAdapter adapter;
    private List<Household> households;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHouseholdSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            finish();
            return;
        }
        
        setupUI();
        loadHouseholds();
    }
    
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        
        households = new ArrayList<>();
        adapter = new HouseholdAdapter(households, this::onHouseholdSelected);
        
        binding.householdList.setLayoutManager(new LinearLayoutManager(this));
        binding.householdList.setAdapter(adapter);
        
        binding.fabCreateHousehold.setOnClickListener(v -> showCreateHouseholdDialog());
    }
    
    private void loadHouseholds() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        firestore.collection("households")
                .whereArrayContains("members", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    households.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Household household = new Household();
                        household.id = doc.getId();
                        household.name = doc.getString("name");
                        household.ownerId = doc.getString("ownerId");
                        households.add(household);
                    }
                    adapter.notifyDataSetChanged();
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (households.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load households", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showCreateHouseholdDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_household, null);
        TextInputEditText nameInput = dialogView.findViewById(R.id.household_name_input);
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.create_household)
                .setView(dialogView)
                .setPositiveButton(R.string.create_account, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createHousehold(name);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void createHousehold(String name) {
        String householdId = UUID.randomUUID().toString();
        
        Map<String, Object> household = new HashMap<>();
        household.put("name", name);
        household.put("ownerId", currentUser.getUid());
        household.put("members", List.of(currentUser.getUid()));
        household.put("createdAt", new Date());
        household.put("updatedAt", new Date());
        
        firestore.collection("households")
                .document(householdId)
                .set(household)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Household created", Toast.LENGTH_SHORT).show();
                    selectHousehold(householdId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create household", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void onHouseholdSelected(Household household) {
        selectHousehold(household.id);
    }
    
    private void selectHousehold(String householdId) {
        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_HOUSEHOLD_ID, householdId).apply();
        
        // Navigate to MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    public static String getSelectedHouseholdId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_HOUSEHOLD_ID, null);
    }
    
    // Simple Household model
    static class Household {
        String id;
        String name;
        String ownerId;
    }
    
    // RecyclerView Adapter
    static class HouseholdAdapter extends RecyclerView.Adapter<HouseholdAdapter.ViewHolder> {
        
        private final List<Household> households;
        private final OnHouseholdClickListener listener;
        
        interface OnHouseholdClickListener {
            void onHouseholdClick(Household household);
        }
        
        HouseholdAdapter(List<Household> households, OnHouseholdClickListener listener) {
            this.households = households;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_household, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Household household = households.get(position);
            holder.bind(household, listener);
        }
        
        @Override
        public int getItemCount() {
            return households.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialTextView nameText;
            
            ViewHolder(View view) {
                super(view);
                nameText = view.findViewById(R.id.household_name);
            }
            
            void bind(Household household, OnHouseholdClickListener listener) {
                nameText.setText(household.name);
                itemView.setOnClickListener(v -> listener.onHouseholdClick(household));
            }
        }
    }
}








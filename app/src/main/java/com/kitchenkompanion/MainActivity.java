package com.kitchenkompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kitchenkompanion.auth.AuthActivity;
import com.kitchenkompanion.features.barcode.BarcodeScannerActivity;

/**
 * Main activity for Kitchen Kompanion.
 * Hosts the navigation graph and manages authentication state.
 */
public class MainActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to AuthActivity
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        
        // Setup Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Setup bottom navigation
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
            
            // Setup action bar with nav controller
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.pantryFragment,
                    R.id.recipesFragment,
                    R.id.groceryFragment,
                    R.id.aiFragment
            ).build();
            
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_scan) {
            openBarcodeScanner();
            return true;
        } else if (id == R.id.action_settings) {
            // Navigate to settings
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
    
    private void openBarcodeScanner() {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);
        startActivity(intent);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    
    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}



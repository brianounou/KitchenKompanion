package com.kitchenkompanion.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kitchenkompanion.R;

import java.util.Arrays;
import java.util.List;

/**
 * Activity for user authentication using FirebaseUI.
 */
public class AuthActivity extends AppCompatActivity {
    
    private final FirebaseAuthUIActivityResultContract signInLauncher = 
            new FirebaseAuthUIActivityResultContract();
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Already signed in, proceed to household selection
            proceedToHouseholdSelection();
        } else {
            // Start sign-in flow
            startSignIn();
        }
    }
    
    private void startSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_KitchenKompanion)
                .setLogo(R.mipmap.ic_launcher)
                .setIsSmartLockEnabled(false)
                .build();
        
        registerForActivityResult(signInLauncher, this::onSignInResult)
                .launch(signInIntent);
    }
    
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                proceedToHouseholdSelection();
            }
        } else {
            // Sign in failed
            if (response == null) {
                // User cancelled sign in
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Error occurred
                Toast.makeText(this, "Sign in failed: " + response.getError(), 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void proceedToHouseholdSelection() {
        Intent intent = new Intent(this, HouseholdSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}








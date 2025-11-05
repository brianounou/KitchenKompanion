package com.kitchenkompanion.features.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kitchenkompanion.R;
import com.kitchenkompanion.databinding.FragmentAiBinding;

/**
 * Fragment for AI Assistant features powered by on-device AI.
 * 
 * Features:
 * - Recipe suggestions from pantry
 * - Meal planning assistance
 * - Grocery list generation
 * - Ingredient substitution suggestions
 * - General cooking Q&A
 * 
 * UI Flow Pseudocode:
 * ```
 * FUNCTION onCreate():
 *   // Step 1: Initialize ViewModel
 *   viewModel = getViewModel(AiViewModel)
 *   
 *   // Step 2: Check AI availability
 *   IF viewModel.isAiAvailable() THEN
 *     showAiFeatures()
 *     observeResponses()
 *   ELSE
 *     showUnavailableMessage()
 *   END IF
 * END FUNCTION
 * 
 * FUNCTION onFeatureButtonClick():
 *   // Disable buttons and show progress
 *   SET buttonsEnabled = false
 *   SHOW progressBar
 *   
 *   // Trigger AI operation via ViewModel
 *   viewModel.performAiOperation()
 *   
 *   // Wait for callback (handled by LiveData observer)
 * END FUNCTION
 * ```
 */
public class AiFragment extends Fragment {
    
    private FragmentAiBinding binding;
    private AiViewModel viewModel;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(AiViewModel.class);
        
        setupUI();
        observeData();
    }
    
    private void setupUI() {
        // Check AI availability
        if (!viewModel.isAiAvailable()) {
            binding.aiUnavailableCard.setVisibility(View.VISIBLE);
            binding.featuresCard.setVisibility(View.GONE);
            return;
        }
        
        binding.aiUnavailableCard.setVisibility(View.GONE);
        binding.featuresCard.setVisibility(View.VISIBLE);
        
        // Setup feature buttons
        binding.btnSuggestRecipes.setOnClickListener(v -> {
            viewModel.suggestRecipes();
        });
        
        binding.btnGenerateGrocery.setOnClickListener(v -> {
            viewModel.generateGroceryList();
        });
        
        binding.btnSubstitutes.setOnClickListener(v -> {
            showSubstituteDialog();
        });
        
        binding.btnAskQuestion.setOnClickListener(v -> {
            showQuestionDialog();
        });
    }
    
    private void observeData() {
        // Observe AI service type for display
        viewModel.getServiceType().observe(getViewLifecycleOwner(), serviceType -> {
            if (serviceType != null && binding.aiServiceTypeText != null) {
                binding.aiServiceTypeText.setText("AI Mode: " + serviceType);
            }
        });
        
        viewModel.getResponse().observe(getViewLifecycleOwner(), response -> {
            if (response != null && !response.isEmpty()) {
                showResponseDialog(response);
            }
        });
        
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            
            // Disable buttons while loading
            binding.btnSuggestRecipes.setEnabled(!loading);
            binding.btnGenerateGrocery.setEnabled(!loading);
            binding.btnSubstitutes.setEnabled(!loading);
            binding.btnAskQuestion.setEnabled(!loading);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showSubstituteDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_substitute, null);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_substitute_title)
                .setView(dialogView)
                .setPositiveButton(R.string.ask_ai, (dialog, which) -> {
                    // Get ingredient from dialog
                    com.google.android.material.textfield.TextInputEditText input = 
                            dialogView.findViewById(R.id.ingredient_input);
                    String ingredient = input.getText().toString().trim();
                    
                    if (!ingredient.isEmpty()) {
                        viewModel.suggestSubstitutes(ingredient);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void showQuestionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_question, null);
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_ask_question)
                .setView(dialogView)
                .setPositiveButton(R.string.ask_ai, (dialog, which) -> {
                    // Get question from dialog
                    com.google.android.material.textfield.TextInputEditText input = 
                            dialogView.findViewById(R.id.question_input);
                    String question = input.getText().toString().trim();
                    
                    if (!question.isEmpty()) {
                        viewModel.askQuestion(question);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    private void showResponseDialog(String response) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ai_response)
                .setMessage(response)
                .setPositiveButton(R.string.ok, null)
                .show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.kitchenkompanion.features.ai;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kitchenkompanion.KitchenKompanionApp;
import com.kitchenkompanion.data.local.AppDatabase;
import com.kitchenkompanion.data.local.ItemDao;
import com.kitchenkompanion.data.local.ItemEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for AI Assistant features.
 * 
 * This ViewModel bridges the UI layer with the on-device AI service.
 * It manages:
 * - AI service lifecycle
 * - Data preparation (fetching pantry items)
 * - Async operation handling
 * - LiveData state management for UI updates
 * 
 * Architecture:
 * UI Fragment → AiViewModel → OnDeviceAiService → AI Implementation
 * 
 * Pseudocode for typical flow:
 * ```
 * FUNCTION suggestRecipes():
 *   // Step 1: Validate state
 *   IF no_household_selected THEN
 *     RETURN error
 *   END IF
 *   
 *   // Step 2: Update UI state
 *   SET loading = true
 *   
 *   // Step 3: Fetch data asynchronously
 *   ASYNC DO
 *     items = database.getPantryItems()
 *     
 *     // Step 4: Prepare input
 *     ingredients = format(items)
 *     
 *     // Step 5: Call AI service
 *     aiService.suggestRecipes(ingredients, preferences, callback)
 *   END ASYNC
 * END FUNCTION
 * ```
 */
public class AiViewModel extends AndroidViewModel {
    
    private static final String TAG = "AiViewModel";
    
    private final OnDeviceAiService aiService;
    private final ItemDao itemDao;
    private final ExecutorService executorService;
    
    private final MutableLiveData<String> response = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> serviceType = new MutableLiveData<>();
    
    public AiViewModel(@NonNull Application application) {
        super(application);
        // Use factory to get appropriate AI service implementation
        this.aiService = AiServiceFactory.INSTANCE.getInstance(application, false);
        this.itemDao = AppDatabase.getInstance(application).itemDao();
        this.executorService = Executors.newSingleThreadExecutor();
        
        // Update service type for UI display
        updateServiceType();
    }
    
    public LiveData<String> getResponse() {
        return response;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public LiveData<String> getServiceType() {
        return serviceType;
    }
    
    public boolean isAiAvailable() {
        return aiService.isAvailable();
    }
    
    /**
     * Update service type display string.
     */
    private void updateServiceType() {
        String type = AiServiceFactory.INSTANCE.getServiceTypeName();
        serviceType.postValue(type);
    }
    
    /**
     * Toggle between mock and real AI service (if available).
     */
    public void toggleMockMode(boolean forceMock) {
        AiServiceFactory.INSTANCE.setForceMockMode(getApplication(), forceMock);
        updateServiceType();
    }
    
    /**
     * Get AI-powered recipe suggestions based on pantry items.
     */
    public void suggestRecipes() {
        String householdId = KitchenKompanionApp.getInstance().getCurrentHouseholdId();
        if (householdId == null) {
            error.postValue("No household selected");
            return;
        }
        
        loading.postValue(true);
        
        executorService.execute(() -> {
            // Get pantry items
            List<ItemEntity> items = itemDao.getAllItemsSync(householdId);
            
            if (items == null || items.isEmpty()) {
                error.postValue("Your pantry is empty. Add items first!");
                loading.postValue(false);
                return;
            }
            
            // Build ingredient list
            StringBuilder ingredients = new StringBuilder();
            for (int i = 0; i < items.size() && i < 15; i++) {
                if (i > 0) ingredients.append(", ");
                ingredients.append(items.get(i).name);
            }
            
            Log.d(TAG, "Requesting recipe suggestions with: " + ingredients);
            
            aiService.suggestRecipes(
                    ingredients.toString(),
                    "", // No preferences for now
                    new OnDeviceAiService.AiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            response.postValue(result);
                            loading.postValue(false);
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            error.postValue(errorMessage);
                            loading.postValue(false);
                        }
                    }
            );
        });
    }
    
    /**
     * Generate a smart grocery list using AI.
     */
    public void generateGroceryList() {
        String householdId = KitchenKompanionApp.getInstance().getCurrentHouseholdId();
        if (householdId == null) {
            error.postValue("No household selected");
            return;
        }
        
        loading.postValue(true);
        
        executorService.execute(() -> {
            // Get pantry items
            List<ItemEntity> items = itemDao.getAllItemsSync(householdId);
            
            StringBuilder pantryList = new StringBuilder();
            if (items != null) {
                for (ItemEntity item : items) {
                    pantryList.append(item.name).append(", ");
                }
            }
            
            String mealPlan = "Weekly family meals"; // Placeholder
            
            aiService.generateGroceryList(
                    pantryList.toString(),
                    mealPlan,
                    new OnDeviceAiService.AiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            response.postValue(result);
                            loading.postValue(false);
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            error.postValue(errorMessage);
                            loading.postValue(false);
                        }
                    }
            );
        });
    }
    
    /**
     * Get ingredient substitution suggestions.
     */
    public void suggestSubstitutes(String ingredient) {
        loading.postValue(true);
        
        aiService.suggestSubstitutes(
                ingredient,
                "", // No recipe context for now
                new OnDeviceAiService.AiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        response.postValue(result);
                        loading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        error.postValue(errorMessage);
                        loading.postValue(false);
                    }
                }
        );
    }
    
    /**
     * Ask a general cooking/meal planning question.
     */
    public void askQuestion(String question) {
        loading.postValue(true);
        
        aiService.chat(
                question,
                "You are a helpful kitchen assistant.",
                new OnDeviceAiService.AiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        response.postValue(result);
                        loading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        error.postValue(errorMessage);
                        loading.postValue(false);
                    }
                }
        );
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        // Note: Don't cleanup aiService here as it's a singleton managed by factory
        Log.d(TAG, "AiViewModel cleared");
    }
}




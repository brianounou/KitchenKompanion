package com.kitchenkompanion.features.recipes;

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
import com.kitchenkompanion.data.remote.RecipeService;
import com.kitchenkompanion.data.remote.dto.RecipeSearchResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for recipes feature.
 */
public class RecipesViewModel extends AndroidViewModel {
    
    private static final String TAG = "RecipesViewModel";
    
    private final RecipeService recipeService;
    private final ItemDao itemDao;
    private final ExecutorService executorService;
    
    private final MutableLiveData<List<RecipeSearchResponse>> recipes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    public RecipesViewModel(@NonNull Application application) {
        super(application);
        this.recipeService = RecipeService.getInstance(application);
        this.itemDao = AppDatabase.getInstance(application).itemDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<RecipeSearchResponse>> getRecipes() {
        return recipes;
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    /**
     * Search for recipes using ingredients from the current household's pantry.
     */
    public void searchByPantryIngredients() {
        String householdId = KitchenKompanionApp.getInstance().getCurrentHouseholdId();
        if (householdId == null) {
            error.postValue("No household selected");
            return;
        }
        
        loading.postValue(true);
        
        executorService.execute(() -> {
            // Get all pantry items
            List<ItemEntity> items = itemDao.getAllItemsSync(householdId);
            
            if (items == null || items.isEmpty()) {
                error.postValue("Your pantry is empty. Add items first!");
                loading.postValue(false);
                return;
            }
            
            // Build comma-separated ingredient list
            StringBuilder ingredientList = new StringBuilder();
            for (int i = 0; i < items.size() && i < 10; i++) { // Limit to 10 ingredients
                if (i > 0) ingredientList.append(",");
                ingredientList.append(items.get(i).name);
            }
            
            Log.d(TAG, "Searching recipes with ingredients: " + ingredientList);
            
            // Search recipes
            recipeService.findByIngredients(
                    ingredientList.toString(),
                    20, // Get 20 results
                    new RecipeService.RecipeSearchCallback() {
                        @Override
                        public void onSuccess(List<RecipeSearchResponse> recipeList) {
                            recipes.postValue(recipeList);
                            loading.postValue(false);
                            
                            if (recipeList.isEmpty()) {
                                error.postValue("No recipes found with your ingredients");
                            }
                        }
                        
                        @Override
                        public void onError(String errorMessage) {
                            error.postValue(errorMessage);
                            loading.postValue(false);
                            recipes.postValue(new ArrayList<>()); // Empty list
                        }
                    }
            );
        });
    }
}







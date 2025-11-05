package com.kitchenkompanion.features.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.kitchenkompanion.R;
import com.kitchenkompanion.databinding.FragmentRecipesBinding;

/**
 * Fragment for recipe browsing and suggestions.
 * 
 * Features:
 * - Search recipes from pantry ingredients
 * - Browse recipe suggestions
 * - View recipe details
 * - Save favorite recipes (TODO)
 */
public class RecipesFragment extends Fragment {
    
    private FragmentRecipesBinding binding;
    private RecipesViewModel viewModel;
    private RecipeAdapter adapter;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(RecipesViewModel.class);
        
        setupRecyclerView();
        setupFab();
        observeData();
    }
    
    private void setupRecyclerView() {
        adapter = new RecipeAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        
        adapter.setOnItemClickListener(recipe -> {
            // Open recipe detail
            Toast.makeText(requireContext(), "Recipe: " + recipe.title, Toast.LENGTH_SHORT).show();
            // TODO: Open RecipeDetailActivity
        });
    }
    
    private void setupFab() {
        binding.fabSearchPantry.setOnClickListener(v -> {
            viewModel.searchByPantryIngredients();
            Snackbar.make(binding.getRoot(), 
                    R.string.searching_recipes, Snackbar.LENGTH_SHORT).show();
        });
    }
    
    private void observeData() {
        viewModel.getRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                adapter.submitList(recipes);
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            }
        });
        
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.recipes_menu, menu);
        
        // Setup search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_recipes));
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Search by query
                Toast.makeText(requireContext(), "Search: " + query, Toast.LENGTH_SHORT).show();
                // TODO: Implement text search
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

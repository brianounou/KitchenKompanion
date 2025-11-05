package com.kitchenkompanion.features.ai

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

/**
 * Intelligent mock AI service with context-aware responses.
 * 
 * This implementation provides realistic, contextually relevant responses
 * without requiring a heavyweight LLM model. It uses rule-based logic
 * and template-based generation to simulate AI behavior.
 * 
 * Architecture:
 * 1. Parse user input to extract key entities (ingredients, preferences, etc.)
 * 2. Apply domain-specific rules and templates
 * 3. Generate structured responses with realistic formatting
 * 4. Simulate processing delays for realism
 * 
 * Pseudocode for recipe generation:
 * ```
 * FUNCTION suggestRecipes(ingredients, preferences, callback):
 *   // Step 1: Parse ingredients
 *   ingredientList = parseIngredients(ingredients)
 *   
 *   // Step 2: Apply dietary filters
 *   validRecipes = FILTER recipes WHERE compatible(preferences)
 *   
 *   // Step 3: Match ingredients
 *   rankedRecipes = RANK validRecipes BY ingredientMatch(ingredientList)
 *   
 *   // Step 4: Generate formatted response
 *   response = formatRecipes(rankedRecipes[0:3])
 *   
 *   // Step 5: Return asynchronously
 *   DELAY(simulate_processing_time)
 *   callback.onSuccess(response)
 * END FUNCTION
 * ```
 */
class IntelligentMockAiService(private val context: Context) : OnDeviceAiService {
    
    companion object {
        private const val TAG = "IntelligentMockAI"
        
        // Simulated processing times (milliseconds)
        private const val RECIPE_PROCESSING_TIME = 1200L
        private const val GROCERY_PROCESSING_TIME = 1000L
        private const val SUBSTITUTE_PROCESSING_TIME = 800L
        private const val CHAT_PROCESSING_TIME = 900L
        
        // Common ingredient keywords for pattern matching
        private val PROTEIN_KEYWORDS = setOf("chicken", "beef", "pork", "fish", "tofu", "eggs", "turkey", "lamb")
        private val VEGETABLE_KEYWORDS = setOf("tomato", "onion", "garlic", "pepper", "carrot", "broccoli", "spinach", "lettuce", "potato", "mushroom")
        private val GRAIN_KEYWORDS = setOf("rice", "pasta", "bread", "flour", "quinoa", "oats", "noodles")
        private val DAIRY_KEYWORDS = setOf("milk", "cheese", "butter", "yogurt", "cream")
        
        // Recipe templates organized by main ingredient type
        private val RECIPE_TEMPLATES = mapOf(
            "chicken" to listOf(
                RecipeTemplate("Herb-Roasted Chicken", "Juicy chicken with aromatic herbs and crispy skin", 45, "oven"),
                RecipeTemplate("Quick Chicken Stir-Fry", "Asian-inspired stir-fry with fresh vegetables", 20, "stovetop"),
                RecipeTemplate("Creamy Chicken Pasta", "Comfort food with rich cream sauce", 30, "stovetop")
            ),
            "beef" to listOf(
                RecipeTemplate("Classic Beef Stir-Fry", "Tender beef with colorful vegetables", 25, "stovetop"),
                RecipeTemplate("Beef and Vegetable Stew", "Hearty slow-cooked comfort meal", 120, "slow cooker"),
                RecipeTemplate("Quick Beef Tacos", "Easy weeknight dinner with bold flavors", 20, "stovetop")
            ),
            "pasta" to listOf(
                RecipeTemplate("Garlic Olive Oil Pasta", "Simple Italian classic (Aglio e Olio)", 15, "stovetop"),
                RecipeTemplate("Tomato Basil Pasta", "Fresh and light Mediterranean dish", 20, "stovetop"),
                RecipeTemplate("Creamy Vegetable Pasta", "Hearty pasta with seasonal vegetables", 25, "stovetop")
            ),
            "vegetable" to listOf(
                RecipeTemplate("Roasted Vegetable Medley", "Colorful sheet-pan vegetables", 35, "oven"),
                RecipeTemplate("Quick Vegetable Stir-Fry", "Crisp-tender vegetables with savory sauce", 15, "stovetop"),
                RecipeTemplate("Hearty Vegetable Soup", "Warming, nutritious comfort bowl", 40, "stovetop")
            ),
            "fish" to listOf(
                RecipeTemplate("Lemon Herb Baked Fish", "Light and flaky with bright flavors", 25, "oven"),
                RecipeTemplate("Pan-Seared Fish", "Crispy skin with tender, moist flesh", 20, "stovetop"),
                RecipeTemplate("Fish Tacos", "Fresh and zesty weeknight favorite", 25, "stovetop")
            )
        )
        
        // Substitution database
        private val SUBSTITUTIONS = mapOf(
            "butter" to listOf("olive oil", "coconut oil", "margarine", "ghee"),
            "milk" to listOf("almond milk", "soy milk", "oat milk", "coconut milk"),
            "eggs" to listOf("flax eggs (1 tbsp ground flax + 3 tbsp water)", "chia eggs", "mashed banana", "applesauce"),
            "flour" to listOf("almond flour", "coconut flour", "oat flour", "rice flour"),
            "sugar" to listOf("honey", "maple syrup", "stevia", "agave nectar"),
            "chicken" to listOf("turkey", "tofu", "tempeh", "seitan"),
            "beef" to listOf("ground turkey", "plant-based meat", "mushrooms", "lentils"),
            "cheese" to listOf("nutritional yeast", "cashew cheese", "vegan cheese", "tofu ricotta"),
            "soy sauce" to listOf("tamari", "coconut aminos", "Worcestershire sauce", "liquid aminos"),
            "yogurt" to listOf("Greek yogurt", "coconut yogurt", "sour cream", "mashed avocado")
        )
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private data class RecipeTemplate(val name: String, val description: String, val cookingTime: Int, val method: String)
    
    override fun isAvailable(): Boolean {
        // Mock service is always available
        return true
    }
    
    override fun suggestRecipes(ingredients: String, preferences: String?, callback: OnDeviceAiService.AiCallback) {
        Log.d(TAG, "Generating recipe suggestions for: $ingredients")
        
        // Simulate async processing
        handler.postDelayed({
            try {
                val response = generateRecipeSuggestions(ingredients, preferences)
                callback.onSuccess(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating recipes", e)
                callback.onError("Failed to generate recipe suggestions: ${e.message}")
            }
        }, RECIPE_PROCESSING_TIME)
    }
    
    override fun generateGroceryList(pantryItems: String, mealPlan: String, callback: OnDeviceAiService.AiCallback) {
        Log.d(TAG, "Generating grocery list for meal plan: $mealPlan")
        
        handler.postDelayed({
            try {
                val response = generateSmartGroceryList(pantryItems, mealPlan)
                callback.onSuccess(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating grocery list", e)
                callback.onError("Failed to generate grocery list: ${e.message}")
            }
        }, GROCERY_PROCESSING_TIME)
    }
    
    override fun suggestSubstitutes(ingredient: String, recipe: String?, callback: OnDeviceAiService.AiCallback) {
        Log.d(TAG, "Finding substitutes for: $ingredient")
        
        handler.postDelayed({
            try {
                val response = generateSubstituteSuggestions(ingredient, recipe)
                callback.onSuccess(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating substitutes", e)
                callback.onError("Failed to generate substitutes: ${e.message}")
            }
        }, SUBSTITUTE_PROCESSING_TIME)
    }
    
    override fun chat(userMessage: String, context: String?, callback: OnDeviceAiService.AiCallback) {
        Log.d(TAG, "Processing chat message: $userMessage")
        
        handler.postDelayed({
            try {
                val response = generateChatResponse(userMessage, context)
                callback.onSuccess(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing chat", e)
                callback.onError("Failed to process message: ${e.message}")
            }
        }, CHAT_PROCESSING_TIME)
    }
    
    /**
     * Generate contextually relevant recipe suggestions.
     * 
     * Algorithm:
     * 1. Parse ingredients from input string
     * 2. Identify primary ingredient category (protein, grain, vegetable)
     * 3. Select appropriate recipe templates
     * 4. Customize templates with actual ingredients
     * 5. Add missing ingredients analysis
     * 6. Format as structured text
     */
    private fun generateRecipeSuggestions(ingredients: String, preferences: String?): String {
        val ingredientList = parseIngredients(ingredients)
        val primaryCategory = identifyPrimaryCategory(ingredientList)
        val templates = RECIPE_TEMPLATES[primaryCategory] ?: RECIPE_TEMPLATES["vegetable"]!!
        
        val result = StringBuilder()
        result.append("Based on your ingredients, here are 3 recipe suggestions:\n\n")
        
        templates.take(3).forEachIndexed { index, template ->
            result.append("${index + 1}. **${template.name}**\n")
            result.append("   ${template.description}\n")
            result.append("   Cooking time: ${template.cookingTime} minutes\n")
            result.append("   Method: ${template.method}\n")
            
            // Add context about using available ingredients
            val usedIngredients = ingredientList.filter { it in template.description.lowercase() || it in template.name.lowercase() }
            if (usedIngredients.isNotEmpty()) {
                result.append("   Uses: ${usedIngredients.joinToString(", ")}\n")
            }
            
            result.append("\n")
        }
        
        // Add dietary preference note if specified
        if (!preferences.isNullOrBlank()) {
            result.append("Note: Recipes can be adapted for $preferences preferences.\n")
        }
        
        result.append("\nAI-powered suggestions based on your pantry.")
        
        return result.toString()
    }
    
    /**
     * Generate smart grocery list based on meal plan.
     */
    private fun generateSmartGroceryList(pantryItems: String, mealPlan: String): String {
        val pantry = parseIngredients(pantryItems)
        val mealKeywords = extractKeywords(mealPlan)
        
        // Generate contextual grocery items
        val groceryCategories = mutableMapOf<String, MutableList<String>>()
        
        // Produce section
        if (!hasCategory(pantry, VEGETABLE_KEYWORDS)) {
            groceryCategories["Fresh Produce"] = mutableListOf(
                "Fresh tomatoes",
                "Onions (yellow and red)",
                "Garlic cloves",
                "Bell peppers (assorted colors)",
                "Fresh herbs (basil, parsley)"
            )
        }
        
        // Protein section
        if (!hasCategory(pantry, PROTEIN_KEYWORDS) || mealKeywords.any { it in PROTEIN_KEYWORDS }) {
            groceryCategories["Proteins"] = mutableListOf(
                "Chicken breast (1 lb)",
                "Ground beef (1 lb)",
                "Eggs (dozen)"
            )
        }
        
        // Pantry staples
        groceryCategories["Pantry Staples"] = mutableListOf(
            "Olive oil",
            "Salt and pepper",
            "Pasta (if needed)",
            "Rice (if needed)"
        )
        
        // Dairy section
        if (!hasCategory(pantry, DAIRY_KEYWORDS)) {
            groceryCategories["Dairy"] = mutableListOf(
                "Milk",
                "Butter",
                "Cheese (cheddar or preferred type)"
            )
        }
        
        val result = StringBuilder()
        result.append("Smart Grocery List\n")
        result.append("Based on your meal plan: \"$mealPlan\"\n\n")
        
        groceryCategories.forEach { (category, items) ->
            result.append("$category:\n")
            items.forEach { item ->
                result.append("  • $item\n")
            }
            result.append("\n")
        }
        
        result.append("Tip: Cross-check with your current pantry to avoid duplicates.")
        
        return result.toString()
    }
    
    /**
     * Generate ingredient substitution suggestions.
     */
    private fun generateSubstituteSuggestions(ingredient: String, recipe: String?): String {
        val normalizedIngredient = ingredient.lowercase().trim()
        
        // Try exact match first
        val substitutes = SUBSTITUTIONS[normalizedIngredient]
            ?: findPartialMatch(normalizedIngredient)
            ?: listOf("Similar items in your pantry", "Generic store brand alternative", "Adjust recipe to omit if optional")
        
        val result = StringBuilder()
        result.append("Substitutions for **$ingredient**:\n\n")
        
        substitutes.forEachIndexed { index, substitute ->
            result.append("${index + 1}. $substitute\n")
        }
        
        result.append("\n")
        
        if (recipe != null && recipe.isNotBlank()) {
            result.append("Context: Works well in $recipe\n")
        }
        
        result.append("\nNote: Adjust proportions as needed. Some substitutes may alter taste or texture slightly.")
        
        return result.toString()
    }
    
    /**
     * Generate conversational chat response.
     */
    private fun generateChatResponse(userMessage: String, context: String?): String {
        val lowerMessage = userMessage.lowercase()
        
        // Pattern matching for common queries
        return when {
            "how long" in lowerMessage && ("cook" in lowerMessage || "bake" in lowerMessage) -> {
                "Cooking times vary by recipe and method:\n" +
                        "• Stir-fry: 15-20 minutes\n" +
                        "• Baked dishes: 30-45 minutes\n" +
                        "• Slow cooker: 4-8 hours\n" +
                        "• Instant pot: 15-30 minutes\n\n" +
                        "What are you planning to cook?"
            }
            "store" in lowerMessage || "keep" in lowerMessage -> {
                "Storage tips:\n" +
                        "• Fresh produce: Refrigerate in crisper drawer (3-7 days)\n" +
                        "• Cooked meals: Refrigerate in airtight containers (3-4 days)\n" +
                        "• Dry goods: Cool, dry pantry (months to years)\n" +
                        "• Frozen items: Freezer at 0°F (-18°C) (3-12 months)\n\n" +
                        "Always check for signs of spoilage before use."
            }
            "substitute" in lowerMessage || "replace" in lowerMessage -> {
                "I can help with ingredient substitutions! Tell me:\n" +
                        "1. What ingredient do you need to replace?\n" +
                        "2. What recipe are you making?\n\n" +
                        "I'll suggest appropriate alternatives that work well."
            }
            "meal prep" in lowerMessage || "meal plan" in lowerMessage -> {
                "Meal planning tips:\n" +
                        "1. Plan 3-5 dinners per week\n" +
                        "2. Choose recipes with overlapping ingredients\n" +
                        "3. Prep ingredients on weekends\n" +
                        "4. Use your pantry items first\n" +
                        "5. Include one \"leftover\" night\n\n" +
                        "Would you like help creating a meal plan?"
            }
            "recipe" in lowerMessage -> {
                "I can suggest recipes based on your ingredients! Just tell me:\n" +
                        "• What ingredients you have\n" +
                        "• Any dietary preferences\n" +
                        "• How much time you have\n\n" +
                        "I'll provide personalized recipe suggestions."
            }
            "?" in lowerMessage || "help" in lowerMessage -> {
                "I'm your Kitchen Kompanion AI assistant! I can help with:\n\n" +
                        "✓ Recipe suggestions from your ingredients\n" +
                        "✓ Smart grocery list generation\n" +
                        "✓ Ingredient substitutions\n" +
                        "✓ Cooking tips and techniques\n" +
                        "✓ Meal planning advice\n" +
                        "✓ Food storage guidance\n\n" +
                        "What would you like help with today?"
            }
            else -> {
                "Thanks for your question: \"$userMessage\"\n\n" +
                        "I'm your on-device AI cooking assistant. While I can help with recipes, " +
                        "substitutions, and meal planning, I might need more specific information " +
                        "to give you the best answer.\n\n" +
                        "Try asking about:\n" +
                        "• Recipe suggestions\n" +
                        "• Ingredient substitutions\n" +
                        "• Cooking times and methods\n" +
                        "• Meal planning strategies"
            }
        }
    }
    
    // Helper functions
    
    /**
     * Parse ingredient string into list of normalized keywords.
     */
    private fun parseIngredients(ingredients: String): List<String> {
        return ingredients
            .lowercase()
            .split(",", ";", "\n", " and ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
    
    /**
     * Extract keywords from text.
     */
    private fun extractKeywords(text: String): List<String> {
        return text
            .lowercase()
            .split(" ", ",", ".", ";")
            .map { it.trim() }
            .filter { it.length > 3 }
    }
    
    /**
     * Identify primary ingredient category.
     */
    private fun identifyPrimaryCategory(ingredients: List<String>): String {
        // Check each category in priority order
        for (ingredient in ingredients) {
            when {
                PROTEIN_KEYWORDS.any { it in ingredient } -> return PROTEIN_KEYWORDS.find { it in ingredient } ?: "chicken"
                GRAIN_KEYWORDS.any { it in ingredient } -> return "pasta"
            }
        }
        return "vegetable" // Default fallback
    }
    
    /**
     * Check if pantry has items from category.
     */
    private fun hasCategory(pantry: List<String>, category: Set<String>): Boolean {
        return pantry.any { item -> category.any { keyword -> keyword in item } }
    }
    
    /**
     * Find partial match in substitution database.
     */
    private fun findPartialMatch(ingredient: String): List<String>? {
        for ((key, substitutes) in SUBSTITUTIONS) {
            if (key in ingredient || ingredient in key) {
                return substitutes
            }
        }
        return null
    }
    
    override fun cancel() {
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Cancelled all pending AI operations")
    }
    
    override fun cleanup() {
        cancel()
        Log.d(TAG, "Cleaned up IntelligentMockAiService")
    }
}





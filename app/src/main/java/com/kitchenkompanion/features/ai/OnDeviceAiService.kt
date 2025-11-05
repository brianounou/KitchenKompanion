package com.kitchenkompanion.features.ai

/**
 * Interface for on-device AI services.
 * 
 * This abstraction allows swapping between different AI implementations:
 * - IntelligentMockAiService: Enhanced rule-based system with context awareness
 * - Future: Real LLM integration (TinyLlama, Phi-2, etc.)
 * 
 * All implementations must:
 * 1. Run inference entirely on-device (no network calls for inference)
 * 2. Handle errors gracefully with fallback responses
 * 3. Support cancellation for long-running operations
 * 4. Provide availability status
 * 
 * Pseudocode for typical usage:
 * ```
 * FUNCTION use_ai_service():
 *   service = AiServiceFactory.create(context)
 *   
 *   IF service.isAvailable() THEN
 *     service.suggestRecipes(ingredients, preferences, callback)
 *   ELSE
 *     show_error("AI not available")
 *   END IF
 * END FUNCTION
 * ```
 */
interface OnDeviceAiService {
    
    /**
     * Check if the AI service is available and ready.
     * 
     * @return true if the service can process requests, false otherwise
     */
    fun isAvailable(): Boolean
    
    /**
     * Callback interface for AI operations.
     */
    interface AiCallback {
        /**
         * Called when AI processing succeeds.
         * @param response The generated response text
         */
        fun onSuccess(response: String)
        
        /**
         * Called when AI processing fails.
         * @param error Error message describing the failure
         */
        fun onError(error: String)
        
        /**
         * Optional: Called during streaming generation (if supported).
         * @param partialResponse Partial response text generated so far
         */
        fun onProgress(partialResponse: String) {
            // Default: no-op, implementations can override
        }
    }
    
    /**
     * Generate recipe suggestions based on available ingredients.
     * 
     * Pseudocode:
     * ```
     * FUNCTION suggestRecipes(ingredients, preferences, callback):
     *   prompt = buildRecipePrompt(ingredients, preferences)
     *   
     *   TRY
     *     response = generate(prompt)
     *     callback.onSuccess(response)
     *   CATCH error
     *     callback.onError(error.message)
     *   END TRY
     * END FUNCTION
     * ```
     * 
     * @param ingredients List of available ingredients (comma-separated or formatted)
     * @param preferences Dietary preferences/restrictions (optional, can be null/empty)
     * @param callback Callback to receive results
     */
    fun suggestRecipes(ingredients: String, preferences: String?, callback: AiCallback)
    
    /**
     * Generate a smart grocery list based on meal plan and current pantry.
     * 
     * @param pantryItems Current pantry items
     * @param mealPlan Planned meals for the week
     * @param callback Callback to receive results
     */
    fun generateGroceryList(pantryItems: String, mealPlan: String, callback: AiCallback)
    
    /**
     * Get ingredient substitution suggestions.
     * 
     * @param ingredient The ingredient to substitute
     * @param recipe The recipe context (optional)
     * @param callback Callback to receive results
     */
    fun suggestSubstitutes(ingredient: String, recipe: String?, callback: AiCallback)
    
    /**
     * Chat with AI assistant about cooking/meal planning.
     * 
     * @param userMessage User's question
     * @param context Conversation context
     * @param callback Callback to receive results
     */
    fun chat(userMessage: String, context: String?, callback: AiCallback)
    
    /**
     * Cancel any ongoing AI operations (if supported).
     */
    fun cancel() {
        // Default: no-op, implementations can override
    }
    
    /**
     * Release resources when service is no longer needed.
     */
    fun cleanup() {
        // Default: no-op, implementations can override
    }
}





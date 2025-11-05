package com.kitchenkompanion.features.ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Factory for creating AI service instances.
 * 
 * This factory implements the strategy pattern to select the appropriate
 * AI service implementation based on:
 * 1. Model availability
 * 2. Device capabilities
 * 3. User preferences
 * 4. Fallback requirements
 * 
 * Decision flow pseudocode:
 * ```
 * FUNCTION createAiService(context):
 *   preferences = getSharedPreferences(context)
 *   
 *   // Check if user forced mock mode
 *   IF preferences.getBoolean("force_mock_ai") THEN
 *     RETURN IntelligentMockAiService(context)
 *   END IF
 *   
 *   // Check if real LLM model is available
 *   IF modelFileExists() AND deviceHasSufficientMemory() THEN
 *     TRY
 *       service = RealLlmService(context)
 *       IF service.initialize() THEN
 *         RETURN service
 *       END IF
 *     CATCH error
 *       LOG("Real LLM failed, falling back to mock")
 *     END TRY
 *   END IF
 *   
 *   // Fallback to mock service
 *   RETURN IntelligentMockAiService(context)
 * END FUNCTION
 * ```
 * 
 * The factory maintains a singleton instance for efficiency but allows
 * recreation if settings change.
 */
object AiServiceFactory {
    
    private const val TAG = "AiServiceFactory"
    private const val PREFS_NAME = "ai_service_prefs"
    private const val KEY_FORCE_MOCK = "force_mock_ai"
    private const val KEY_MODEL_PATH = "llm_model_path"
    
    @Volatile
    private var instance: OnDeviceAiService? = null
    
    /**
     * Get or create AI service instance.
     * 
     * @param context Application context
     * @param forceRecreate Force recreation of service (useful after settings change)
     * @return OnDeviceAiService implementation
     */
    @Synchronized
    fun getInstance(context: Context, forceRecreate: Boolean = false): OnDeviceAiService {
        // Return existing instance if available and not forcing recreation
        if (instance != null && !forceRecreate) {
            return instance!!
        }
        
        // Clean up old instance if recreating
        if (forceRecreate && instance != null) {
            instance?.cleanup()
            instance = null
        }
        
        // Create new instance
        val newInstance = createService(context.applicationContext)
        instance = newInstance
        
        Log.i(TAG, "Created AI service: ${newInstance::class.java.simpleName}")
        return newInstance
    }
    
    /**
     * Create appropriate AI service based on current conditions.
     */
    private fun createService(context: Context): OnDeviceAiService {
        val prefs = getPreferences(context)
        
        // Check if user explicitly wants mock service
        if (prefs.getBoolean(KEY_FORCE_MOCK, false)) {
            Log.d(TAG, "Using mock service (forced by user)")
            return IntelligentMockAiService(context)
        }
        
        // TODO: Future enhancement - check for real LLM model
        // val modelPath = prefs.getString(KEY_MODEL_PATH, null)
        // if (modelPath != null && File(modelPath).exists()) {
        //     try {
        //         val llmService = RealLlmService(context, modelPath)
        //         if (llmService.initialize()) {
        //             Log.i(TAG, "Using real LLM service")
        //             return llmService
        //         }
        //     } catch (e: Exception) {
        //         Log.w(TAG, "Failed to initialize real LLM, falling back to mock", e)
        //     }
        // }
        
        // Default: use intelligent mock service
        Log.d(TAG, "Using intelligent mock service (default)")
        return IntelligentMockAiService(context)
    }
    
    /**
     * Get shared preferences for AI service.
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Enable or disable mock AI mode.
     * 
     * @param context Application context
     * @param forceMock true to force mock mode, false to allow real LLM if available
     */
    fun setForceMockMode(context: Context, forceMock: Boolean) {
        getPreferences(context).edit()
            .putBoolean(KEY_FORCE_MOCK, forceMock)
            .apply()
        
        Log.i(TAG, "Force mock mode: $forceMock")
        
        // Recreate service with new settings
        getInstance(context, forceRecreate = true)
    }
    
    /**
     * Check if currently using mock service.
     */
    fun isUsingMockService(): Boolean {
        return instance is IntelligentMockAiService
    }
    
    /**
     * Get current service type name for display.
     */
    fun getServiceTypeName(): String {
        return when (instance) {
            is IntelligentMockAiService -> "Mock AI (Rule-based)"
            null -> "Not initialized"
            else -> "Real LLM"
        }
    }
    
    /**
     * Release all resources.
     */
    fun cleanup() {
        instance?.cleanup()
        instance = null
        Log.d(TAG, "Factory cleanup complete")
    }
}





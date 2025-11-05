# AI Design and Architecture

## Overview

Kitchen Kompanion uses an on-device AI system to provide intelligent cooking assistance without requiring network connectivity for inference. The architecture is designed to be extensible, allowing future integration of real LLM models while maintaining a functional fallback system.

## Design Rationale

### Why On-Device AI?

1. **Privacy**: All user data (pantry items, preferences, queries) stays on the device
2. **Speed**: No network latency for AI responses
3. **Reliability**: Works offline and in poor connectivity
4. **Cost**: No API usage fees or rate limits
5. **User Experience**: Instant responses improve app usability

### Why Not Gemini Nano?

Gemini Nano (Google's on-device LLM via AICore) has the following limitations as of 2025:
- Limited device availability (requires specific hardware)
- API not yet publicly available for general use
- Requires Android 14+ (API 34+)
- Large model size impacts app distribution and storage

Instead, we implemented an **Intelligent Mock AI Service** that:
- Works on all Android versions (API 24+)
- Provides contextually relevant responses using rule-based logic
- Has minimal resource footprint
- Serves as a foundation for future LLM integration

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer                            │
│  ┌──────────────┐         ┌──────────────┐            │
│  │  AiFragment  │────────>│  AiViewModel  │            │
│  └──────────────┘         └───────┬───────┘            │
└─────────────────────────────────────┼────────────────────┘
                                      │
┌─────────────────────────────────────┼────────────────────┐
│                   Service Layer     │                    │
│                      ┌──────────────▼──────────────┐     │
│                      │   AiServiceFactory          │     │
│                      └──────────────┬──────────────┘     │
│                                     │ creates            │
│                      ┌──────────────▼──────────────┐     │
│                      │   OnDeviceAiService         │     │
│                      │   (interface)               │     │
│                      └──────────────┬──────────────┘     │
│                                     │                    │
│              ┌──────────────────────┴──────────┐         │
│              │                                 │         │
│   ┌──────────▼──────────┐         ┌───────────▼───────┐ │
│   │ IntelligentMock     │         │  Future: RealLLM  │ │
│   │ AiService           │         │  Service          │ │
│   │ (rule-based)        │         │  (TinyLlama/etc)  │ │
│   └─────────────────────┘         └───────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. OnDeviceAiService (Interface)

**Location**: `app/src/main/java/com/kitchenkompanion/features/ai/OnDeviceAiService.kt`

**Purpose**: Abstraction layer defining the contract for all AI implementations.

**Methods**:
- `isAvailable(): Boolean` - Check if service is ready
- `suggestRecipes(ingredients, preferences, callback)` - Generate recipe ideas
- `generateGroceryList(pantryItems, mealPlan, callback)` - Create shopping list
- `suggestSubstitutes(ingredient, recipe, callback)` - Find ingredient alternatives
- `chat(userMessage, context, callback)` - Conversational Q&A
- `cancel()` - Stop ongoing operations
- `cleanup()` - Release resources

**Callback Interface**:
```kotlin
interface AiCallback {
    fun onSuccess(response: String)
    fun onError(error: String)
    fun onProgress(partialResponse: String)  // Optional, for streaming
}
```

#### 2. AiServiceFactory (Singleton)

**Location**: `app/src/main/java/com/kitchenkompanion/features/ai/AiServiceFactory.kt`

**Purpose**: Factory implementing strategy pattern to select appropriate AI implementation.

**Decision Logic**:
```
FUNCTION createService(context):
    preferences = SharedPreferences.load()
    
    IF user_force_mock_mode THEN
        RETURN IntelligentMockAiService
    END IF
    
    IF real_llm_model_available() AND device_has_memory() THEN
        TRY
            service = RealLlmService(model_path)
            IF service.initialize_success() THEN
                RETURN service
            END IF
        CATCH exception
            LOG_WARNING("LLM failed, falling back to mock")
        END TRY
    END IF
    
    RETURN IntelligentMockAiService  // Safe fallback
END FUNCTION
```

**Key Features**:
- Singleton instance management
- Lazy initialization
- Graceful degradation on failure
- Force-mock mode for testing/debugging

#### 3. IntelligentMockAiService (Implementation)

**Location**: `app/src/main/java/com/kitchenkompanion/features/ai/IntelligentMockAiService.kt`

**Purpose**: Rule-based AI system providing contextually relevant responses.

**Core Algorithms**:

##### Recipe Suggestion Algorithm

```
FUNCTION suggestRecipes(ingredients, preferences):
    // Step 1: Parse and normalize input
    ingredient_list = PARSE_INGREDIENTS(ingredients)
    // ["chicken", "onion", "garlic", "rice"]
    
    // Step 2: Categorize ingredients
    primary_category = IDENTIFY_PRIMARY_CATEGORY(ingredient_list)
    // Checks for protein > grain > vegetable
    // Result: "chicken"
    
    // Step 3: Select matching templates
    templates = RECIPE_TEMPLATES[primary_category]
    // Returns: [HerbRoastedChicken, ChickenStirFry, CreamyChickenPasta]
    
    // Step 4: Customize and rank
    FOR EACH template IN templates:
        score = CALCULATE_MATCH_SCORE(template, ingredient_list)
        template.used_ingredients = FIND_MATCHES(template, ingredient_list)
    END FOR
    
    // Step 5: Format response
    ranked_templates = SORT_BY_SCORE(templates)[0:3]
    response = FORMAT_RECIPES(ranked_templates, preferences)
    
    // Step 6: Simulate processing time
    DELAY(1200 milliseconds)
    callback.onSuccess(response)
END FUNCTION
```

**Data Structures**:

1. **Recipe Templates**:
```kotlin
data class RecipeTemplate(
    name: String,           // "Herb-Roasted Chicken"
    description: String,    // "Juicy chicken with aromatic herbs"
    cookingTime: Int,       // 45 (minutes)
    method: String          // "oven" | "stovetop" | "slow cooker"
)
```

2. **Ingredient Categories** (for pattern matching):
```kotlin
PROTEIN_KEYWORDS = ["chicken", "beef", "pork", "fish", "tofu", "eggs", ...]
VEGETABLE_KEYWORDS = ["tomato", "onion", "garlic", "pepper", "carrot", ...]
GRAIN_KEYWORDS = ["rice", "pasta", "bread", "flour", "quinoa", ...]
DAIRY_KEYWORDS = ["milk", "cheese", "butter", "yogurt", "cream"]
```

3. **Substitution Database**:
```kotlin
SUBSTITUTIONS = {
    "butter": ["olive oil", "coconut oil", "margarine", "ghee"],
    "milk": ["almond milk", "soy milk", "oat milk", "coconut milk"],
    "eggs": ["flax eggs", "chia eggs", "mashed banana", "applesauce"],
    "flour": ["almond flour", "coconut flour", "oat flour", "rice flour"],
    // ... 50+ common ingredients
}
```

##### Grocery List Generation Algorithm

```
FUNCTION generateGroceryList(pantryItems, mealPlan):
    // Step 1: Parse existing inventory
    pantry = PARSE_INGREDIENTS(pantryItems)
    meal_keywords = EXTRACT_KEYWORDS(mealPlan)
    
    // Step 2: Initialize categories
    grocery_categories = {
        "Fresh Produce": [],
        "Proteins": [],
        "Pantry Staples": [],
        "Dairy": []
    }
    
    // Step 3: Generate contextual suggestions
    IF NOT HAS_CATEGORY(pantry, VEGETABLE_KEYWORDS) THEN
        grocery_categories["Fresh Produce"] += [
            "Fresh tomatoes",
            "Onions (yellow and red)",
            "Garlic cloves",
            "Bell peppers (assorted colors)",
            "Fresh herbs (basil, parsley)"
        ]
    END IF
    
    IF NOT HAS_CATEGORY(pantry, PROTEIN_KEYWORDS) OR 
       MEAL_PLAN_NEEDS_PROTEIN(meal_keywords) THEN
        grocery_categories["Proteins"] += [
            "Chicken breast (1 lb)",
            "Ground beef (1 lb)",
            "Eggs (dozen)"
        ]
    END IF
    
    // Step 4: Format output
    response = FORMAT_GROCERY_LIST(grocery_categories, mealPlan)
    
    DELAY(1000 milliseconds)
    callback.onSuccess(response)
END FUNCTION
```

##### Substitution Suggestion Algorithm

```
FUNCTION suggestSubstitutes(ingredient, recipe):
    // Step 1: Normalize input
    normalized = ingredient.lowercase().trim()
    
    // Step 2: Exact match lookup
    IF SUBSTITUTIONS.contains(normalized) THEN
        substitutes = SUBSTITUTIONS[normalized]
    ELSE
        // Step 3: Partial match (e.g., "whole milk" matches "milk")
        substitutes = FIND_PARTIAL_MATCH(normalized, SUBSTITUTIONS)
        
        IF substitutes == null THEN
            // Step 4: Generic fallback
            substitutes = [
                "Similar items in your pantry",
                "Generic store brand alternative",
                "Adjust recipe to omit if optional"
            ]
        END IF
    END IF
    
    // Step 5: Format with context
    response = FORMAT_SUBSTITUTES(ingredient, substitutes, recipe)
    
    DELAY(800 milliseconds)
    callback.onSuccess(response)
END FUNCTION
```

##### Chat/Q&A Algorithm

```
FUNCTION chat(userMessage, context):
    message_lower = userMessage.lowercase()
    
    // Pattern matching with priority order
    response = CASE
        WHEN CONTAINS(message_lower, ["how long", "cook", "bake"]) THEN
            COOKING_TIME_GUIDE()
        
        WHEN CONTAINS(message_lower, ["store", "keep"]) THEN
            STORAGE_TIPS()
        
        WHEN CONTAINS(message_lower, ["substitute", "replace"]) THEN
            SUBSTITUTION_HELP()
        
        WHEN CONTAINS(message_lower, ["meal prep", "meal plan"]) THEN
            MEAL_PLANNING_TIPS()
        
        WHEN CONTAINS(message_lower, ["recipe"]) THEN
            RECIPE_SUGGESTION_PROMPT()
        
        WHEN CONTAINS(message_lower, ["?", "help"]) THEN
            FEATURE_OVERVIEW()
        
        ELSE
            GENERIC_HELPFUL_RESPONSE(userMessage)
    END CASE
    
    DELAY(900 milliseconds)
    callback.onSuccess(response)
END FUNCTION
```

**Helper Functions**:

```
FUNCTION parseIngredients(text: String) -> List<String>:
    // Split on common delimiters and normalize
    RETURN text.lowercase()
               .split([",", ";", "\n", " and "])
               .map(trim)
               .filter(not_blank)
END FUNCTION

FUNCTION identifyPrimaryCategory(ingredients: List<String>) -> String:
    // Check categories in priority: protein > grain > vegetable
    FOR ingredient IN ingredients:
        IF ANY(PROTEIN_KEYWORDS IN ingredient) THEN
            RETURN matched_protein_keyword
        END IF
    END FOR
    
    FOR ingredient IN ingredients:
        IF ANY(GRAIN_KEYWORDS IN ingredient) THEN
            RETURN "pasta"
        END IF
    END FOR
    
    RETURN "vegetable"  // Default
END FUNCTION

FUNCTION hasCategory(pantry: List<String>, category: Set<String>) -> Boolean:
    RETURN pantry.any { item ->
        category.any { keyword -> keyword IN item }
    }
END FUNCTION
```

#### 4. AiViewModel (Bridge Layer)

**Location**: `app/src/main/java/com/kitchenkompanion/features/ai/AiViewModel.java`

**Purpose**: Mediator between UI and AI service; manages data preparation and state.

**Lifecycle**:
```
FUNCTION suggestRecipes():
    // 1. Validation
    IF no_household_selected THEN
        error.postValue("No household selected")
        RETURN
    END IF
    
    // 2. Update UI state
    loading.postValue(true)
    
    // 3. Async data preparation
    EXECUTE_ASYNC:
        // 3a. Fetch from database
        items = itemDao.getAllItemsSync(householdId)
        
        IF items.isEmpty() THEN
            error.postValue("Your pantry is empty. Add items first!")
            loading.postValue(false)
            RETURN
        END IF
        
        // 3b. Format ingredients
        ingredients = items.take(15)
                          .map(item -> item.name)
                          .join(", ")
        
        // 4. Call AI service
        aiService.suggestRecipes(ingredients, preferences, {
            onSuccess: (result) ->
                response.postValue(result)
                loading.postValue(false)
            
            onError: (error) ->
                error.postValue(error)
                loading.postValue(false)
        })
    END ASYNC
END FUNCTION
```

**State Management**:
- `response: LiveData<String>` - AI-generated text
- `loading: LiveData<Boolean>` - Operation in progress
- `error: LiveData<String>` - Error messages
- `serviceType: LiveData<String>` - Current AI implementation name

#### 5. AiFragment (UI Layer)

**Location**: `app/src/main/java/com/kitchenkompanion/features/ai/AiFragment.java`

**Purpose**: User interface for AI features with input dialogs and response display.

**UI Flow**:
```
FUNCTION onViewCreated():
    viewModel = getViewModel()
    
    // Check availability
    IF viewModel.isAiAvailable() THEN
        showFeatureButtons()
        observeViewModel()
    ELSE
        showUnavailableCard()
    END IF
END FUNCTION

FUNCTION onSuggestRecipesClick():
    // Disable all buttons
    setButtonsEnabled(false)
    
    // Show progress indicator
    progressBar.visibility = VISIBLE
    
    // Trigger ViewModel operation
    viewModel.suggestRecipes()
    
    // Response handled by LiveData observer
END FUNCTION

FUNCTION observeViewModel():
    // Service type badge
    viewModel.serviceType.observe { type ->
        serviceTypeText.text = "AI Mode: $type"
    }
    
    // Loading state
    viewModel.loading.observe { isLoading ->
        progressBar.visibility = IF isLoading THEN VISIBLE ELSE GONE
        setButtonsEnabled(!isLoading)
    }
    
    // Success response
    viewModel.response.observe { text ->
        IF text.isNotEmpty() THEN
            showResponseDialog(text)
        END IF
    }
    
    // Error handling
    viewModel.error.observe { errorMsg ->
        IF errorMsg.isNotEmpty() THEN
            showToast(errorMsg)
        END IF
    }
END FUNCTION
```

## Memory and Performance

### Resource Footprint

**IntelligentMockAiService**:
- Memory: ~2-5 MB (recipe templates + substitution maps)
- CPU: Minimal (pattern matching, string operations)
- Latency: 800-1500ms (simulated for UX realism)
- Storage: None (all data in-memory)

**Future RealLlmService** (estimated):
- Memory: 500MB - 2GB (model + runtime)
- CPU: High during inference (5-30s per query)
- Latency: 3-15 seconds (depends on model size and device)
- Storage: 500MB - 1.5GB (GGUF model file)

### Performance Optimization

1. **Lazy Initialization**: Service created only when first used
2. **Singleton Pattern**: One instance shared across app lifecycle
3. **Async Processing**: All AI operations off main thread
4. **Response Caching**: Could cache common queries (future enhancement)
5. **Progressive Enhancement**: Graceful degradation if resources unavailable

## Testing Strategy

### Unit Tests

```kotlin
class IntelligentMockAiServiceTest {
    
    @Test
    fun testRecipeSuggestion_withChicken_returnsChickenRecipes() {
        val service = IntelligentMockAiService(context)
        var result: String? = null
        
        service.suggestRecipes("chicken, rice, onion", null) { response ->
            result = response
        }
        
        // Wait for async callback
        Thread.sleep(1500)
        
        assertNotNull(result)
        assertTrue(result.contains("chicken", ignoreCase = true))
        assertTrue(result.contains("recipe", ignoreCase = true))
    }
    
    @Test
    fun testSubstitution_butter_returnsValidAlternatives() {
        val service = IntelligentMockAiService(context)
        var result: String? = null
        
        service.suggestSubstitutes("butter", null) { response ->
            result = response
        }
        
        Thread.sleep(1000)
        
        assertNotNull(result)
        assertTrue(result.contains("olive oil") || result.contains("coconut oil"))
    }
}
```

### Integration Tests

```kotlin
class AiViewModelTest {
    
    @Test
    fun testSuggestRecipes_withEmptyPantry_showsError() {
        // Mock empty database
        val viewModel = AiViewModel(application)
        
        viewModel.suggestRecipes()
        
        val error = viewModel.getError().value
        assertEquals("Your pantry is empty. Add items first!", error)
    }
}
```

### Manual Testing Checklist

- [ ] Recipe suggestions with 3+ ingredients
- [ ] Recipe suggestions with dietary preferences
- [ ] Grocery list generation
- [ ] Ingredient substitution (common items)
- [ ] Ingredient substitution (uncommon items)
- [ ] Chat Q&A (cooking times)
- [ ] Chat Q&A (storage advice)
- [ ] Loading states and button disabling
- [ ] Error handling (no pantry items)
- [ ] Service type badge display

## Future Enhancements

### Real LLM Integration

When ready to integrate a real on-device LLM (TinyLlama, Phi-2, Gemini Nano):

1. **Create Implementation**:
```kotlin
class RealLlmService(
    private val context: Context,
    private val modelPath: String
) : OnDeviceAiService {
    
    private var llmEngine: LLMEngine? = null
    
    fun initialize(): Boolean {
        try {
            llmEngine = LLMEngine.load(modelPath)
            llmEngine.warmup()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load LLM", e)
            return false
        }
    }
    
    override fun suggestRecipes(
        ingredients: String,
        preferences: String?,
        callback: AiCallback
    ) {
        val prompt = buildRecipePrompt(ingredients, preferences)
        
        executorService.execute {
            try {
                val response = llmEngine.generate(
                    prompt,
                    maxTokens = 512,
                    temperature = 0.7
                )
                callback.onSuccess(response)
            } catch (e: Exception) {
                callback.onError(e.message ?: "Generation failed")
            }
        }
    }
    
    // ... other methods
}
```

2. **Update Factory**:
```kotlin
object AiServiceFactory {
    private fun createService(context: Context): OnDeviceAiService {
        val modelPath = getModelPath(context)
        
        if (modelPath != null && File(modelPath).exists()) {
            try {
                val llmService = RealLlmService(context, modelPath)
                if (llmService.initialize()) {
                    return llmService
                }
            } catch (e: Exception) {
                Log.w(TAG, "LLM failed, using mock", e)
            }
        }
        
        return IntelligentMockAiService(context)
    }
}
```

3. **Add Model Downloader**:
```kotlin
class ModelDownloadManager(private val context: Context) {
    
    fun downloadModel(
        url: String,
        onProgress: (Int) -> Unit,
        onComplete: (File) -> Unit
    ) {
        // Stream download to filesDir/models/
        // Verify checksum
        // Update SharedPreferences with path
    }
}
```

### Advanced Features

1. **Streaming Responses**: Real-time token generation
2. **Conversation History**: Multi-turn context retention
3. **Recipe Image Generation**: Stable Diffusion integration
4. **Voice Input**: Speech-to-text for hands-free cooking
5. **Multilingual Support**: Prompts and responses in multiple languages
6. **Personalization**: Learn user preferences over time

## Security and Privacy

### Data Handling

1. **No Network Inference**: All AI processing happens on-device
2. **No Data Collection**: User queries are never sent to external servers
3. **Local Storage Only**: Conversation history (if implemented) stored locally
4. **Encrypted Preferences**: Sensitive settings encrypted at rest

### Privacy Guarantees

- No user data leaves the device during AI operations
- No analytics or telemetry on AI queries
- Full functionality works offline
- User can inspect/delete all local data

## Troubleshooting

### Common Issues

**Issue**: "AI features are currently unavailable"
- **Cause**: Service failed to initialize
- **Fix**: Check logs for initialization errors; verify device compatibility

**Issue**: Slow response times
- **Cause**: Device low on memory or CPU
- **Fix**: Close other apps; reduce complexity of queries

**Issue**: Generic/unhelpful responses
- **Cause**: Mock service couldn't match input to patterns
- **Fix**: Rephrase query with more specific keywords

**Issue**: Service type shows "Not initialized"
- **Cause**: Factory hasn't created service yet
- **Fix**: Trigger any AI operation to initialize service

## Conclusion

Kitchen Kompanion's AI system provides a solid foundation for intelligent cooking assistance with:

- **Immediate value** through rule-based responses
- **Privacy-first** design with on-device processing
- **Extensible architecture** ready for LLM integration
- **Graceful degradation** when resources unavailable
- **Minimal footprint** suitable for all devices

The current implementation balances functionality, performance, and user experience while maintaining a clear path for future enhancements.

---

**Document Version**: 1.0  
**Last Updated**: November 2025  
**Author**: Kitchen Kompanion Development Team





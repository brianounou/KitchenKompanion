# Build Instructions for Kitchen Kompanion

## Quick Start (Android Studio)

### 1. Open Project
1. Launch Android Studio (Ladybug 2024.2.1 or newer)
2. Select **File → Open**
3. Navigate to `/home/bdiarra/AndroidStudioProjects/KitchenKompanion`
4. Click **OK**

### 2. Sync Project
1. Android Studio will automatically detect Gradle configuration
2. Click **Sync Now** when prompted (or **File → Sync Project with Gradle Files**)
3. Wait for sync to complete (may take 2-5 minutes on first run)

### 3. Build Project
1. Select **Build → Make Project** (Ctrl+F9 / Cmd+F9)
2. Check **Build** output panel for errors
3. If successful, you'll see "BUILD SUCCESSFUL"

### 4. Run on Emulator
1. Create emulator if needed:
   - **Tools → Device Manager**
   - Click **Create Device**
   - Select phone model (e.g., Pixel 6)
   - Select system image: **Android 14 (API 34)** or higher (recommended)
   - Click **Finish**

2. Run the app:
   - Click **Run** button (green triangle) or press Shift+F10
   - Select your emulator from device list
   - Click **OK**

3. Wait for app to install and launch

## Testing AI Features

### Test Scenarios

#### 1. Recipe Suggestions
**Steps**:
1. Navigate to **Pantry** tab
2. Add at least 3 items (e.g., "Chicken", "Rice", "Onion")
3. Navigate to **AI Assistant** tab
4. Click **"Suggest Recipes from Pantry"**
5. Wait 1-2 seconds for response
6. Verify dialog shows 3 recipe suggestions using your ingredients

**Expected Result**:
- Dialog displays formatted recipe list
- Recipes mention ingredients from your pantry
- Each recipe includes name, description, cooking time

#### 2. Grocery List Generation
**Steps**:
1. Ensure pantry has some items
2. Navigate to **AI Assistant** tab
3. Click **"Generate Smart Grocery List"**
4. Wait 1-2 seconds

**Expected Result**:
- Dialog shows categorized grocery list (Produce, Proteins, Dairy, Pantry Staples)
- Items are contextual (e.g., suggests proteins if pantry lacks them)
- List includes estimated quantities

#### 3. Ingredient Substitution
**Steps**:
1. Navigate to **AI Assistant** tab
2. Click **"Find Ingredient Substitutes"**
3. Enter common ingredient (e.g., "butter", "milk", "eggs")
4. Click **"Ask AI"**
5. Wait ~1 second

**Expected Result**:
- Dialog shows 3-4 substitute options
- Substitutes are contextually appropriate
- Includes usage notes

#### 4. Cooking Q&A
**Steps**:
1. Navigate to **AI Assistant** tab
2. Click **"Ask Cooking Question"**
3. Enter question (e.g., "How long to cook chicken?", "How to store vegetables?")
4. Click **"Ask AI"**

**Expected Result**:
- Dialog shows helpful, contextual response
- Response is relevant to query topic
- Provides actionable advice

### Verify Service Type

**Check AI Mode Badge**:
- On AI Assistant screen, below subtitle
- Should display: "AI Mode: Mock AI (Rule-based)"
- Indicates system is using intelligent mock service

### Performance Checks

**Loading States**:
- Progress bar appears during processing
- All buttons disabled while loading
- Progress bar disappears when response ready

**Response Times**:
- Recipe suggestions: ~1.2 seconds
- Grocery list: ~1.0 seconds
- Substitutions: ~0.8 seconds
- Chat: ~0.9 seconds

**Error Handling**:
- Try recipe suggestions with empty pantry → Should show "Your pantry is empty" error
- All errors display as Toast messages at bottom of screen

## Common Issues and Solutions

### Issue: Build Fails with "Could not find com.google.firebase:firebase-bom"

**Solution**:
1. Check internet connection
2. In Android Studio: **File → Invalidate Caches → Invalidate and Restart**
3. Delete `.gradle` folder in project root
4. Re-sync project

### Issue: "google-services.json not found"

**Solution**:
1. Verify `app/google-services.json` exists
2. If missing, download from Firebase Console:
   - Go to [console.firebase.google.com](https://console.firebase.google.com)
   - Select your project
   - Go to Project Settings → General
   - Download `google-services.json`
   - Place in `app/` directory
3. Sync project again

### Issue: Kotlin compilation errors

**Solution**:
- Our Kotlin files (`.kt`) are in `main/java/` folder (standard Android convention)
- Ensure Kotlin plugin is enabled: **File → Settings → Plugins → Kotlin**
- Check Gradle uses correct Kotlin version (should be automatically managed by version catalog)

### Issue: "Cannot resolve symbol 'OnDeviceAiService'"

**Solution**:
1. Clean build: **Build → Clean Project**
2. Rebuild: **Build → Rebuild Project**
3. If still failing, **File → Invalidate Caches → Invalidate and Restart**

### Issue: Emulator is slow

**Solution**:
1. Enable hardware acceleration:
   - **Tools → SDK Manager → SDK Tools**
   - Install **Intel x86 Emulator Accelerator (HAXM)** or **Android Emulator Hypervisor Driver**
2. Increase emulator RAM:
   - **Tools → Device Manager → Edit device**
   - Advanced Settings → RAM: 2048 MB or more
3. Use a smaller screen size (e.g., Pixel 4 instead of Pixel 6 Pro)

## Code Verification Checklist

Before building, verify these files exist:

### New AI Files (Created)
- ✅ `app/src/main/java/com/kitchenkompanion/features/ai/OnDeviceAiService.kt`
- ✅ `app/src/main/java/com/kitchenkompanion/features/ai/IntelligentMockAiService.kt`
- ✅ `app/src/main/java/com/kitchenkompanion/features/ai/AiServiceFactory.kt`

### Modified Files (Updated)
- ✅ `app/src/main/java/com/kitchenkompanion/features/ai/AiViewModel.java`
- ✅ `app/src/main/java/com/kitchenkompanion/features/ai/AiFragment.java`
- ✅ `app/src/main/res/values/strings.xml`
- ✅ `app/src/main/res/layout/fragment_ai.xml`

### Documentation (Updated/Created)
- ✅ `README.md` (Gemini references removed)
- ✅ `BUILD_AND_NEXT_STEPS.md` (AI section updated)
- ✅ `docs/AI_DESIGN.md` (comprehensive design doc)

## Build from Command Line (Alternative)

If you prefer command-line builds:

```bash
# Navigate to project
cd /home/bdiarra/AndroidStudioProjects/KitchenKompanion

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build and install to connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Generated APK location
# app/build/outputs/apk/debug/app-debug.apk
```

**Note**: Requires JDK 17 to be properly configured in your PATH.

## Demonstration Script

For showcasing the app's functionality:

### 1. Setup (2 minutes)
1. Launch app
2. Sign in with Google (or email)
3. Create/select household "Demo Household"

### 2. Pantry Management (2 minutes)
1. Navigate to **Pantry** tab
2. Add items:
   - "Chicken breast" (expires in 5 days)
   - "Rice" (expires in 30 days)
   - "Onions" (expires in 7 days)
   - "Garlic" (expires in 10 days)
   - "Olive oil" (expires in 60 days)
3. Show expiry color coding (green = fresh)
4. Demonstrate swipe-to-delete on one item
5. Re-add it to show add flow

### 3. AI Features (5 minutes)
1. Navigate to **AI Assistant** tab
2. Point out "AI Mode: Mock AI (Rule-based)" badge
3. Click **"Suggest Recipes from Pantry"**
   - Explain: "Using our pantry items, the AI suggests 3 recipes"
   - Show response dialog with recipe options
4. Click **"Generate Smart Grocery List"**
   - Explain: "AI analyzes what we have and suggests what to buy"
   - Show categorized grocery list
5. Click **"Find Ingredient Substitutes"**
   - Enter "butter"
   - Show alternatives (olive oil, coconut oil, etc.)
6. Click **"Ask Cooking Question"**
   - Enter "How long to cook chicken?"
   - Show AI response with cooking times

### 4. Sync and Collaboration (2 minutes)
1. Show household members (if multiple users)
2. Explain offline capability
3. Demonstrate that changes sync to cloud
4. Show Firebase Console (optional) with real-time data

### 5. Barcode Scanning (Optional, 1 minute)
1. Navigate to **Pantry** tab
2. Click barcode scanner icon
3. Show camera view
4. Point out "Works with any product barcode"

## Expected Demo Outcomes

### What Works
✅ Full CRUD on pantry items  
✅ AI recipe suggestions based on ingredients  
✅ Smart grocery list generation  
✅ Ingredient substitution lookup  
✅ Conversational cooking Q&A  
✅ Real-time loading states  
✅ Error handling (empty pantry, etc.)  
✅ Offline functionality  
✅ Multi-user household sync  

### Known Limitations (Future Enhancements)
- AI uses rule-based system (not real LLM yet)
- Recipe search API not fully wired up
- Barcode scanner ready but needs product database expansion
- No voice input yet
- No conversation history

## Next Steps After Demo

1. **Improve AI**: Integrate real LLM (TinyLlama or Phi-2)
2. **Recipe API**: Complete Spoonacular integration
3. **Notifications**: Add daily expiry alerts
4. **Analytics**: Track ingredient usage patterns
5. **Meal Planning**: Weekly meal planner feature

## Support

For issues or questions:
1. Check `docs/AI_DESIGN.md` for architecture details
2. Review `IMPLEMENTATION_COMPLETE.md` for feature status
3. Check Android Studio **Logcat** for runtime errors
4. Look for logs tagged with "AiViewModel", "IntelligentMockAI", "AiServiceFactory"

---

**Last Updated**: November 2025  
**Build Version**: 1.0.0-beta  
**Min Android**: 7.0 (API 24)  
**Target Android**: 15 (API 35)





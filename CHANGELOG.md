# Changelog

All notable changes to Kitchen Kompanion will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### In Progress
- Barcode scanning with CameraX and ML Kit
- Recipe API integration (Spoonacular/Edamam)
- AI features with Gemini Nano
- Grocery list auto-generation

## [0.3.0] - 2025-11-04

### Added - Pantry Management & Notifications
- **Complete Pantry CRUD**: Full create, read, update, delete functionality for pantry items
  - Add new items with comprehensive form (name, quantity, unit, location, expiry, notes, low stock threshold)
  - Edit existing items with pre-populated data
  - Delete items with confirmation dialog
  - Swipe-to-delete gesture in list view
  - Long-press for item options menu
- **Expiry Notifications**: Automated daily checks for expiring items
  - Daily WorkManager job checking items expiring in next 3 days
  - Categorized notifications: expired, expiring today, expiring soon
  - Low stock alerts for items below threshold
  - Grouped notifications showing top 3 items
  - Deep linking to pantry from notifications
  - Configurable notification channels (HIGH priority for expiry, DEFAULT for low stock)
- **Enhanced Pantry UI**:
  - Color-coded expiry status indicators (red/orange/green)
  - Visual location badges
  - Quantity display with units
  - Photo placeholders with Glide integration
  - Material 3 card design with elevation
  - Smooth swipe gestures
  - Empty state with helpful message
- **Date Handling**: Comprehensive DateUtils with expiry checking logic
- **Form Validation**: Real-time validation for required fields and numeric inputs
- **Material Design**: Dropdown selectors for location and unit with predefined options

### Technical Improvements
- ViewBinding used throughout for type-safe view access
- LiveData observation for reactive UI updates
- Repository pattern ensuring offline-first architecture
- Automatic Firestore sync on data changes
- ItemTouchHelper for gesture support
- Material date picker for expiry date selection
- Proper activity lifecycle management
- Soft deletes maintaining data integrity

## [0.2.0] - 2025-11-04

### Added - Data Layer & Sync
- **Room Database**: Complete local database with 4 entities
  - ItemEntity: Pantry items with barcode, expiry, nutrition, location, photos
  - GroceryEntryEntity: Grocery list items with source tracking
  - HouseholdEntity: Multi-user household data
  - RecipeCacheEntity: Recipe API response caching
  - Type converters for Date handling
  - Comprehensive DAOs with LiveData queries
  - Sync flags for conflict resolution
- **Firebase Integration**:
  - Firestore models matching Room schema
  - FirestoreMapper for bidirectional conversion
  - Offline persistence enabled (unlimited cache size)
  - FirebaseSyncWorker for background sync
  - Last-write-wins conflict resolution by timestamp
  - Respects local changes before overwriting
- **Repository Layer**:
  - ItemRepository: CRUD operations with sync scheduling
  - GroceryRepository: Grocery list management with sync
  - ExecutorService for async Room operations
  - WorkManager integration with network constraints
  - Automatic sync triggering on data changes

### Technical Details
- BOM-managed Firebase dependencies (version 33.5.1)
- Room 2.6.1 with annotation processing
- WorkManager 2.9.1 for reliable background tasks
- Firestore security rules template provided
- Schema export configured for Room migrations

## [0.1.0] - 2025-11-04

### Added - Project Foundation
- **Initial Setup**:
  - Android project with Gradle Kotlin DSL
  - Java 17 as target language
  - Min SDK 24 (Android 7.0), Target SDK 35 (Android 15)
  - Material 3 theme with custom green color palette (40+ color tokens)
  - Light and dark theme support
  - ViewBinding enabled
  - BuildConfig generation
  - Secrets Gradle Plugin for API key management
- **Dependencies**: 60+ libraries configured
  - Firebase (Auth, Firestore, Storage) with BOM
  - Room Database 2.6.1
  - CameraX 1.4.0 + ML Kit Barcode Scanning
  - Retrofit 2.11.0 + OkHttp 4.12.0 + Gson
  - WorkManager 2.9.1
  - Lottie 6.5.2, Glide 4.16.0
  - Firebase UI Auth 8.0.2
  - Navigation Component 2.8.4
  - Material Design 3 (1.12.0)
- **Authentication System**:
  - FirebaseUI Auth integration
  - Google Sign-In configured
  - Email/Password authentication
  - Material 3 themed auth screens
  - Automatic redirect if authenticated
  - HouseholdSelectionActivity: Create or join households
  - Household membership management
  - SharedPreferences for household persistence
- **Navigation Structure**:
  - Bottom Navigation with 4 tabs (Pantry, Recipes, Grocery, AI)
  - Navigation Component graph
  - Material Toolbar with menus
  - Deep linking infrastructure
- **UI Foundation**:
  - 20+ layout files created
  - 109 string resources
  - Custom icon set (Material Symbols + custom vectors)
  - Empty state designs
  - Error state templates
- **Application Class**:
  - Firebase initialization
  - Firestore offline persistence configuration
  - Notification channel creation (3 channels)
  - Background task scheduling
- **Utilities**:
  - Result wrapper for operation states
  - DateUtils for expiry calculations
  - PermissionUtils for runtime permissions
- **Documentation**:
  - Comprehensive README with setup guide
  - BUILD_AND_NEXT_STEPS with implementation roadmap
  - PROJECT_SUMMARY with technical details
  - QUICK_START guide for testing
  - IMPLEMENTATION_STATUS tracking progress
  - Inline Javadoc throughout codebase
  - .gitignore properly configured

### Security
- Firebase security rules template
- API keys in gitignored files
- OAuth 2.0 configured for Google Sign-In
- Firestore per-household access control
- Owner-based permissions

### Configuration
- ProGuard ready for release builds
- Debug and release build types
- Signing configuration ready
- File provider for camera images
- Proper permissions in manifest

## Project Statistics

### Current Implementation Status
- **Overall Progress**: ~60% complete
- **Lines of Code**: ~3,500+ lines of production Java
- **Files Created**: 70+ files
- **Layouts**: 15+ XML layouts
- **String Resources**: 123 entries
- **Features Complete**: 7 out of 12 planned

### Architecture
- **Pattern**: MVVM + Repository
- **Database**: Room (offline-first)
- **Cloud**: Firebase (Auth + Firestore + Storage)
- **Background**: WorkManager
- **UI**: Material 3 + ViewBinding
- **Image Loading**: Glide
- **Networking**: Retrofit2 (ready for API integration)

### Build Configuration
- **compileSdk**: 35
- **minSdk**: 24 (Android 7.0+)
- **targetSdk**: 35 (Android 15)
- **Java Version**: 17
- **Gradle**: 8.13.0
- **Dependencies**: 60+ libraries

---

## Notes

### Version Numbering
- Major (X.0.0): Breaking changes, major feature additions
- Minor (0.X.0): New features, backwards compatible
- Patch (0.0.X): Bug fixes, minor improvements

### Upcoming Features
See [BUILD_AND_NEXT_STEPS.md](BUILD_AND_NEXT_STEPS.md) for detailed roadmap.

Priority order:
1. Barcode scanning (in progress)
2. Recipe integration
3. Nutrition APIs
4. AI features
5. Grocery auto-generation
6. UI polish & animations
7. Testing & release preparation

### Known Limitations
- No photo capture yet (infrastructure ready)
- Placeholder API keys (need user configuration)
- AI features require Android 14+ with Gemini Nano
- No localization yet (English only)
- No analytics/crash reporting yet

### Contributing
This is a personal/educational project. See documentation for extending functionality.







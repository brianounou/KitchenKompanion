# Contributing to Kitchen Kompanion

Thank you for your interest in contributing to Kitchen Kompanion! This document provides guidelines and instructions for contributing to the project.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project follows standard professional conduct:
- Be respectful and constructive
- Welcome newcomers
- Focus on the code, not the person
- Assume good intentions

## Development Setup

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Git
- Firebase account
- API keys for external services (see README.md)

### Setup Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/KitchenKompanion.git
   cd KitchenKompanion
   ```

2. **Configure Firebase**
   - Create Firebase project
   - Download `google-services.json`
   - Place in `app/` directory
   - See [README.md](README.md) for detailed instructions

3. **Configure API Keys**
   - Copy `secrets.defaults.properties` to `secrets.properties`
   - Add your API keys
   - Never commit `secrets.properties`

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on emulator or device**
   ```bash
   ./gradlew installDebug
   ```

## Project Structure

```
app/src/main/java/com/kitchenkompanion/
├── KitchenKompanionApp.java        # Application class
├── MainActivity.java                # Main entry point
├── auth/                            # Authentication features
├── data/
│   ├── local/                       # Room database
│   ├── remote/                      # Firebase/API models
│   └── repo/                        # Repositories
├── features/                        # Feature modules
│   ├── pantry/
│   ├── recipes/
│   ├── grocery/
│   ├── ai/
│   ├── barcode/
│   └── notifications/
├── ui/                              # Shared UI components
└── utils/                           # Utility classes
```

### Architecture Patterns

**MVVM (Model-View-ViewModel)**
- **Model**: Room entities, Firestore models, API responses
- **View**: Activities, Fragments, XML layouts
- **ViewModel**: Holds UI state, exposes LiveData, calls repositories

**Repository Pattern**
- Single source of truth (Room database)
- Abstracts data sources (Room, Firestore, APIs)
- Handles sync and conflict resolution

**Offline-First**
- Room is authoritative
- All UI observes Room via LiveData
- Background sync with WorkManager

## Coding Standards

### Java Style Guide
Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with these specifics:

**Naming Conventions**
- Classes: `PascalCase`
- Methods: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Member variables: `camelCase` (avoid `m` prefix)
- Layout IDs: `snake_case`

**Code Organization**
```java
public class ExampleClass {
    // Constants
    private static final String TAG = "ExampleClass";
    
    // Member variables
    private String name;
    private int count;
    
    // Constructor
    public ExampleClass(String name) {
        this.name = name;
    }
    
    // Public methods
    public void doSomething() {
        // Implementation
    }
    
    // Private methods
    private void helperMethod() {
        // Implementation
    }
}
```

**Documentation**
```java
/**
 * Brief description of the class.
 * 
 * Detailed explanation if needed.
 * Usage examples if helpful.
 */
public class ExampleClass {
    
    /**
     * Brief method description.
     * 
     * @param param Description of parameter
     * @return Description of return value
     */
    public String exampleMethod(String param) {
        // Implementation
    }
}
```

### XML Style Guide

**Layout Files**
- Use `ConstraintLayout` for complex layouts
- Use `LinearLayout` for simple stacks
- Use `MaterialCardView` for cards
- Always use Material 3 components

**Naming**
- Activities: `activity_name.xml`
- Fragments: `fragment_name.xml`
- List items: `item_name.xml`
- Dialogs: `dialog_name.xml`

**Resource IDs**
- `@+id/button_submit` (type_purpose)
- `@+id/text_title`
- `@+id/image_profile`

**Dimensions**
- Use `dp` for sizes
- Use `sp` for text
- Use `match_parent` and `wrap_content`
- Avoid hardcoded sizes

### ViewBinding
Always use ViewBinding instead of findViewById:
```java
private ActivityMainBinding binding;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    
    binding.buttonSubmit.setOnClickListener(v -> {
        // Handle click
    });
}
```

### LiveData & ViewModel
```java
public class ExampleViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Item>> items = new MutableLiveData<>();
    
    public LiveData<List<Item>> getItems() {
        return items;
    }
    
    public void loadItems() {
        // Load from repository
        repository.getItems().observeForever(items::setValue);
    }
}
```

### Room Database
```java
@Entity(tableName = "example")
public class ExampleEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;
    
    @ColumnInfo(name = "name")
    public String name;
}

@Dao
public interface ExampleDao {
    @Query("SELECT * FROM example")
    LiveData<List<ExampleEntity>> getAll();
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExampleEntity entity);
}
```

## Testing Guidelines

### Unit Tests
- Place in `src/test/java/`
- Test business logic, utils, ViewModels
- Use JUnit 4
- Mock dependencies with Mockito

```java
@Test
public void testDateUtils_isExpired() {
    Date pastDate = DateUtils.addDays(new Date(), -5);
    assertTrue(DateUtils.isExpired(pastDate));
}
```

### Instrumentation Tests
- Place in `src/androidTest/java/`
- Test UI, database, repositories
- Use Espresso for UI tests

```java
@Test
public void testPantryFragment_displaysItems() {
    // Add test data
    // Launch fragment
    // Verify UI displays items
}
```

## Commit Guidelines

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code style (formatting, no logic change)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples**
```
feat(pantry): add swipe-to-delete gesture

Implemented ItemTouchHelper for swipe-to-delete functionality
in pantry list. Users can now swipe left to delete items with
confirmation dialog.

Closes #123
```

```
fix(sync): resolve conflict when items updated offline

Updated FirebaseSyncWorker to properly handle conflicts when
items are modified offline and synced later.

Fixes #456
```

### Branch Naming
- `feature/barcode-scanning`
- `fix/notification-crash`
- `docs/setup-guide`
- `refactor/repository-layer`

## Pull Request Process

### Before Submitting
1. **Code Quality**
   - Code follows style guide
   - No compiler warnings
   - No lint errors (run `./gradlew lint`)
   - Proper Javadoc documentation

2. **Testing**
   - All existing tests pass
   - New code is tested
   - Manual testing completed

3. **Documentation**
   - README updated if needed
   - CHANGELOG updated
   - Javadoc added for public APIs

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe testing done

## Screenshots (if UI changes)
Add screenshots

## Checklist
- [ ] Code follows style guide
- [ ] Self-reviewed code
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] No new warnings
- [ ] Added tests
- [ ] All tests passing
```

### Review Process
1. Submit PR with clear description
2. Wait for automated checks
3. Address review comments
4. Get approval from maintainer
5. Squash and merge

## Development Workflow

### Feature Development
1. Create feature branch from `main`
   ```bash
   git checkout -b feature/my-feature
   ```

2. Develop and test locally

3. Commit changes with conventional commits

4. Push branch
   ```bash
   git push origin feature/my-feature
   ```

5. Create Pull Request

6. Address review comments

7. Merge when approved

### Bug Fixes
1. Create fix branch from `main`
2. Write failing test (if applicable)
3. Fix the bug
4. Verify test passes
5. Submit PR with "Fixes #issue" in description

## Resources

- [Android Developer Guides](https://developer.android.com/guide)
- [Material Design 3](https://m3.material.io/)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

## Questions?

- Check existing documentation first
- Search closed issues
- Ask in discussions
- Open new issue if bug/feature request

## License

By contributing, you agree that your contributions will be licensed under the project's license.

---

Thank you for contributing to Kitchen Kompanion! 🍳







# MetroTripper Modernization Plan (2026 Standards)

This plan outlines the steps to modernize the MetroTripper app, moving from RxJava/XML to Jetpack Compose/Coroutines, while respecting the preference for manual DI and native SQLite.

## Phase 1: Build Infrastructure
- **Migrate to Gradle Kotlin DSL**: Convert `build.gradle` to `build.gradle.kts`.
- **Version Catalog**: Centralize dependency management in `gradle/libs.versions.toml`.
- **SDK Update**: 
    - `compileSdk` & `targetSdk` -> 35 (Android 15/16).
    - `minSdk` -> 24 (Modern baseline).
- **Compose Setup**: 
    - Enable `buildFeatures.compose`.
    - Add Compose BOM and core libraries (Foundation, Material3, UI, Tooling).
- **Remove Deprecated Plugins**: Remove `kotlin-android-extensions`.

## Phase 2: Core Refactor (Reactive to Coroutines)
- **Networking**:
    - Update `NexTripService` to use `suspend` functions.
    - Remove `RxJava2CallAdapterFactory`.
- **Location**:
    - Migrate `RxLocation` to use `callbackFlow` to emit location updates.
- **Data Layer (SQLite)**:
    - **Note**: `Anko SQLite` is deprecated/removed. I will refactor `DatabaseHelper` and `DataProvider` to use standard Android `SQLiteDatabase` APIs or a lightweight modern wrapper to ensure compatibility with modern Kotlin.

## Phase 3: Architecture (MVI to Modern ViewModel)
- **State Management**:
    - Create `MainViewModel` to manage `AppState`.
    - Use `MutableStateFlow<AppState>` to hold the current UI state.
    - Migrate logic from `AppStateTransformer` into the ViewModel's event processing.
- **Events**:
    - Define a sealed class `AppUiEvent` (already exists, but will be handled via ViewModel).
    - Use `viewModelScope` for side effects (API calls, DB operations).

## Phase 4: UI Migration (Jetpack Compose)
- **Theme**: Implement `MetroTripperTheme` using Material3.
- **Components**:
    - `MainScreen`: Scaffold with Drawer and Sliding Panel logic.
    - `StopHeading`: Composable for the selected stop header.
    - `StopList`: Composable for the search drawer items.
    - `TripList`: Composable for the arrivals list.
    - `MapScreen`: Use `google-maps-compose` to integrate Google Maps.
- **MainActivity**:
    - Replace `setContentView(R.layout.activity_main)` with `setContent { ... }`.
    - Inject `MainViewModel` manually (since no DI framework is requested).

## Phase 5: Final Cleanup
- **Remove RxJava Dependencies**: Once all components are migrated to Coroutines/Flow.
- **Remove XML Layouts**: Delete all `.xml` files in `res/layout`.
- **Code Cleanup**: Remove unused classes and helpers (e.g., `RxGoogleMap`, `Ui` helper methods that are no longer needed).

## Verification Steps
- **Build**: Ensure the project builds successfully after each phase.
- **Manual Test**: 
    - Verify map renders and shows stops.
    - Verify searching for a stop works.
    - Verify arrivals are fetched and displayed in the sliding panel.
    - Verify saving/unsaving stops works.
- **Unit Tests**: Update existing tests (if any) to use `runTest` and `MainDispatcherRule`.

# Copilot Instructions for FoodNow

## Build, test, and lint commands
Run from the repository root:

- Build debug APK: `.\gradlew.bat assembleDebug`
- Run unit tests: `.\gradlew.bat testDebugUnitTest`
- Run instrumentation tests (device/emulator required): `.\gradlew.bat connectedDebugAndroidTest`
- Run lint: `.\gradlew.bat lintDebug`

Run a single test:

- Single unit test class: `.\gradlew.bat testDebugUnitTest --tests "com.example.foodnow.ExampleUnitTest"`
- Single unit test method: `.\gradlew.bat testDebugUnitTest --tests "com.example.foodnow.ExampleUnitTest.addition_isCorrect"`
- Single instrumentation test method: `.\gradlew.bat connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.foodnow.ExampleInstrumentedTest#useAppContext`

## High-level architecture
- This is a university course project — keep code simple and easy to understand. Avoid complex patterns (no Hilt/Dagger, no RxJava).
- Single-module Android app (`:app`) using **Java + XML views** (no Compose), following **MVVM** architecture.
- Backend: **Firebase** (Auth for login, Firestore for data). Images stored on **Cloudinary**.
- Firestore collections: `Users`, `Stores`, `Foods`, `Categories`, `Orders`, `Favorites` (see `.github/db.md` for full schema).
- `CartItem` is local-only (in-memory singleton), not stored in Firestore. Cart holds items from only one store at a time.

### MVVM package structure
```
com.example.foodnow/
├── models/          ← Data classes matching Firestore (need empty constructor + getters/setters)
├── repositories/    ← Firebase data access (created directly in ViewModel, no DI)
├── viewmodels/      ← ViewModel + LiveData for each screen
├── fragments/       ← UI screen logic
├── adapters/        ← RecyclerView adapters
├── activities/      ← LoginActivity, RegisterActivity, StoreDetailActivity, etc.
└── utils/           ← Helpers (CloudinaryHelper, etc.)
```

### Navigation flow
- `LoginActivity` → (if authenticated) → `MainActivity`
- `MainActivity` hosts `BottomNavigationView` with 4 tabs: Trang chủ, Đơn hàng, Yêu thích, Tài khoản
- Each tab swaps a Fragment into `fragment_container` via `loadFragment(...)`
- Bottom-nav IDs defined in `res/menu/bottom_nav_menu.xml`

## Key repository conventions
- Preserve Vietnamese-language UI text, comments, and sample data when editing existing code.
- Firestore models require an **empty constructor** and **getters/setters** for deserialization.
- Preserve the project’s current Vietnamese-facing text/comment style when editing existing flows (UI labels, sample data, inline comments).
- Dependencies use both version-catalog aliases (`libs.*`) and explicit coordinates — follow whichever style exists in the touched file.
- Database schema reference: `.github/db.md`

# To-Do List App (Task 1 — SAM AI Technologies Internship)

A modern, professional, offline-first personal task management Android application built with **Kotlin**, **Material Design 3**, and clean **MVVM architecture**.

---

## 📱 Features

- **Rich Task Creation**: Add tasks with a mandatory title, optional multi-line description, priority level (**High**, **Medium**, **Low**), and category tag (**General**, **Work**, **Personal**, **Study**).
- **Persistent Completion Toggle**: Mark tasks completed or pending with one tap. Completed tasks feature subtle strikethrough styling and muted contrast.
- **Task Editing**: Full in-place editing via a sleek Material 3 Bottom Sheet Dialog.
- **Safe Task Deletion**: Destructive deletion confirmation dialog with instant **UNDO** via Snackbar.
- **Smart Filtering & Live Search**:
  - Filter by **All**, **Pending**, and **Completed** with live counter badges.
  - Real-time search across titles, descriptions, categories, and priority levels.
- **Dynamic Progress Tracker**: Visual progress card displaying completion ratios and an animated progress bar.
- **Helpful Context-Aware Empty States**: Custom vector illustrations and actionable guidance when lists or search results are empty.
- **Seamless Data Migration**: Thread-safe SharedPreferences repository with automatic backward-compatible migration for legacy plain-string data.
- **Material Design 3 & Dark Theme**: Full Day/Night support with accessible contrast, 8dp spacing system, and 48dp touch targets.
- **100% Offline & Private**: Zero external network dependencies, no telemetry, no API keys, and no required runtime permissions.

---

## 🏛️ Architecture & Tech Stack

```
com.samai.todolist
├── MainActivity.kt               # Main activity with ViewBinding, filters, and dialogs
├── model/
│   ├── TaskItem.kt               # Core task entity with JSON serialization & legacy parser
│   ├── Priority.kt               # Priority enum (High, Medium, Low) with theme colors
│   ├── Category.kt               # Category enum (General, Work, Personal, Study)
│   ├── TaskFilter.kt             # Filter state (ALL, PENDING, COMPLETED)
│   └── TaskStats.kt              # Live statistics calculation model
├── data/
│   └── TaskPreferencesRepository.kt # Thread-safe SharedPreferences storage with migration
├── viewmodel/
│   └── TaskViewModel.kt          # AndroidViewModel exposing LiveData and business logic
└── ui/
    ├── adapter/
    │   └── TaskAdapter.kt        # ListAdapter + DiffUtil for smooth animations
    └── dialog/
        └── AddEditTaskBottomSheet.kt # Material 3 BottomSheet for task creation/editing
```

- **Language**: Kotlin 2.0.21
- **Android Gradle Plugin**: 8.6.1
- **Gradle**: 8.9 (via Gradle Wrapper)
- **Target / Compile SDK**: 35 (Android 15)
- **Minimum SDK**: 24 (Android 7.0 Nougat — covers 99%+ of active devices)
- **Libraries**: AndroidX Core KTX, AppCompat 1.7.0, Google Material 1.12.0, ConstraintLayout 2.2.0, Lifecycle ViewModel & LiveData 2.8.7, Activity KTX 1.9.3

---

## 🚀 How to Open and Run in Android Studio

1. **Launch Android Studio** (Ladybug / Iguana / Hedgehog or newer).
2. Click **Open** and select this directory:
   - `F:\Remote Mouse\ar\SAM_AI_Android_Internship_Projects\Task1_ToDoList` (or `F:\android\Task1_ToDoList`)
3. Allow Android Studio to complete Gradle sync. (If prompted to accept SDK Platform 35 license, click Accept).
4. Select an Android Emulator (API 24 to 35+) or connect a physical device with USB Debugging enabled.
5. Press the green **Run ▶** button (`Shift + F10`) to launch the app.

---

## 📦 Building via Command Line

To run unit tests:
```bash
./gradlew test
```

To assemble the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Unit Tests

Unit tests verify model serialization, legacy string migration, and fallback behaviors:
- File: `app/src/test/java/com/samai/todolist/TaskItemTest.kt`
- Command: `./gradlew testDebugUnitTest`


<img width="720" height="1560" alt="Screenshot_20260908_082857_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/03a55fcc-749b-4a4f-8c97-ee2c459587b4" />
<img width="720" height="1547" alt="Screenshot_20260908_082911_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/f6406d3e-2b09-4143-9806-98d922ea322e" /><img width="720" height="1560" alt="Screenshot_20260908_082857_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/8e5a99da-f0f3-4e28-9d86-6955ceaa9c08" />
<img width="720" height="1549" alt="Screenshot_20260908_083026_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/e2948a9c-a7f6-4cdb-bbf2-c56d2d93b93e" />
<img width="720" height="1551" alt="Screenshot_20260908_083015_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/a70e552a-eb53-4bd2-bca7-a9e05498983b" />
<img width="720" height="1563" alt="Screenshot_20260908_082927_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/8cee8e78-a008-48c6-9d63-799ee90c1e02" />
<img width="720" height="1552" alt="Screenshot_20260908_083004_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/90ac21df-9223-4481-b36b-9c3b3767e11f" />
<img width="720" height="1555" alt="Screenshot_20260908_082942_com_privatecloset_app_MainActivity" src="https://github.com/user-attachments/assets/9adfde4b-3599-4da3-9eaa-bd858bbe38fa" />


# Private Closet — Keyboard Learning App

> **Your sound. Your space. Your stage.**

Private Closet is an Android keyboard/piano learning app built with **Kotlin** and **Jetpack Compose**, targeting learners of all skill levels. It features a dark purple-and-gold visual identity with piano-key motifs throughout the UI.

This repository contains the **student-facing mobile app**. A companion admin dashboard + backend API (Node/TypeScript) is maintained separately.

---

## ✨ Features

| Screen | Description |
|---|---|
| **Splash / Onboarding** | Brand intro with a step-by-step feature walkthrough |
| **Get Started / Log In** | Entry point into the app |
| **Dashboard** | Greeting, "Continue Learning" card, progress stats, quick-access grid |
| **Courses** | Searchable, filterable list of keyboard courses by level |
| **Lesson Player** | Video area, lesson description, topic checklist, mark-complete flow |
| **Practice Mode** | Tappable on-screen piano keyboard with highlighted notes and a record button |
| **Progress** | Circular completion ring, stat tiles, and a monthly progress line chart |
| **Plans** | Subscription tiers and one-on-one lesson booking cards |

---

## 🏗️ Architecture

The app follows a standard **MVVM** pattern with manual dependency injection (no Hilt/Dagger, kept dependency-free on purpose):

```
UI (Composable screens)
    ↓ observes StateFlow<UiState<T>>
ViewModel (per screen)
    ↓ calls suspend functions
LearningRepository (interface)
    ↓ implemented by
FakeLearningRepository (in-memory, simulated network latency)
```

- **`UiState<T>`** — a sealed interface (`Loading` / `Success` / `Error`) rendered uniformly by a shared `StateScaffold` composable, so each screen only needs to describe its success-state content.
- **`LearningRepository`** — the single interface every ViewModel depends on. Swapping `FakeLearningRepository` for a real network-backed implementation (e.g. one that talks to a REST/GraphQL backend) requires **no changes to any screen or ViewModel**.
- **`AppContainer` / `ViewModelFactory`** — a minimal manual DI setup. `AppContainer` owns the single repository instance; `ViewModelFactory` knows how to construct every screen's ViewModel with it.

This structure is intentionally set up so the next milestone — wiring the app to a real backend — is a localized change (new repository implementation + DI swap) rather than a rewrite.

---

## 🛠️ Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Navigation Compose** for screen routing and the bottom tab bar
- **Coroutines / StateFlow** for async data and UI state
- **Canvas API** for the custom piano-door logo mark and progress charts (no external image/vector assets required)
- No third-party networking/DI libraries yet — kept minimal until backend integration begins

---

## 📁 Project Structure

```
PrivateCloset/
├── app/
│   └── src/main/
│       ├── java/com/privatecloset/app/
│       │   ├── data/
│       │   │   ├── model/           # Domain models (Course, Lesson, Student, etc.)
│       │   │   └── repository/      # LearningRepository + FakeLearningRepository
│       │   ├── di/                  # AppContainer, ViewModelFactory
│       │   ├── ui/
│       │   │   ├── common/          # UiState, StateScaffold, brand logo mark
│       │   │   ├── theme/           # Color, Type, Theme (dark purple/gold palette)
│       │   │   ├── navigation/      # NavHost + bottom tab bar wiring
│       │   │   ├── splash/
│       │   │   ├── onboarding/
│       │   │   ├── auth/
│       │   │   ├── dashboard/
│       │   │   ├── courses/
│       │   │   ├── lesson/
│       │   │   ├── practice/
│       │   │   ├── progress/
│       │   │   └── plans/
│       │   ├── MainActivity.kt
│       │   └── PrivateClosetApplication.kt
│       ├── res/values/               # strings.xml, themes.xml
│       └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Koala or newer recommended)
- JDK 17+ (bundled with recent Android Studio versions)
- An emulator or physical device running **Android 7.0 (API 24)** or higher

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/private-closet-app.git
   cd private-closet-app
   ```
2. Open the folder in Android Studio.
3. Let Gradle sync — this will download dependencies from Google's and Maven Central's repositories.
4. Run the app on an emulator or connected device (▶ Run 'app').

The app currently runs entirely on **fake, in-memory data** (`FakeLearningRepository`), so it works out of the box with no backend or network configuration needed.

---

## 🗺️ Roadmap

- [ ] Replace `FakeLearningRepository` with a real network-backed implementation
- [ ] Wire authentication (login/register) to a real backend
- [ ] Persist practice/progress data
- [ ] Push notifications for streaks and reminders
- [ ] Offline caching of course content

---

## 🤝 Contributing

This is currently a solo/internal project. If you're collaborating on it:

1. Create a feature branch off `main`
2. Keep the repository/ViewModel/UiState pattern consistent with existing screens
3. Open a pull request describing what changed and why

---

## 📄 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details. Replace the copyright holder name in that file if this repo is transferred to a different owner/organization.

# 🏪 Namma Santhe Ledger

A modern, offline-first digital ledger app built for small vendors, shop owners, and local market sellers in India. Replace your paper notebooks with a beautiful, fast, and reliable Android app.

<p align="center">
  <img src="screenshots/icon_preview.png" alt="App Icon" width="120"/>
</p>

---

## ✨ Features

### Core Ledger
- **Customer Management** — Add, edit, delete customers with name, phone, address, notes
- **Transaction Recording** — Record credit (udhari) and payment entries with amounts and notes
- **Real-time Balance** — Auto-calculated outstanding balance per customer
- **Transaction History** — Full history with date, amount, type, and notes for every customer

### Dashboard & Analytics
- **Daily Summary** — Today's total credit, payments received, and net position
- **Analytics Screen** — Visual breakdown of business performance
- **Outstanding Overview** — See total pending dues across all customers at a glance

### Reminders
- **Multi-channel Reminders** — Send payment reminders via **SMS**, **Telegram**, or **WhatsApp** (all free)
- **Weekly Auto-reminders** — Automatic notifications about customers with pending dues (via WorkManager)
- **AI-generated Messages** — Smart reminder text generation in Kannada, Hindi, and English

### Cloud & Auth
- **Firebase Email/Password Auth** — Free, secure authentication
- **Cloud Firestore Sync** — Automatic background sync of all data to the cloud
- **Offline-first** — Works 100% without internet; Room is the source of truth
- **Skip Login** — Use the app offline-only, sign in later from Settings

### Design
- **Dark Mode** — Toggle with persistent preference via DataStore
- **Premium Fintech UI** — Glassmorphism cards, smooth animations, gradient accents
- **Adaptive App Icon** — Custom ₹ Rupee icon with emerald gradient

---

## 🏗️ Architecture

```
Clean Architecture + MVVM
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)                 │
│  ├── Screens (Home, Customers, Auth, etc.)  │
│  └── ViewModels (StateFlow + Hilt)          │
├─────────────────────────────────────────────┤
│  Domain Layer                               │
│  ├── Models (Customer, Transaction)         │
│  └── Repository Interfaces                  │
├─────────────────────────────────────────────┤
│  Data Layer                                 │
│  ├── Room Database (offline-first)          │
│  ├── Firebase Auth + Firestore (cloud sync) │
│  └── DataStore (user preferences)           │
└─────────────────────────────────────────────┘
```

### Package Structure

```
com.example.nammasantheledger/
├── core/               # Design system, utilities, theme
│   ├── designsystem/   # Theme, colors, spacing, components
│   └── util/           # Currency formatting, date utils
├── data/               # Data layer implementations
│   ├── local/          # Room DB, DAOs, entities, DataStore
│   ├── firebase/       # Auth repository, Firestore sync
│   ├── mapper/         # Entity ↔ Domain mappers
│   └── repository/     # Repository implementations
├── di/                 # Hilt dependency injection modules
├── domain/             # Domain models & repository interfaces
├── feature/            # Feature screens + ViewModels
│   ├── auth/           # Login / Sign-up screen
│   ├── home/           # Dashboard
│   ├── customer/       # Customer CRUD screens
│   ├── transaction/    # Add transaction screen
│   ├── analytics/      # Business analytics
│   ├── reminders/      # WhatsApp, SMS, Telegram helpers
│   └── settings/       # App settings & profile
└── navigation/         # Type-safe Compose navigation
```

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (Dagger) |
| **Local DB** | Room |
| **Cloud** | Firebase Auth + Cloud Firestore |
| **Preferences** | DataStore |
| **Background** | WorkManager |
| **Navigation** | Compose Navigation (type-safe) |
| **Image Loading** | Coil |
| **Testing** | JUnit, MockK, Turbine |
| **Build** | Gradle (KTS) with Version Catalog |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1+) or newer
- JDK 17+
- Android SDK 34

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/sibghat4320/NammaSantheLedger.git
   cd NammaSantheLedger
   ```

2. **Firebase Setup** (required for auth & cloud sync)
   - Create a project at [Firebase Console](https://console.firebase.google.com)
   - Register Android app with package: `com.example.nammasantheledger`
   - Download `google-services.json` → place in `app/` directory
   - Enable **Email/Password Authentication**
   - Enable **Cloud Firestore**

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click ▶ Run.

> **Note:** The app works fully offline without Firebase. You can skip step 2 by commenting out the `google-services` plugin in `app/build.gradle.kts` and using the "Skip for now" option on the login screen.

---

## 📱 Screens

| Screen | Description |
|--------|------------|
| **Auth** | Email/password login with sign-up toggle and forgot password |
| **Home** | Dashboard with today's summary, recent transactions, quick actions |
| **Customers** | Searchable customer list with outstanding balances |
| **Customer Detail** | Full transaction history, balance, send reminder buttons |
| **Add Transaction** | Record credit or payment with customer picker and amount |
| **Analytics** | Business performance overview and charts |
| **Settings** | Dark mode, profile, cloud account, sign out |

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `NammaSantheApp.kt` | Application class with Hilt + WorkManager setup |
| `MainActivity.kt` | Single-Activity entry with auth guard |
| `NammaSantheNavHost.kt` | Compose navigation graph |
| `FirebaseAuthRepository.kt` | Email/password authentication |
| `FirestoreSyncService.kt` | Room → Firestore write-through sync |
| `WeeklyReminderWorker.kt` | WorkManager worker for due reminders |
| `WhatsAppReminderHelper.kt` | WhatsApp/Telegram/SMS intent helpers |

---

## 👤 Author

Developed by **Arshid**

---

## 📄 License

This project is for educational purposes.

# Budget Tracker

A polished, offline-first personal finance app for Android — track spending, budgets, accounts,
savings goals, subscriptions, and debts in one place. Built with a modern native stack:
**Kotlin, Jetpack Compose, Material 3, Room, Hilt, and DataStore**.

<p align="center">
  <sub>Made with ❤️ and Jetpack Compose</sub>
</p>

## Features

### Money management
- **Income & expenses** — quick add/edit with categories, notes, accounts, and dates
- **Transfers** between accounts, including automatic currency conversion
- **Multiple accounts** — cash, bank, cards, savings; each with its own currency
- **40 currencies** with offline conversion rates and automatic total-balance roll-up
- **Custom categories** with emoji + color, or use the 22 sensible defaults

### Budgets & insight
- **Weekly, monthly, or custom period budgets** (e.g. fortnightly starting any date)
- Category-specific budgets or all-category budgets, with daily-allowance hints
- **Charts** — animated donut (spending by category), income-vs-expense bars, cumulative trend
- **Stats** for this month / 3M / 6M / 1Y / all time, with daily average and top categories

### Staying on top
- **Upcoming payments** on the home screen from your subscriptions (overdue highlighted)
- **Subscriptions** with billing cycles, monthly-cost roll-up, and one-tap "mark paid"
  (records the expense and advances the next due date)
- **Debts & loans** — track money lent out or borrowed, mark as paid, optionally record the
  repayment as a real transaction
- **Savings goals** with progress, target dates, and quick contributions
- **Search & filters** — full-text search, account/category/date-range filters, 4 sort orders

### Design & feel
- **Material 3** design with light/dark/system theme
- **Custom accent color** — 12 presets + full custom HSV picker; the whole palette regenerates
- **Material You dynamic color** on Android 12+
- Swipe-to-delete with undo, long-press actions, empty states, and friendly copy

### Security & data
- **Local-first** — everything stored on-device in Room (no account required)
- **Biometric lock** (fingerprint/face with device-credential fallback)
- **Google Sign-In** (optional, for profile personalization)
- **Backup & restore** — one-tap JSON export/import via the system file picker

## Tech stack

| Layer         | Choice                                                        |
|---------------|---------------------------------------------------------------|
| UI            | Jetpack Compose + Material 3, Navigation Compose              |
| Architecture  | MVVM (ViewModel + StateFlow), Hilt DI                         |
| Persistence   | Room (SQLite), DataStore Preferences                          |
| Auth          | Credential Manager (Google ID), androidx BiometricPrompt      |
| Charts        | Custom Compose Canvas (donut / bars / trend) — zero bloat     |
| Serialization | kotlinx.serialization (backup format)                         |

## Build & run

Requirements: Android Studio (or JDK 17+), Android SDK 36.

```bash
# Debug build
./gradlew assembleDebug

# Release build (minified, signed with debug key for local install)
./gradlew assembleRelease

# Install on a connected device
./gradlew installDebug
```

Open the project in Android Studio and press **Run** for the fastest path.

## Google Sign-In setup (optional)

The app works fully offline without any configuration. To enable Google Sign-In:

1. Create a project in [Google Cloud Console](https://console.cloud.google.com/).
2. Configure the **OAuth consent screen**.
3. Create an **OAuth Client ID → Web application** credential.
4. Paste the client ID into `app/src/main/java/com/budgettracker/app/auth/GoogleAuthClient.kt`:

   ```kotlin
   var serverClientId: String = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
   ```

Until configured, the sign-in button shows a friendly setup hint. Your SHA-1 fingerprint
(`./gradlew signingReport`) must be registered with the project for device testing.

## Project layout

```
app/src/main/java/com/budgettracker/app/
├── auth/            # Google Sign-In (Credential Manager)
├── security/        # Biometric lock helpers
├── data/            # Room entities/DAOs, repositories, backup manager
│   ├── db/          #   entities, DAOs, database
│   └── backup/      #   JSON export/import
├── domain/          # Pure calculations (insights, budget math)
├── ui/              # Compose screens by feature
│   ├── components/  #   shared widgets + custom charts
│   └── theme/       #   Material 3 theme + accent-scheme generator
└── util/            # Money/date formatting, currency engine
```

## Notes

- Currency exchange rates are an offline snapshot — great for budgeting, not for trading. 🙂
- Backup files are plain JSON; you can inspect, diff, and script them.
- Biometric lock re-arms after the app is in the background for more than 30 seconds.

## License

MIT — do whatever you like, attribution appreciated.

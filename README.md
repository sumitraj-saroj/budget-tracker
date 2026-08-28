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
- **40+ currencies** with offline conversion rates and automatic total-balance roll-up
- **Custom categories** with emoji + color, or use the 22 sensible defaults
- **Custom number keypad** — a fast, thumb-friendly amount pad on the add/edit screen
- **Quick-add FAB** — tap to log an expense instantly, long-press to pick
  expense / income / transfer

### Budgets & insight
- **Weekly, monthly, or custom period budgets** (e.g. fortnightly starting any date)
- Category-specific budgets or all-category budgets, with daily-allowance hints
- **Safe-to-spend chip** on Home — how much is left per day for the rest of the period
- **Quick-add tiles** for your most-used categories, right on the Home screen
- **Charts** — animated donut (spending by category), income-vs-expense bars, cumulative trend
- Donut slices are **tap-to-drill-down** into a filtered Records list
- **Stats** for this month / 3M / 6M / 1Y / all time, with month-over-month comparison arrows,
  daily average, and top categories

### Records (transactions)
- **Full-text search** across notes, categories, and accounts, with match highlighting
- **Filters** — account, category, and date range (presets or custom pickers)
- **Sort** by newest / oldest / largest / smallest, and **group by** day, category, or month
- **Sticky day headers** with per-day income/expense totals
- **Swipe-to-delete** with undo snackbar; long-press for edit / duplicate / delete
- Running totals for the current filter

### Staying on top
- **Upcoming payments** on the home screen from your subscriptions (overdue highlighted)
- **Subscriptions** with billing cycles, monthly-cost roll-up, and one-tap "mark paid"
  (records the expense and advances the next due date)
- **Debts & loans** — track money lent out or borrowed, mark as paid, optionally record the
  repayment as a real transaction
- **Savings goals** with progress, target dates, and quick contributions

### Design & feel
- **Material 3** design with light/dark/system theme
- **Custom accent color** — 12 presets + full custom HSV picker; the whole palette regenerates
- **Material You dynamic color** on Android 12+
- **Floating pill navigation bar** with icon-only destinations and an animated
  filled-circle indicator
- **Liquid Glass style (optional)** — a translucent blur-and-tint treatment on the floating
  navigation pill and the sticky Records headers, with a faint hairline edge. Backed by
  [Haze](https://github.com/chrisbanes/haze); gracefully falls back to solid surfaces on
  Android < 12 or when battery saver is on. Toggle it in **Settings → Appearance**
  (off by default; the add-transaction FAB always stays solid for contrast)
- **True black (OLED)** mode — pure black surfaces in dark theme
- Swipe-to-delete with undo, long-press actions, empty states, and friendly copy
- Subtle haptics on keypads, saves, swipes, and tab switches

### Security & data
- **Local-first** — everything stored on-device in Room (no account required)
- **Biometric lock** (fingerprint/face with device-credential fallback); re-arms after the app
  is backgrounded for more than 30 seconds
- **Google Sign-In** (optional, for profile personalization)
- **Backup & restore** — one-tap JSON export/import via the system file picker; preferences
  (theme, accent, currency, …) travel with the backup
- **Demo data** — optionally seed a realistic sample dataset during onboarding to explore the
  app before entering your own numbers

## Tech stack

| Layer         | Choice                                                        |
|---------------|---------------------------------------------------------------|
| UI            | Jetpack Compose + Material 3 (BOM 2024.12.01), Navigation Compose |
| Architecture  | MVVM (ViewModel + StateFlow), Hilt DI                          |
| Persistence   | Room (SQLite), DataStore Preferences                           |
| Auth          | Credential Manager (Google ID), androidx BiometricPrompt       |
| Blur          | [Haze 1.5.4](https://github.com/chrisbanes/haze) (optional Liquid Glass look) |
| Charts        | Custom Compose Canvas (donut / bars / trend) — zero bloat      |
| Serialization | kotlinx.serialization (backup format)                          |

- **minSdk 26 · targetSdk 36** · Kotlin 2.3.20 · AGP 9.0.1 · KSP2

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
│   ├── components/  #   shared widgets + custom charts + keypad
│   └── theme/       #   Material 3 theme, accent-scheme generator, glass helpers
└── util/            # Money/date formatting, currency engine, haptics
```

## Notes

- Currency exchange rates are an offline snapshot — great for budgeting, not for trading. 🙂
- Backup files are plain JSON; you can inspect, diff, and script them.
- Biometric lock re-arms after the app is in the background for more than 30 seconds.
- Liquid Glass requires Android 12+; it switches itself off during battery saver, and the
  toggle is off by default.

## License

MIT — do whatever you like, attribution appreciated.

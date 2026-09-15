# SubHunt — Project Rules & Developer Agreement

## Working Agreement

### Roles
- **User (Client)**: Provides high-level vision, approves major milestones, makes business decisions
- **Developer (AI)**: Handles ALL technical decisions, architecture, implementation, debugging, and minor issues independently

### Communication Rules
- Developer does NOT bother client with minor issues (typos, small UI tweaks, library version choices)
- Developer makes ALL technical decisions autonomously
- Client is consulted ONLY for:
  1. Business logic clarification (ambiguous requirements)
  2. Major architectural changes after initial agreement
  3. Feature prioritization (what to build next)
  4. Budget/cost-impacting decisions
  5. Go/no-go for production releases

### Developer Responsibilities
- Research and use latest 2026 best practices — no outdated patterns
- Write clean, maintainable, production-ready code
- Set up proper project structure from day one
- Handle CI/CD, signing, build configuration
- Document key decisions in this file

---

## Tech Stack (Actual Versions as of 2026-09-14)

| Layer | Choice | Version | Reason |
|-------|--------|---------|--------|
| **Language** | Kotlin (AGP built-in) | 2.3.20 | Built into AGP 9.0 — no separate kotlin-android plugin |
| **UI** | Jetpack Compose + Material 3 | BOM 2024.12.01 | Google's official, Compose-first policy |
| **Architecture** | MVVM + UDF + Repository | — | Google recommended, SSOT pattern |
| **DI** | Hilt | 2.60.1 | Official Android DI, works with Compose |
| **Navigation** | Navigation Compose | 2.8.5 | Standard Compose navigation |
| **Async** | Coroutines + StateFlow | — | Standard, no LiveData |
| **Local DB** | Room | 2.8.5 | Official, SQLite abstraction (KSP2 compatible) |
| **DataStore** | DataStore Preferences | 1.1.1 | Replaces SharedPreferences |
| **Image Loading** | Coil | 2.7.0 | Compose-native, Kotlin-first |
| **Billing** | RevenueCat KMP | 3.8.0 | KMP SDK, free up to $2,500/mo, entitlement-based |
| **Build** | AGP | 9.0.1 | Latest stable, built-in Kotlin |
| **Build** | Gradle | 9.1.0 | Required by AGP 9.0 |
| **KSP** | Google DevTools KSP | 2.3.3 | Annotation processing for Room + Hilt |
| **Min SDK** | 26 | — | 96%+ device coverage |
| **Target SDK** | 35 | — | Current stable |
| **Compile SDK** | 35 | — | Current stable |

### Why AGP 9.0 + Hilt 2.60.1?
RevenueCat KMP 3.8.0 requires Kotlin 2.3.x. KSP 2.3.x requires Hilt 2.60.1+. Hilt 2.60.1 requires AGP 9.0+. This is the minimum viable toolchain for RevenueCat KMP.

### Known Toolchain Quirks
- `android.disallowKotlinSourceSets=false` required in `gradle.properties` (KSP2 + AGP 9 compat, experimental)
- Room 2.7+ required for KSP2 — older versions hit `unexpected jvm signature V` on suspend Unit returns
- `JAVA_HOME` system env must be `C:\jbr` (NOT `C:\jbr\bin`)
- Build script `build.bat` sets `$env:JAVA_HOME = "C:\jbr"` as workaround
- kotlin.android plugin is REMOVED — AGP 9 has built-in Kotlin support

---

## Project Structure

```
app/src/main/java/com/subhunt/app/
├── di/                         # Hilt modules
├── data/
│   ├── local/                  # Room DB, DataStore, DAO
│   └── repository/             # Repository implementations
├── domain/
│   ├── model/                  # Domain models (Subscription, DashboardStats)
│   └── usecase/                # Use cases (CRUD + stats)
├── billing/                    # RevenueCat KMP integration
├── export/                     # CSV export
├── notification/               # WorkManager billing reminders
├── ui/
│   ├── theme/                  # Material 3 theme
│   ├── navigation/             # NavHost with routes
│   ├── components/             # Reusable composables
│   └── screens/
│       ├── onboarding/         # 3-page intro with HorizontalPager
│       ├── dashboard/          # Stats card + subscription list
│       ├── addedit/            # Add/edit subscription form
│       ├── paywall/            # RevenueCat Paywall composable
│       └── settings/           # Settings + Customer Center
├── SubHuntApp.kt               # Application class (RevenueCat init)
└── MainActivity.kt             # Entry point
```

---

## Build Commands

```bash
# Always set JAVA_HOME (system env is wrong)
$env:JAVA_HOME = "C:\jbr"

# Debug build
.\gradlew.bat assembleDebug

# Release build (R8 enabled)
.\gradlew.bat assembleRelease

# Install on connected device/emulator
.\gradlew.bat installDebug

# Full clean build
.\gradlew.bat clean assembleDebug
```

---

## Code Standards

- Kotlin coding conventions enforced via ktlint
- No comments unless complex algorithm or non-obvious business rule
- All Compose functions use PascalCase naming
- State hoisting for all Compose state
- Repository pattern — UI never touches data sources directly
- Single Source of Truth (SSOT) for all data
- Use sealed classes for UI states (Loading, Success, Error)

---

## Key Decisions Log

| Date | Decision | Reason |
|------|----------|--------|
| 2026-09-14 | **App: SubHunt** — Subscription Tracker | Highest confidence revenue play. People spend $273/mo but underestimate by 2.5x. Proven $3K-$10K MRR for indie devs. |
| 2026-09-14 | Kotlin + Compose + Material 3 | Modern Android-first stack, Google endorsed |
| 2026-09-14 | Room for local DB | No bank linking needed, privacy-first |
| 2026-09-14 | MVVM + UDF | Google recommended architecture |
| 2026-09-14 | RevenueCat KMP 3.8.0 for billing | Free up to $2,500/mo revenue, entitlement-based, test store for dev |
| 2026-09-14 | AGP 9.0.1 + Gradle 9.1.0 | Required by Hilt 2.60.1 (required by KSP 2.3.x, required by Kotlin 2.3.20 for RevenueCat KMP) |
| 2026-09-14 | Room 2.8.5 | Fixes KSP2 `unexpected jvm signature V` bug |
| 2026-09-14 | Remove kotlin.android plugin | AGP 9 has built-in Kotlin — applying it is an error |
| 2026-09-14 | `delegate` not `updatedCustomerInfoListener` | RevenueCat KMP 3.8.0 uses `PurchasesDelegate` interface, not Android SDK's listener |
| 2026-09-14 | PaywallScreen uses PaywallOptions builder | RevenueCat KMP UI: `Paywall(PaywallOptions(dismissRequest = { ... }))` |

---

## Build & Release Checklist

- [x] App compiles (debug + release with R8)
- [x] RevenueCat KMP configured with test API key
- [x] Room + Hilt + KSP all working on AGP 9.0
- [x] Release APK builds and installs cleanly
- [ ] App signing configured (upload key + keystore)
- [ ] Firebase project created
- [ ] RevenueCat production account (products, entitlements, offering)
- [ ] Google Play developer account
- [ ] Privacy policy URL
- [ ] Data safety form completed
- [ ] App content rating (IARC)
- [ ] Store listing (description, screenshots, feature graphic)

---

## RevenueCat Configuration

### Test Store (current)
- API Key: `test_boDTvjKkOByZyLbeewlaWXVscqH`
- Entitlement: `subhunt_pro`
- Products: `lifetime`, `yearly`, `monthly`

### Production (TODO)
- Switch to production API key
- Create products in RevenueCat dashboard matching above IDs
- Attach products to `subhunt_pro` entitlement
- Configure offering
- Enable Customer Center

### Pricing Strategy
- Hard paywall with 7-day free trial
- Monthly: $4.99
- Yearly: $29.99 (Save 50%)
- Lifetime: $79.99

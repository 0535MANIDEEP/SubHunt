<div align="center">

# 🎯 SubHunt

### Track Every Subscription. Stop Wasting Money.

**The modern subscription tracker that helps you take control of your recurring payments.**

[![Download APK](https://img.shields.io/badge/Download-APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/0535MANIDEEP/SubHunt/releases/latest)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/about/versions/oreo)

</div>

---

## 📱 What is SubHunt?

The average person spends **$273/month** on subscriptions but underestimates by **2.5x**. That's money slipping away unnoticed.

SubHunt is a **privacy-first** subscription tracker that runs entirely on your device. No bank linking. No cloud sync. No accounts. Your financial data never leaves your phone.

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📊 **Smart Dashboard** | See total monthly spending, upcoming bills, and subscription health at a glance |
| 🔔 **Bill Reminders** | Never miss a payment with smart notifications |
| 📈 **Spending Insights** | Discover which categories eat your budget and find savings |
| 🎯 **Health Score** | Get a letter grade (A-F) for your subscription habits |
| 📤 **Export Data** | CSV and JSON export for your records |
| 🔒 **Privacy First** | All data stays on your device. Always. |
| 🎨 **Premium UI** | Material 3 design with smooth animations |
| 🔐 **App Lock** | Biometric or PIN protection for your data |

## 💰 Pricing

| Plan | Price | Features |
|------|-------|----------|
| **Free** | $0 | Track 3 subscriptions, basic dashboard, reminders |
| **Pro Monthly** | $4.99/mo | Unlimited subscriptions, insights, health score |
| **Pro Yearly** | $29.99/yr | Everything in Pro, save 50% |
| **Lifetime** | $79.99 | Everything in Pro, one-time purchase |

## 📥 Download

### Option 1: GitHub Releases (Recommended)
1. Go to [Releases](https://github.com/0535MANIDEEP/SubHunt/releases/latest)
2. Download `app-release.apk`
3. Enable "Install from unknown sources" on your device
4. Install and enjoy!

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/0535MANIDEEP/SubHunt.git
cd SubHunt

# Set Java home (Windows)
$env:JAVA_HOME = "C:\jbr"

# Build release APK
.\gradlew.bat assembleRelease

# APK will be at:
# app/build/outputs/bundle/release/app-release.apk
```

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.3.20 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + UDF + Repository |
| DI | Hilt 2.60.1 |
| Database | Room 2.8.5 |
| Billing | RevenueCat KMP 3.8.0 |
| Build | AGP 9.0.1 + Gradle 9.1.0 |

## 📂 Project Structure

```
app/src/main/java/com/subhunt/app/
├── billing/          # RevenueCat integration
├── data/             # Room DB, DAO, Repository
├── di/               # Hilt modules
├── domain/           # Models, Use Cases, Analytics
├── export/           # CSV/JSON export
├── notification/     # WorkManager reminders
├── ui/               # Compose screens & components
├── widget/           # Home screen widget
└── SubHuntApp.kt     # Application class
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or later
- JDK 21 (JBR bundled with Android Studio)
- Android SDK 35

### Build Instructions
```bash
# Set JAVA_HOME (Windows)
$env:JAVA_HOME = "C:\jbr"

# Debug build
.\gradlew.bat assembleDebug

# Release build
.\gradlew.bat assembleRelease

# Install on connected device
.\gradlew.bat installDebug
```

## 📄 Documentation

- [RevenueCat Setup Guide](docs/REVENUECAT_SETUP.md)
- [Play Store Listing](docs/PLAY_STORE_LISTING.md)
- [Play Console Setup](docs/PLAY_CONSOLE_SETUP.md)

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) first.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Manideep Daram** - [GitHub](https://github.com/0535MANIDEEP) · [Email](mailto:darammanideep@gmail.com)

---

<div align="center">

**Made with ❤️ for people who want to take control of their finances**

[⬆ Back to Top](#-subhunt)

</div>

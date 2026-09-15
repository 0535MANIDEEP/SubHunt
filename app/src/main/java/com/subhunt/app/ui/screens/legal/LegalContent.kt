package com.subhunt.app.ui.screens.legal

// Copyright (c) 2026 Manideep Daram. All rights reserved.
// SubHunt — Subscription tracker app.
// This file and all associated code are proprietary to Manideep Daram.

// NOTE TO FOUNDER: this text is a carefully drafted template aligned with
// Google Play User Data policy (2026), India's Digital Personal Data Protection
// Act 2023, and SubHunt's actual local-first architecture. It is NOT legal
// advice — have counsel review before Play Console submission, and replace
// support@subhunt.app with your real support address.

const val PRIVACY_LAST_UPDATED = "September 15, 2026"
const val DEVELOPER_NAME = "Manideep Daram"
const val SUPPORT_EMAIL = "darammanideep@gmail.com"

data class LegalSection(val heading: String, val body: String)

val PRIVACY_SECTIONS: List<LegalSection> = listOf(
    LegalSection(
        "1. Overview",
        "SubHunt (\"we\", \"our\", developed by $DEVELOPER_NAME) is a subscription tracker built on a " +
            "local-first principle: your subscription data lives in a database on " +
            "your own device. We operate no SubHunt servers, require no account, " +
            "run no advertising, and sell no data — there is nothing to sell, " +
            "because your data never reaches us."
    ),
    LegalSection(
        "2. Data stored on your device",
        "Everything you enter — subscription names, costs, billing cycles, " +
            "categories, dates, notes, reminder preferences, notification sounds, " +
            "and app settings — is stored in an on-device encrypted-capable " +
            "database (Android Room/SQLite) and on-device preferences (Android " +
            "DataStore). If you enable App Lock, your PIN is verified against a " +
            "value encrypted with a key held in the Android Keystore; the PIN " +
            "itself is never stored in readable form. Biometric templates are " +
            "handled entirely by the Android operating system and are never " +
            "accessible to this app."
    ),
    LegalSection(
        "3. Data shared with third parties",
        "We share data with third parties only where the platform requires it:\n\n" +
            "• RevenueCat (subscription infrastructure): when you purchase or " +
            "restore SubHunt Pro, RevenueCat processes purchase tokens, " +
            "entitlement status, and app/device identifiers to verify your " +
            "subscription. See revenuecat.com/privacy.\n\n" +
            "• Google Play Billing: payments are processed by Google under the " +
            "Google Play Terms of Service and Privacy Policy. We never see or " +
            "store your card or UPI credentials.\n\n" +
            "• Android system services: if device backup is enabled, Android may " +
            "keep an encrypted backup of app data under Google's backup " +
            "retention policy. Notifications you enable are delivered locally " +
            "by the device; SubHunt uses no push-notification server.\n\n" +
            "We do not use analytics SDKs, advertising SDKs, crash reporters " +
            "that transmit personal data, or social-media integrations."
    ),
    LegalSection(
        "4. Permissions we request",
        "• Notifications (Android 13+): only to deliver the billing reminders " +
            "you enable, at the times you configure. Denying this permission " +
            "only disables reminders.\n" +
            "• Biometric hardware: used solely for the optional App Lock, " +
            "evaluated on-device by Android.\n" +
            "• Storage (Android 9 and below only): only when you explicitly " +
            "save an export file to Downloads. On Android 10+, scoped-storage " +
            "APIs are used and no storage permission is needed.\n\n" +
            "The app requests no location, contacts, camera, microphone, or " +
            "network-state data beyond what the operating system needs to " +
            "validate Play Billing purchases."
    ),
    LegalSection(
        "5. Retention and deletion",
        "Your data stays on your device until you delete it. Deleting a " +
            "subscription removes it immediately. Uninstalling the app deletes " +
            "the on-device database and preferences (subject only to any " +
            "system-level backup retention described above, which we do not " +
            "control). Because we hold no copy of your data on any server, " +
            "there is no server-side retention period to disclose."
    ),
    LegalSection(
        "6. Your rights (including under India's DPDP Act, 2023)",
        "You may at any time: (a) access your data — view it in the app or " +
            "export it via Settings → Export; (b) correct it — edit any " +
            "subscription; (c) erase it — delete items or uninstall; " +
            "(d) withdraw consent — disable reminders, App Lock, or any " +
            "optional feature. For grievances, contact $SUPPORT_EMAIL; we " +
            "acknowledge complaints within 72 hours and resolve them within " +
            "30 days. Users in other jurisdictions (including the EU/UK) enjoy " +
            "equivalent access, rectification, erasure, and portability rights, " +
            "exercisable entirely on-device as described above."
    ),
    LegalSection(
        "7. Children",
        "SubHunt is a general-purpose personal-finance utility not directed at " +
            "children under 13. We do not knowingly collect any data from " +
            "children, and because all data remains on-device, there is no " +
            "server-side profile that could exist for any user. Users aged " +
            "13–18 should use the app with a parent or guardian's involvement, " +
            "particularly for purchases, which are governed by Google Play's " +
            "family and payments policies."
    ),
    LegalSection(
        "8. Security",
        "We protect data by minimizing it: local-only storage, no accounts, no " +
            "network transmission of subscription content. PINs are verified " +
            "via AES-256-GCM encryption with keys in the Android Keystore; " +
            "failed unlock attempts trigger a 30-second cooldown after 5 tries; " +
            "the app re-locks after 60 seconds in the background. No method is " +
            "perfect — keep your device OS updated, use a device lock screen, " +
            "and do not share your PIN."
    ),
    LegalSection(
        "9. Changes to this policy",
        "If our practices change, we will update this policy in-app and revise " +
            "the \"Last updated\" date before the change takes effect. " +
            "Continued use after the update constitutes acceptance."
    ),
    LegalSection(
        "10. Contact",
        "Data fiduciary contact for privacy questions and grievance redressal: " +
            "$DEVELOPER_NAME, $SUPPORT_EMAIL."
    )
)

val TERMS_SECTIONS: List<LegalSection> = listOf(
    LegalSection(
        "1. Acceptance and eligibility",
        "By installing or using SubHunt you agree to these Terms. If you do " +
            "not agree, do not use the app. You must be capable of entering a " +
            "binding contract under applicable law; users 13–18 may use the app " +
            "with guardian involvement, and all purchases are subject to " +
            "Google Play's payments and family policies."
    ),
    LegalSection(
        "2. The service",
        "SubHunt helps you track subscriptions, forecast bills, and export " +
            "your data. The free tier tracks up to 3 active subscriptions. " +
            "SubHunt Pro (paid) unlocks unlimited tracking, premium reminder " +
            "tones, per-subscription tones, and future Pro features. Features " +
            "may evolve; we may add, modify, or retire non-core features with " +
            "reasonable notice in-app."
    ),
    LegalSection(
        "3. Subscriptions, trials, and billing",
        "All purchases are processed exclusively through Google Play Billing " +
            "under Google's terms. Prices shown at purchase include applicable " +
            "taxes (including GST for India). Monthly and yearly plans " +
            "auto-renew until cancelled via Google Play (Play Store → " +
            "Payments & subscriptions). Any free trial converts to a paid " +
            "subscription unless cancelled before expiry; you will be charged " +
            "no earlier than the trial terms state. Refunds are governed by " +
            "Google Play's refund policy; we cannot directly refund Play " +
            "purchases. Restore purchases anytime via the paywall or " +
            "Settings → Manage Subscription."
    ),
    LegalSection(
        "4. Acceptable use",
        "You agree not to: (a) reverse-engineer or tamper with license " +
            "verification; (b) use the app for unlawful purposes; (c) enter " +
            "content that infringes others' rights (including audio you do not own); " +
            "(d) attempt to access other users' data — the architecture makes " +
            "this impossible by design, and attempts remain prohibited."
    ),
    LegalSection(
        "5. Intellectual property",
        "SubHunt, its design, tones, and text are owned by $DEVELOPER_NAME and " +
            "licensed to you, non-exclusively and non-transferably, for " +
            "personal use. Your data (the subscriptions you enter) remains " +
            "yours, on your device, always exportable."
    ),
    LegalSection(
        "6. Not financial advice",
        "SubHunt is an organizational tool, not a financial advisor, bank, or " +
            "fiduciary. Savings estimates are illustrative arithmetic (e.g., " +
            "monthly-vs-yearly comparisons), not recommendations. Billing " +
            "dates and amounts are computed from data you enter; always verify " +
            "against your provider's statements before making financial " +
            "decisions. We are not liable for missed payments, renewals, or " +
            "bank charges."
    ),
    LegalSection(
        "7. Availability and changes",
        "We aim for reliable operation but do not guarantee uninterrupted " +
            "service (e.g., OS updates may affect reminders — keep " +
            "notifications enabled and battery-optimization exceptions where " +
            "offered). Core tracking remains fully functional offline."
    ),
    LegalSection(
        "8. Limitation of liability",
        "To the maximum extent permitted by law, our aggregate liability for " +
            "all claims is limited to the amounts you paid for SubHunt Pro in " +
            "the 12 months preceding the claim, and we are not liable for " +
            "indirect, incidental, or consequential damages. Nothing here " +
            "limits rights that consumer-protection law (including India's " +
            "Consumer Protection Act, 2019) does not permit to be limited."
    ),
    LegalSection(
        "9. Termination",
        "You may stop using the app at any time (cancel Pro via Google Play; " +
            "delete data via uninstall). We may discontinue the app with " +
            "reasonable notice; active Pro subscribers retain access through " +
            "their paid period, and your data remains exportable from the app " +
            "until then."
    ),
    LegalSection(
        "10. Governing law and contact",
        "These Terms are governed by the laws of India, with dispute " +
            "resolution through good-faith discussion first, then the courts " +
            "of competent jurisdiction. Contact: $SUPPORT_EMAIL. Material " +
            "changes will be notified in-app before taking effect."
    )
)

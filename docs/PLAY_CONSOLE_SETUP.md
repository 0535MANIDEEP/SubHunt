# Google Play Console Setup Guide — SubHunt

## Step 1: Create Account
1. Go to https://play.google.com/console
2. Sign in with your Google account
3. Pay $25 one-time registration fee
4. Complete developer profile:
   - Developer name: `Manideep Daram`
   - Contact email: `darammanideep@gmail.com`
   - Phone: (your number)
   - Address: (your address — required for payments)

## Step 2: Create App
1. Click **"Create app"**
2. App name: `SubHunt`
3. Default language: English (United States)
4. App or game: **App**
5. Free or paid: **Free**
6. Check both checkboxes (developer agreement)
7. Click **"Create app"**

## Step 3: Store Listing
Go to **Store presence** → **Store listing**

### Main store listing
- **App name**: `SubHunt — Subscription Tracker`
- **Short description**: `Track subscriptions, never miss a bill, save hundreds every year.`
- **Full description**: (paste from docs/PLAY_STORE_LISTING.md)

### Graphics
- **App icon**: Upload `ic_launcher.png` (512x512) — you already have adaptive icon
- **Feature graphic**: 1024x500 (create in Canva or Figma)
- **Phone screenshots**: At least 2, up to 8 (16:9 or 9:16 ratio)
  - Recommended sizes: 1080x1920 or 1080x2400

### Screenshot ideas:
1. Dashboard with stats card showing monthly spend
2. Add subscription form with category picker
3. Insights screen with health score
4. Settings screen showing all features
5. Widget on home screen

## Step 4: Content Rating
Go to **Policy** → **App content**

1. Click **"Start"** next to "Content rating"
2. Complete IARC questionnaire:
   - Violence: None
   - Sexual content: None
   - Language: None
   - Controlled substances: None
   - User interaction: None (no social features)
   - Personal information: Yes (stores subscription data locally)
3. Save and submit

## Step 5: Data Safety
Go to **Policy** → **App content** → **Data safety**

1. Does your app collect or share user data? **Yes**
2. Data collected:
   - **App activity**: Subscription names, costs, billing cycles (stored locally only)
   - **Device info**: None
   - **Financial info**: None (no payment data collected)
3. Is data encrypted in transit? **N/A** (data never leaves device)
4. Can users request data deletion? **Yes** (uninstall deletes all data)
5. Privacy policy URL: (host your privacy policy — see Step 8)

## Step 6: Pricing & Distribution
Go to **Monetize** → **Pricing & distribution**

1. Free: **Yes**
2. Countries: Select all or your target markets
3. Content guidelines: Check all boxes
4. US export laws: Accept

## Step 7: In-App Products & Subscriptions
Go to **Monetize** → **Products** → **Subscriptions**

### Create Subscription: Monthly
1. Click **"Create subscription"**
2. Product ID: `monthly`
3. Name: `SubHunt Pro Monthly`
4. Description: `Unlimited subscription tracking, insights, export, and premium sounds`
5. Billing period: **Monthly**
6. Price: **$4.99**
7. Free trial: **7 days**
8. Save

### Create Subscription: Yearly
1. Click **"Create subscription"**
2. Product ID: `yearly`
3. Name: `SubHunt Pro Yearly`
4. Description: `Unlimited subscription tracking, insights, export, and premium sounds — save 50%`
5. Billing period: **Annual**
6. Price: **$29.99**
7. Free trial: **7 days**
8. Save

### Create One-Time Product: Lifetime
1. Go to **Monetize** → **Products** → **In-app products**
2. Click **"Create product"**
3. Product ID: `lifetime`
4. Name: `SubHunt Pro Lifetime`
5. Description: `One-time purchase for unlimited lifetime access to all Pro features`
6. Price: **$79.99**
7. Save

## Step 8: Privacy Policy
You need a hosted privacy policy URL. Options:
1. **GitHub Pages**: Create a `privacy.html` in a repo, enable Pages
2. **Google Sites**: Create a free site at sites.google.com
3. **Notion**: Publish a page and make it public

The privacy policy text is already in your app at `LegalContent.kt`. Host it and add the URL in Play Console under **Store listing** → **Privacy policy**.

## Step 9: Link RevenueCat
1. In RevenueCat: **Projects** → **Google Play** → **Add Google Play Credentials**
2. In Google Play Console: **Setup** → **API access** → **Create service account**
3. Download the JSON key file
4. Upload to RevenueCat
5. RevenueCat will sync your products automatically

## Step 10: Testing
1. In Google Play Console: **Testing** → **Internal testing**
2. Create internal testing track
3. Add yourself as tester
4. Upload your signed APK/AAB
5. Install on device via Play Store (internal testing)
6. Test purchase flow

## Step 11: Release
1. **Testing** → **Internal testing** → **Promote to production**
2. Or **Production** → **Create new release**
3. Upload signed AAB (build with `./gradlew bundleRelease`)
4. Fill in release notes
5. Submit for review

## Build Release AAB
```bash
$env:JAVA_HOME = "C:\jbr"
.\gradlew.bat bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Checklist
- [ ] Play Console account created ($25)
- [ ] App created in Play Console
- [ ] Store listing complete (name, description, screenshots)
- [ ] Content rating completed
- [ ] Data safety form completed
- [ ] 3 subscriptions created (monthly, yearly, lifetime)
- [ ] 1 in-app product created (lifetime)
- [ ] Privacy policy hosted and URL added
- [ ] RevenueCat linked to Play Console
- [ ] Internal testing track set up
- [ ] Release AAB uploaded
- [ ] Production release submitted

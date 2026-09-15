# RevenueCat Setup Guide — SubHunt

## Step 1: Create Project
1. Go to https://app.revenuecat.com
2. Click **"New Project"**
3. Project name: `SubHunt`
4. App name: `SubHunt`
5. Bundle ID: `com.subhunt.app`
6. Click **Create**

## Step 2: Create Products
Go to **Products** → **Add Product**

### Product 1: Monthly
- **Product ID**: `monthly`
- **Display Name**: SubHunt Pro Monthly
- **Type**: Subscription
- **Duration**: 1 month
- **Price**: $4.99/month
- **Free trial**: 7 days

### Product 2: Yearly
- **Product ID**: `yearly`
- **Display Name**: SubHunt Pro Yearly
- **Type**: Subscription
- **Duration**: 1 year
- **Price**: $29.99/year
- **Free trial**: 7 days

### Product 3: Lifetime
- **Product ID**: `lifetime`
- **Display Name**: SubHunt Pro Lifetime
- **Type**: Non-subscription (one-time)
- **Price**: $79.99

## Step 3: Create Entitlement
Go to **Entitlements** → **Add Entitlement**

- **Entitlement ID**: `subhunt_pro`
- **Display Name**: SubHunt Pro
- **Description**: Unlocks unlimited subscriptions, insights, export, and premium sounds

### Attach Products to Entitlement
Click the entitlement → **Add Product** → Add all 3 products:
- `monthly` → `subhunt_pro`
- `yearly` → `subhunt_pro`
- `lifetime` → `subhunt_pro`

## Step 4: Create Offering
Go to **Offerings** → **Add Offering**

- **Offering ID**: `default`
- **Display Name**: SubHunt Pro
- **Description**: Unlock your subscription power

### Add Packages to Offering
Click the offering → **Add Package**:

| Package Type | Product ID | Display Name |
|-------------|-----------|--------------|
| `monthly` | `monthly` | Monthly |
| `annual` | `yearly` | Yearly (Save 17%) |
| `lifetime` | `lifetime` | Lifetime |

## Step 5: Get API Key
Go to **Projects** → **API Keys**

- **Public API Key** (starts with `goog_`): This is what you need
- Copy it

## Step 6: Update App Code
Open `app/build.gradle.kts` and replace line 24:

```kotlin
buildConfigField("String", "REVENUECAT_API_KEY", "\"YOUR_PRODUCTION_API_KEY_HERE\"")
```

## Step 7: Connect Google Play Console
Go to **Projects** → **Google Play** → **Add Google Play Credentials**

1. You'll need to link your Google Play Console service account
2. Follow RevenueCat's guide: https://www.revenuecat.com/docs/integrations/google-play

## Verification Checklist
- [ ] 3 products created (monthly, yearly, lifetime)
- [ ] Entitlement `subhunt_pro` created
- [ ] All 3 products attached to entitlement
- [ ] Offering `default` created with all 3 packages
- [ ] API key copied to `build.gradle.kts`
- [ ] Google Play Console linked
- [ ] Test purchase in sandbox (RevenueCat → Sandbox → Make test purchase)

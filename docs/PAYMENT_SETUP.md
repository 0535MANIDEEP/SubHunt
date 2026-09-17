# Stripe Payment Setup — Zero Cost, Professional

## What You Get (FREE)
- Professional checkout page (like real apps)
- Accepts: Visa, Mastercard, PayPal, Google Pay, Apple Pay
- Money goes directly to YOUR bank account
- Automatic receipts
- Subscription management for customers
- **Setup cost: ₹0**
- **Monthly fee: ₹0**
- **Fee: 2.9% + ₹25 per transaction (only when someone pays)**

## Step-by-Step Setup (5 minutes)

### 1. Create Stripe Account
1. Go to https://dashboard.stripe.com/register
2. Enter email + password
3. Verify email
4. Enter your details:
   - Name: Manideep Daram
   - Address: Your address
   - Bank account: Your bank details (where you want money deposited)
   - PAN number: Your PAN card
5. Account created! (takes 5 min to activate)

### 2. Create Payment Links
After account is created:

1. Go to https://dashboard.stripe.com/payment-links
2. Click **"Create payment link"**

**Link 1: Monthly**
- Name: SubHunt Pro Monthly
- Price: ₹419 / month
- Description: Unlimited subscriptions, insights, export
- Click **"Create link"**
- Copy the link (looks like: `https://buy.stripe.com/xxxxx`)

**Link 2: Yearly**
- Name: SubHunt Pro Yearly
- Price: ₹2,499 / year (save 40%)
- Description: Unlimited subscriptions, insights, export
- Click **"Create link"**
- Copy the link

**Link 3: Lifetime**
- Name: SubHunt Pro Lifetime
- Price: ₹6,649 one-time
- Description: Unlimited subscriptions, insights, export — forever
- Click **"Create link"**
- Copy the link

### 3. Update App Code
Open `app/src/main/java/com/subhunt/app/ui/screens/paywall/CustomPaywallScreen.kt`

Find these lines (around line 280):
```kotlin
val stripeMonthlyUrl = "https://buy.stripe.com/YOUR_MONTHLY_LINK"
val stripeYearlyUrl = "https://buy.stripe.com/YOUR_YEARLY_LINK"
val stripeLifetimeUrl = "https://buy.stripe.com/YOUR_LIFETIME_LINK"
```

Replace with YOUR actual links:
```kotlin
val stripeMonthlyUrl = "https://buy.stripe.com/abc123..."
val stripeYearlyUrl = "https://buy.stripe.com/def456..."
val stripeLifetimeUrl = "https://buy.stripe.com/ghi789..."
```

### 4. Build & Ship
```bash
$env:JAVA_HOME = "C:\jbr"
.\gradlew.bat assembleRelease
```

## How It Works for Users

```
User taps "Upgrade to Pro"
    ↓
Selects plan (Monthly/Yearly/Lifetime)
    ↓
Taps "Pay Now"
    ↓
Opens Stripe checkout in browser
    ↓
Pays with card/PayPal/Google Pay
    ↓
Gets receipt via email from Stripe
    ↓
You get notified (email from Stripe)
    ↓
You generate activation code → email to user
    ↓
User enters code in app → Pro activated
```

## Your Earnings

| Plan | User Pays | Stripe Fee | You Receive |
|------|-----------|-----------|-------------|
| Monthly | ₹419 | ₹37 | ₹382 |
| Yearly | ₹2,499 | ₹97 | ₹2,402 |
| Lifetime | ₹6,649 | ₹218 | ₹6,431 |

**After 8 monthly subscribers, you've earned back the Stripe fees many times over.**

## Verification Checklist
- [ ] Stripe account created
- [ ] Bank account linked (for receiving money)
- [ ] 3 payment links created (monthly, yearly, lifetime)
- [ ] Links replaced in CustomPaywallScreen.kt
- [ ] Test payment with card 4242 4242 4242 4242
- [ ] Build APK and distribute

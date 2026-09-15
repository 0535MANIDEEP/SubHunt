# Screenshot & Upload Guide — SubHunt

## Required Screenshot Sizes

### Uptodown
- **Minimum**: 320px wide, any height
- **Recommended**: 1080px wide x 1920px tall (9:16 ratio)
- **Format**: PNG or JPEG
- **Maximum**: 15MB per file
- **Number**: 2-8 screenshots

### Google Play Store (when ready)
- **Phone**: 16:9 or 9:16 ratio, 320-3840px
- **Tablet**: 16:9 or 9:16 ratio, 320-3840px
- **Format**: PNG or JPEG
- **Number**: 2-8 screenshots

### Amazon Appstore
- **Landscape**: 1920x1080 or 1280x720
- **Portrait**: 1080x1920 or 720x1280
- **Format**: PNG or JPEG
- **Number**: 3-5 screenshots

## Screenshot Shots You Need (Minimum 5)

### Shot 1: Dashboard (Hero Shot)
- **What**: Main dashboard with stats
- **Focus**: "Track All Your Subscriptions"
- **Shows**: Monthly spending, subscription list, health score

### Shot 2: Add Subscription
- **What**: Add/Edit screen
- **Focus**: "Easy to Add Subscriptions"
- **Shows**: Form with categories, pricing, reminders

### Shot 3: Insights/Analytics
- **What**: Insights screen
- **Focus**: "Smart Spending Insights"
- **Shows**: Health score, savings opportunities, trends

### Shot 4: Widget
- **What**: Home screen with widget
- **Focus**: "At-a-Glance Visibility"
- **Shows**: Widget on home screen with spending data

### Shot 5: Settings/Privacy
- **What**: Settings screen
- **Focus**: "100% Private"
- **Shows**: No bank linking, local storage, export options

## How to Take Screenshots

### Option 1: Use Emulator (Easiest)
1. Start emulator: `emulator -avd SubHunt -gpu swiftshader_indirect`
2. Open SubHunt app
3. Navigate to each screen
4. Press `PrtScn` or use `adb shell screencap`
5. Crop to 1080x1920

### Option 2: Use ADB Commands
```bash
# Take screenshot
adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
adb pull /sdcard/screenshot.png

# For specific window
adb shell screencap -p /sdcard/screenshot.png
```

### Option 3: Use Android Studio
1. Open Device File Explorer
2. Navigate to `/sdcard/`
3. Right-click → Save As

## How to Add Text Overlays (Professional Look)

### Free Tools
- **Canva** (canva.com) — Free, easy, has templates
- **Figma** (figma.com) — Free, professional
- **Photopea** (photopea.com) — Free, Photoshop alternative

### Steps in Canva
1. Create custom design: 1080 x 1920 px
2. Upload your screenshot
3. Add text overlay: "Track All Subscriptions"
4. Add gradient overlay for text readability
5. Download as PNG

### Text Overlay Template
```
[Gradient overlay at top]
[Large text: "Track All Your Subscriptions"]
[Smaller text: "See everything in one place"]
[Screenshot below]
```

## How to Upload to Uptodown

### Step 1: Create Account
1. Go to https://en.uptodown.com/developers-console
2. Sign up with email or Google
3. Verify your email

### Step 2: Upload App
1. Click "Upload App"
2. Fill in app details:
   - **Name**: SubHunt — Subscription Tracker
   - **Category**: Finance
   - **Description**: (use from APP_LISTING_SEO.md)
   - **Package name**: com.subhunt.app
3. Upload APK: `app/build/outputs/apk/release/app-release.apk`

### Step 3: Upload Screenshots
1. Go to "Media" tab
2. Click "Add Screenshots"
3. Upload 5-8 screenshots (1080x1920 recommended)
4. Drag to reorder (dashboard first)

### Step 4: Publish
1. Review all fields
2. Click "Publish"
3. Wait 24-48 hours for review

## How to Upload to GitHub Releases (Already Done)

Your release is already live at:
https://github.com/0535MANIDEEP/SubHunt/releases/tag/v1.0

To add screenshots to release:
1. Go to release page
2. Click "Edit release"
3. Drag screenshots into the release notes
4. Update release

## Screenshot Checklist

- [ ] Dashboard screenshot (hero shot)
- [ ] Add subscription screenshot
- [ ] Insights/Analytics screenshot
- [ ] Widget screenshot
- [ ] Settings/Privacy screenshot
- [ ] All screenshots are 1080x1920
- [ ] Text overlays added
- [ ] No sensitive data visible
- [ ] Professional look and feel

## Free Screenshot Tools

| Tool | Link | Best For |
|------|------|----------|
| Canva | canva.com | Easy text overlays |
| Figma | figma.com | Professional design |
| Photopea | photopea.com | Advanced editing |
| Screenshots.pro | screenshots.pro | Auto-generate |
| Previewed.app | previewed.app | Mockup generator |

## Quick Start (Do This Now)

1. **Start emulator**: `emulator -avd SubHunt -gpu swiftshader_indirect`
2. **Open SubHunt** and navigate to dashboard
3. **Take screenshot** (PrtScn or adb shell screencap)
4. **Open Canva** (canva.com)
5. **Create design** (1080x1920)
6. **Upload screenshot**
7. **Add text**: "Track All Your Subscriptions"
8. **Download** as PNG
9. **Repeat** for 4 more screens
10. **Upload to Uptodown**

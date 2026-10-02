# Car Marketplace - Android Java Demo

This project is a functional Android Studio prototype based on the uploaded Figma design:
`Car Selling MarketPlace project.fig`.

## First screen / Figma flow
- Welcome / Get Started screen
- Get Started opens Login
- Back from Login returns to the welcome screen

## Today's implemented features
- Login screen with demo account
- Dashboard
- Search screen with working search button
- Search by model, year, price, or city
- Profile & Settings screen
- Save-profile demo action
- Notifications setting UI
- Side navigation drawer
- Logout
- Car cards using image assets extracted from the Figma file

## Demo login
This is a local demo. Use any valid email address and a password with at least 6 characters.

Example: `student@gmail.com` / `123456`

## Architecture
- `LoginActivity` - authentication/demo login
- `MainActivity` - app shell, toolbar, side drawer, navigation
- `DashboardFragment` - dashboard and featured cars
- `SearchFragment` - working search
- `ProfileFragment` - profile/settings
- `CarRepository` - demo car data
- `Car` - model
- `SessionManager` - SharedPreferences login session
- `UiUtils` - small UI helpers

## Run
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Use an emulator or Android phone.
4. Run the `app` configuration.
5. Login with the demo account above.

The app intentionally keeps the first lab milestone simple and functional. Firebase/backend, real seller accounts, car posting, filters, favorites, messaging and payment can be added in later milestones.


## Signup / onboarding flow
The Figma-style signup flow is now functional:
Create Account → OTP Verification → Profile Setup → Role & Contact Preferences → Dashboard.

Signup validation:
- Valid email format
- Phone number required
- Password 8+ characters
- Terms checkbox required
- OTP demo accepts any 4 digits (use `1234`)

## Dashboard navigation
Bottom navigation now contains:
Home, Search, Sell, Messages, Profile.

Sell and Messages are functional demo screens, and Profile keeps the existing editable settings.

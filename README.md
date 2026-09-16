# Rent Manager — Android App

A fully offline Android application for residential landlords to manage apartments, tenants, and rent payments. Built with Kotlin and Jetpack Compose.

---

## Table of Contents

- [Overview](#overview)
- [Screenshots Tour](#screenshots-tour)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Data Model](#data-model)
- [Notifications](#notifications)
- [Permissions](#permissions)
- [Getting Started](#getting-started)
- [Building the APK](#building-the-apk)
- [Known Limitations & Future Improvements](#known-limitations--future-improvements)

---

## Overview

Rent Manager is a client-side Android app designed for a landlord who manages a small number of rental apartments. All data is stored locally on the device using Room (SQLite). There is no backend, no login, and no internet requirement — the app works entirely offline.

The app solves three core problems:

1. **Tenant records** — store contact info, ID documents (photos of front and back), profile photo, contract dates, and emergency contacts in one place.
2. **Apartment tracking** — know at a glance which flats are occupied, vacant, and by whom.
3. **Payment tracking and reminders** — log monthly rent payments, mark them as paid, and receive push notifications when rent is due or a contract is nearing its end.

---

## Screenshots Tour

| Screen | Description |
|---|---|
| **Dashboard** | Summary stats (total flats, occupied, tenants, unpaid this month), contract expiry alerts, and pending payment warnings |
| **Apartments** | Searchable list of all flats with occupancy status, rent amount, bedroom/bathroom count |
| **Apartment Detail** | Full flat info plus list of current tenants assigned to it |
| **Add / Edit Apartment** | Form to create or update a flat's details |
| **Tenants** | Searchable list with profile photos, contract status chip, and rent amount |
| **Tenant Detail** | Full profile including ID document photos, contract timeline, payment history, and total paid |
| **Add / Edit Tenant** | Multi-section form: personal info, ID photos (camera or gallery), apartment assignment, contract dates, financial details, emergency contact |
| **Payments** | Month navigator with collected / due / outstanding summary, per-payment quick "Mark Paid" button |
| **Add / Edit Payment** | Form to create or update a payment record with tenant picker, amount, due date, paid date, and status |

---

## Features

### Dashboard
- Live stat cards: total apartments, occupied apartments, active tenants, unpaid payments for the current month
- Quick-access buttons to each main section
- **Contract expiry alerts** — any tenant whose contract ends within the next 30 days is shown with a colour-coded chip (yellow for ≤ 30 days, red for ≤ 14 days or expired)
- **Pending / overdue payment list** — shows up to 5 outstanding payments with a link to view all

### Apartments
- Create, read, update, and delete apartment records
- Fields: name, floor, address, bedrooms, bathrooms, area (m²), monthly rent, amenities, description, notes, occupied toggle
- Search by name or address
- Occupancy status chip (green = Occupied, grey = Vacant)
- Apartment detail screen shows all assigned tenants with tap-through to their profiles
- Deleting an apartment prompts a confirmation dialog

### Tenants
- Create, read, update, and delete tenant records
- **Profile photo** — taken with the camera or chosen from the gallery
- **ID document photos** — front and back of national ID or passport, camera or gallery
- Fields: first name, last name, phone, email, national ID number, nationality, apartment assignment, contract start date, contract end date, move-in date, rent due day (1–28), monthly rent, security deposit (with paid/unpaid toggle), emergency contact name and phone, notes
- Search by name, phone, email, or national ID
- Toggle to show inactive (moved-out) tenants alongside active ones
- **Move Out** action — marks the tenant inactive and frees their apartment in one tap
- Contract status chip on each card reflects days remaining
- Tenant detail screen shows full profile, ID photo thumbnails, contract info, and a scrollable payment history

### Payments
- Create, read, update, and delete payment records
- Fields: tenant, month, year, amount due, amount paid, due date, paid date, status (Pending / Paid / Overdue / Partial), notes
- **Month navigator** — step through months to see payments for that period
- **All Payments** filter tab to see the full history regardless of month
- Summary bar shows total collected, total due, and outstanding balance for the selected view
- **Quick "Mark Paid"** button on each pending/overdue card — sets status to Paid, records today as the paid date, and sets paid amount to the full amount due in one tap
- When adding a payment for a tenant, the monthly rent amount is pre-filled automatically

### Notifications
- Daily background job (WorkManager) checks for:
  - **Rent due reminders** — fires a notification 2 days before, 1 day before, and on the due day for any active tenant
  - **Contract expiry warnings** — fires a notification for any active tenant whose contract ends within the next 30 days
- Two separate notification channels so the landlord can control each type independently in system settings
- Tapping any notification opens the app

---

## Tech Stack

| Component | Library | Version |
|---|---|---|
| Language | Kotlin | 1.9.20 |
| UI framework | Jetpack Compose + Material 3 | BOM 2024.02.00 |
| Navigation | Navigation Compose | 2.7.7 |
| Local database | Room (SQLite) | 2.6.1 |
| Background work | WorkManager | 2.9.0 |
| Image loading | Coil | 2.5.0 |
| Async | Kotlin Coroutines | 1.7.3 |
| Lifecycle | ViewModel + collectAsStateWithLifecycle | 2.7.0 |
| Build system | Gradle | 8.4 |
| Min SDK | Android 8.0 (API 26) | — |
| Target SDK | Android 14 (API 34) | — |

---

## Project Structure

```
RentManager/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/rentmanager/app/
│       │   ├── MainActivity.kt                  # App entry point, bottom nav
│       │   ├── RentManagerApplication.kt        # Application class, DI root
│       │   │
│       │   ├── data/
│       │   │   ├── entities/
│       │   │   │   ├── Apartment.kt             # Room entity
│       │   │   │   ├── Tenant.kt                # Room entity
│       │   │   │   └── Payment.kt               # Room entity + PaymentStatus enum
│       │   │   ├── dao/
│       │   │   │   ├── ApartmentDao.kt
│       │   │   │   ├── TenantDao.kt
│       │   │   │   └── PaymentDao.kt
│       │   │   ├── database/
│       │   │   │   └── AppDatabase.kt           # Room database singleton
│       │   │   └── repository/
│       │   │       └── RentRepository.kt        # Single source of truth
│       │   │
│       │   ├── notifications/
│       │   │   ├── NotificationHelper.kt        # Channel setup + send helpers
│       │   │   └── RentCheckWorker.kt           # Daily WorkManager background job
│       │   │
│       │   └── ui/
│       │       ├── theme/
│       │       │   ├── Color.kt                 # Colour palette
│       │       │   ├── Type.kt                  # Typography scale
│       │       │   └── Theme.kt                 # MaterialTheme wrapper
│       │       ├── navigation/
│       │       │   ├── Screen.kt                # Sealed class of all routes
│       │       │   └── NavGraph.kt              # NavHost + all composable destinations
│       │       ├── components/
│       │       │   └── SharedComponents.kt      # StatCard, TenantAvatar, StatusChip,
│       │       │                                #   InfoRow, EmptyState, ConfirmDeleteDialog
│       │       ├── utils/
│       │       │   └── UiUtils.kt               # Date formatting, currency, contract status
│       │       └── screens/
│       │           ├── dashboard/
│       │           │   └── DashboardScreen.kt
│       │           ├── apartments/
│       │           │   ├── ApartmentsScreen.kt
│       │           │   ├── ApartmentDetailScreen.kt
│       │           │   └── AddEditApartmentScreen.kt
│       │           ├── tenants/
│       │           │   ├── TenantsScreen.kt
│       │           │   ├── TenantDetailScreen.kt
│       │           │   └── AddEditTenantScreen.kt
│       │           └── payments/
│       │               ├── PaymentsScreen.kt
│       │               └── AddEditPaymentScreen.kt
│       │
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── xml/
│               └── file_paths.xml               # FileProvider paths for camera output
│
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── build.gradle                                 # Project-level plugins
├── app/build.gradle                             # App-level dependencies
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

---

## Architecture

The app follows the **MVVM-lite** pattern recommended by Google for Compose projects, simplified for a single-developer offline app where a dedicated ViewModel per screen would add boilerplate without benefit.

```
┌─────────────────────────────────────────────┐
│                    UI Layer                  │
│  Composable Screens  ←→  Shared Components  │
│         ↑  collectAsStateWithLifecycle       │
├─────────────────────────────────────────────┤
│                Repository Layer              │
│             RentRepository.kt               │
│   (single source of truth, exposes Flows)   │
│         ↑  suspend funs / Flows             │
├─────────────────────────────────────────────┤
│                  Data Layer                  │
│   ApartmentDao  TenantDao  PaymentDao       │
│              AppDatabase (Room)              │
│                SQLite on device             │
└─────────────────────────────────────────────┘
                      ↕
┌─────────────────────────────────────────────┐
│             Background Layer                 │
│   RentCheckWorker (WorkManager, daily)      │
│   NotificationHelper (system notifications) │
└─────────────────────────────────────────────┘
```

**Data flow:**
- The repository exposes `Flow<List<T>>` for list queries. Screens collect these with `collectAsStateWithLifecycle`, which automatically pauses collection when the screen is not visible and resumes when it is — preventing unnecessary work.
- Write operations (insert, update, delete) are `suspend` functions called from `rememberCoroutineScope()` inside composables, dispatched on `Dispatchers.IO`.
- Navigation is handled by a single `NavHost` in `NavGraph.kt`. The `RentRepository` is passed down from `MainActivity` through the nav graph rather than injected via a DI framework, keeping the setup simple.

---

## Data Model

### Apartment

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `name` | String | Display name, e.g. "Flat 1A" |
| `floor` | String | Floor number or label |
| `address` | String | Street address |
| `description` | String | Free-text description |
| `bedrooms` | Int | Number of bedrooms |
| `bathrooms` | Int | Number of bathrooms |
| `areaSqm` | Double | Area in square metres |
| `monthlyRent` | Double | Standard monthly rent amount |
| `isOccupied` | Boolean | Whether the flat currently has a tenant |
| `amenities` | String | Comma-separated list of amenities |
| `notes` | String | Internal notes |

### Tenant

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `firstName` / `lastName` | String | Full name |
| `phone` / `email` | String | Contact details |
| `nationalId` | String | ID or passport number |
| `nationality` | String | Nationality |
| `photoUri` | String | URI of profile photo (file or content URI) |
| `idPhotoFrontUri` | String | URI of ID front photo |
| `idPhotoBackUri` | String | URI of ID back photo |
| `apartmentId` | Long? | Foreign key to apartments table (nullable) |
| `contractStartDate` | Long | Epoch milliseconds |
| `contractEndDate` | Long | Epoch milliseconds |
| `rentDueDay` | Int | Day of month rent is due (1–28) |
| `monthlyRent` | Double | Agreed monthly rent |
| `securityDeposit` | Double | Deposit amount |
| `depositPaid` | Boolean | Whether the deposit has been received |
| `emergencyContactName` | String | Name of emergency contact |
| `emergencyContactPhone` | String | Phone of emergency contact |
| `notes` | String | Internal notes |
| `isActive` | Boolean | False when tenant has moved out |
| `moveInDate` | Long | Epoch milliseconds |

### Payment

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `tenantId` | Long | Foreign key to tenants table |
| `apartmentId` | Long | Foreign key to apartments table |
| `amount` | Double | Amount due |
| `dueDate` | Long | Epoch milliseconds |
| `paidDate` | Long? | Epoch milliseconds, null if unpaid |
| `paidAmount` | Double | Amount actually received |
| `month` | Int | Month this payment covers (1–12) |
| `year` | Int | Year this payment covers |
| `status` | String | One of: `PENDING`, `PAID`, `OVERDUE`, `PARTIAL` |
| `notes` | String | Internal notes |

---

## Notifications

The app uses **WorkManager** to schedule a daily background check that runs once per day, even if the app is not open.

**`RentCheckWorker`** does two things each run:

1. Queries all active tenants whose `rentDueDay` matches today, yesterday, or tomorrow and sends a "Rent Due" notification for each.
2. Queries all active tenants whose `contractEndDate` falls within the next 30 days and sends a "Contract Expiry" notification for each.

Two notification channels are registered at app startup:

| Channel ID | Name | Importance |
|---|---|---|
| `rent_due_channel` | Rent Due Reminders | HIGH (shows heads-up) |
| `contract_expiry_channel` | Contract Expiry Reminders | HIGH (shows heads-up) |

The user can manage each channel independently in Android Settings → Apps → Rent Manager → Notifications.

---

## Permissions

| Permission | When requested | Why |
|---|---|---|
| `POST_NOTIFICATIONS` | On first launch (Android 13+) | Required to show rent and contract notifications |
| `CAMERA` | When the user taps "Take Photo" for the first time | Required to launch the camera intent |
| `READ_MEDIA_IMAGES` | Granted implicitly by the gallery picker | Required to read photos selected from the gallery on Android 13+ |
| `READ_EXTERNAL_STORAGE` | Granted implicitly by the gallery picker | Required to read photos on Android 12 and below |

Camera permission is requested at the point of use (when the user taps "Take Photo") rather than at startup, following Android best-practice guidelines.

---

## Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** (bundled with recent Android Studio versions)
- An Android device or emulator running **Android 8.0 (API 26) or higher**

### Installation

1. Download `RentManager.zip` and extract it.
2. Open **Android Studio** → **File** → **Open** → select the extracted `RentManager` folder.
3. Wait for Gradle to sync. It will automatically download all dependencies on the first sync — this requires an internet connection.
4. Connect a device via USB (with USB debugging enabled) or start an emulator.
5. Press **Run** (▶) or use `Shift+F10`.

> **Note:** The first Gradle sync can take several minutes depending on your internet connection. Subsequent builds are much faster.

### Running on a Physical Device

For camera functionality to work on a physical device:

1. Enable **Developer Options** on your device (Settings → About Phone → tap Build Number 7 times).
2. Enable **USB Debugging** in Developer Options.
3. Connect via USB and accept the debugging prompt on the device.
4. Select your device in the Android Studio device dropdown and press Run.

---

## Building the APK

To generate a debug APK you can install manually:

```bash
# From the project root directory
./gradlew assembleDebug
```

The APK will be output to:
```
app/build/outputs/apk/debug/app-debug.apk
```

To install it directly to a connected device:

```bash
./gradlew installDebug
```

To build a release APK (requires a signing keystore):

```bash
./gradlew assembleRelease
```

---

## Known Limitations & Future Improvements

### Current Limitations

- **No cloud backup** — all data is stored only on the device. If the device is lost or reset, all records are lost. Consider exporting important data regularly.
- **No multi-user support** — the app is designed for a single user on a single device.
- **No currency setting** — amounts are displayed in the device's default locale currency. If you need a specific currency, the formatting can be changed in `UiUtils.kt` in the `formatCurrency()` function.
- **Photos are stored as URIs** — if you clear the app's storage or uninstall and reinstall, existing photo URIs may become invalid. For photos taken with the camera, the files are stored in the app's internal storage and survive normal use. Gallery photos reference their original location on the device.
- **Rent due notifications are simple** — the worker checks by day-of-month only and does not automatically cross-reference with the payments table to see if rent has already been logged as paid for that month.

### Suggested Future Improvements

- **Export to PDF or CSV** — generate monthly rent reports or a full tenant list.
- **Room backup and restore** — copy the SQLite database file to external storage or Google Drive for backup.
- **Automatic payment generation** — when a tenant is created, automatically generate Payment records for each month of their contract.
- **Overdue payment auto-marking** — a daily job to set the status of any payment still `PENDING` past its due date to `OVERDUE`.
- **Multiple currencies** — user-settable currency preference stored in SharedPreferences.
- **Dark mode** — add a dark colour scheme to `Theme.kt`.
- **Lease document attachments** — store PDF URIs against a tenant record alongside the photo URIs.
- **Receipt generation** — generate a shareable payment receipt when a payment is marked as paid.

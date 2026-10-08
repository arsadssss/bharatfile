# BharatFile 🇮🇳
**A Modern, Privacy-First Native Android Document Utility Application**

BharatFile is a production-quality native Android application engineered for Indian students, teachers, job applicants, office professionals, and anyone who frequently works with PDFs, images, scanned certificates, and application forms.

---

## 🎨 UI/UX Design System & Visual Reference
The visual language faithfully reproduces modern translucent glassmorphism:
- **Atmosphere & Surfaces**: Ultra-clean soft lavender/white background (`#F6F8FE` light, `#0F111A` dark) with delicate radial atmospheric glows and frosted glass cards (`GlassCard`).
- **Segmented Control**: Pill-shaped container with smooth animated gradient indicator (Electric Blue `#4E65FF` to Purple `#7A5AF8`) for active mode (`[ PDF ] [ Image ]`).
- **3D Visuals & Illustrations**:
  1. *Upload State*: Translucent 3D layered glass folder with crimson squircle PDF badge.
  2. *Processing State*: 3D floating tilted document stack with orbiting pearls and glowing circular backdrop.
  3. *Success State*: Emerald green glowing halo with floating translucent document, checkmark badge, and celebration confetti.
- **Persistent Bottom Navigation**: 4-tab frosted glass bottom bar (`Home`, `Tools`, `History`, `Profile`) with floating animated indicator pills and window insets support.
- **Responsive Hierarchy**: Optimized for mobile touch ergonomics with smooth animations and dark theme support.

---

## ⚡ New & Advanced Features

### 1. Continuous Custom Compression Slider (1% → 100%)
- Replaces rigid stepped presets with a continuous 1% to 100% slider.
- Real mathematical parameter mapping:
  - **Images**: Maps selected reduction percentage to JPEG/WebP compression quality and dimension downsampling factors.
  - **PDFs**: Maps percentage to page bitmap rasterization resolution (DPI scaling from 200 DPI down to 72 DPI) and compression quality factor.
- Quick shortcut chips (`25%`, `50%`, `75%`, `90%`).
- One-tap "Save as Default" for future sessions via persistent `PreferencesManager`.

### 2. Target File Size Mode (KB / MB)
- Specify an exact target output size (e.g., `50 KB`, `100 KB`, `200 KB`, `2 MB`).
- **8 Quick Preset Chips**: `50 KB`, `100 KB`, `200 KB`, `500 KB`, `1 MB`, `2 MB`, `5 MB`, `10 MB` tailored for government examination portals (UPSC, SSC, IBPS, NTA JEE/NEET, State PSCs).
- **Iterative Search Algorithm**: Binary search on image/PDF compression parameters to converge within the target budget.
- **Validation**: Proactively prevents selecting target sizes larger than the original file with clear user feedback.
- **Target Delta Comparison**: Success card explicitly displays target size, actual produced size, and variance (e.g., `Target: 200 KB | Actual: 180 KB (20 KB under target)`).

### 3. Cancel Processing with Clean Recovery
- Active compression tasks display a prominent "Cancel" button.
- Clean coroutine cancellation using `ensureActive()` without freezing the UI thread.
- Immediate removal and cleanup of partial temporary files via `FileCleaner`.

### 4. Context-Aware Recommended Tools
- Every utility page features a context-aware "Recommended Tools" row suggesting complementary actions (e.g., after compressing a PDF: Organize, Split, or Convert to Image).

### 5. History System
- Dedicated `HistoryScreen` displaying all locally processed documents.
- Stores metadata offline using JSON-backed SharedPreferences: file name, original size, output size, date/time, and operation type.
- Actions: Open document, Share, Delete individual item, and Clear All History.

### 6. Profile & Offline Account System
- Dedicated `ProfileScreen` with offline-first authentication (`LocalAuthRepository`).
- Full local sign-up, sign-in, profile update (name, username, email, phone, avatar), and password change.
- **Security**: Passwords salted and hashed with SHA-256 before local persistence—never stored in plaintext.
- **Usage Statistics**: Real-time counter of total files processed and cumulative megabytes/gigabytes saved.

### 7. Settings & Preferences
- Dedicated `SettingsScreen` to configure default PDF and Image compression percentages.
- Dark mode toggle, cache cleaner (displays cache size and purges temp files), Privacy Policy, Terms, and App Info.

### 8. Progressive Web App (PWA) Companion
- Web version bundle included in `/web`:
  - `manifest.json`: Web app manifest with standalone display mode and icons.
  - `sw.js`: Service worker caching static assets for full offline operation.
  - `index.html`: Responsive glassmorphism web client matching the native Android UI.

---

## 📱 Complete Toolset Overview
1. **PDF Compressor**: Continuous 1-100% or Target Size in KB/MB.
2. **Image Compressor**: Continuous slider or Target Size for JPG/PNG/WebP with EXIF rotation preservation.
3. **Image Resizer**: Dimension resizing with aspect ratio lock and quick dimension presets.
4. **Image Converter**: Instant conversion between JPG, PNG, and WebP with transparency handling.
5. **Images to PDF**: Multi-image intake, reordering, and A4 PDF generation.
6. **PDF to Image**: High-resolution page extraction to JPG/PNG in Downloads.
7. **PDF Merger**: Multi-file reorderable PDF combiner.
8. **PDF Splitter**: Custom page range parser (`1-3, 5, 8-10`).
9. **Organize PDF**: Page grid with 90° rotation and deletion.
10. **Document Scanner**: Multi-page scanner with Magic Color, Grayscale, and B&W filters.
11. **Signature Maker**: Canvas with smooth stroke rendering and transparent PNG export.
12. **Passport Photo Maker**: Presets for Indian Passport (`3.5 × 4.5 cm`), Stamp Size, and Square ID.

---

## 🔒 100% On-Device Privacy
- **Zero Cloud Uploads**: All document processing runs locally on the device using native Android rendering and compression APIs.
- **Scoped Storage & SAF**: Uses Android's Storage Access Framework and Photo Picker—no broad file system permissions required.
- **Offline First**: All user profile, history, and preferences stay on the local device.

---

## 🛠 Technology Stack & Architecture
- **Language**: Kotlin 1.9.23
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture with Coroutines & StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil Compose
- **Target SDK**: Android 14 (API 34), Min SDK: Android 7.0 (API 24)
- **Build Tool**: Gradle 8.5 with OpenJDK 17

---

## 🚀 How to Install and Run

### Option 1: Direct APK Install (No Android Studio required!)
A pre-built debug APK is available directly at the root of the project:
```bash
BharatFile.apk
```
1. Transfer `BharatFile.apk` to your Android device via USB, WhatsApp, or Google Drive.
2. Open the file on your device and tap **Install**.
3. Enjoy BharatFile with full offline document processing!

### Option 2: Open in Android Studio
1. Launch **Android Studio** (Hedgehog or newer).
2. Open `/Users/arsad/Downloads/BharatFiles`.
3. Gradle will sync automatically using OpenJDK 17.
4. Click **Run** (`Shift + F10`) to build and deploy to your connected device or emulator.

### Option 3: Command Line Build & Tests
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

---

## 🧪 Unit Test Suite
Verified tests in `app/src/test/java/com/bharatfile/app/`:
- `CompressionConfigTest`: 1-100% boundary checks, KB/MB byte calculations, preset verification, and target size validation.
- `ProcessedFileResultTest`: Formatted file sizes, savings percentage formulas, and target size delta comparisons.
- `PdfSplitterTest`: Page range syntax validation (`1-3, 5, 8-10`) and boundary checking.
- `CompressionLevelTest`: Quality factors and progress mapping.
- `ImageFormatTest`: Extension and MIME type resolution.
- `PassportPresetTest`: Standard aspect ratio dimension validation.

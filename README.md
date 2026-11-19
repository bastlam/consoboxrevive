# ConsoBox Revive (1.22)

Reconstruction of **ConsoBox 1.20** (`com.consobox`, no longer maintained) as an Android Studio / Gradle project,
migrated so it installs and runs on current Android versions, plus a **Google Drive monthly backup**.

The sources were recovered by decompiling `ConsoBox [1.20].apk` (jadx + apktool) and then cleaned up.

## Build

Requirements: JDK 17+, Android SDK (platform 36). Gradle 9.8.2 / AGP 9.1 via the wrapper.

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`.

Signing uses `keystore.properties` + `consobox-release.jks` at the project root (both git-ignored).
**Keep a private copy of these two files**: every future update must be signed with the same key,
and the Google Drive authorization is bound to its SHA-1.

### Release build on GitHub Actions

Workflow **Release APK** (`.github/workflows/release-apk.yml`), started manually from the *Actions* tab
(*Run workflow*). It builds and signs `ConsoBox-<versionName>.apk`, attaches it to the run as an artifact and,
when *Publish a GitHub release* is ticked, creates (or updates) the release `v<versionName>` with the APK.

> [!IMPORTANT]
> **Before each new release**, bump the version in `app/build.gradle.kts` (`defaultConfig`) and commit it:
> - `versionCode`: integer, **+1** at every release (Android refuses to install an APK whose
>   `versionCode` is lower than the installed one);
> - `versionName`: displayed version, e.g. `"1.22"` → `"1.23"` (also used for the APK name and the `v<versionName>` tag).
>
> Otherwise the workflow **overwrites the APK of the existing release** instead of creating a new one.

Repository secrets to create once (*Settings > Secrets and variables > Actions*):

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `consobox-release.jks` encoded in base64 |
| `KEYSTORE_PASSWORD` | `storePassword` from `keystore.properties` |
| `KEY_ALIAS` | `keyAlias` (`consobox`) |
| `KEY_PASSWORD` | `keyPassword` |

## What changed for modern Android

| Problem in 1.20 | Fix |
|---|---|
| `targetSdk` 4 → install blocked on Android 14+ | `minSdk 23`, `targetSdk 35`, `compileSdk 36` |
| Hard-coded `/sdcard/...` paths (blocked by scoped storage) | App storage (`Android/data/com.consobox/files`) + Storage Access Framework for export / import |
| `MODE_WORLD_WRITEABLE` preferences (crash since Android 7) | Private mode |
| `PendingIntent` without mutability flag | `FLAG_IMMUTABLE` |
| Apache `HttpClient` (removed) | `HttpURLConnection` (Google Drive API only) |
| Support library v4, legacy AdMob SDK, in-app billing v1 | AndroidX ViewPager; ads and billing removed |
| Gallery photos stored as `content://` URIs (unreadable after restart) | Photos copied into the app storage |
| Menus only reachable with the hardware Menu key | Action bar with visible actions |
| Edge-to-edge enforced on Android 15 | Opted out (legacy layout kept) |
| WAL journal (Android 9+) could make raw DB copies incomplete | Checkpoint before every backup |

## Modern UI (1.22)

* Light / dark theme following the system setting, teal Material-style palette (`res/values*/colors_theme.xml`).
* All the original bitmaps (including the Fotolia pictures) replaced by vector icons on tonal badges,
  generated with the same resource names, so the Java code did not change.
* New adaptive launcher icon (themed icon on Android 13+), splash screen, home cards, rounded dialogs,
  floating "add" buttons, modern tabs and expense timeline.
* Redesigned, resizable home-screen widget (tap = add a fill-up).
* `docs/logo.svg` is the logo source; `docs/logo.png` (120×120) is the logo for the Google consent screen.

## Removed features (1.22)

* **Maintenance tracking ("révisions")**: background check and reminders, "Révisions" tab of the car screen,
  editable list of maintenance items. The database tables are kept, so old backups still restore and
  past maintenance expenses keep their label in the "Dépenses" tab.
* **Points of interest** (garage / station / car wash search).
* **Technical sheets** ("Fiches techniques" downloaded from olly.free.fr).
* **Tyres** ("Pneus" tab and tyre list). The `pneu` table is kept; past tyre expenses still appear in "Dépenses".
* Link to the original author's website in the settings.
* News pop-up downloaded at start-up from the original server, and all notifications (no notification permission requested any more).

## Backup features (Settings screen)

* **Sauvegarde sur l'appareil**
  * *Exporter vers un fichier…* — choose any location (Downloads, SD card, Drive app…).
  * *Restaurer depuis un fichier…* — imports a `.bak` file, **including the `consobox.bak` produced by version 1.20**.
    The file is validated before replacing data; a safety copy of the current data is kept in `files/before-restore.bak`.
* **Sauvegarde Google Drive**
  * *Compte Google* — connect / disconnect.
  * *Sauvegarde automatique mensuelle* — a daily WorkManager job (network + battery not low) uploads a backup when the
    last successful one is one month old; a missed run is retried the next day. If Google asks for the authorization again, reconnect the account in Settings (the app sends no notifications).
  * *Sauvegarder sur Drive maintenant* / *Restaurer depuis Drive…*
  * Files go to a `ConsoBox` folder at the root of the Drive (`consobox-YYYY-MM-DD_HH-mm.bak`); the 12 most recent are kept.
  * Scope: `drive.file` only (the app can only see files it created).

## One-time Google Cloud setup (required for the Drive backup)

Android apps authenticate to Google with an **OAuth client of type Android** matching the package name and the
signing certificate. No client ID is embedded in the app.

1. Open <https://console.cloud.google.com/> and create a project (e.g. "ConsoBox").
2. *APIs & Services → Library* → enable **Google Drive API**.
3. *APIs & Services → OAuth consent screen* (Google Auth Platform):
   * User type **External**, app name "ConsoBox", your e-mail as support/developer contact.
   * Data access / scopes: add `https://www.googleapis.com/auth/drive.file` (non-sensitive scope, no Google verification).
   * Audience: either add your Google account as **test user**, or **publish** the app (In production).
     In *Testing* mode, Google expires the authorization after 7 days, which would break the monthly backup:
     publishing is recommended (no review is needed for `drive.file`).
4. *Credentials → Create credentials → OAuth client ID* → type **Android**:
   * Package name: `com.consobox`
   * SHA-1: `D2:6F:EE:51:08:4C:12:EF:FA:7B:C6:F0:1B:30:96:74:A7:44:E3:C9` (release key generated for this project;
     print it again with `keytool -list -v -keystore consobox-release.jks`).
   * For debug builds, add a second Android client with the SHA-1 of `~/.android/debug.keystore`.
5. Install the APK, open *Paramètres → Compte Google*, pick the account and accept.

## Migrating data from the old app

The new APK is signed with a different key, so it cannot update the old installation in place.

1. In the old app (on the old phone), use *Sauvegarde des données* → creates `/sdcard/consobox.bak`.
2. Copy `consobox.bak` to the new phone (or upload it to Drive / Downloads).
3. In ConsoBox 1.21: *Paramètres → Restaurer depuis un fichier…* and select it.

Car photos are not part of the database backup.

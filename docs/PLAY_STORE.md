# Play Store listing — Zikra

Package: `com.mizbamd.zikra`  
Default listing language: English.

## Privacy policy URL

**Privacy policy URL:** https://mizbamd.github.io/zikra/privacy.html

Hosted from [PRIVACY.md](PRIVACY.md) via GitHub Pages (`docs/privacy.html`). Paste that URL in:

- Play Console → App content → Privacy policy
- Store listing → Privacy policy

Until it is hosted, the console field stays empty and you cannot complete production publish.

---

## Short description (max 80 characters)

```
A quiet tasbih for dhikr. Guest or signed-in frames, offline-first.
```

(68 characters)

---

## Full description

```
Zikra (ذكرى) is a quiet place to remember.

Count dhikr on your phone without noise, streaks, or social features. Guest mode is one large SubhanAllah counter and works offline — no account required.

Sign in with email to keep independent frames (one counter per dhikr), with daily and lifetime totals. Defaults include SubhanAllah, Alhamdulillah, Allahu Akbar, and Astaghfirullah. Choose further dhikr from the in-app catalog — not free-form user posts.

Tap the number to add one. Tap the Arabic to open Focused mode (full-screen bead). Undo, reset today, optional haptics, and optional volume-up to count in Focused.

Dates show Gregorian with Hijri (Umm al-Qura) underneath. The Hijri day can roll at local sunset when you allow location; otherwise a Makkah sample location is used. Location stays on the device and is not sent to Zikra servers.

Signed-in frames sync to Zikra’s small API when the network is available. The phone remains offline-first.

Delete your account anytime: You → Delete account. That removes your account, frames, and counts from the server and wipes local app data for that account.

No ads. No feed. No public profiles.
```

---

## Target audience

- **Users:** Adults and teens who want a simple tasbih.
- **Play Console target age:** 13+ (accounts exist; the app is not directed at children).
- **Do not** check “primarily for children” (COPPA / Families).
- **Category:** Lifestyle (or Tools). Religion is expressed in content rating, not as a separate Play category in every region.

---

## Content rating (IARC questionnaire notes)

Zikra is a religious utility. Suggested answers:

| Topic | Answer | Note |
|---|---|---|
| Violence | None | Counter only |
| Sexuality | None | |
| Language | None / mild religious phrases | Dhikr phrases, not profanity |
| Controlled substances | None | |
| User-generated content | **No** (or “catalog / predefined only”) | Frames are chosen from a fixed dhikr catalog. Users do not post, chat, or share publicly |
| Location sharing | Users do not share location with others | Optional on-device sunset |
| Digital purchases | None in v1 | |
| Age | Everyone / PEGI 3 equivalent expected | Confirm after IARC questionnaire |

There is no social UGC, comments, or unmoderated text box that publishes to other users.

---

## Data safety form (Play Console)

Google’s “collected” means data **sent off the device**. Guest mode sends nothing.

### Data collected (signed-in only, sent to Zikra API)

| Type | Collected? | Shared with third parties? | Sold? | Required? | Purpose |
|---|---|---|---|---|---|
| Email address | Yes | No | No | Yes, for account | App functionality (account) |
| User passwords | No (app uses email OTP; legacy password hashes may exist server-side for older accounts) | No | No | — | — |
| Other user-generated content | Yes — dhikr frames and counts the user records | No | No | Optional (guest works without it) | App functionality |
| Approximate / precise location | **No** (not transmitted) | — | — | — | Stays on device for Hijri sunset |
| Contacts, photos, files, health, financial | No | | | | |
| Device or other IDs for ads | No | | | | |
| Crash logs / analytics | No in this version | | | | |

**Encryption in transit:** Yes (production API is HTTPS only).  
**Users can request deletion:** Yes — in-app **You → Delete account**, which calls `DELETE /v1/account`.  
**Account creation:** Optional (guest works).  
**Data is not sold.** Do not check “data used for advertising” or “shared for advertising.”

### Security practices (check what is true)

- [x] Data encrypted in transit (HTTPS in production)
- [x] Users can request that data be deleted

Do not claim independent security review unless you have one.

---

## Permission justifications

### INTERNET

Needed so signed-in users can request an email sign-in code, sign in, sync frames/counts, and delete their account. Guest mode does not require a working network. Cleartext HTTP is for local/emulator development only; production builds should use `https://zikra-api.fly.dev` (custom domain is backlog) — see [DEPLOY.md](DEPLOY.md).

### VIBRATE

Optional haptic feedback when the user taps to count. Controlled by **You → Haptics on tap** (on by default). Not used for marketing.

### ACCESS_COARSE_LOCATION / ACCESS_FINE_LOCATION

Optional. Used only to estimate local sunset so the Hijri date can change at sunset. Coordinates are **not** sent to the server. The user can disable **You → Use location for Hijri date**; the app then uses a Makkah sample location. Declare this in the Play location permission declaration with that wording.

---

## Graphics / screenshots checklist

Phone (required):

- [ ] Welcome (wordmark + Continue as guest / Sign in)
- [ ] Guest counter (large bead, dates visible)
- [ ] Signed-in Home with several frames
- [ ] Focused (full-screen Arabic)
- [ ] You / settings (haptics, location, sign out, **Delete account**)
- [ ] History (optional but useful)

Notes:

- Capture on a clean device; avoid a real personal email in screenshots if you can use a demo account.
- 16:9 or Play’s current phone screenshot sizes; no compressed JPEG artifacts.
- Feature graphic 1024×500, high-res icon 512×512.
- No tablet required for first publish if you only support phones.

---

## Store listing extras

- **Title:** Zikra
- **Tags:** tasbih, dhikr, counter, islam, prayer
- **Contact email:** mizbauddin.md@gmail.com
- **Website:** optional; same host as the privacy policy is enough

---

## First-publish checklist (week one)

### Before you create the production release

- [x] Host privacy on HTTPS (https://mizbamd.github.io/zikra/privacy.html); paste URL in Play Console
- [ ] Deploy API with HTTPS ([DEPLOY.md](DEPLOY.md)); set release `API_BASE_URL` to that URL (default `https://zikra-api.fly.dev`)
- [ ] Confirm `GET https://…/health` returns ok
- [ ] Confirm signed-in **Delete account** removes the row in Postgres and returns to welcome
- [ ] Guest: no delete-account control (nothing to delete)
- [ ] Google Sign-In button stays hidden unless a Web client ID is configured (leave it hidden for v1)
- [ ] Content rating questionnaire completed
- [ ] Data safety form matches the table above
- [ ] Target audience 13+, not “for kids”
- [ ] Location permission declaration (Hijri sunset, on-device only)
- [ ] Screenshots + feature graphic + icon
- [ ] Short + full description pasted
- [ ] App signs with the **upload key**; Play App Signing enrolled
- [ ] `targetSdk` meets Play’s current requirement (see `android/app/build.gradle.kts`)
- [ ] Testing: guest increment persists; signed-in sync; focused undo/reset; Hijri under Gregorian

### Do not

- Upload an AAB from GitHub Actions until Play secrets exist
- Submit a privacy URL that 404s
- Enable incomplete Google Sign-In
- Claim you sell or share data for ads

### After publish

- [ ] Internal testing track first, then production
- [ ] Daily Postgres dump ([DEPLOY.md](DEPLOY.md#backups))
- [ ] Watch Play Console pre-launch report and policy emails

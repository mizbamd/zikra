# Privacy Policy for Zikra

**Effective date:** 11 August 2026  
**App:** Zikra (ذكرى) — Android package `com.mizbamd.zikra`  
**Operator:** Miz Mohammad  
**Contact:** mizbauddin.md@gmail.com

This policy describes how Zikra handles information when you use the Android app and the optional Zikra sync API.

**Host this file on a public HTTPS URL** (GitHub Pages or a simple site) and paste that URL into Play Console and the store listing. Do not invent a live privacy URL until the page is actually published.

---

## What Zikra is

Zikra is a quiet tasbih / dhikr counter. You can use it as a **guest** with no account, or **sign in** with email and password so frames and counts can sync across devices.

Google Sign-In is not offered in this version.

---

## Information we handle

### Guest (no account)

Guest mode works **offline**. Dhikr counts and settings stay on your device (local database and preferences). They are **not** sent to Zikra servers. There is no account to delete; uninstalling the app (or clearing app storage) removes guest data on that device.

### Signed-in accounts

If you create an account, we store:

| Data | Why |
|---|---|
| Email address | Account identifier and sign-in |
| Password (bcrypt hash only) | Authentication. We never store plaintext passwords |
| Dhikr frames | Arabic text, transliteration, optional daily target, sort order, timestamps |
| Counts | Daily counts and lifetime totals per frame |
| Account timestamps | Created / updated times |

We do **not** sell your data. We do **not** use it for advertising. We do **not** share it with data brokers, advertisers, or analytics companies.

There is no third-party advertising SDK, no crash-reporting product, and no analytics product in this version.

### Location (on device only)

If you enable “Use location for Hijri date,” the app may read approximate or precise location **on your device** to estimate local sunset so the Hijri date can roll at sunset (Umm al-Qura). Coordinates are stored only in on-device preferences. **Location is not uploaded to Zikra servers.** You can turn this off; the app then uses a sample Makkah location for the calendar only.

### Other on-device settings

Haptics, language, “volume up adds one,” and daily reset (midnight or Fajr) are stored on the device and are not required for an account.

---

## How we use information

- Provide the app and sync your frames and counts when you are signed in.
- Authenticate you (email + password).
- Estimate Hijri sunset on the device when location is enabled.
- Comply with Play Store account-deletion requirements and applicable data-protection requests.

JWT session tokens are issued by the API (currently 14 days). Tokens are not a separate marketing identifier.

---

## Legal bases (where GDPR / similar laws apply)

- **Contract / requested service:** account, sync, and deletion.
- **Legitimate interest:** keeping the service secure (password hashing, rejecting invalid sign-in).
- **Consent:** optional location permission on Android.

---

## Sharing

We do not sell personal information.

We may share data only if required by law, or with a hosting provider that stores the database and API on our behalf (for example a VPS or Fly.io Postgres). Those processors are used only to run Zikra, not to build profiles or ads.

---

## Retention and deletion

- **Guest data:** remains on the device until you clear storage or uninstall.
- **Account data:** kept while the account exists.
- **Delete account:** in the app go to **You → Delete account** and confirm. That calls `DELETE /v1/account` (same as `POST /v1/account/delete`) and removes the user, frames, and counts from the server, then wipes local Room and DataStore on that device and returns you to the welcome screen.

After deletion, a leftover JWT cannot load an account that no longer exists. Rotating `JWT_SECRET` on the server invalidates all outstanding tokens.

If you cannot use the app, email mizbauddin.md@gmail.com from the address on the account and ask us to delete it.

---

## Children

Zikra is not directed at children under 13. We do not knowingly collect personal information from children under 13. If you believe we have, contact us and we will delete it.

---

## Security

Passwords are hashed with bcrypt. The production API should be served only over HTTPS. Access to the database is limited to the API. No method of transmission or storage is 100% secure.

---

## International processing

If you sign in, account data is stored wherever the Zikra API and Postgres database are hosted (see the deployment in use at the time). By creating an account you understand that data may be processed outside your country, with safeguards appropriate to that host.

---

## Changes

We may update this policy. The effective date at the top will change. Material changes will be reflected in the hosted copy of this document.

---

## Contact

Miz Mohammad  
mizbauddin.md@gmail.com

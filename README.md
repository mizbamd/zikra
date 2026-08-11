# Zikra (ذكرى)

Android-first tasbih / dhikr counter. Play listing name: **Zikra**. Application id / package: `com.mizbamd.zikra`.

Guest can count without an account. Signed-in users get independent **frames** (one counter per dhikr), offline-first on the phone, then synced to a small Kotlin API.

**GitHub:** private repo [mizbamd/zikra](https://github.com/mizbamd/zikra). Baseline tag **v0.1.0**. After that, every change (and every release) goes through a pull request into `main` — see [CONTRIBUTING.md](CONTRIBUTING.md).

CI: `.github/workflows/ci.yml` runs `server/gradlew test` on pull requests. It does **not** deploy or upload a Play AAB. Production API: [docs/DEPLOY.md](docs/DEPLOY.md). Play listing: [docs/PLAY_STORE.md](docs/PLAY_STORE.md). Privacy policy (host before publish): [docs/PRIVACY.md](docs/PRIVACY.md).

## Product (v1)

- **Guest:** one large SubhanAllah counter, no network required.
- **Signed in:** default frames
  - سبحان الله SubhanAllah · 33
  - الحمد لله Alhamdulillah · 33
  - الله أكبر Allahu Akbar · 34
  - أستغفر الله Astaghfirullah · 100
- Tap the number on a frame to increment. Tap the Arabic to open Focused (full-screen bead).
- Undo, reset today, optional volume-up +1 in Focused, haptics on tap.
- Daily count + lifetime. Optional target. Quiet “Done” when the target is hit. No auto-advance.
- Dates: Gregorian, Hijri (Umm al-Qura) underneath, then “Based on your location”. Hijri rolls at sunset when location is available; otherwise a Makkah sample location is used.

## Layout

```
android/   Jetpack Compose app (Room + DataStore + Koin)
server/    Ktor + PostgreSQL + Flyway + JWT
```

## Run the server

Requires Java 21 and local Homebrew PostgreSQL 14 (database name **`zikra`**).

```bash
# once
createdb zikra   # or: psql -d postgres -c 'CREATE DATABASE zikra'
cp server/.env.example server/.env   # already filled for local Postgres

./server/run.sh
# equivalent: cd server && ./gradlew run
```

Health check: `GET http://localhost:8080/health` → `{"status":"ok","service":"zikra"}`.

Flyway runs on startup.

### Env (`server/.env`)

| Variable | Local default |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/zikra` |
| `DATABASE_USER` | your macOS user (`samreen`) |
| `DATABASE_PASSWORD` | empty (Homebrew peer/trust) |
| `JWT_SECRET` | change before any shared deploy; ≥ 32 random chars when `ZIKRA_ENV=production` |
| `PORT` | `8080` |
| `HOST` | `0.0.0.0` |
| `ZIKRA_ENV` | unset locally; `production` rejects the local JWT default |
| `CORS_ORIGINS` | optional allow-list; empty = any origin |
| `GOOGLE_WEB_CLIENT_ID` | unset — Google Sign-In is a stub in v1 (button stays hidden) |

Auth: `POST /v1/auth/register` and `POST /v1/auth/login` with `{ "email", "password" }` (password 8+ chars). Sync: `GET/POST /v1/sync` with `Authorization: Bearer <jwt>`. Account deletion: `DELETE /v1/account` (or `POST /v1/account/delete`) with the same Bearer token — used by **You → Delete account**. JWTs expire after 14 days; production is HTTPS only (Fly or Caddy).

## Run the Android app

You need **Android Studio** (or a command-line SDK) with **compile/target SDK 35** (34 works if you lower `compileSdk` / `targetSdk` in `android/app/build.gradle.kts`). `minSdk` is 26.

1. Install Android Studio and open `android/`.
2. Let it create `android/local.properties` with `sdk.dir=...`, or copy `android/local.properties.example`.
3. Emulator talks to the server at `http://10.0.2.2:8080` (already the default `API_BASE_URL`).
4. Physical device: set `api.base.url=http://YOUR_LAN_IP:8080` in `local.properties`.
5. Run the `app` configuration, or:

```bash
cd android
./gradlew :app:installDebug   # needs ANDROID_HOME / sdk.dir
```

Guest mode works with the server off. Sign-in shows a clear error if the API is down.

Google Sign-In: add `google.web.client.id=...` to `android/local.properties` when you have a Web client ID. Until then the button explains that it is not configured.

## Production API

See **[docs/DEPLOY.md](docs/DEPLOY.md)**. Default path is Fly.io (`fly.toml`) or a $5 VPS with Caddy + `docker-compose.prod.yml`. Point a later domain at `https://api.zikra.app` and set Android release `api.base.url` to that HTTPS URL. Daily `pg_dump` is documented there; CI does not upload Play AABs.

## Brand

Forest `#16352F` / `#0F241F` / `#1E463D`, gold `#C9A24A` / `#E4C878`, cream `#F6F1E6`, ink `#14241F`.

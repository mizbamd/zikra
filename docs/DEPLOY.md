# Deploy the Zikra API

Stack stays small: **Ktor + Postgres 14**. No Redis, Kafka, or Firebase.

The Android app is offline-first. The API is only for signed-in register / login / sync / **account deletion**.

Production **must be HTTPS**. Terminate TLS at Fly or Caddy; Ktor listens on HTTP inside the private network (`HOST=0.0.0.0`).

---

## Env

Copy [server/.env.example](../server/.env.example). In production:

| Variable | Notes |
|---|---|
| `ZIKRA_ENV` | Set to `production`. Rejects the local JWT default and secrets shorter than 32 characters |
| `JWT_SECRET` | Long random string (≥ 32 chars). Rotating it signs out every device |
| `DATABASE_URL` | `jdbc:postgresql://…` or `postgres://user:pass@host:5432/db` (Fly-style URLs are converted) |
| `DATABASE_USER` / `DATABASE_PASSWORD` | Used when the URL has no userinfo |
| `PORT` | `8080` (Fly and Compose map this) |
| `HOST` | `0.0.0.0` (already the default; required so the process is reachable in a container) |
| `CORS_ORIGINS` | Optional comma-separated origins. Empty = allow any (fine for a mobile API) |

JWT lifetime is **14 days** (`Security.TOKEN_TTL_SECONDS`). Logout is local; account **deletion** removes the user so leftover tokens cannot load data. Use HTTPS so tokens are not sent in the clear.

Do not commit `.env`, keystores, or Play secrets.

---

## Health

`GET /health` → `{"status":"ok","service":"zikra"}`.

Fly and Docker healthchecks use this path.

---

## Account deletion (API)

Authenticated:

- `DELETE /v1/account`
- `POST /v1/account/delete` (same)

Header: `Authorization: Bearer <jwt>`.

Deletes the user, frames, and daily counts. The Android **You** screen calls this, then wipes Room + DataStore and returns to welcome.

---

## Option A — Fly.io (documented default)

Cheapest reasonable path for a small JVM: Fly Postgres + one shared-cpu app. You do **not** need a Fly account to merge these files.

From the repo root, when you are ready:

```bash
# once
fly auth login
fly apps create zikra-api          # must match fly.toml app name, or edit fly.toml
fly postgres create --name zikra-db --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 1
fly postgres attach zikra-db -a zikra-api

fly secrets set ZIKRA_ENV=production JWT_SECRET="$(openssl rand -base64 48)"
# attach already sets DATABASE_URL (postgres://…). Env.parseDatabaseUrl accepts it.

fly deploy
fly scale count 1 -a zikra-api
```

`fly.toml` forces HTTPS on the public URL. Bind is `0.0.0.0:8080`. JVM RAM: `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75` in the image.

Later: `fly certs add api.zikra.app` and point DNS at Fly.

Machine size: start at **shared-cpu-1x / 512MB–1GB**. If Flyway + Hikari OOM on boot, bump memory rather than adding Redis.

---

## Option B — $5 VPS + Caddy + Docker Compose

Any small VPS (Ubuntu). Install Docker and Caddy. Put a `.env` next to [docker-compose.prod.yml](../docker-compose.prod.yml) (copy from `server/.env.example`, set `ZIKRA_ENV=production` and a long `JWT_SECRET`).

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Caddyfile example (TLS is automatic):

```
api.zikra.app {
    reverse_proxy 127.0.0.1:8080
}
```

Do not expose Postgres publicly. Compose publishes only the API port.

---

## Domain

Plan: **https://api.zikra.app** when the domain exists.

Set the Android **release** `API_BASE_URL` to that HTTPS URL via `android/local.properties`:

```
api.base.url=https://api.zikra.app
```

or the `API_BASE_URL` environment variable when assembling release. Do not ship `http://10.0.2.2:8080` in a Play build.

---

## Backups

This is not a backup product. Take a **daily `pg_dump`**.

Fly:

```bash
fly postgres connect -a zikra-db
# or from CI / a laptop:
fly ssh console -a zikra-db -C "pg_dump -U postgres postgres" > zikra-$(date +%F).sql
```

Compose / VPS:

```bash
docker compose -f docker-compose.prod.yml exec -T postgres \
  pg_dump -U "$DATABASE_USER" zikra > zikra-$(date +%F).sql
```

Keep a few days of dumps off the box (object storage or another disk). Test a restore once before you need it:

```bash
psql "$DATABASE_URL" < zikra-YYYY-MM-DD.sql
```

---

## CI

GitHub Actions compiles/tests the server on pull requests. A push to `main` prints a deploy reminder. **CI does not deploy and does not upload a Play AAB** until you add secrets on purpose.

---

## Local

Unchanged: Homebrew Postgres 14, `cp server/.env.example server/.env`, `./server/run.sh`, `GET http://localhost:8080/health`.

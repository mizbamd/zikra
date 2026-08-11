# Zikra API

Kotlin + Ktor (Netty) + PostgreSQL + Flyway.

```bash
cp .env.example .env
./run.sh
curl http://localhost:8080/health
```

## Auth and sync

| Method | Path | Auth |
|---|---|---|
| `POST` | `/v1/auth/register` | no |
| `POST` | `/v1/auth/login` | no |
| `GET` / `POST` | `/v1/sync` | Bearer JWT |
| `GET` | `/v1/me` | Bearer JWT |
| `DELETE` | `/v1/account` | Bearer JWT |
| `POST` | `/v1/account/delete` | Bearer JWT (same as DELETE) |

`DELETE /v1/account` permanently deletes the user, frames, and daily counts. The Android You screen uses this for Play Store account deletion.

JWT lifetime is 14 days. Serve the API over **HTTPS in production**.

## Production

See [docs/DEPLOY.md](../docs/DEPLOY.md): `server/Dockerfile`, `docker-compose.prod.yml`, and `fly.toml`.

See the root README for env vars and running the Android app.

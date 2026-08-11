# Contributing to Zikra

`main` is the release branch. **v0.1.0 is the baseline.** After that, do not push commits straight to `main`.

## Branch + PR

1. Branch from the latest `main`:
   - `feat/…` new behavior
   - `fix/…` bug fix
   - `chore/…` tooling, docs, deps
2. Push the branch and open a **pull request into `main`**.
3. Fill in the PR template. Merge only after you have run the app (or the API) for the items you touched.
4. Delete the branch after merge.

## Releases

Every release is a PR that lands on `main`, then a git tag:

```bash
git checkout main
git pull
git tag -a vX.Y.Z -m "Zikra vX.Y.Z"
git push origin vX.Y.Z
```

## Commit author

Use the GitHub account email so history stays attributable:

- Name: Miz Mohammad
- Email: `mizbauddin.md@gmail.com`

Do not commit `local.properties`, `server/.env`, keystores, or `google-services.json`.

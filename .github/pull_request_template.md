## Summary

<!-- What does this PR change, and why? -->

-

## Test plan

- [ ] Guest counter increments and persists after app restart
- [ ] Signed-in home frames increment independently
- [ ] Focused mode: +1, undo, reset today
- [ ] Hijri date still shows under Gregorian
- [ ] `./server/run.sh` + `GET /health` still ok (if this PR touches the API)

## Release

- [ ] This PR is intended to merge to `main`
- [ ] After merge, tag `vX.Y.Z` if this is a release

# SPEC/Kiro — WCS deterministic shared deadline on cold runners

**BASE_SHA:** `5784f7a340ba1148aa26e0ed86d35e0749d12614`
**STATUS:** CANDIDATE / NOT_CERTIFIED
**A0.7:** OPEN
**Issues:** #167 / original PR #168 (historical stale baseline)

## Objective / observed root cause
The integration test `slowChunkedResponseExpiresOneSharedDeadlineWithoutFallback`
had a 350 ms *total wall-time budget*. On a cold Windows 17 runner, that
can expire while setting up localhost HTTP before the handler has observed
the first request, making the strict `requests == 1` assertion flaky.
The underlying behavior must still enforce a *shared* time budget and
must never retry WCS 1.0 after timeout.

## Minimal design and test
Change only the deterministic fixture/test:
- Request deadline: 1500 ms (bounded; enough for cold localhost setup).
- Stream: 30 separate chunks at 100 ms each (~3000 ms), so the total
  cannot complete within one shared 1500 ms deadline.
- Keep assertions on `InterruptedIOException`, elapsed time <7s,
  exactly 1 WCS request, original target bytes unchanged, and zero owned
  staging leftovers.
- No production changes, unbounded waits, retries, or reductions in
  negative-path requirements.

## Certification gates
Exact-head CI/Certification/Runtime Identity/GeoParquet and predecessor
main-push attestation are mandatory. After merge, verify exact-main push
certification and authenticated attestation.
This test only certifies the asynchronous deadline contract under CI,
not full native WCS GUI/runtime behavior.

Consolidated and independently retested with #189/#191/#192 source and tests on current main 5784f7a340ba1148aa26e0ed86d35e0749d12614; prior green runs on old SHA are not substituted for this candidate's native gates.


## Post-LAS exact-main port
Source and contract code ported without intersecting the already merged P0 #194 file provenance fix on main `5784f7a340ba1148aa26e0ed86d35e0749d12614`. Must certify this new candidate and exact predecessor-main push; earlier PR #196 full matrix alone is insufficient.

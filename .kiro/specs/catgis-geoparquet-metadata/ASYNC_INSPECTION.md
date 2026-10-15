# SPEC/Kiro #112 - existing GeoParquet inspection off EDT

BASE: 3d53ef5d741e0461ad239fc346b922b6ab26fe8f (normal merge of PR #118).
STATUS: IMPLEMENTED_CANDIDATE / NOT_CERTIFIED. A0.7 and full #112 stay OPEN.
Requirements: REQ-PC-2, REQ-PC-5, REQ-PC-6, REQ-PC-7, REQ-PC-8, REQ-PC-11.

## Evidence, cause and scope

AddLayerDialog.onAccept calls OpenFileAction.openSelectedFiles on the Swing
EDT. The GeoParquet branch called readMetadata and then getSummary (which
reads metadata again), and unconditionally displayed a result/error even
when showDialogs=false. No layer is actually imported by this branch.

The base OpenFileAction source blob is
34424ec6dfc6fc4aba2cbfd29e69794cf3114216. Its bytes were reconstructed from
exact-ref connector reads and the Git blob hash matched BEFORE the single
branch-body change. No unrelated format or project behavior is changed.

## Minimal design and acceptance

- Replace only the GeoParquet branch with GeoParquetInspection.open. Reuse the
  certified metadata reader unchanged; one getSummary call per inspection.
- GeoParquetInspectionTask is a FutureTask with explicit INSPECTED, FAILED or
  CANCELLED outcomes. All completion delivery is queued onto EDT, including
  cancellation before execution. Reject accidental reader execution on EDT.
  A missing/blank summary is not success. Preserve actual exception causes.
- Execute in an explicit ThreadPoolExecutor: maximum TWO physical worker
  threads, SynchronousQueue (zero pending queue), AbortPolicy and idle daemon
  retirement. Do not equate Future cancellation with physical I/O termination.
  An uncooperative cancelled reader continues occupying its actual worker.
- Existing interactive open remains per-file and ordered. A small standard
  modal progress dialog runs its secondary event loop; no unfinished future
  is synchronously waited for on EDT. Painting and Cancel remain dispatchable.
  This is not a modeless whole-application job dashboard.
- Cancel, ESC/window closure and owner disposal dismiss the presentation and
  request interruption. Invalidate presentation even if Future.cancel loses a
  race with successful computation whose completion is still queued. Do not
  display late success after dismissal. Remove owner listeners and stop timers.
- A 30-second Swing timer requests COOPERATIVE cancellation, tells the user
  that no layer loaded, and dismisses the progress dialog. This is NOT a hard
  deadline or forced kill of arbitrary filesystem/provider code. Busy errors
  do not promise that cancelling an uncooperative read frees a physical slot.
- Non-EDT interactive callers marshal presentation to EDT. Caller interruption
  preserves the interrupt flag and dismisses/cancels the request.
- showDialogs=false and headless UI requests return false WITHOUT file reads
  or dialogs: there is no metadata consumer and no layer to import in that
  entry point. Direct GeoParquetReader APIs still perform metadata inspection.
  Existing drag/drop aggregation continues to report that these layers were
  not loaded; no metadata success is counted as a loaded layer.
- Read-only: no project creation/dirty state/layer mutation, output files,
  extra native library, dependency, tool menu, workflow or gate changes.
  Progress/Cancel are subordinate controls of the existing GeoParquet open
  flow, not a new GIS feature or a full-ingestion claim.

Primary contracts reviewed (no copied implementation):
- https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/FutureTask.html
- https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Dialog.html
- https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/SwingUtilities.html

## Failure-driven design review

An unpublished SwingWorker variant failed a supplementary pre-start
cancellation contract on the isolated JVM21: cancellation prevented execution
but did not deliver the expected done callback. Rather than use a runtime
version workaround or weaken the test, the candidate uses FutureTask's
terminal-state done contract and explicit EDT delivery. Completion is never
read before the future becomes terminal. This observation is NOT evidence
that every JDK17/OS SwingWorker behaves identically.

The revised exact task source passed 47 supplementary JDK-only checks using
javac --release 17 and OpenJDK 21.0.12.1 in a headless isolated container.
They inspect real worker/EDT execution, one-read behavior, errors, empty
results, pre-start cancellation, direct-EDT rejection, uncooperative late
completion, physical two-worker saturation and zero queue. These checks are
supporting evidence only, not full CATGIS/JDK17/multiplatform/GUI certification.

## Tests and mandatory verification

15 NEW ordinary JUnit methods, no existing test removed/changed/disabled:
- GeoParquetInspectionTaskContractTest (11): genuine Swing EDT/worker tests,
  real empty Parquet and corrupted file integrity/handle closure, pre-start and
  mid-read cancellation, discarded late result, physical saturation despite
  cancelled futures, exception identity, blank results and EDT misuse refusal.
- GeoParquetOpenFlowContractTest (4): actual silent open entry point with a
  file-I/O tripwire, real valid/corrupt files with unchanged project identity,
  headless presentation absence and the exact delegation boundary. Static
  branch inspection supports integration; it is not displayed GUI evidence.

Require exact-head CI 4/4, Certification 15/15, independent GeoParquet interop,
prior-main push attestation, normal expected-head merge and NEW main push
CI/Certification/Interop plus independent post-main attestation. Do not
certify from supplementary checks or inherited prior-candidate results.

## Explicit remaining work

No actual GUI shown here: visual layout, HiDPI/focus, native look and feel,
physical Cancel/ESC/owner-closing gestures and long sessions remain NOT_VERIFIED.
The current cancellation is for ONE inspection, not the entire multi-file
batch; other formats retain their existing threading and notification paths.
No partial batch rollback or generalized import coordinator is introduced.

GeoParquet row-page/WKB/typed-attribute ingestion, streaming, full PROJJSON
validation and CRS transformation remain separate full-#112 acceptance. The
metadata decoder is unchanged and retains its stated bounds/limitations.
No user-PC, OpenCode, C:\catgis, release or historical Git objects are changed.

# SPEC/Kiro #141 - atomic CATGIS project document save

ORIGIN_BASE: e8aba016621ae0265ce090f3a3e5d6f5d37f9167
CURRENT_BASE_AFTER_MAIN_SYNC: f7e0a43155a92f9d2a0062ffe6e8c6749a1a1237
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## User data-loss failure

SaveProjectAction opened the final .catgis through FileWriter. Existing content was truncated before serialization completed. A write/disk/process failure could therefore destroy the last valid project even though normal roundtrip tests passed.

## Design

ProjectFilePublisher owns a unique staging file in the destination filesystem. It rejects missing/non-directory parents and symlink/nonregular final targets, executes the existing serializer against staging, requires a bounded nonempty file with CATGIS_PROJECT header, forces staged bytes, rechecks the target and publishes only with ATOMIC_MOVE + REPLACE_EXISTING. AtomicMoveNotSupported is terminal and preserves the prior destination. Owned staging is always deleted.

SaveProjectAction changes projectFile/name, marks clean, remembers chooser paths and emits success only after publish returns. The existing project serializer format is unchanged.

## Tests

ProjectFilePublisherContractTest uses real files and bytes:
- successful create/replace;
- partial writer exception after writing CATGIS_PROJECT/PARTIAL;
- injected AtomicMoveNotSupportedException;
- invalid staged header;
- size limit before publish;
- directory target;
- missing parent;
- byte-identical old project and zero owned stages on every rejected replacement.

Existing CoreProjectPersistenceContractTest remains and exercises the application roundtrip/failure state.

## Residual

persistVectorLayers executes before project-document serialization. This tranche prevents .catgis truncation but is NOT a cross-file transaction over editable vector sources, auto-generated shapefiles or their sidecars. R1.1 remains open until recovery/transaction semantics for the full save workflow are decided and tested. Directory fsync semantics are platform-dependent and are not claimed by FileChannel.force on the staged file.

Require exact-head CI/Certification/RuntimeIdentity and predecessor-main proof, normal expected-head merge, then fresh-main proof. No PC/OpenCode/release actions.

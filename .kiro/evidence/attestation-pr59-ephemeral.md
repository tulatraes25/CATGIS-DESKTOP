# CATGIS — exact post-PR59 main push attestation (temporary, do not merge)

This PR is a **read-only certification probe**. Its document and branch MUST
NOT be merged to main; close after recording proof.

Expected protected-main SHA: `08aa5d5ad5e1f3aa53d51fd5ceb89c3ce000c033`
Merged candidate PR: #59
Certified PR head: `903704b41fae0f1820c94552a9668c775b367536`
Required: authenticated GitHub Actions `event=push` CATGIS CI and CATGIS
Certification SUCCESS on **that exact main SHA**, including Linux Full Suite,
Windows Full Suite, real PostGIS, architecture guardrails and inventories.

Do not confuse PR-triggered runs with push-triggered runs or treat queued
workflows as success. Only the genuine Main Push Attestation job result
establishes the proof. No Windows/Linux GUI or installer certification implied.

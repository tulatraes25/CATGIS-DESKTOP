# Ephemeral CATGIS exact-main push certification request

DO NOT MERGE THIS FILE. This PR exists solely to trigger the repository's
authenticated CATGIS Main Push Attestation workflow without weakening CI gates.

Expected protected-main SHA: `9408d52d49b430b4fe19d6d86a103cc2ecfa8fc7`
Origin: PR #53, candidate SHA `580b92e9725361bf2f9331f97e86a7b99b7a83f3`.

Require successful push-triggered CI and Certification for this exact main
commit and all required Linux/Windows/PostGIS/architecture jobs, not prior PR
or pre-merge statuses. Record run IDs and proof on PR #51 or issue #50. Close
this request without merging; it is not an application change.

# Ephemeral CATGIS exact-main push certification request

DO NOT MERGE THIS FILE. This PR exists solely to trigger the repository's
authenticated CATGIS Main Push Attestation workflow without weakening CI gates.

Expected protected-main SHA: `47b4b890dbb313bc33c61ebf94fb793e17d4bc8a`
Origin: PR #54, candidate SHA `0842e4b220bf528e1083157521b898971f81f2d4`.

Require successful push-triggered CI and Certification for this exact main
commit and all required Linux/Windows/PostGIS/architecture jobs, not prior PR
or pre-merge statuses. Record run IDs and proof on PR #51 or issue #50. Close
this request without merging; it is not an application change.

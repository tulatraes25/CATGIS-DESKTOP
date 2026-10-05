# Security policy

CATGIS-DESKTOP is a public repository. Secrets and machine-specific credentials must never be committed.

## Never commit

- Database passwords or connection files containing passwords.
- API keys, access tokens, cookies, session material, or private credentials.
- `.env` files with real values.
- `config/local*.json` files with real values.
- Private keys or signing material (`.pfx`, `.p12`, `.key`, `.jks`, `.keystore`).
- PostgreSQL password files (`.pgpass`) or database dumps containing sensitive data.
- Private municipal datasets unless they have been explicitly approved for publication.

Use environment variables or local ignored configuration files. Commit only templates with empty/example values.

## CATSERVER connection configuration

For local CATSERVER deployments, create credentials outside Git and provide them through environment variables or ignored local configuration. The public template is:

`CATSERVER/docs/07_datos_conexion_catserver.example.txt`

## Credential exposure response

If a credential is ever committed to this repository:

1. Treat it as compromised immediately.
2. Rotate/revoke it at the source.
3. Remove it from the current branch.
4. Audit other files and branches for copies.
5. Rewrite Git history only after important divergent/local work has been preserved.
6. Re-clone or rebase local working copies after the history rewrite.

## 2026-10 repository sanitation

A repository audit identified a CATSERVER connection document containing database passwords in the public Git tree. The credential values are intentionally not reproduced here. The affected database roles must be considered compromised even if the previously stored credentials no longer authenticate.

The first sanitation phase removes the file from the active tree and blocks it from being re-added. Historical Git cleanup is handled separately so that divergent local CATGIS work can be preserved before any destructive history rewrite.


## Credential rotation closure

History rewriting does not make exposed credentials safe. Until rotation is certified, treat any operational CATSERVER PostgreSQL login that used, reused, inherited, or may have shared a password with the historically exposed connection document as compromised.

Do not recover the old credential values merely to compare them. Instead, inventory the live deployment and rotate credentials at the source.

Minimum rotation review for the current CATSERVER architecture:

- PostgreSQL bootstrap/superuser credential used to administer the CATSERVER database, if it may have appeared in or been reused from exposed material;
- CATSERVER administrative login(s), including any deployed equivalent of `catserver_admin_user`;
- ETL/import login(s), including any deployed equivalent of `catserver_etl_user`;
- read/viewer login(s), including any deployed equivalent of `catserver_read_user` or `catserver_viewer_user`;
- any additional login returned by the live PostgreSQL role inventory that is used by CATGIS/CATSERVER and cannot be proven out-of-scope;
- `CATSERVER_WEB_API_KEY` only if it existed during the exposure window, was reused with an exposed secret, or its scope is uncertain.

Rotation evidence must not include the old or new secret values. Record only role/service identifiers, timestamps, success/failure, dependent configuration updated, and proof that the old credential is no longer accepted when that can be tested safely.

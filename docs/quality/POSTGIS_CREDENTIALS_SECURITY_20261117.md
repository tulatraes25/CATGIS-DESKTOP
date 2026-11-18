# Credenciales PostGIS — almacén local (2026-11-17)

Issue #261 (audit #263). Estado: **implementado y certificado**.

## Problema

- La clave AES-GCM se derivaba de datos públicos/predecibles
  (`user.name|os.arch|MachineGuid|salt fija`) y el `MachineGuid` se obtenía con
  un subproceso PowerShell (fallo → passphrase distinta).
- `deobfuscate` lanzaba `RuntimeException` ante cualquier ciphertext inválido, de
  modo que el fallback legacy no podía ejecutarse correctamente y una credencial
  corrupta rompía la carga.
- El XOR legacy no tenía integridad: basura Base64 se interpretaba como clave y
  podía migrarse como contraseña corrupta.
- `Preferences` rechaza claves > 80 caracteres; fingerprints largos fallaban.

## Solución

- **`PostgisCredentialVault` v2**:
  - Secreto de instalación **aleatorio de 32 bytes** en `~/.catgis/postgis.key`
    (override `-Dcatgis.postgis.keyfile`), escritura atómica y permisos
    `rw-------` en POSIX (best effort en Windows).
  - AES-256-GCM con sal aleatoria por registro (PBKDF2-HMAC-SHA256 120k) y AAD
    `CATGIS_POSTGIS_V2`; prefijo `v2:`.
- **Migración legacy fail-closed**: v1 (PBKDF2 viejo) y XOR se intentan sólo
  para migrar; errores, ciphertext alterado o texto no imprimible devuelven `""`
  (se pide la clave de nuevo) en lugar de lanzar o aceptar basura.
- **Claves Preferences largas** se almacenan como `password.h.<sha256>` (las
  claves cortas existentes siguen funcionando).

## Evidencia

`PostgisCredentialsVaultContractTest` (7):

1. Roundtrip v2 con secret instalado de 32 bytes.
2. Ciphertext alterado/ajeno → falla cerrado.
3. Otro key file no puede descifrar credenciales previas.
4. Migración XOR real y rechazo de basura.
5. Ciphertexts distintos para el mismo texto (salt/IV).
6. Roundtrip por Preferences y borrado.
7. Cifrado nunca contiene el texto plano; entrada inválida rechazada.

Suite local: 1661 tests, 0 fallos, 15 skips.

## Límites

- En Windows la protección de la clave depende de ACL de usuario (no DPAPI); el
  requisito de "OS-backed" completo queda como mejora futura (DPAPI/Keychain/
  Secret Service).
- La eliminación segura de la clave del disco no está certificada por hardware.

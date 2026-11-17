# Firma y notarización de la beta (opcional)

La beta se publica **sin firma por defecto**. El workflow
`.github/workflows/release-beta.yml` firma/notariza solo si el repositorio tiene
configurados los secretos correspondientes. Sin ellos imprime
`UNSIGNED_BETA: ...` y publica el artefacto sin firma (comportamiento actual).

## Windows (Authenticode)

Secretos:

- `WINDOWS_CERT_PFX_BASE64`: certificado `.pfx` codificado en base64.
- `WINDOWS_CERT_PASSWORD`: password del `.pfx`.

El workflow importa el PFX en `%RUNNER_TEMP%` y firma cada `.exe` del app-image
con `signtool` (`/fd sha256 /td sha256 /tr http://timestamp.digicert.com`).
No se sube el certificado ni el password a artefactos.

## macOS (codesign + notarytool)

Secretos:

- `MACOS_SIGNING_CERTIFICATE_BASE64`: certificado Developer ID `.p12` en base64.
- `MACOS_SIGNING_PASSWORD`: password del `.p12`.
- `MACOS_SIGNING_IDENTITY`: identidad de firma (p. ej.
  `Developer ID Application: Nombre (TEAMID)`).
- `MACOS_NOTARY_APPLE_ID`, `MACOS_NOTARY_TEAM_ID`, `MACOS_NOTARY_PASSWORD`:
  credenciales de notarización (app-specific password).

El workflow importa el certificado a un keychain temporal, firma el `.app` con
`codesign --deep --options runtime --timestamp`, y si están los secretos de
notarización ejecuta `notarytool submit --wait` + `stapler staple`.

## Linux

Los artefactos Linux son tar.gz sin firma. Para una distribución formal se
recomienda publicar checksums SHA-256 (el workflow los genera siempre) y, si se
requiere, firmar el checksum con GPG fuera del pipeline o agregar
`GPG_SIGNING_KEY_BASE64` en una tranche futura.

## Checksums

El workflow genera `<archivo>.sha256` para cada artefacto y los publica junto a
la pre-release. Verificación:

```bash
sha256sum -c catgis-catmap-beta-linux.tar.gz.sha256
```

## Límites

- La firma/notarización **no** se ha ejecutado en esta sesión (no hay
  certificados configurados): los artefactos `v1.0.0-beta.1` son sin firma.
- A0.5/A0.7/F3 siguen `NOT_CERTIFIED`; la firma no certifica GUI ni runtime.

---
name: catgis-engineering
description: "Dirigir, retomar, implementar, investigar, auditar y certificar CATGIS Desktop, CATMAP, CATSERVER y su visor web en tulatraes25/CATGIS-DESKTOP. Usar cuando el usuario pida seguir CATGIS, continuar SPEC/Kiro, arreglar bugs, revisar arquitectura, compatibilidad Windows/Linux/macOS, mejorar GIS/GDAL/PostGIS, limpiar GitHub, preparar OpenCode, validar una release o decidir sobre borrado local. Exige GitHub-first, SHAs y PR exactos, certificación multiplataforma, pruebas científicas y seguridad de datos; coordina OpenCode con DeepSeek Flash sin modificar la fuente GitHub certificada."
---

# CATGIS Engineering — SPEC/Kiro y GitHub-first

## Modelo operativo
- **Repositorio canónico:** tulatraes25/CATGIS-DESKTOP, rama main. Fuente publicada con licencia propietaria/source-available; no reutilizar material tercero sin permiso y procedencia comprobada.
- **ChatGPT:** director técnico, investigador, implementador GitHub-first, auditor de causa raíz y puerta de certificación. Actuar feedback-first, proponiendo alternativas mejores cuando tengan evidencia.
- **GitHub:** verdad de código, SHA, PR, CI, merge y releases; la memoria y los informes históricos no sustituyen el repositorio real.
- **OpenCode + DeepSeek Flash:** sincronizar SHAs exactos certificados; hacer exclusivamente verificaciones Windows/GUI/locales habilitadas. No reconstruir ni reinterpretar código mediante prompts.
- **Usuario:** priorizar el producto; minimizarle pasos manuales.

## Al comenzar toda tarea
1. Leer GitHub en vivo: main HEAD, PR abiertos, issues, branches, workflows y estados exactos. Si falta acceso, declarar GITHUB_CONNECTOR_BLOCKED; no inventar trabajo.
2. Leer fuente y pruebas del SHA real. Consultar README.md, SECURITY.md y .kiro/specs/catgis-product-certification/{requirements,design,tasks}.md cuando apliquen. La documentación de avance puede estar desactualizada.
3. Precisar el último CLOSED_CERTIFIED, los pendientes P0–P3/A0.7 y una puerta atómica. Revalidar SHAs, no reciclar los de conversaciones pasadas.
4. Mantener congelamiento de nuevas funcionalidades y pruebas locales prematuras hasta cerrar los gates de arquitectura.

## Flujo SPEC/Kiro
Para cada gate: **objetivo → evidencia → causa raíz → requisitos/no-objetivos → riesgo → diseño mínimo → pruebas y criterios de aceptación → implementación → verificación exacta → decisión**.

- Diferenciar bug confirmado, hipótesis, bloqueo de infraestructura, dependencia externa y deuda real.
- Probar éxito y fallos: timeout, cancelación, rollback, estado previo intacto, concurrencia, paths con espacios, CRS/NoData, EDT y native runtime.
- Priorizar pruebas científicas diminutas y reales en CI frente a mocks que solo comprueban argumentos.
- No fabricar rondas, porcentajes ni PASS. Ante un hallazgo que aleje de la meta, investigar más la causa y corregirla.
- "Continuar", "seguir" o "en loop" = ejecutar autónomamente las siguientes tareas verificables del turno, sin prometer trabajo asíncrono.

## Cambio GitHub-first y certificación
1. Verificar la SHA actual de main y PR conflictivos. Crear rama corta desde ese commit. Jamás force-push ni modificar main directamente.
2. Implementar por conector GitHub un cambio atómico, sus pruebas y SPEC. Revisar diffs y dependencias. No filtrar secretos, datos municipales, backups privados ni binarios pesados.
3. Abrir PR draft hasta obtener CI y Certification exactos del PR head y atestación del push de main previo. Ver references/github-certification.md.
4. Si una atestación falla mientras la certificación de main aún corre, contrastar logs y repetir **sobre el mismo SHA** después; si falla por código, arreglar causa raíz.
5. Solo merge normal con expected_head_sha tras todos los gates. Verificar nuevo main SHA y nuevas ejecuciones push CI/Certification y atestación post-merge. El PR verde no certifica el merge.
6. Mantener issues abiertos si queda alguna aceptación sin evidencia; no declarar A0.7 cerrada por un paquete construido.

## Calidad técnica geoespacial
- CATGIS Desktop: Java 17 + Gradle + Swing + GeoTools/JTS + GDAL; CATMAP, PostgreSQL/PostGIS (CATSERVER) y visor web con matrices independientes.
- Plataformas: Windows, Linux, Mac Intel x86_64, Mac Apple Silicon arm64. Compilación, test suite, app-image, CLI nativo, JNI y GUI son capas de evidencia distintas.
- GIS: datos raster/vector reales; EPSG/orden de ejes, NoData, muestras/píxeles, bandas, georreferencia y BBOX, WMS/WFS/WCS y XYZ norte frente a TMS sur.
- Fiabilidad: recursos limitados, salida atómica, limpieza staging y recuperación, LRU RAM por bytes, límites HTTP, procesos argv sin shell y trabajos pesados fuera de Swing EDT.
- Nunca confundir ejecutable encontrado con GDAL funcional, salida 0 con geografía válida, .app generada con GUI probada, skipped con PASS.
- Ver references/gis-quality.md para pruebas y evidencia física de cada sistema operativo.

## Frontera con PC y OpenCode
- Si el usuario ordena acabar GitHub primero: NO ejecutar pruebas GUI locales, ni modificar ni borrar la carpeta C:\catgis.
- Al habilitar local, dar un solo prompt OpenCode con repositorio, branch/SHA exactos, fetch/checkout aislado, comprobación de git rev-parse HEAD y tests de escritorio específicos. Ver references/opencode-handoff.md.
- Para limpiar PC primero inventariar cambios no subidos, ignored/untracked, worktrees, GIS personales, bases, credenciales, exports y backups; obtener aprobación explícita de paths destructivos después del respaldo.
- Para cuota GitHub distinguir Git history, Actions artifacts/cache, logs, LFS y cuota de cuenta; no borrar nada irreproducible.

## Informe por turno
En español: hecho real con PR/issue/SHA; pruebas PASS/FAIL/PENDING verificadas; riesgo residual; A0.7; siguiente gate prioritario. Si algo no está probado, marcar NOT_VERIFIED y no sobreprometer.

## Referencias condicionadas
- references/github-certification.md — antes de abrir, fusionar o atestar un PR.
- references/gis-quality.md — ante GIS/GDAL/CRS/PostGIS/EDT/packaging.
- references/opencode-handoff.md — antes de pedir ejecución a OpenCode o limpiar PC.

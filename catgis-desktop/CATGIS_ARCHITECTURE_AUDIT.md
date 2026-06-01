# CATGIS Desktop — Auditoría de arquitectura (profunda)

Fecha: 2026-05-31  
Repo auditado: `C:\CATGIS\catgis-desktop`  
Commit observado: `ba849ad`  
Alcance: arquitectura general, estabilidad, mantenibilidad, ruta de evolución, decisión plataforma.

---

## 1) Resumen ejecutivo

CATGIS Desktop hoy es un **monolito funcional** que logra entregar valor real (carga GIS, edición, PostGIS/CATSERVER, topografía, CATMAP, exportaciones), pero con deuda estructural importante.

Diagnóstico corto:
- **Estabilidad operativa**: media-alta (compila y tests actuales pasan).
- **Estabilidad arquitectónica**: media-baja (acoplamiento global alto, clases gigantes, poca modularidad real).
- **Escalabilidad de producto**: limitada si se sigue agregando features sin reestructuración.
- **Riesgo principal**: complejidad creciente + regresiones cruzadas.

Decisión recomendada:
- **NO migrar de plataforma ahora.**
- **Sí reestructurar fuerte en Java 17**, con plan incremental.
- Migración completa (C++/Qt, .NET, web/Electron) hoy sería más riesgo/costo que beneficio.

---

## 2) Evidencia objetiva de la auditoría

### 2.1 Métricas del código
- Clases Java en `src/ar/com/catgis`: **249**
- Tests Java en `src/test/java`: **25**
- Cobertura JaCoCo total: **12% instrucciones / 10% ramas**
- Cobertura paquete `ar.com.catgis.layout`: **0%**

### 2.2 Clases de alto riesgo (tamaño)
- `MapPanel.java` ~ **452 KB** (clase Dios de interacción/render/edición)
- `MapLayoutComposerDialog.java` ~ **436 KB** (CATMAP muy concentrado)
- `LayersPanel.java` ~ **137 KB**
- múltiples servicios/dialogs > 40-90 KB

### 2.3 Acoplamiento global
- Referencias a `CatgisDesktopApp.` en el código: **1380**
- `CatgisDesktopApp` mantiene estado global estático:
  - `public static MapPanel mapPanel;`
  - `public static LayersPanel layersPanel;`
  - `public static Project currentProject;`
  - etc.

Esto crea dependencia transversal UI↔lógica↔datos, y dificulta pruebas/mantenimiento.

### 2.4 Estructura de paquetes
- Casi todo en `package ar.com.catgis`
- Solo `layout` separado como subpaquete.
- Falta separación clara por capas (dominio/aplicación/infra/ui).

### 2.5 Build y runtime
- Java toolchain 17 (correcto y vigente).
- Stack principal: Swing + GeoTools + PostGIS + PDFBox.
- `compileJava` y `test` ejecutan OK en esta auditoría.

---

## 3) Estado de estabilidad real

### Fortalezas
1. Stack Java+GeoTools maduro para GIS de escritorio.
2. Persistencia de proyecto propia (`.catgis`) funcional.
3. Capacidades GIS relevantes ya integradas (vector/raster/PostGIS/CATMAP).
4. Base multiplataforma potencial (aunque hoy foco Windows).

### Debilidades
1. **Acoplamiento estático extremo** (`CatgisDesktopApp` como service-locator global).
2. **Clases demasiado grandes** (MapPanel/CATMAP concentran demasiadas responsabilidades).
3. **Cobertura de tests baja** en general y nula en layout.
4. **Arquitectura híbrida** en CATMAP (modelo + ramas legacy) que complica UX y export.
5. Falta un contrato modular explícito para evolución segura.

---

## 4) ¿Seguir en Java o migrar?

## Recomendación: seguir en Java 17 (con refactor arquitectónico fuerte)

Por qué:
1. Ya existe un producto funcional grande; migrar ahora destruiría velocidad.
2. GeoTools/JTS/eco Java GIS es sólido para escritorio técnico.
3. El principal problema es arquitectura interna, no el lenguaje.
4. Costo de reescritura completa es **enorme** y de alto riesgo operativo.

### Evaluación de alternativas

#### A) Java 17 + refactor modular (recomendado)
- Riesgo: medio
- Costo: medio-alto
- Time-to-value: alto (mejora desde semana 1)
- Impacto: alto en estabilidad futura

#### B) JavaFX/Compose Desktop manteniendo core Java
- Riesgo: medio-alto
- Costo: alto
- Beneficio: UX moderna, pero no soluciona por sí sola acoplamiento
- Recomendable solo después de estabilizar core

#### C) Reescritura C++/Qt o .NET/WPF
- Riesgo: muy alto
- Costo: enorme
- Time-to-value: bajo en corto plazo
- No recomendable ahora

#### D) Migrar a web/Electron
- Riesgo: muy alto para GIS desktop pesado
- Costo: enorme
- Penalidad en performance/UX offline para ciertos flujos
- No recomendable como reemplazo total de CATGIS Desktop en esta etapa

---

## 5) Qué reestructurar (sin romper el producto)

## Objetivo: Monolito modular

### 5.1 Capa núcleo (domain/core)
- `Project`, `Layer`, `LayerGroup`, entidades cartográficas.
- Sin dependencias Swing.

### 5.2 Capa aplicación (use-cases)
- Casos de uso: abrir/guardar proyecto, importar/exportar, operaciones raster/vector, CATMAP commands.
- Contratos (interfaces) para servicios.

### 5.3 Capa infraestructura (adapters)
- GeoTools adapters, PostGIS adapters, archivos, network caches.

### 5.4 Capa UI desktop
- Swing dialogs/panels/toolbars.
- Sin lógica pesada incrustada.

---

## 6) Deuda crítica a resolver primero

1. Reemplazar estado global estático por `AppContext` inyectado.
2. Partir `MapPanel` en:
   - Render engine
   - Interaction controller
   - Editing controller
   - Selection/identify controller
3. Partir `MapLayoutComposerDialog` en:
   - LayoutController
   - LayoutRenderer
   - Panels (properties/items/toolbars)
4. Unificar CATMAP a “single source of truth” (todo en `LayoutModel`).
5. Crear pruebas de regresión visual de layout (golden images).

---

## 7) Roadmap recomendado (12 meses)

### Fase 0 (2-4 semanas) — Estabilización base
- Freeze de features nuevas grandes.
- Checklist de regresión crítica por módulo.
- Telemetría de errores locales y logs consistentes.

### Fase 1 (1-2 meses) — Desacople mínimo viable
- Introducir `AppContext` y servicios no estáticos.
- Reducir dependencias directas a `CatgisDesktopApp.*`.
- Mover casos de uso fuera de diálogos.

### Fase 2 (2-3 meses) — CATMAP robusto
- Unificar arquitectura de layout.
- Export preview/PDF/print 1:1.
- Propiedades contextuales completas.

### Fase 3 (2-3 meses) — Núcleo GIS y edición
- Refactor MapPanel en módulos.
- Undo/redo coherente y command pattern extendido.
- Mejorar pruebas de vector/raster/CRS.

### Fase 4 (2-4 meses) — Profesionalización
- Cobertura > 35% global y > 60% en núcleo/cartografía.
- Contratos plugin/módulos más formales.
- Hardening de release pipeline.

---

## 8) ¿Puede competir fuerte con rivales?

Sí, pero no con “más funciones sueltas”.  
Compite si logra estas 4 condiciones:
1. Arquitectura estable (menos regresiones).
2. CATMAP realmente profesional y consistente.
3. Edición/vector/postgis confiables en flujo diario.
4. QA automatizado fuerte (test funcional + visual).

Hoy:
- Compite en casos básicos/intermedios.
- No compite todavía en robustez editorial/profesional contra QGIS/ArcGIS.

---

## 9) Conclusión ejecutiva

CATGIS no necesita cambio de lenguaje para crecer fuerte.  
Necesita **disciplina arquitectónica**, modularización y QA serio.

Decisión recomendada:
- **Seguir en Java 17**.
- **No migrar de plataforma ahora**.
- Ejecutar reestructuración incremental con foco en desacople, CATMAP y pruebas.

Esfuerzo estimado:
- Reestructuración saludable: **grande**.
- Reescritura total de plataforma: **enorme** (no recomendable en esta etapa).


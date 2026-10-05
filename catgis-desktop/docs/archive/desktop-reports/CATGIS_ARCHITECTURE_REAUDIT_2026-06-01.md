# CATGIS Desktop - Reauditoria de arquitectura

Fecha: 2026-06-01  
Repositorio: `C:\CATGIS\catgis-desktop`  
HEAD auditado: `a21b7b3`  
Objetivo: verificar si la reestructuracion posterior a la auditoria inicial mejoro la estabilidad arquitectonica real.

## 1. Resultado ejecutivo

La arquitectura **mejoro de forma real**, especialmente en CATMAP/layout y cobertura de pruebas. No obstante, CATGIS todavia no esta plenamente reestructurado: sigue siendo un monolito con fuerte dependencia de estado global, clases gigantes y una migracion incompleta hacia servicios y modelo unificado.

Dictamen:
- Estado anterior: monolito funcional con deuda alta.
- Estado actual: monolito funcional con avances parciales serios.
- Recomendacion: seguir en Java 17 y continuar refactor incremental.
- No conviene migrar de plataforma en este punto.

## 2. Cambios positivos detectados

### CATMAP / Layout

Se agregaron o consolidaron clases especificas:
- `LayoutExportContext`
- `LayoutCartouche`
- `LayoutEllipse`
- `LayoutLine`
- `LayoutGraticule`

Tambien hay tests dedicados:
- `LayoutModelTest`
- `LayoutMapTest`
- `LayoutLegendTest`
- `LayoutElementTests`
- `LayoutTemplateManagerTest` (actualmente sin trackear en git)

Impacto:
- CATMAP ya no esta completamente sin cobertura.
- Hay mas tipos reales de `LayoutElement`.
- Hay mejores bases para leyenda, mapa, grilla, escala y elementos graficos.

### Tests / Cobertura

Antes:
- Cobertura global aproximada: 12%
- Cobertura `ar.com.catgis.layout`: 0%

Ahora:
- Cobertura global: 14%
- Cobertura `ar.com.catgis.layout`: 31%
- Tests Java detectados: 32

Esto es una mejora real, especialmente porque la zona mas problematica era CATMAP.

### Build

Validaciones ejecutadas:
- `gradlew compileJava`: exitoso.
- `gradlew test`: exitoso tras limpiar bloqueo de archivo generado.
- `gradlew build -x checkstyleMain -x checkstyleTest`: exitoso.

## 3. Lo que sigue mal o incompleto

### Acoplamiento global

Se agrego `AppContext`, pero el acoplamiento global no bajo todavia.

Medicion actual:
- Referencias a `CatgisDesktopApp.`: 1385

Esto significa que `AppContext` existe, pero todavia funciona mas como puente que como reemplazo arquitectonico real.

### Estado global legado

`CatgisDesktopApp` conserva:
- `public static MapPanel mapPanel`
- `public static LayersPanel layersPanel`
- `public static StatusBar statusBar`
- `public static Project currentProject`

Riesgo:
- Las regresiones cruzadas siguen siendo probables.
- Testear componentes aislados sigue siendo dificil.

### Clases gigantes

Las clases criticas siguen siendo muy grandes:
- `MapPanel.java`: ~452 KB
- `MapLayoutComposerDialog.java`: ~444 KB
- `LayersPanel.java`: ~137 KB

Esto muestra que hubo avances en layout, pero no una particion profunda real de `MapPanel` ni de `MapLayoutComposerDialog`.

### Serializacion de proyecto

Existen:
- `ProjectSerializer`
- `ProjectDeserializer`

Pero `SaveProjectAction` y `LoadProjectAction` siguen manteniendo su propia logica antigua de escritura/lectura.

Riesgo:
- Hay dos caminos de serializacion en paralelo.
- Los tests nuevos pueden validar el serializer nuevo sin que el flujo real de la aplicacion lo use.

### CATMAP no esta 100% single source of truth

En `MapLayoutComposerDialog`, el render de `LayoutMap` se sigue salteando en overlay cuando existe template:
- `if (el instanceof LayoutMap && templateHasMap) continue;`

Conclusion:
- CATMAP mejoro, pero todavia no esta completamente unificado.
- El mapa principal sigue dependiendo de una ruta especial de template/render legacy.

## 4. Estado git

Hay archivos nuevos sin trackear:
- `src/test/java/ar/com/catgis/PointSymbolCatalogTest.java`
- `src/test/java/ar/com/catgis/layout/LayoutTemplateManagerTest.java`

Riesgo:
- Si se genera build o release sin revisar/stagear esos tests, la auditoria queda parcialmente fuera de control de version.

## 5. Veredicto por area

| Area | Estado actual | Veredicto |
|---|---|---|
| Java 17 / build | Bueno | Mantener |
| CATMAP layout model | Mejoro | Seguir refactor |
| CATMAP export/preview | Mejoro parcialmente | Validar visualmente |
| Tests layout | Mejoro fuerte | Ampliar |
| AppContext | Parcial | No alcanza todavia |
| MapPanel | Sin mejora estructural visible | Refactor pendiente |
| Persistencia proyecto | Parcial / duplicada | Unificar urgente |
| Arquitectura modular | Inicial | Falta mucho |

## 6. Recomendacion actualizada

No migrar de plataforma.

Seguir con Java 17, pero exigir la siguiente fase:

1. Integrar realmente `ProjectSerializer/ProjectDeserializer` en `SaveProjectAction/LoadProjectAction`.
2. Reducir referencias directas a `CatgisDesktopApp.*` por zonas, empezando por CATMAP, PostGIS/CATSERVER y acciones de proyecto.
3. Extraer controladores de `MapPanel` en unidades testables.
4. Eliminar la excepcion de `LayoutMap` en overlay y convertir el mapa principal en elemento real persistente.
5. Subir cobertura de `layout` a 60% y global al menos a 25% antes de seguir agregando features grandes.

## 7. Conclusion

La reestructuracion posterior a la auditoria **si produjo mejoras reales**, especialmente en CATMAP y tests. Pero no debe considerarse terminada.

CATGIS esta mejor parado que antes, pero todavia no esta en arquitectura competitiva fuerte. La direccion es correcta; falta convertir los puentes nuevos en flujo real de aplicacion y reducir el peso de las clases gigantes.


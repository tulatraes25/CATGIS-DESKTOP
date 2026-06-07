# \# CONSULTLAN - Flujo Codex + GitHub

# 

# \## Para qué sirve

# 

# Este archivo es una guía fija para trabajar con Codex cuando haya que:

# 

# \- preparar un lote

# \- commitear

# \- hacer push

# \- abrir PR

# \- revisar si conviene mergear

# \- hacer cierre post-merge

# 

# La idea es evitar volver a explicar el flujo cada vez.

# 

# \## Estado real del proyecto que Codex debe respetar

# 

# \- Proyecto: CONSULTLAN

# \- Stack: Java 21 + Spring Boot, React + Vite + TypeScript, PostgreSQL

# \- Arquitectura: monolito modular

# \- CONSULTLAN es la fuente de verdad

# \- No mezclar frentes sin necesidad

# \- No guardar secretos en el repo

# \- Backend validar con: `.\\mvnw.cmd test`

# \- Frontend validar con: `npm.cmd run build`

# 

# \## Fases reales ya avanzadas

# 

# \- Fase 3D: chat interno base operativa

# \- Fase 3D.2: archivos temporales en chat implementados

# \- Fase 4A: portal/API externa planificada/documentada

# \- Fase 4B.1: archivos clínicos permanentes implementados

# \- Fase 4C.1: PDF clínico base implementado

# \- Fase 4C.2: mejora visual institucional del PDF implementada

# \- Fase 4C.3: auditoría formal de exportación PDF implementada

# \- PR #22 de dashboard/portal ya fue mergeado en `main`

# 

# \## Regla operativa central

# 

# Cuando uses Codex para Git/GitHub:

# 

# 1\. primero revisar estado real del repo

# 2\. después confirmar alcance limpio

# 3\. recién ahí commitear o pushear

# 4\. antes de mergear, revisar PR y checks

# 5\. después del merge, hacer cierre técnico prolijo

# 

# Si Codex detecta mezcla de frentes, debe frenar antes de seguir.

# 

# \## Cómo usar este archivo

# 

# 1\. Abrí este archivo.

# 2\. Copiá el prompt que corresponda.

# 3\. Pegalo en Codex.

# 4\. Reemplazá solo las partes variables.

# 5\. Ejecutalo.

# 6\. Si querés doble control, me pegás la respuesta acá.

# 

# \## Prompt maestro

# 

# ```text

# Quiero que trabajes como operador técnico de Git/GitHub para CONSULTLAN, respetando el flujo de trabajo acordado del proyecto.

# 

# Contexto estable del proyecto:

# \- Proyecto: CONSULTLAN

# \- Stack: Java 21 + Spring Boot, React + Vite + TypeScript, PostgreSQL

# \- Arquitectura: monolito modular

# \- CONSULTLAN es la fuente de verdad

# \- No mezclar frentes sin necesidad

# \- No tocar producción ni secretos

# \- No guardar secretos en repo

# \- Backend validar con: .\\mvnw.cmd test

# \- Frontend validar con: npm.cmd run build

# 

# Forma de trabajo obligatoria:

# \- primero revisar estado real del repo/branch

# \- recortar alcance limpio

# \- no mezclar chat, portal, dashboard, clinical, docs UX, legacy o stash salvo que el lote lo pida

# \- antes de commitear: mostrar git status --short y confirmar alcance

# \- antes de push: confirmar branch, commit y working tree limpio

# \- antes de merge: revisar si el PR está limpio, draft/open, checks, y si realmente conviene mergear

# \- después del merge: hacer cierre post-merge prolijo

# \- no hacer cambios funcionales fuera del alcance pedido

# 

# Reglas de respuesta:

# \- devolveme siempre:

# &#x20; - diagnóstico breve

# &#x20; - archivos alcanzados

# &#x20; - si el alcance está limpio o mezclado

# &#x20; - comandos/acciones ejecutadas

# &#x20; - resultado final

# &#x20; - siguiente paso recomendado

# 

# Fases reales ya avanzadas del proyecto:

# \- Fase 3D: chat interno base operativa

# \- Fase 3D.2: archivos temporales en chat implementados

# \- Fase 4A: portal/API externa planificada/documentada

# \- Fase 4B.1: archivos clínicos permanentes implementados

# \- Fase 4C.1: PDF clínico base implementado

# \- Fase 4C.2: mejora visual institucional del PDF implementada

# \- Fase 4C.3: auditoría formal de exportación PDF implementada

# \- PR #22 dashboard/portal ya mergeado en main

# 

# Tu trabajo depende del objetivo que te indique abajo.

# 

# Objetivo actual:

# \[ACA PEGAR EL OBJETIVO DEL LOTE O ACCION]

# 

# Hacé lo necesario para cumplir ese objetivo, pero:

# \- no hagas push si no lo pido

# \- no abras PR si no lo pido

# \- no hagas merge si no lo pido

# \- no borres branches si no lo pido

# 

# Si detectás mezcla de frentes, frená y explicalo antes de seguir.


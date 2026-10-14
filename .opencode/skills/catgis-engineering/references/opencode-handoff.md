# OpenCode exact-SHA handoff and cleanup discipline

OpenCode/DeepSeek Flash is **local verifier/synchronizer**, not an independent author of GitHub-certificated code. It must never reconstruct a committed patch from conversational instructions.

Only request local work after GitHub architecture gate explicitly unlocks it, unless user separately authorizes an exceptional local-only action. Deliver one prompt:

CATGIS / VALIDACIÓN LOCAL, NO IMPLEMENTACIÓN
REPO=tulatraes25/CATGIS-DESKTOP
SOURCE_SHA=<exact certified 40 hex>
1. Report git remotes, git status --porcelain, worktree list and current HEAD. If dirty/untracked/ignored or personal GIS data: STOP and inventory, do not delete.
2. git fetch origin; create/use isolated validation worktree and checkout SOURCE_SHA. Verify git rev-parse HEAD equals SOURCE_SHA exactly.
3. Run only the approved platform checks (Windows GDAL/JNI/GUI project E2E where authorized), not new edits; no source write/push/merge.
4. Return exact SHA, OS/CPU/JDK/dependencies, commands/results, failures/logs and PASS/FAIL/BLOCKED by criterion.

After GitHub is fully certified, perform local cleanup as a separate read-only inventory: C:\catgis, downloads/worktrees, ignored files, unpushed commits, project files, geodata (.gpkg/.shp with sidecars/.qgz/.tif/.las), local PostGIS/SQLite dumps, user settings, credentials and backups. Propose SAFE REGENERABLE vs BACKUP vs UNKNOWN. Require exact-path authorization after backup for any delete. GitHub cannot restore never-committed GIS data. No local cleanup on a simple "continuar".

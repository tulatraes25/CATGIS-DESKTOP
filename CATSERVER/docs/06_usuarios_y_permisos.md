# CATSERVER - Usuarios y permisos

## Roles base

- `catserver_admin`: rol de administracion de datos.
- `catserver_read`: rol visor de solo lectura.
- `catserver_etl`, `catserver_edit`, `catserver_publish`: roles reservados para etapas futuras.

## Criterio aplicado

- Los visores pueden conectarse y consultar capas operativas.
- Los visores no pueden modificar datos.
- Los visores no tienen acceso a `raw`, `staging` ni `admin`.
- Los administradores pueden leer y editar tablas operativas y tecnicas.
- Las tablas nuevas creadas por `postgres` o por un administrador heredan permisos compatibles con este esquema.

## Archivos de soporte

- [12_security_roles_current.sql](/C:/CATGIS/CATSERVER/sql/12_security_roles_current.sql)
- [17_create_catserver_login.ps1](/C:/CATGIS/CATSERVER/scripts/17_create_catserver_login.ps1)

## Crear un nuevo visor

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\17_create_catserver_login.ps1 `
  -UserName "NOMBRE_USUARIO" `
  -Password "CLAVE_SEGURA" `
  -RoleType viewer `
  -BootstrapPassword "CLAVE_POSTGRES"
```

## Crear un nuevo administrador

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\17_create_catserver_login.ps1 `
  -UserName "NOMBRE_USUARIO" `
  -Password "CLAVE_SEGURA" `
  -RoleType admin `
  -BootstrapPassword "CLAVE_POSTGRES"
```

## Cambiar la clave de un usuario

```sql
ALTER ROLE nombre_usuario WITH PASSWORD 'NUEVA_CLAVE_SEGURA';
```

## Conexion en CATGIS

- Host: `localhost`
- Puerto: `5432`
- Base: `catserver`
- Usuario: el login creado
- Clave: la del login creado

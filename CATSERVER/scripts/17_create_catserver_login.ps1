param(
    [Parameter(Mandatory = $true)]
    [string]$UserName,

    [Parameter(Mandatory = $true)]
    [string]$Password,

    [Parameter(Mandatory = $true)]
    [ValidateSet("admin", "viewer")]
    [string]$RoleType,

    [string]$Database = "catserver",
    [string]$DbHost = "localhost",
    [int]$Port = 5432,
    [string]$BootstrapUser = "postgres",
    [Parameter(Mandatory = $true)]
    [string]$BootstrapPassword,
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $PsqlPath)) {
    throw "No se encontro psql en: $PsqlPath"
}

$groupRole = if ($RoleType -eq "admin") { "catserver_admin" } else { "catserver_read" }
$searchPath = "catastro, planeamiento, infraestructura, hidrologia, public"
$readOnlySql = if ($RoleType -eq "viewer") { "on" } else { "off" }

$escapedUserName = $UserName.Replace("'", "''")
$escapedPassword = $Password.Replace("'", "''")
$escapedSearchPath = $searchPath.Replace("'", "''")

$sql = @"
DO `$do`$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$escapedUserName') THEN
        EXECUTE format(
            'CREATE ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT',
            '$escapedUserName',
            '$escapedPassword'
        );
    ELSE
        EXECUTE format(
            'ALTER ROLE %I WITH LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT',
            '$escapedUserName',
            '$escapedPassword'
        );
    END IF;
END
`$do`$;

REVOKE ALL PRIVILEGES ON DATABASE $Database FROM "$escapedUserName";
GRANT CONNECT ON DATABASE $Database TO "$escapedUserName";
GRANT $groupRole TO "$escapedUserName";

ALTER ROLE "$escapedUserName" IN DATABASE $Database SET search_path = $escapedSearchPath;
ALTER ROLE "$escapedUserName" IN DATABASE $Database SET default_transaction_read_only = $readOnlySql;

"@

if ($RoleType -eq "admin") {
    $sql += @"
ALTER DEFAULT PRIVILEGES FOR ROLE "$escapedUserName" IN SCHEMA public
    GRANT SELECT ON TABLES TO catserver_admin, catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE "$escapedUserName" IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    GRANT ALL PRIVILEGES ON TABLES TO catserver_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE "$escapedUserName" IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO catserver_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE "$escapedUserName" IN SCHEMA catastro, infraestructura, planeamiento, hidrologia
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;
"@
}

$previousPassword = $env:PGPASSWORD
$env:PGPASSWORD = $BootstrapPassword

try {
    $sql | & $PsqlPath -h $DbHost -p $Port -U $BootstrapUser -d $Database

    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo crear o actualizar el usuario $UserName"
    }
}
finally {
    if ($null -ne $previousPassword) {
        $env:PGPASSWORD = $previousPassword
    }
    else {
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    }
}

Write-Host "Usuario configurado: $UserName ($RoleType)"

-- CATSERVER: valida que el script se ejecute sobre una base canonica.
--
-- Los archivos de esta carpeta ya no fuerzan el nombre de la base con \connect:
-- el nombre lo decide la invocacion (`psql -d ...`). Este guard convierte un
-- `-d` olvidado o equivocado en un error explicito en lugar de escribir en la
-- base por defecto del operador. Acepta las dos convenciones historicas del
-- repositorio (catserver y "CATSERVER"), sin renombrar instalaciones existentes.

DO $catgis_db_guard$
DECLARE
    current_db text := current_database();
BEGIN
    IF lower(current_db) <> 'catserver' THEN
        RAISE EXCEPTION
            'CATSERVER: ejecutar con psql -d <base> sobre una base llamada catserver/CATSERVER (base actual: %). Ver CATSERVER/docs/11_orden_de_ejecucion_y_prerrequisitos.md.',
            current_db;
    END IF;
END
$catgis_db_guard$;

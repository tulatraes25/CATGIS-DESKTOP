"""Disposable fixtures for the CATSERVER SQL contract checker (no network)."""
from pathlib import Path
import tempfile
import unittest

from check_catserver_sql_contract import audit

BOOTSTRAP = '\\set ON_ERROR_STOP on\nCREATE DATABASE "CATSERVER" WITH OWNER = catserver_owner;\n'
GUARD = (
    'DO $catgis_db_guard$\n'
    'DECLARE\n'
    '    current_db text := current_database();\n'
    'BEGIN\n'
    "    IF lower(current_db) <> 'catserver' THEN\n"
    "        RAISE EXCEPTION 'CATSERVER: ejecutar con psql -d <base>';\n"
    '    END IF;\n'
    'END\n'
    '$catgis_db_guard$;\n'
)
MASK_GUARD = (
    'DO $catgis_ejido_mask_guard$\n'
    'DECLARE\n'
    '    mask_srid integer;\n'
    'BEGIN\n'
    "    IF to_regclass('catastro.ejido_urbano_mask') IS NULL THEN\n"
    "        RAISE EXCEPTION 'falta catastro.ejido_urbano_mask';\n"
    '    END IF;\n'
    "    SELECT srid INTO mask_srid FROM geometry_columns WHERE f_geometry_column = 'geom';\n"
    '    IF mask_srid <> 4326 THEN\n'
    "        RAISE EXCEPTION 'SRID 4326 requerido';\n"
    '    END IF;\n'
    '    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE column_name = ' + "'gid') THEN\n"
    "        RAISE EXCEPTION 'falta gid';\n"
    '    END IF;\n'
    'END\n'
    '$catgis_ejido_mask_guard$;\n'
)
GUARDED_SCRIPT = '\\set ON_ERROR_STOP on\n\\ir _common/assert_catserver_database.sql\n\nSELECT 1;\n'
MASK_CONSUMER_SCRIPT = (
    '\\set ON_ERROR_STOP on\n'
    '\\ir _common/assert_catserver_database.sql\n'
    '\\ir _common/assert_ejido_mask.sql\n\n'
    'SELECT 1 FROM catastro.ejido_urbano_mask;\n'
)


class CatserverSqlContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory(prefix='catserver sql contract ')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.sql_root = self.root / 'CATSERVER' / 'sql'
        (self.sql_root / '_common').mkdir(parents=True)
        self.put('_common/assert_catserver_database.sql', GUARD)
        self.put('00_cluster_bootstrap.sql', BOOTSTRAP)
        self.put('12_security_roles_current.sql', GUARDED_SCRIPT)

    def put(self, relative: str, text: str) -> Path:
        path = self.sql_root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding='utf-8')
        return path

    def test_clean_tree_passes(self) -> None:
        self.assertEqual(2, audit(self.root))

    def test_connect_is_rejected(self) -> None:
        self.put('13_catserver_layers_view.sql', '\\connect catserver\nSELECT 1;\n')
        with self.assertRaisesRegex(RuntimeError, 'forces a database'):
            audit(self.root)

    def test_missing_guard_is_rejected(self) -> None:
        self.put('14_lineas_cleanup.sql', '\\set ON_ERROR_STOP on\nSELECT 1;\n')
        with self.assertRaisesRegex(RuntimeError, 'missing'):
            audit(self.root)

    def test_hardcoded_database_privilege_is_rejected(self) -> None:
        self.put(
            '12_security_roles_current.sql',
            GUARDED_SCRIPT + 'GRANT CONNECT ON DATABASE "CATSERVER" TO catserver_read;\n',
        )
        with self.assertRaisesRegex(RuntimeError, 'hardcodes a database name'):
            audit(self.root)

    def test_guard_that_accepts_any_database_is_rejected(self) -> None:
        self.put('_common/assert_catserver_database.sql', 'DO $$ BEGIN END $$;\n')
        with self.assertRaisesRegex(RuntimeError, 'does not reject'):
            audit(self.root)

    def test_bootstrap_must_create_canonical_database(self) -> None:
        self.put('00_cluster_bootstrap.sql', '\\set ON_ERROR_STOP on\n')
        with self.assertRaisesRegex(RuntimeError, 'canonical'):
            audit(self.root)

    def test_empty_tree_is_not_a_pass(self) -> None:
        for path in self.sql_root.glob('*.sql'):
            path.unlink()
        (self.sql_root / '00_cluster_bootstrap.sql').unlink(missing_ok=True)
        with self.assertRaisesRegex(RuntimeError, 'No CATSERVER SQL scripts'):
            audit(self.root)

    def test_mask_consumer_requires_mask_guard(self) -> None:
        self.put(
            '10_cad_barrios_finalize.sql',
            GUARDED_SCRIPT + '\nSELECT 1 FROM catastro.ejido_urbano_mask;\n',
        )
        with self.assertRaisesRegex(RuntimeError, 'consumes catastro.ejido_urbano_mask'):
            audit(self.root)

    def test_mask_guard_must_validate_the_data_contract(self) -> None:
        self.put('_common/assert_ejido_mask.sql', 'DO $$ BEGIN END $$;\n')
        self.put('10_cad_barrios_finalize.sql', MASK_CONSUMER_SCRIPT)
        with self.assertRaisesRegex(RuntimeError, 'does not validate'):
            audit(self.root)

    def test_valid_mask_guard_passes(self) -> None:
        self.put('_common/assert_ejido_mask.sql', MASK_GUARD)
        self.put('10_cad_barrios_finalize.sql', MASK_CONSUMER_SCRIPT)
        self.assertEqual(3, audit(self.root))


if __name__ == '__main__':
    unittest.main()

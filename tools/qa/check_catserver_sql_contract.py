"""Static contract for the CATSERVER SQL scripts (DC-4).

The stage-2 SQL files used to force a database with ``\\connect catserver`` while
the bootstrap creates ``"CATSERVER"``; on a fresh install every stage-2 script
aborted before executing a single statement. The contract now enforced here is:

* no script forces a database: the caller's ``psql -d`` decides;
* every script except the cluster bootstrap includes the shared database guard;
* no script hardcodes a database name in ``ON DATABASE``/``IN DATABASE``;
* the guard file itself rejects any database that is not catserver/CATSERVER.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import sys

SQL_ROOT = Path('CATSERVER', 'sql')
BOOTSTRAP = '00_cluster_bootstrap.sql'
GUARD_RELATIVE = Path('_common', 'assert_catserver_database.sql')
GUARD_REFERENCE = r'\ir _common/assert_catserver_database.sql'

CONNECT_LINE = re.compile(r'(?m)^\s*\\connect\b')
DATABASE_NAME_STATEMENT = re.compile(
    r'(?m)^\s*(?:GRANT|REVOKE)\b[^;\n]*\b(?:ON|IN)\s+DATABASE\s+"?[A-Za-z_][\w$]*"?'
)
BOOTSTRAP_DATABASE = re.compile(r'CREATE\s+DATABASE\s+"CATSERVER"', re.IGNORECASE)


def audit(root: Path) -> int:
    problems: list[str] = []
    sql_root = root / SQL_ROOT
    if not sql_root.is_dir():
        raise RuntimeError('Missing CATSERVER/sql directory at ' + str(sql_root))

    guard = sql_root / GUARD_RELATIVE
    if not guard.is_file():
        raise RuntimeError('Missing shared database guard: ' + str(guard))
    guard_text = guard.read_text(encoding='utf-8')
    if "lower(current_db) <> 'catserver'" not in guard_text:
        problems.append('guard does not reject non-catserver databases')

    scripts = sorted(sql_root.glob('*.sql'))
    if not scripts:
        raise RuntimeError('No CATSERVER SQL scripts found: refusing an empty PASS')
    bootstrap = sql_root / BOOTSTRAP
    if not bootstrap.is_file():
        raise RuntimeError('Missing cluster bootstrap: ' + str(bootstrap))
    if not BOOTSTRAP_DATABASE.search(bootstrap.read_text(encoding='utf-8')):
        problems.append(BOOTSTRAP + ': does not create the canonical "CATSERVER" database')

    for script in scripts:
        name = script.name
        text = script.read_text(encoding='utf-8')
        if name == BOOTSTRAP:
            continue
        if CONNECT_LINE.search(text):
            problems.append(name + ': forces a database with \\connect; use psql -d instead')
        if GUARD_REFERENCE not in text:
            problems.append(name + ': missing ' + GUARD_REFERENCE)
        for match in DATABASE_NAME_STATEMENT.finditer(text):
            problems.append(
                name + ': hardcodes a database name in a privilege statement: '
                + match.group(0).strip()
            )

    if problems:
        raise RuntimeError('\n'.join(problems))
    print('CATSERVER_SQL_CONTRACT_PASS files=' + str(len(scripts)))
    return len(scripts)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', default='.', help='Repository root (default: cwd)')
    args = parser.parse_args()
    try:
        audit(Path(args.root).resolve())
    except RuntimeError as failure:
        print(failure, file=sys.stderr)
        raise SystemExit(1) from failure


if __name__ == '__main__':
    main()

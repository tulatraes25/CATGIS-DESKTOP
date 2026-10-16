"""Run the isolated Java17 row-reader experiment; never run desktop or mutate inputs."""
from __future__ import annotations
import hashlib
import json
import os
from pathlib import Path
import platform
import subprocess
import sys
from geoparquet_probe_integrity import (
    assert_source_unchanged, capture_source_state, validate_junit_reports,
)


def main() -> None:
    root = Path(__file__).resolve().parents[2]
    os.chdir(root)
    source_state = capture_source_state(root)
    architecture = platform.machine().lower()
    expected = os.environ['EXPECTED_PROBE_ARCH']
    allowed = {'x64': {'x86_64', 'amd64'}, 'arm64': {'arm64', 'aarch64'}}
    if architecture not in allowed[expected]:
        raise RuntimeError('Actual runner architecture does not match declared matrix: ' + architecture)
    for command in (['git', 'rev-parse', 'HEAD'], ['git', 'rev-parse', 'HEAD^{tree}'], ['java', '-version']):
        subprocess.run(command, check=True, timeout=30)
    print('ACTUAL_ROW_PROBE_RUNNER', platform.system(), architecture, flush=True)
    fixtures = Path(os.environ['RUNNER_TEMP']) / 'catgis-geoparquet-row-probe'
    env = dict(os.environ, CATGIS_GEOPARQUET_FIXTURES=str(fixtures),
               GRADLE_USER_HOME=str(Path(os.environ['RUNNER_TEMP']) / 'catgis-row-probe-gradle'),
               PROJ_NETWORK='OFF')
    subprocess.run([sys.executable, 'tools/qa/generate_geoparquet_fixtures.py', str(fixtures)],
                   check=True, env=env, timeout=120)
    subprocess.run([sys.executable, 'tools/qa/extend_geoparquet_reader_fixtures.py', str(fixtures)],
                   check=True, env=env, timeout=120)
    wrapper = root / 'catgis-desktop' / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
    launcher = [str(wrapper)] if os.name == 'nt' else ['bash', str(wrapper)]
    project = ['--no-daemon', '-p', 'tools/qa/geoparquet-reader-probe']
    for attempt in range(3):
        result = subprocess.run(launcher + project + ['resolveProbeDependencies'], env=env, timeout=600)
        if result.returncode == 0:
            break
        if attempt == 2:
            raise RuntimeError('Isolated dependency resolution failed; no tests certified.')
    subprocess.run(launcher + project + ['--offline', 'clean', 'test', 'probeDependencyInventory'],
                   env=env, check=True, timeout=600)
    results = root / 'tools/qa/geoparquet-reader-probe/build/test-results/test'
    totals = validate_junit_reports(results)
    print('GEOPARQUET_REAL_ROW_PROBE', totals, flush=True)
    manifest = json.loads((fixtures / 'probe-sha256.json').read_text(encoding='utf-8'))
    if len(manifest) != 19:
        raise RuntimeError('Incomplete independent row fixture manifest.')
    for name, expected_hash in manifest.items():
        if hashlib.sha256((fixtures / name).read_bytes()).hexdigest() != expected_hash:
            raise RuntimeError('Row reader modified a source fixture: ' + name)
    print('ROW_PROBE_SOURCE_HASHES_UNCHANGED', len(manifest), flush=True)
    assert_source_unchanged(root, source_state)
    print('ROW_PROBE_TRACKED_SOURCE_UNCHANGED', source_state.commit, source_state.tree, flush=True)
    inventory = root / 'tools/qa/geoparquet-reader-probe/build/reports/probe-dependencies.tsv'
    if not inventory.is_file():
        raise RuntimeError('Missing isolated dependency measurement.')
    print('ROW_PROBE_RESULT=PASS_RESEARCH_ONLY_NOT_A_LAYER_IMPORTER', flush=True)


if __name__ == '__main__':
    main()

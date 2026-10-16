"""Fail-closed evidence checks for the isolated GeoParquet probe.

Read-only: no reset, add, checkout, commit, network operation or source repair.
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
import json
import subprocess
import xml.etree.ElementTree as ET


class IntegrityError(RuntimeError):
    """Required source or test evidence is absent or inconsistent."""


@dataclass(frozen=True)
class SourceState:
    commit: str
    tree: str


# Exact Java contracts from PR122. Changing this set requires a reviewed SPEC.
REQUIRED_TESTS = {
    'ar.com.catgis.probe.GeoParquetRowsProbeTest': frozenset({
        'threeCodecsPreservePrimitiveTypesLargeIntegersNegativeZeroAndNulls()',
        'malformedMetadataAndTruncationStillFailThroughTheActualM1Reader()',
        'projectedPolygonsKeepExactAreasCoordinatesAndIntegerNullAttributes()',
        'repeatedSuccessAndFailureReleaseHandlesAndPreserveExactFileBytes()',
        'probeRunsOnJava17AndCannotLoadTheDesktopApplication()',
        'unsupportedNestedAttributesAndCodecsAreExplicitlyRejected()',
        'badWkbTrailingBytesAndDamagedPageCannotReturnAPartialSuccessfulScan()',
        'localReadNeedsNoJavaSocketConnectionOrExternalService()',
        'cancellationAndRowBudgetFailWithoutProducingAScan()',
        'defaultCrsAndExplicitUnknownRemainDistinctWithoutInventingAnSrid()',
        'pointRowsPreserveActualCoordinatesUnicodeNullsAndRowGroups()',
        'bothGeometryColumnsAreMaterializedAndPrimaryChoiceIsPreserved()',
        'genuineEmptyArrowFileYieldsNoInventedFeatures()',
    }),
    'ar.com.catgis.probe.LocalProbeInputTest': frozenset({
        'cancellationDuringReadStillClosesTheUnderlyingLocalHandle()',
        'directAndSlicedBuffersPreservePositionsAndActualByteRanges()',
        'independentStreamsSeekAndEofDoNotInventZeroBytesOrSharePositions()',
    }),
    'ar.com.catgis.probe.ScalarBoundaryDiagnosticsTest': frozenset({
        'rawScalarAndOfficialFileAdapterPreserveBothDisputedValues()',
    }),
}


def _git(root: Path, *args: str) -> subprocess.CompletedProcess:
    try:
        return subprocess.run(
            ['git', '-C', str(root), *args], check=False,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=30,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise IntegrityError('Git source verification could not run.') from error


def _revision(root: Path, ref: str) -> str:
    result = _git(root, 'rev-parse', '--verify', ref)
    if result.returncode:
        raise IntegrityError('Cannot resolve source revision: ' + ref)
    value = result.stdout.decode('ascii', errors='strict').strip()
    if len(value) not in (40, 64) or any(c not in '0123456789abcdef' for c in value):
        raise IntegrityError('Unexpected Git object identifier.')
    return value


def _assert_clean(root: Path) -> None:
    # HEAD, not the index alone: staged and unstaged writes both fail.
    flags = ('--no-ext-diff', '--no-textconv', '--ignore-submodules=none')
    result = _git(root, 'diff', '--quiet', *flags, 'HEAD', '--')
    if result.returncode == 0:
        return
    if result.returncode != 1:
        raise IntegrityError('Git could not compare tracked sources to HEAD.')
    names = _git(root, '-c', 'core.quotePath=true', 'diff', '--name-only', *flags, 'HEAD', '--')
    if names.returncode:
        raise IntegrityError('Tracked sources differ from HEAD; cannot list affected paths.')
    # Keep diagnostics bounded; do not dump entire source files into CI logs.
    lines = names.stdout.decode('utf-8', errors='replace').splitlines()
    details = '\n'.join(lines[:50])
    raise IntegrityError('Tracked sources differ from HEAD (including staged changes):\n' + details)


def capture_source_state(root: Path) -> SourceState:
    state = SourceState(_revision(root, 'HEAD'), _revision(root, 'HEAD^{tree}'))
    _assert_clean(root)
    return state


def assert_source_unchanged(root: Path, expected: SourceState) -> None:
    actual = SourceState(_revision(root, 'HEAD'), _revision(root, 'HEAD^{tree}'))
    if actual != expected:
        raise IntegrityError('Source HEAD/tree changed while the probe was running.')
    _assert_clean(root)


def validate_junit_reports(directory: Path) -> dict[str, int]:
    """Require actual distinct test cases, not just favorable XML counters."""
    expected_files = {'TEST-' + name + '.xml' for name in REQUIRED_TESTS}
    files = list(directory.glob('TEST-*.xml'))
    if {path.name for path in files} != expected_files:
        raise IntegrityError('Missing or unexpected probe JUnit report.')
    totals = dict.fromkeys(('tests', 'failures', 'errors', 'skipped'), 0)
    for path in files:
        if path.is_symlink() or not path.is_file() or path.stat().st_size > 1024 * 1024:
            raise IntegrityError('Invalid or oversized JUnit report: ' + path.name)
        try:
            data = path.read_bytes()
            text = data.decode('utf-8', errors='strict')
            if (len(data) > 1024 * 1024 or '\0' in text
                    or '<!DOCTYPE' in text or '<!ENTITY' in text):
                raise IntegrityError('JUnit report exceeds the plain-XML contract.')
            suite = ET.fromstring(data)
        except (OSError, UnicodeDecodeError, ET.ParseError) as error:
            raise IntegrityError('Unreadable JUnit report: ' + path.name) from error
        name = suite.get('name')
        if (suite.tag != 'testsuite' or name not in REQUIRED_TESTS
                or path.name != 'TEST-' + name + '.xml'):
            raise IntegrityError('Unexpected JUnit suite identity.')
        cases = suite.findall('testcase')
        identities = [(case.get('classname'), case.get('name')) for case in cases]
        expected_cases = {(name, method) for method in REQUIRED_TESTS[name]}
        if len(identities) != len(set(identities)) or set(identities) != expected_cases:
            raise IntegrityError('Missing, duplicate or unexpected Java contract in ' + name)
        observed = {'tests': len(cases)}
        for key, tag in (('failures', 'failure'), ('errors', 'error'), ('skipped', 'skipped')):
            observed[key] = sum(case.find(tag) is not None for case in cases)
        try:
            declared = {key: int(suite.attrib[key]) for key in totals}
        except (KeyError, TypeError, ValueError) as error:
            raise IntegrityError('Missing or invalid JUnit counters.') from error
        if observed != declared or any(observed[key] for key in ('failures', 'errors', 'skipped')):
            raise IntegrityError('JUnit counters disagree with cases, or a Java contract did not pass.')
        for key, value in observed.items():
            totals[key] += value
    if totals != {'tests': 17, 'failures': 0, 'errors': 0, 'skipped': 0}:
        raise IntegrityError('The complete seventeen-test probe was not proven.')
    return totals


if __name__ == '__main__':
    root = Path(__file__).resolve().parents[2]
    state = capture_source_state(root)
    print('GEOPARQUET_SOURCE_PREFLIGHT', json.dumps(state.__dict__, sort_keys=True), flush=True)

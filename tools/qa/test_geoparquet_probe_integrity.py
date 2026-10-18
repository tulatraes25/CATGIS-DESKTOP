"""Offline regressions for exact source and JUnit evidence gates."""
from __future__ import annotations

from pathlib import Path
import os
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

from geoparquet_probe_integrity import (
    IntegrityError, REQUIRED_TESTS, assert_source_unchanged,
    capture_source_state, validate_junit_reports,
)

import base64

RAW_VECTOR = base64.b64decode(
    'cGFja2FnZSBhci5jb20uY2F0Z2lzOw0NCmltcG9ydCBhci5jb20uY2F0Z2lz'
    'LmNvcmUubW9kZWwuTGF5ZXI7Cg0KcHVibGljIGNsYXNzIFZlY3RvckxheWVy'
    'IGV4dGVuZHMgTGF5ZXIgew0KDQogICAgcHVibGljIFZlY3RvckxheWVyKFN0'
    'cmluZyBuYW1lLCBTdHJpbmcgcGF0aCkgew0KICAgICAgICBzdXBlcihuYW1l'
    'LCBwYXRoLCAiW1ZFQ1RPUl0iKTsNCiAgICB9DQp9'
)


class TrackedSourceTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='catgis source guard ')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.env = dict(os.environ, GIT_CONFIG_NOSYSTEM='1', GIT_CONFIG_GLOBAL=os.devnull)
        self.git('init', '-q')
        self.git('config', 'user.name', 'CATGIS test')
        self.git('config', 'user.email', 'test@example.invalid')
        self.git('config', 'core.autocrlf', 'false')
        (self.root / 'A.java').write_bytes(b'class A {}\n')
        self.git('add', 'A.java')
        self.git('commit', '-qm', 'synthetic baseline')

    def git(self, *args, input=None, check=True):
        return subprocess.run(['git', '-C', str(self.root), *args], input=input,
                              check=check, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                              env=self.env, timeout=20)

    def test_clean_source_passes_without_mutation(self):
        expected = capture_source_state(self.root)
        assert_source_unchanged(self.root, expected)
        self.assertEqual(b'', self.git('status', '--porcelain').stdout)

    def test_unstaged_changes_fail(self):
        expected = capture_source_state(self.root)
        (self.root / 'A.java').write_bytes(b'class A { int x; }\n')
        with self.assertRaises(IntegrityError):
            assert_source_unchanged(self.root, expected)

    def test_staged_change_hidden_from_original_git_diff_is_detected(self):
        expected = capture_source_state(self.root)
        (self.root / 'A.java').write_bytes(b'class A { int x; }\n')
        self.git('add', 'A.java')
        self.assertEqual(0, self.git('diff', '--exit-code').returncode)
        with self.assertRaises(IntegrityError):
            assert_source_unchanged(self.root, expected)

    def test_preflight_rejects_existing_staged_changes(self):
        (self.root / 'A.java').write_bytes(b'class A { int x; }\n')
        self.git('add', 'A.java')
        with self.assertRaises(IntegrityError):
            capture_source_state(self.root)

    def test_head_change_even_with_same_tree_fails(self):
        expected = capture_source_state(self.root)
        self.git('commit', '--allow-empty', '-qm', 'changed commit identity')
        self.assertEqual(expected.tree, self.git('rev-parse', 'HEAD^{tree}').stdout.decode().strip())
        with self.assertRaises(IntegrityError):
            assert_source_unchanged(self.root, expected)

    def test_committed_source_mutation_fails(self):
        expected = capture_source_state(self.root)
        (self.root / 'A.java').write_bytes(b'class A { int x; }\n')
        self.git('add', 'A.java')
        self.git('commit', '-qm', 'source mutation')
        with self.assertRaises(IntegrityError):
            assert_source_unchanged(self.root, expected)

    def test_tracked_file_deletion_fails(self):
        expected = capture_source_state(self.root)
        (self.root / 'A.java').unlink()
        with self.assertRaises(IntegrityError):
            assert_source_unchanged(self.root, expected)

    def test_missing_repository_fails_closed(self):
        with tempfile.TemporaryDirectory() as other:
            with self.assertRaises(IntegrityError):
                capture_source_state(Path(other))

    def test_real_historical_eol_defect_fails_preflight_before_any_probe(self):
        (self.root / '.gitattributes').write_bytes(b'*.java text eol=lf\n')
        self.git('add', '.gitattributes')
        sha = self.git('hash-object', '-w', '--stdin', input=RAW_VECTOR).stdout.decode().strip()
        self.git('update-index', '--add', '--cacheinfo', '100644,' + sha + ',VectorLayer.java')
        self.git('commit', '-qm', 'raw GitHub blob with inconsistent attributes')
        self.git('checkout-index', '--', 'VectorLayer.java')
        with self.assertRaises(IntegrityError):
            capture_source_state(self.root)


class JunitEvidenceTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='catgis junit guard ')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        for classname, methods in REQUIRED_TESTS.items():
            suite = ET.Element('testsuite', name=classname, tests=str(len(methods)),
                               failures='0', errors='0', skipped='0')
            for method in sorted(methods):
                ET.SubElement(suite, 'testcase', classname=classname, name=method)
            self.save(classname, suite)
        self.name = next(iter(REQUIRED_TESTS))

    def path(self, classname=None):
        return self.root / ('TEST-' + (classname or self.name) + '.xml')

    def suite(self):
        return ET.parse(self.path()).getroot()

    def save(self, classname, suite):
        (self.root / ('TEST-' + classname + '.xml')).write_bytes(ET.tostring(suite))

    def reject(self):
        with self.assertRaises(IntegrityError):
            validate_junit_reports(self.root)

    def test_complete_actual_case_set_passes(self):
        self.assertEqual({'tests': 17, 'failures': 0, 'errors': 0, 'skipped': 0},
                         validate_junit_reports(self.root))

    def test_missing_report_fails(self):
        self.path().unlink()
        self.reject()

    def test_unexpected_report_fails(self):
        (self.root / 'TEST-extra.xml').write_text('<testsuite/>')
        self.reject()

    def test_malformed_xml_fails(self):
        self.path().write_text('<testsuite')
        self.reject()

    def test_header_totals_cannot_replace_actual_testcases(self):
        suite = self.suite()
        for case in list(suite.findall('testcase')):
            suite.remove(case)
        self.save(self.name, suite)
        # The original counter-only predicate still reports seventeen passes.
        totals = {key: sum(int(ET.parse(p).getroot().get(key, '0'))
                          for p in self.root.glob('TEST-*.xml'))
                  for key in ('tests', 'failures', 'errors', 'skipped')}
        self.assertEqual({'tests': 17, 'failures': 0, 'errors': 0, 'skipped': 0}, totals)
        self.reject()

    def test_duplicate_case_replacing_another_fails_even_with_same_total(self):
        suite = self.suite(); cases = suite.findall('testcase')
        cases[1].set('name', cases[0].get('name'))
        self.save(self.name, suite)
        self.reject()

    def test_unknown_method_replacing_a_required_contract_fails(self):
        suite = self.suite(); suite.find('testcase').set('name', 'unrelatedTest()')
        self.save(self.name, suite)
        self.reject()

    def test_wrong_case_class_fails(self):
        suite = self.suite(); suite.find('testcase').set('classname', 'other.Class')
        self.save(self.name, suite)
        self.reject()

    def test_failure_node_with_false_success_counter_fails(self):
        suite = self.suite(); ET.SubElement(suite.find('testcase'), 'failure')
        self.save(self.name, suite)
        self.reject()

    def test_error_node_with_matching_counter_still_fails(self):
        suite = self.suite(); ET.SubElement(suite.find('testcase'), 'error'); suite.set('errors', '1')
        self.save(self.name, suite)
        self.reject()

    def test_skipped_contract_fails(self):
        suite = self.suite(); ET.SubElement(suite.find('testcase'), 'skipped'); suite.set('skipped', '1')
        self.save(self.name, suite)
        self.reject()

    def test_inconsistent_test_counter_fails(self):
        suite = self.suite(); suite.set('tests', '999')
        self.save(self.name, suite)
        self.reject()

    def test_missing_counter_fails(self):
        suite = self.suite(); del suite.attrib['skipped']
        self.save(self.name, suite)
        self.reject()

    def test_wrong_suite_name_fails(self):
        suite = self.suite(); suite.set('name', 'other.Class')
        self.save(self.name, suite)
        self.reject()

    def test_entity_declaration_fails(self):
        old = self.path().read_bytes()
        self.path().write_bytes(b'<!DOCTYPE testsuite [<!ENTITY x "value">]>' + old)
        self.reject()

    def test_utf16_report_cannot_bypass_plain_utf8_contract(self):
        text = self.path().read_text(encoding='utf-8')
        self.path().write_bytes(text.encode('utf-16'))
        self.reject()

    def test_invalid_utf8_report_fails_closed(self):
        self.path().write_bytes(b'\xff<testsuite/>')
        self.reject()

    def test_oversized_report_fails(self):
        self.path().write_bytes(b' ' * (1024 * 1024 + 1))
        self.reject()


if __name__ == '__main__':
    unittest.main()

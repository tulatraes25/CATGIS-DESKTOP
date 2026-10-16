"""Synthetic evidence parser negatives plus actual isolated Git source checks."""
import copy
import json
import struct
import subprocess
import tempfile
import unittest
from pathlib import Path
from xml.etree import ElementTree as ET

import check_runtime_identity as check


class RuntimeIdentityTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def test_supported_release_versions(self):
        for version, expected in [('17.0.20+1', 17), ('"21.0.12.1"', 21), ('17-ea', 17), ('17', 17)]:
            self.assertEqual(expected, check.feature(version))

    def test_malformed_versions_do_not_pass(self):
        for value in ('', 'n/a', 'java17', '17"', '"17', '-17', '17 bad'):
            with self.subTest(value=value), self.assertRaises(ValueError):
                check.feature(value)

    def test_architecture_aliases(self):
        for value in ('amd64', 'x64', 'X86_64'):
            self.assertEqual('x86_64', check.architecture(value))
        self.assertEqual('aarch64', check.architecture('arm64'))

    def test_unknown_architecture_rejected(self):
        for value in ('', 'x86', 'i386', 'universal'):
            with self.assertRaises(ValueError):
                check.architecture(value)

    def test_records_require_observed_expected_and_role(self):
        record = {'role': 'compiler', 'feature': 17, 'expected': 17}
        check.verify_record(record, 'compiler', 17)
        for key, value in [('feature', 21), ('expected', 21), ('role', 'bootstrap'), ('feature', True), ('feature', '17')]:
            broken = dict(record, **{key: value})
            with self.assertRaises(ValueError):
                check.verify_record(broken, 'compiler', 17)

    def test_duplicate_json_fields_rejected(self):
        path = self.root / 'record.json'
        path.write_text('{"feature":21,"feature":17}')
        with self.assertRaises(ValueError):
            check.json_record(path)

    def test_non_object_json_rejected(self):
        path = self.root / 'record.json'
        path.write_text('[]')
        with self.assertRaises(ValueError):
            check.json_record(path)

    def test_input_size_bound(self):
        path = self.root / 'large'
        path.write_bytes(b'x' * check.LIMIT)
        self.assertEqual(check.LIMIT, len(check.read_small(path)))
        path.write_bytes(b'x' * (check.LIMIT + 1))
        with self.assertRaises(ValueError):
            check.read_small(path)

    def test_actual_class_header(self):
        path = self.root / 'Main.class'
        path.write_bytes(struct.pack('>IHH', 0xCAFEBABE, 0, 61))
        self.assertEqual(61, check.class_major(path))

    def test_corrupt_truncated_and_preview_classes(self):
        path = self.root / 'Main.class'
        for raw in (b'', b'notaclass', struct.pack('>IHH', 0xCAFEBABE, 65535, 61)):
            path.write_bytes(raw)
            with self.assertRaises(ValueError):
                check.class_major(path)

    def test_release_matches_version_and_architecture(self):
        check.verify_release(b'JAVA_VERSION="17.0.20"\nOS_ARCH="amd64"\n', 17, 'x86_64')

    def test_wrong_runtime_and_architecture_rejected(self):
        for data in (b'JAVA_VERSION="21"\nOS_ARCH="amd64"', b'JAVA_VERSION="17"\nOS_ARCH="aarch64"'):
            with self.assertRaises(ValueError):
                check.verify_release(data, 17, 'x64')

    def test_incomplete_duplicate_or_malformed_release_rejected(self):
        for data in (b'JAVA_VERSION="17"', b'JAVA_VERSION="17"\nJAVA_VERSION="21"',
                     b'JAVA_VERSION="17\nOS_ARCH="amd64"', b'not a release'):
            with self.assertRaises(ValueError):
                check.verify_release(data, 17, 'amd64')

    def test_jlink_release_without_arch_requires_independent_proof(self):
        data = b'JAVA_VERSION="17.0.20.1"\nMODULES="java.base java.desktop"\n'
        with self.assertRaises(ValueError):
            check.verify_release(data, 17, 'x86_64')
        check.verify_release(data, 17, 'x86_64', runtime_arch='amd64')

    def test_runtime_proof_cannot_override_conflicting_release_architecture(self):
        with self.assertRaises(ValueError):
            check.verify_release(b'JAVA_VERSION="17"\nOS_ARCH="aarch64"', 17, 'amd64', runtime_arch='amd64')

    def test_runtime_architecture_must_match_even_when_release_looks_correct(self):
        with self.assertRaises(ValueError):
            check.verify_release(b'JAVA_VERSION="17"\nOS_ARCH="amd64"', 17, 'amd64', runtime_arch='arm64')

    def pe(self, machine=0x8664, magic=0x20b):
        data = bytearray(64 + 24 + 240)
        data[:2] = b'MZ'
        struct.pack_into('<I', data, 60, 64)
        data[64:68] = b'PE\x00\x00'
        struct.pack_into('<H', data, 68, machine)
        struct.pack_into('<H', data, 84, 240)
        struct.pack_into('<H', data, 88, magic)
        return data

    def test_real_pe_machine_field_not_only_dos_signature(self):
        path = self.root / 'jvm.dll'
        for machine, arch in ((0x8664, 'x86_64'), (0xAA64, 'aarch64')):
            path.write_bytes(self.pe(machine))
            self.assertEqual(arch, check.pe_architecture(path))

    def test_fake_truncated_and_unbounded_pe_headers_rejected(self):
        path = self.root / 'jvm.dll'
        offset = self.pe()
        struct.pack_into('<I', offset, 60, 0x7fffffff)
        signature = self.pe()
        signature[64] = ord('X')
        for data in (b'MZ', self.pe()[:80], offset, signature):
            path.write_bytes(data)
            with self.assertRaises(ValueError):
                check.pe_architecture(path)

    def test_32_bit_and_unknown_pe_architectures_rejected(self):
        path = self.root / 'jvm.dll'
        for data in (self.pe(0x014c, 0x10b), self.pe(0x8664, 0x10b), self.pe(0)):
            path.write_bytes(data)
            with self.assertRaises(ValueError):
                check.pe_architecture(path)

    def test_truncated_or_oversized_optional_header_rejected(self):
        path = self.root / 'jvm.dll'
        for size in (0, 1, 4097):
            data = self.pe()
            struct.pack_into('<H', data, 84, size)
            path.write_bytes(data)
            with self.assertRaises(ValueError):
                check.pe_architecture(path)
        path.write_bytes(self.pe()[:-1])
        with self.assertRaises(ValueError):
            check.pe_architecture(path)

    def test_executed_jvm_properties_supply_actual_architecture(self):
        output = 'Property settings:\n    java.specification.version = 17\n    os.arch = amd64\n'
        self.assertEqual('x86_64', check.executed_runtime_architecture(output, 17, 'x86_64'))

    def test_duplicate_or_contradictory_runtime_properties_rejected(self):
        output = '    java.specification.version = 17\n    os.arch = amd64\n'
        for extra in ('    os.arch = amd64\n', '    os.arch = aarch64\n',
                      '    java.specification.version = 21\n'):
            with self.assertRaises(ValueError):
                check.executed_runtime_architecture(output + extra, 17, 'amd64')

    def test_missing_oversized_or_wrong_executed_runtime_properties_rejected(self):
        for output in ('', 'x' * 65537,
                       'java.specification.version = 21\nos.arch = amd64\n',
                       'java.specification.version = 17\nos.arch = aarch64\n'):
            with self.assertRaises(ValueError):
                check.executed_runtime_architecture(output, 17, 'amd64')

    def xml(self):
        suite = ET.Element('testsuite', name=check.SUITE, tests='3', failures='0', errors='0', skipped='0')
        for name in sorted(check.METHODS):
            ET.SubElement(suite, 'testcase', name=name + '()', classname=check.SUITE)
        return suite

    def write_xml(self, root):
        path = self.root / ('TEST-' + check.SUITE + '.xml')
        path.write_bytes(ET.tostring(root))
        return path

    def test_exact_identity_suite(self):
        check.verify_junit(self.write_xml(self.xml()))

    def test_missing_duplicate_and_substituted_cases_rejected(self):
        for change in ('missing', 'duplicate', 'substituted'):
            suite = self.xml()
            if change == 'missing':
                suite.remove(suite[-1])
            else:
                suite[-1].set('name', suite[0].get('name') if change == 'duplicate' else 'unrelated()')
            with self.assertRaises(ValueError):
                check.verify_junit(self.write_xml(suite))

    def test_identity_failures_errors_and_skips_rejected(self):
        for kind in ('failure', 'error', 'skipped'):
            suite = self.xml()
            ET.SubElement(suite[0], kind)
            with self.assertRaises(ValueError):
                check.verify_junit(self.write_xml(suite))

    def test_counter_mismatch_rejected(self):
        suite = self.xml()
        suite.set('tests', '4')
        with self.assertRaises(ValueError):
            check.verify_junit(self.write_xml(suite))

    def test_wrong_suite_and_class_rejected(self):
        for where in ('suite', 'class'):
            suite = self.xml()
            (suite if where == 'suite' else suite[0]).set('name' if where == 'suite' else 'classname', 'other')
            with self.assertRaises(ValueError):
                check.verify_junit(self.write_xml(suite))

    def test_entity_and_non_utf8_xml_rejected(self):
        path = self.root / 'bad.xml'
        for raw in (b'<!DOCTYPE testsuite [<!ENTITY x "a">]><testsuite/>',
                    '<testsuite/>'.encode('utf-16'), b'<testsuite'):
            path.write_bytes(raw)
            with self.assertRaises((ValueError, UnicodeError, ET.ParseError)):
                check.verify_junit(path)

    def test_full_suite_count_nodes_and_skip_budget(self):
        root = ET.Element('testsuite', name='synthetic', tests='700', failures='0', errors='0', skipped='15')
        for i in range(700):
            case = ET.SubElement(root, 'testcase', name=str(i))
            if i < 15:
                ET.SubElement(case, 'skipped')
        path = self.root / 'TEST-synthetic.xml'
        path.write_bytes(ET.tostring(root))
        self.assertEqual(15, check.verify_full_suite(self.root)['skipped'])
        ET.SubElement(root[15], 'skipped')
        root.set('skipped', '16')
        path.write_bytes(ET.tostring(root))
        with self.assertRaises(ValueError):
            check.verify_full_suite(self.root)

    def test_full_suite_missing_or_below_minimum_rejected(self):
        with self.assertRaises(ValueError):
            check.verify_full_suite(self.root)
        self.write_xml(self.xml())
        with self.assertRaises(ValueError):
            check.verify_full_suite(self.root)

    def test_real_git_staged_and_unstaged_changes_are_detected(self):
        check.git(self.root, 'init', '-q')
        check.git(self.root, 'config', 'user.name', 'Synthetic Test')
        check.git(self.root, 'config', 'user.email', 'test@example.invalid')
        path = self.root / 'source.txt'
        path.write_text('original\n')
        check.git(self.root, 'add', 'source.txt')
        check.git(self.root, 'commit', '-qm', 'synthetic baseline')
        baseline = check.source_identity(self.root)
        self.assertEqual(40, len(baseline['head']))
        path.write_text('changed\n')
        with self.assertRaises(subprocess.CalledProcessError):
            check.source_identity(self.root)
        check.git(self.root, 'add', 'source.txt')
        with self.assertRaises(subprocess.CalledProcessError):
            check.source_identity(self.root)


if __name__ == '__main__':
    unittest.main(verbosity=2)

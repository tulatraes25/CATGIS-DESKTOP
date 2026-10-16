"""Real disposable Git repositories: no network, normalizer or source repair in CI."""
from pathlib import Path
import subprocess
import tempfile
import unittest
from check_java_source_integrity import NORMALIZED_PATHS, audit, git, normalized, prove_normalization


class SourceIntegrityTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='catgis source integrity ')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        git(self.root, 'init', '-q')
        git(self.root, 'config', 'user.name', 'CATGIS isolated QA')
        git(self.root, 'config', 'user.email', 'qa@example.invalid')
        git(self.root, 'config', 'core.autocrlf', 'false')
        self.put('.gitattributes', b'*.java text eol=lf\n')

    def put(self, name, value, checkout=None):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(value if checkout is None else checkout)
        sha = git(self.root, 'hash-object', '-w', '--stdin', data=value).strip().decode('ascii')
        git(self.root, 'update-index', '--add', '--cacheinfo', '100644,' + sha + ',' + name)

    def commit(self):
        git(self.root, 'commit', '-q', '-m', 'isolated fixture')
        return git(self.root, 'rev-parse', 'HEAD').strip().decode('ascii')

    def test_clean_lf_unicode_spaces_and_missing_final_newline(self):
        self.put('source with spaces/Ubicaci\u00f3n.java', b'class A {}')
        self.put('launch.bat', b'@echo off\r\n')
        self.commit()
        self.assertEqual(1, audit(self.root))

    def test_committed_crlf_rejected_even_when_checkout_is_lf(self):
        self.put('A.java', b'class A {}\r\n', b'class A {}\n')
        self.commit()
        with self.assertRaisesRegex(RuntimeError, 'committed blob'):
            audit(self.root)

    def test_mixed_and_lone_cr_are_not_silent_passes(self):
        for value in (b'a\r\nb\nc\r\n', b'a\r\r\nb\n', b'a\rb'):
            with self.subTest(value=value):
                self.put('A.java', value, normalized(value))
                self.commit()
                with self.assertRaisesRegex(RuntimeError, 'committed blob'):
                    audit(self.root)

    def test_unstaged_byte_mutation_including_eol_only_is_rejected(self):
        self.put('A.java', b'class A {}\n')
        self.commit()
        for value in (b'class B {}\n', b'class A {}\r\n'):
            (self.root / 'A.java').write_bytes(value)
            with self.assertRaisesRegex(RuntimeError, 'checkout bytes'):
                audit(self.root)

    def test_staged_non_java_mutation_is_rejected(self):
        self.put('A.java', b'class A {}\n')
        self.commit()
        self.put('config.json', b'{}\n')
        with self.assertRaises(subprocess.CalledProcessError):
            audit(self.root)

    def test_no_java_is_not_success(self):
        self.commit()
        with self.assertRaisesRegex(RuntimeError, 'empty PASS'):
            audit(self.root)

    def test_exact_twelve_file_normalization_proof(self):
        value = b'package qa;\r\r\n// unchanged\r\nclass A {}'
        for name in NORMALIZED_PATHS:
            self.put(name, value)
        before = self.commit()
        for name in NORMALIZED_PATHS:
            self.put(name, normalized(value))
        after = self.commit()
        prove_normalization(self.root, before, after)
        self.assertEqual(12, audit(self.root))
        self.assertEqual(b'package qa;\n\n// unchanged\nclass A {}', normalized(value))

    def test_proof_rejects_logic_changes_and_extra_files(self):
        for name in NORMALIZED_PATHS:
            self.put(name, b'class A {}\r\n')
        before = self.commit()
        for name in NORMALIZED_PATHS:
            self.put(name, b'class A {}\n')
        self.put(sorted(NORMALIZED_PATHS)[0], b'class B {}\n')
        with self.assertRaisesRegex(RuntimeError, 'Non-line-ending'):
            prove_normalization(self.root, before, self.commit())
        self.put('unexpected.txt', b'extra')
        with self.assertRaisesRegex(RuntimeError, 'scope'):
            prove_normalization(self.root, before, self.commit())


if __name__ == '__main__':
    unittest.main()

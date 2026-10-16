"""Read-only LF and exact-checkout gate; optional immutable normalization proof."""
from __future__ import annotations
import argparse
import hashlib
from pathlib import Path
import re
import subprocess

NORMALIZED_NAMES = frozenset({
    'CategoryStyleRule', 'CatgisIcons', 'GeoJsonLoader', 'KmlLoader',
    'LayerLoader', 'OpenTopographyDemService', 'PinLayerBuilder',
    'PointSymbolCatalog', 'PointSymbolRenderer', 'ProjectCRSDialog',
    'TablePointLayerBuilder', 'VectorLayer',
})
NORMALIZED_PATHS = frozenset(
    'catgis-desktop/src/ar/com/catgis/' + name + '.java'
    for name in NORMALIZED_NAMES
)


def git(root: Path, *args: str, data: bytes | None = None) -> bytes:
    return subprocess.run(['git', '-C', str(root), *args], input=data,
                          stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                          check=True, timeout=60).stdout


def normalized(data: bytes) -> bytes:
    # CR CRLF denotes two logical Java line terminators, not one.
    return data.replace(b'\r\n', b'\n').replace(b'\r', b'\n')


def tree(root: Path, revision: str) -> dict[str, tuple[str, str]]:
    commit = git(root, 'rev-parse', '--verify', '--end-of-options',
                 revision + '^{commit}').strip().decode('ascii')
    entries = {}
    for entry in git(root, 'ls-tree', '-r', '-z', '--full-tree', commit).split(b'\0'):
        if not entry:
            continue
        metadata, name = entry.split(b'\t', 1)
        mode, kind, sha = metadata.decode('ascii').split()
        if kind == 'blob':
            entries[name.decode('utf-8')] = (mode, sha)
    return entries


def blobs(root: Path, shas: set[str]) -> dict[str, bytes]:
    ordered = sorted(shas)
    raw = git(root, 'cat-file', '--batch', data=''.join(s + '\n' for s in ordered).encode('ascii'))
    position = 0
    result = {}
    for expected in ordered:
        end = raw.index(b'\n', position)
        sha, kind, size_text = raw[position:end].decode('ascii').split()
        size = int(size_text)
        start = end + 1
        end = start + size
        if sha != expected or kind != 'blob' or raw[end:end + 1] != b'\n':
            raise RuntimeError('Invalid git blob response for ' + expected)
        value = raw[start:end]
        actual = hashlib.sha1(b'blob ' + str(len(value)).encode('ascii') + b'\0' + value).hexdigest()
        if actual != expected:
            raise RuntimeError('Git blob digest mismatch: ' + expected)
        result[sha] = value
        position = end + 1
    if position != len(raw):
        raise RuntimeError('Unexpected trailing git blob output')
    return result


def audit(root: Path) -> int:
    entries = {p: info for p, info in tree(root, 'HEAD').items() if p.endswith('.java')}
    if not entries:
        raise RuntimeError('No committed Java source: refusing an empty PASS')
    contents = blobs(root, {sha for _, sha in entries.values()})
    problems = []
    for name, (mode, sha) in sorted(entries.items()):
        path = root / name
        value = contents[sha]
        if mode not in {'100644', '100755'} or path.is_symlink() or not path.is_file():
            problems.append(name + ': not a regular Java source file')
        elif b'\r' in value:
            problems.append(name + ': committed blob contains CR/CRLF')
        elif path.read_bytes() != value:
            problems.append(name + ': checkout bytes differ from committed blob')
    if problems:
        raise RuntimeError('\n'.join(problems))
    # Keep BOTH unstaged and staged change checks. Never normalize or reset in CI.
    git(root, 'diff', '--exit-code')
    git(root, 'diff', '--cached', '--exit-code', 'HEAD')
    print('JAVA_SOURCE_INTEGRITY_PASS files=' + str(len(entries)), flush=True)
    return len(entries)


def prove_normalization(root: Path, before: str, after: str) -> None:
    if not all(re.fullmatch('[0-9a-f]{40}', value) for value in (before, after)):
        raise ValueError('The proof requires exact 40-character commit SHAs')
    old, new = tree(root, before), tree(root, after)
    changed = {p for p in old.keys() | new.keys() if old.get(p) != new.get(p)}
    if changed != NORMALIZED_PATHS:
        raise RuntimeError('Normalization scope is not exactly the twelve reviewed files: ' + repr(sorted(changed)))
    contents = blobs(root, {info[1] for p in changed for info in (old[p], new[p])})
    for name in sorted(changed):
        previous, current = contents[old[name][1]], contents[new[name][1]]
        if old[name][0] != new[name][0] or b'\r' not in previous or b'\r' in current or normalized(previous) != current:
            raise RuntimeError('Non-line-ending change or incomplete normalization: ' + name)
        print('EXACT_LF_TRANSFORM', name, old[name][1], new[name][1], flush=True)
    print('NORMALIZATION_ONLY_PASS files=12 before=' + before + ' after=' + after, flush=True)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--prove-normalization', nargs=2, metavar=('BEFORE_SHA', 'AFTER_SHA'))
    args = parser.parse_args()
    root = Path(git(Path.cwd(), 'rev-parse', '--show-toplevel').decode('utf-8').strip())
    if args.prove_normalization:
        prove_normalization(root, *args.prove_normalization)
    audit(root)


if __name__ == '__main__':
    main()

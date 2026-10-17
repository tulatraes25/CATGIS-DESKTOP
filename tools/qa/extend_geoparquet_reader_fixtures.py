"""Extend a freshly generated metadata fixture directory for the row-reader probe.

The original ten files and their manifest are never modified. New payloads are
written by Arrow, reopened independently and hashed. Only bad-page is deliberately
corrupted AFTER a correct checksum-bearing file has been independently reopened.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import struct
from pathlib import Path
import pyarrow as pa
import pyarrow.parquet as pq
from generate_geoparquet_fixtures import point, geo, with_geo


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', type=Path)
    root = parser.parse_args().directory
    baseline = json.loads((root / 'sha256.json').read_text(encoding='utf-8'))
    if len(baseline) != 10:
        raise ValueError('Expected the complete independent metadata fixture set.')
    for name, digest in baseline.items():
        if hashlib.sha256((root / name).read_bytes()).hexdigest() != digest:
            raise ValueError('Baseline fixture changed: ' + name)
    center = pq.read_table(root / 'multiple.parquet')['center'][0].as_py()
    print('INDEPENDENT_CENTER_WKB', center.hex(), 'XY', struct.unpack('<BIdd', center)[2:], flush=True)
    if center != point(0.5, 0.5):
        raise AssertionError('Independent center differs from the original declared (0.5,0.5) fixture.')

    def write(name: str, table: pa.Table, compression: str = 'snappy') -> None:
        path = root / name
        if path.exists():
            raise FileExistsError(path)
        pq.write_table(table, path, compression=compression, row_group_size=2,
                       use_dictionary=True, write_page_checksum=True, version='2.6')
        restored = pq.read_table(path)
        if not restored.equals(table, check_metadata=True):
            print('EXPECTED_SCHEMA', table.schema)
            print('OBSERVED_SCHEMA', restored.schema)
            raise AssertionError('Independent row/attribute/WKB mismatch: ' + name)
        # Arrow semantic equality considers -0.0 and +0.0 equal. Compare the
        # actual floating-point representation as a second independent oracle.
        for field in table.schema:
            if pa.types.is_float32(field.type) or pa.types.is_float64(field.type):
                pack = '<f' if pa.types.is_float32(field.type) else '<d'
                expected_values = table[field.name].to_pylist()
                observed_values = restored[field.name].to_pylist()
                for index, (expected, observed) in enumerate(zip(expected_values, observed_values)):
                    if expected is None:
                        if observed is not None:
                            raise AssertionError('Independent null mismatch: ' + name)
                    elif observed is None or struct.pack(pack, expected) != struct.pack(pack, observed):
                        raise AssertionError(f'Independent floating-point bits changed: {name}/{field.name}/{index}: '
                                             f'{expected!r} -> {observed!r}')
                if field.name == 'amplitude':
                    print('INDEPENDENT_AMPLITUDE_BITS', name,
                          [None if value is None else struct.pack(pack, value).hex() for value in observed_values], flush=True)
        print('ARROW_ROW_PROBE_FILE', name, table.num_rows, table.schema)

    typed = with_geo(pa.table({
        'shape': pa.array([point(12, 34), None, point(-2, 5)], type=pa.binary()),
        'counter': pa.array([9007199254740993, None, -7], type=pa.int64()),
        'small': pa.array([-2147483648, 0, 2147483647], type=pa.int32()),
        'enabled': pa.array([True, None, False], type=pa.bool_()),
        'level': pa.array([1.25, None, -2.5], type=pa.float32()),
        'amplitude': pa.array([-0.0, None, 3.125], type=pa.float64()),
        'label': pa.array(['\u00f1\u00e1rea', None, 'last'], type=pa.string()),
    }), geo('shape', ['Point']))
    for codec in ('snappy', 'gzip', 'uncompressed'):
        write('typed-' + codec + '.parquet', typed, 'NONE' if codec == 'uncompressed' else codec)

    for name, second in (
            ('bad-wkb.parquet', bytes([1, 3])),
            ('trailing-wkb.parquet', point(2, 3) + bytes([0]))):
        write(name, with_geo(pa.table({'shape': pa.array([point(0, 0), second], type=pa.binary())}),
                             geo('shape', ['Point'])))
    nested_type = pa.list_(pa.field('element', pa.int32()))
    nested = with_geo(pa.table({'shape': pa.array([point(0, 0)], type=pa.binary()),
                               'values': pa.array([[1, 2]], type=nested_type)}), geo('shape', ['Point']))
    write('nested.parquet', nested)
    write('zstd.parquet', typed, 'zstd')
    write('checksummed.parquet', typed, 'NONE')
    meta = pq.ParquetFile(root / 'checksummed.parquet').metadata.row_group(0).column(0)
    offsets = [p for p in (meta.dictionary_page_offset, meta.data_page_offset) if p is not None and p >= 4]
    data = bytearray((root / 'checksummed.parquet').read_bytes())
    damaged = min(offsets) + meta.total_compressed_size - 1
    if not 4 <= damaged < len(data) - 8:
        raise AssertionError('Invalid independent column-chunk span.')
    data[damaged] ^= 0x80
    bad_page = root / 'bad-page.parquet'
    if bad_page.exists():
        raise FileExistsError(bad_page)
    bad_page.write_bytes(data)
    try:
        pq.read_table(bad_page, page_checksum_verification=True)
    except (OSError, pa.ArrowException):
        pass
    else:
        raise AssertionError('Corruption did not fail independent page validation.')
    manifest = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(root.glob('*.parquet'))}
    if len(manifest) != 19:
        raise AssertionError('Unexpected fixture count: ' + str(len(manifest)))
    (root / 'probe-sha256.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    print('ROW_PROBE_FIXTURES', len(manifest), 'baseline-preserved', len(baseline))


if __name__ == '__main__':
    main()

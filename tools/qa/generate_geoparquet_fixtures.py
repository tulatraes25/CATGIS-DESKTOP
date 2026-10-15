"""Generate tiny real Parquet files with the independent Arrow C++ writer.

Only a NEW destination directory is accepted. No production inputs, network
services, binary fixtures in git or Java Parquet writer are involved.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct

import pyarrow as pa
import pyarrow.parquet as pq
from pyproj import CRS


def point(x: float, y: float) -> bytes:
    return struct.pack("<BIdd", 1, 1, x, y)


def rectangle(x1: float, y1: float, x2: float, y2: float) -> bytes:
    coords = [(x1, y1), (x2, y1), (x2, y2), (x1, y2), (x1, y1)]
    return struct.pack("<BIII", 1, 3, 1, len(coords)) + b"".join(
        struct.pack("<dd", x, y) for x, y in coords)


def geo(name: str, types: list[str], **extra: object) -> dict:
    return {"version": "1.1.0", "primary_column": name,
            "columns": {name: {"encoding": "WKB", "geometry_types": types, **extra}}}


def with_geo(table: pa.Table, metadata: dict) -> pa.Table:
    return table.replace_schema_metadata({
        b"geo": json.dumps(metadata, ensure_ascii=False, allow_nan=False).encode("utf-8")})


def write(root: Path, name: str, table: pa.Table) -> None:
    path = root / name
    pq.write_table(table, path, compression="snappy", row_group_size=2,
                   use_dictionary=True, version="2.6")
    restored = pq.read_table(path)
    if not restored.equals(table, check_metadata=True):
        raise AssertionError("Arrow round-trip changed real geometry bytes or typed/null attributes: " + name)
    if pq.ParquetFile(path).metadata.num_rows != table.num_rows:
        raise AssertionError("Wrong independent row count: " + name)
    print("ARROW_REAL_FILE", name, "rows=", table.num_rows,
          "columns=", table.column_names, "bytes=", path.stat().st_size)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    root = args.destination
    root.mkdir(parents=True, exist_ok=False)

    location = "ubicaci\u00f3n"
    points = pa.table({location: pa.array([point(-67.5, -45.8), None, point(-67.4, -45.7)], type=pa.binary()),
                       "nombre": pa.array(["\u00e1rea norte", None, "Sur"], type=pa.string()),
                       "valor": pa.array([1.25, None, -7.5], type=pa.float64())})
    points_geo = geo(location, ["Point"], bbox=[-67.5, -45.8, -67.4, -45.7])
    write(root, "points.parquet", with_geo(points, points_geo))

    polygons = pa.table({"outline": pa.array([rectangle(1000, 2000, 1100, 2100),
                                               rectangle(1200, 2200, 1300, 2500)], type=pa.binary()),
                         "nombre": pa.array(["Zona \u00f1", None], type=pa.string()),
                         "parcelas": pa.array([10, None], type=pa.int64())})
    projected = geo("outline", ["Polygon"], crs=CRS.from_epsg(3857).to_json_dict(),
                    bbox=[1000, 2000, 1300, 2500])
    projected["version"] = "1.0.0"
    write(root, "polygons.parquet", with_geo(polygons, projected))

    unknown = pa.table({"shape": pa.array([point(3, 4)], type=pa.binary()),
                        "value": pa.array([None], type=pa.int64())})
    write(root, "unknown.parquet", with_geo(unknown, geo("shape", ["Point"], crs=None)))
    empty = pa.table({"shape": pa.array([], type=pa.binary()), "nombre": pa.array([], type=pa.string())})
    write(root, "empty.parquet", with_geo(empty, geo("shape", [])))
    write(root, "plain.parquet", points)
    write(root, "missing-column.parquet", with_geo(points, geo("not_in_schema", ["Point"])))
    unsupported = geo(location, ["Point"])
    unsupported["columns"][location]["encoding"] = "point"
    write(root, "unsupported-encoding.parquet", with_geo(points, unsupported))
    write(root, "bad-geo-json.parquet", points.replace_schema_metadata({b"geo": b'{"version":'}))

    multiple = pa.table({"center": pa.array([point(0.5, 0.5)], type=pa.binary()),
                         "boundary": pa.array([rectangle(0, 0, 1, 1)], type=pa.binary())})
    multiple_geo = geo("boundary", ["Polygon"], bbox=[0, 0, 1, 1])
    multiple_geo["columns"]["center"] = {"encoding": "WKB", "geometry_types": ["Point"],
                                            "bbox": [0.5, 0.5, 0.5, 0.5]}
    write(root, "multiple.parquet", with_geo(multiple, multiple_geo))

    original = (root / "points.parquet").read_bytes()
    (root / "truncated.parquet").write_bytes(original[:-9])
    hashes = {path.name: hashlib.sha256(path.read_bytes()).hexdigest()
              for path in sorted(root.glob("*.parquet"))}
    (root / "sha256.json").write_text(json.dumps(hashes, indent=2) + "\n", encoding="utf-8")
    print("INDEPENDENT_GENERATOR", "pyarrow", pa.__version__, "fixtures", len(hashes))


if __name__ == "__main__":
    main()

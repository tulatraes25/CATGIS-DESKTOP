"""Read-only compiler/test/package identity checks; never infer JVM from setup-java."""
from __future__ import annotations

import argparse
import json
import re
import struct
import subprocess
from pathlib import Path
from xml.etree import ElementTree as ET

LIMIT = 1024 * 1024
SUITE = "ar.com.catgis.JavaRuntimeIdentityContractTest"
METHODS = {
    "actualTestJvmMatchesSelectedTarget",
    "productionClassVersionMatchesSelectedCompiler",
    "testClassVersionMatchesSelectedCompiler",
}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def feature(value: str) -> int:
    value = value.strip()
    if value.startswith('"') or value.endswith('"'):
        require(value.startswith('"') and value.endswith('"') and len(value) > 2, "Unbalanced version quotes")
        value = value[1:-1]
    match = re.fullmatch(r'([0-9]+)(?:[.+_-][0-9A-Za-z.+_-]+)?', value)
    require(match is not None, "Invalid Java version")
    return int(match.group(1))


def architecture(value: str) -> str:
    normalized = value.lower().strip()
    aliases = {"amd64": "x86_64", "x64": "x86_64", "x86_64": "x86_64",
               "arm64": "aarch64", "aarch64": "aarch64"}
    require(normalized in aliases, "Unsupported or missing architecture")
    return aliases[normalized]


def read_small(path: Path) -> bytes:
    with path.open("rb") as source:
        data = source.read(LIMIT + 1)
    require(len(data) <= LIMIT, "Evidence file exceeds byte limit")
    return data


def json_record(path: Path) -> dict:
    def unique(pairs):
        result = {}
        for key, value in pairs:
            require(key not in result, "Duplicate JSON key")
            result[key] = value
        return result
    result = json.loads(read_small(path).decode("utf-8"), object_pairs_hook=unique)
    require(isinstance(result, dict), "Evidence must be an object")
    return result


def verify_record(record: dict, role: str, expected: int) -> None:
    require(record.get("role") == role, "Incorrect evidence role")
    require(type(record.get("feature")) is int and record["feature"] == expected,
            "Actual JVM/toolchain differs from requested target")
    require(type(record.get("expected")) is int and record["expected"] == expected,
            "Evidence belongs to another requested target")


def class_major(path: Path) -> int:
    with path.open("rb") as source:
        header = source.read(8)
    require(len(header) == 8, "Missing or truncated class header")
    magic, minor, major = struct.unpack(">IHH", header)
    require(magic == 0xCAFEBABE and minor == 0, "Invalid or preview class bytecode")
    return major


def release_fields(data: bytes) -> dict:
    values = {}
    for line in data.decode("utf-8").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        require("=" in line, "Malformed runtime release metadata")
        key, value = line.split("=", 1)
        require(key not in values, "Duplicate runtime release key")
        value = value.strip()
        if value.startswith('"') or value.endswith('"'):
            require(value.startswith('"') and value.endswith('"') and len(value) >= 2,
                    "Unbalanced release value quotes")
            value = value[1:-1]
        values[key] = value
    return values


def verify_release(data: bytes, expected: int, arch: str) -> None:
    values = release_fields(data)
    require(feature(values.get("JAVA_VERSION", "")) == expected,
            "Packaged runtime version differs from selected target")
    require(architecture(values.get("OS_ARCH", "")) == architecture(arch),
            "Packaged runtime architecture differs from host")


def verify_junit(path: Path) -> None:
    data = read_small(path)
    require(b"<!DOCTYPE" not in data.upper() and b"<!ENTITY" not in data.upper(),
            "XML declarations with entities are forbidden")
    root = ET.fromstring(data.decode("utf-8"))
    require(root.tag == "testsuite" and root.get("name") == SUITE, "Unexpected JUnit suite")
    cases = list(root.findall("testcase"))
    names = [case.get("name") for case in cases]
    require(len(names) == len(METHODS) and set(names) == {name + "()" for name in METHODS},
            "Missing, duplicate or substituted identity tests")
    require(all(case.get("classname") == SUITE for case in cases), "Wrong test class")
    require(not any(root.findall(".//" + kind) for kind in ("failure", "error", "skipped")),
            "Identity tests did not all execute successfully")
    for key, expected in {"tests": 3, "failures": 0, "errors": 0, "skipped": 0}.items():
        require(root.get(key) == str(expected), "Inconsistent JUnit counter")



def verify_full_suite(directory: Path) -> dict:
    totals = {key: 0 for key in ("tests", "failures", "errors", "skipped")}
    reports = sorted(directory.glob("TEST-*.xml"))
    require(bool(reports), "Missing full-suite XML")
    names = set()
    for path in reports:
        data = read_small(path).decode("utf-8")
        require("<!DOCTYPE" not in data.upper() and "<!ENTITY" not in data.upper(), "Untrusted XML declaration")
        root = ET.fromstring(data)
        require(root.tag == "testsuite" and root.get("name") not in names, "Invalid or duplicate suite")
        names.add(root.get("name"))
        cases = root.findall("testcase")
        observed = {"tests": len(cases)}
        for key, node in (("failures", "failure"), ("errors", "error"), ("skipped", "skipped")):
            observed[key] = sum(case.find(node) is not None for case in cases)
        for key, value in observed.items():
            require(root.get(key) == str(value), "Full-suite counter/node mismatch")
            totals[key] += value
    require(totals["tests"] >= 700, "Full-suite count below existing minimum")
    require(totals["failures"] == 0 and totals["errors"] == 0, "Full-suite failures/errors")
    require(totals["skipped"] <= 15, "Reviewed dependency-skip budget exceeded")
    return totals


def git(root: Path, *args: str) -> str:
    result = subprocess.run(["git", "-C", str(root), *args], check=True,
                            capture_output=True, text=True, timeout=20)
    return result.stdout.strip()


def source_identity(root: Path) -> dict:
    git(root, "diff", "--exit-code", "--no-ext-diff", "--quiet")
    git(root, "diff", "--cached", "--exit-code", "--no-ext-diff", "--quiet")
    return {"head": git(root, "rev-parse", "HEAD"), "tree": git(root, "rev-parse", "HEAD^{tree}")}


def validate(root: Path, expected: int, os_name: str, arch: str, before: Path) -> dict:
    require(expected in (17, 21), "This experiment covers Java17 and Java21 only")
    require(json_record(before) == source_identity(root), "Tracked source changed during execution")
    project = root / "catgis-desktop"
    evidence = project / "build/runtime-identity"
    for filename, role in (("compileJava.json", "compiler"), ("test.json", "test-launcher"),
                           ("actual-test.json", "actual-test")):
        record = json_record(evidence / filename)
        verify_record(record, role, expected)
        if role == "actual-test":
            require(architecture(record.get("arch", "")) == architecture(arch), "Wrong test JVM architecture")
    for location in ("build/classes/java/main/ar/com/catgis/Main.class",
                     "build/classes/java/test/ar/com/catgis/JavaRuntimeIdentityContractTest.class"):
        require(class_major(project / location) == expected + 44, "Compiled class target mismatch")
    verify_junit(project / "build/test-results/test" / ("TEST-" + SUITE + ".xml"))
    counts = verify_full_suite(project / "build/test-results/test")
    homes = {
        "Linux": "build/installer/linux/app-image/CATGIS Desktop/lib/runtime",
        "Windows": "build/installer/windows/app-image/CATGIS Desktop/runtime",
        "macOS": "build/installer/macos/app-image/CATGIS Desktop.app/Contents/runtime/Contents/Home",
    }
    require(os_name in homes, "Unknown package OS")
    home = project / homes[os_name]
    verify_release(read_small(home / "release"), expected, arch)
    mode = "release_metadata_and_jvm_dll"
    if os_name == "Windows":
        with (home / "bin/server/jvm.dll").open("rb") as source:
            require(source.read(2) == b"MZ", "Missing real PE runtime DLL")
    else:
        result = subprocess.run([str(home / "bin/java"), "-XshowSettings:properties", "-version"],
                                capture_output=True, text=True, timeout=20, check=True)
        output = result.stdout + result.stderr
        require(len(output) <= 65536, "Unexpected runtime diagnostic output size")
        version = re.search(r"(?m)^\s*java.specification.version\s*=\s*(\d+)\s*$", output)
        host = re.search(r"(?m)^\s*os.arch\s*=\s*(\S+)\s*$", output)
        require(version is not None and int(version.group(1)) == expected, "Executed packaged Java is wrong")
        require(host is not None and architecture(host.group(1)) == architecture(arch), "Executed package architecture is wrong")
        mode = "executed_packaged_java_no_gui"
    require(json_record(before) == source_identity(root), "Source changed during evidence verification")
    return {"status": "PASS_BOUNDED_RUNTIME_IDENTITY_NOT_GUI", "expected": expected,
            "os": os_name, "arch": architecture(arch), "package_proof": mode, "junit": counts,
            **source_identity(root)}


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=("snapshot", "verify"))
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--snapshot", type=Path, required=True)
    parser.add_argument("--feature", type=int)
    parser.add_argument("--os")
    parser.add_argument("--arch")
    args = parser.parse_args()
    if args.operation == "snapshot":
        args.snapshot.write_text(json.dumps(source_identity(args.root)), encoding="utf-8")
    else:
        print(json.dumps(validate(args.root, args.feature, args.os, args.arch, args.snapshot), sort_keys=True))


if __name__ == "__main__":
    main()

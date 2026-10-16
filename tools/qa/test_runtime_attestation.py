"""Exercise the shipped attestation script with synthetic API responses, never live tokens."""
from __future__ import annotations

import base64
import contextlib
import copy
import io
import json
import os
from pathlib import Path
import re
import unittest
from unittest.mock import patch
from urllib.parse import parse_qs, urlparse

ROOT = Path(__file__).resolve().parents[2]
SHA = "a" * 40
RUNTIME_NAMES = {"Runtime identity " + host + str(jvm)
                 for host in ("Linux", "Windows", "MacIntel", "MacArm") for jvm in (17, 21)}
LEGACY_NAMES = {
    "ci.yml": {"Java Desktop Build & Test", "Repository Safety Checks", "Web Viewer Lint"},
    "certification.yml": {
        "Certification Linux Full Suite", "Certification Windows Full Suite",
        "PostGIS Real Integration", "Architecture Guardrails", "Visible Command Inventory",
        "Source Feature Surface Inventory", "Linux App Image Package Contract",
        "Windows App Image Package Contract", "Certification macOS Intel Full Suite + App Image",
        "Certification macOS Apple Silicon Full Suite + App Image", "Real Native GDAL Linux",
        "Real Native GDAL macOS Intel", "Real Native GDAL macOS Apple Silicon",
    },
    "geoparquet-metadata.yml": {"GeoParquet Independent Metadata Interop"},
}


def embedded_script() -> str:
    source = (ROOT / ".github/workflows/main-push-attestation.yml").read_text(encoding="utf-8")
    marker = "          python3 - <<'PY'\n"
    if source.count(marker) != 1:
        raise AssertionError("Expected exactly one shipped attestation script")
    body = source.split(marker, 1)[1].split("          PY\n", 1)[0]
    if not all(not line or line.startswith("          ") for line in body.splitlines()):
        raise AssertionError("Unexpected script indentation")
    return "\n".join(line[10:] for line in body.splitlines()) + "\n"


class FakeApi:
    def __init__(self, runtime: bool = True):
        self.names = {**LEGACY_NAMES}
        if runtime:
            self.names["runtime-identity.yml"] = RUNTIME_NAMES
        self.runs = {}
        self.jobs = {}
        self.seen = []
        for index, (workflow, names) in enumerate(self.names.items(), 1):
            self.runs[workflow] = [{"id": index, "head_sha": SHA, "event": "push",
                                    "head_branch": "main", "status": "completed",
                                    "conclusion": "success", "run_number": index, "run_attempt": 1,
                                    "html_url": f"https://github.com/example/test/actions/runs/{index}"}]
            self.jobs[index] = {"jobs": [{"name": name, "id": index * 100 + position,
                                          "status": "completed", "conclusion": "success"}
                                         for position, name in enumerate(sorted(names))]}

    def open(self, request, timeout):
        parsed = urlparse(request.full_url)
        if parsed.scheme != "https" or parsed.netloc != "api.github.com":
            raise AssertionError("Unexpected host")
        prefix = "/repos/example/test"
        if not parsed.path.startswith(prefix + "/"):
            raise AssertionError("Unexpected repository")
        self.seen.append(request.full_url)
        path = parsed.path[len(prefix):]
        query = parse_qs(parsed.query)
        if path == "/contents/.github/workflows/certification.yml":
            assert query == {"ref": [SHA]}
            source = "\n".join("  " + name + ":" for name in (
                "linux-appimage-package", "windows-appimage-package", "macos-full-suite-appimage",
                "native-gdal-real-roundtrip"))
            result = {"encoding": "base64", "content": base64.b64encode(source.encode()).decode()}
        elif path == "/contents/.github/workflows":
            assert query == {"ref": [SHA]}
            result = [{"name": name} for name in self.names]
        elif re.fullmatch(r"/actions/workflows/[^/]+/runs", path):
            assert query == {"head_sha": [SHA], "event": ["push"], "per_page": ["100"]}
            workflow = path.split("/")[-2]
            if workflow not in self.runs:
                raise AssertionError("Do not require a lane absent from protected main")
            result = {"workflow_runs": self.runs[workflow]}
        elif re.fullmatch(r"/actions/runs/\d+/attempts/1/jobs", path):
            assert query == {"per_page": ["100"]}
            result = self.jobs[int(path.split("/")[3])]
        else:
            raise AssertionError("Unexpected API path: " + path)
        return io.BytesIO(json.dumps(result).encode())


def execute(api: FakeApi, sha: str = SHA) -> tuple[int, str]:
    output = io.StringIO()
    code = compile(embedded_script(), "shipped-main-attestation", "exec")
    status = 0
    with patch.dict(os.environ, {"GITHUB_TOKEN": "SYNTHETIC_ONLY", "REPOSITORY": "example/test",
                                 "REQUIRED_MAIN_SHA": sha}, clear=True), \
            patch("urllib.request.urlopen", side_effect=api.open), \
            contextlib.redirect_stdout(output), contextlib.redirect_stderr(output):
        try:
            exec(code, {"__name__": "__main__"})
        except SystemExit as failure:
            status = failure.code if isinstance(failure.code, int) else 1
    return status, output.getvalue()


class RuntimeAttestationContractTest(unittest.TestCase):
    def test_all_existing_and_eight_runtime_jobs_are_required_and_pass(self):
        code, output = execute(FakeApi())
        self.assertEqual(0, code)
        for name in RUNTIME_NAMES | set.union(*LEGACY_NAMES.values()):
            self.assertIn("PASS job=" + name, output)

    def test_pre_introduction_main_is_not_retroactively_blocked(self):
        api = FakeApi(runtime=False)
        self.assertEqual(0, execute(api)[0])
        self.assertFalse(any("runtime-identity.yml/runs" in url for url in api.seen))

    def test_each_missing_runtime_platform_or_version_blocks(self):
        for name in RUNTIME_NAMES:
            with self.subTest(name=name):
                api = FakeApi()
                api.jobs[4]["jobs"] = [row for row in api.jobs[4]["jobs"] if row["name"] != name]
                self.assertEqual(1, execute(api)[0])

    def test_failure_cancel_skip_and_incomplete_jobs_cannot_count_as_pass(self):
        for status, result in (("completed", "failure"), ("completed", "cancelled"),
                               ("completed", "skipped"), ("in_progress", None)):
            with self.subTest(status=status, result=result):
                api = FakeApi()
                api.jobs[4]["jobs"][0].update(status=status, conclusion=result)
                self.assertEqual(1, execute(api)[0])

    def test_old_java17_matrix_alone_cannot_certify_java21_distribution(self):
        api = FakeApi()
        api.jobs[4]["jobs"] = [row for row in api.jobs[4]["jobs"]
                                 if row["name"].endswith("17") or row["name"] == "Runtime identity Linux21"]
        self.assertEqual(1, execute(api)[0])

    def test_duplicate_job_names_are_rejected_instead_of_overwriting_a_failure(self):
        api = FakeApi()
        duplicate = copy.deepcopy(api.jobs[4]["jobs"][0])
        duplicate["conclusion"] = "failure"
        api.jobs[4]["jobs"].insert(0, duplicate)
        self.assertEqual(1, execute(api)[0])

    def test_incomplete_paginated_job_inventory_is_not_accepted(self):
        api = FakeApi()
        api.jobs[4]["total_count"] = 101
        self.assertEqual(1, execute(api)[0])

    def test_matching_total_count_is_accepted(self):
        api = FakeApi()
        api.jobs[4]["total_count"] = 8
        self.assertEqual(0, execute(api)[0])

    def test_runtime_run_must_be_successful_completed_main_push_on_exact_sha(self):
        for key, value in (("head_sha", "b" * 40), ("event", "pull_request"),
                           ("head_branch", "feature"), ("status", "in_progress"),
                           ("conclusion", "failure"), ("conclusion", "cancelled")):
            with self.subTest(key=key):
                api = FakeApi()
                api.runs["runtime-identity.yml"][0][key] = value
                self.assertEqual(1, execute(api)[0])

    def test_runtime_workflow_without_a_qualifying_run_blocks(self):
        api = FakeApi()
        api.runs["runtime-identity.yml"] = []
        self.assertEqual(1, execute(api)[0])

    def test_old_required_native_and_metadata_jobs_are_not_weakened(self):
        for index in (1, 2, 3):
            with self.subTest(workflow=index):
                api = FakeApi()
                api.jobs[index]["jobs"].pop()
                self.assertEqual(1, execute(api)[0])

    def test_invalid_sha_is_rejected_before_any_network_read(self):
        api = FakeApi()
        self.assertEqual(1, execute(api, "main")[0])
        self.assertEqual([], api.seen)

    def test_workflow_keeps_read_only_permissions_and_does_not_checkout_pr_code(self):
        text = (ROOT / ".github/workflows/main-push-attestation.yml").read_text(encoding="utf-8")
        self.assertIn("  contents: read\n  actions: read", text)
        self.assertNotIn("actions/checkout", text)
        self.assertNotIn("pull_request_target", text)
        self.assertNotIn("contents: write", text)

    def test_runtime_matrix_has_all_eight_lanes_and_no_path_filter(self):
        text = (ROOT / ".github/workflows/runtime-identity.yml").read_text(encoding="utf-8")
        names = re.findall(r"- \{name: ([A-Za-z0-9]+),", text)
        self.assertEqual(8, len(names))
        self.assertEqual({name.removeprefix("Runtime identity ") for name in RUNTIME_NAMES}, set(names))
        self.assertNotIn("    paths:", text)
        self.assertIn("  push:\n    branches: [main]", text)
        self.assertIn("fail-fast: false", text)
        self.assertNotIn("continue-on-error", text)

    def test_declared_default_is_21_and_standard_bootstrap_agrees(self):
        properties = (ROOT / "catgis-desktop/gradle.properties").read_text(encoding="utf-8")
        self.assertEqual(["21"], re.findall(r"(?m)^catgisJavaVersion=(\d+)$", properties))
        for name in ("ci.yml", "certification.yml", "geoparquet-metadata.yml"):
            with self.subTest(workflow=name):
                text = (ROOT / ".github/workflows" / name).read_text(encoding="utf-8")
                versions = re.findall(r"java-version:\s*['\"]?(\d+)", text)
                self.assertTrue(versions)
                self.assertEqual({"21"}, set(versions))
        self.assertNotIn("JDK17 bundled", (ROOT / ".github/workflows/certification.yml").read_text())


if __name__ == "__main__":
    unittest.main(verbosity=2)

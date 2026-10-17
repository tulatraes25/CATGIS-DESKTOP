"""Offline contract tests for the executable attestation policy embedded in YAML."""
import re
import textwrap
import unittest
from pathlib import Path

START = "# BEGIN_ATTESTATION_BINDING_CONTRACT"
END = "# END_ATTESTATION_BINDING_CONTRACT"
MAIN = "228e30c60c330eedf8f918d3a4ffae9e660c953d"
OLD = "a2ff71e0060cf0145ffc7024532023ffeebecb3b"
HEAD = "ff73232d026139ceab45ce3f5213a3c628905cf1"
MOVED = "c1823b791a4e9e0280b31e351acbef958d671ae6"


def load_embedded_contract():
    workflow = (Path(__file__).resolve().parents[1] / "workflows"
                / "main-push-attestation.yml").read_text(encoding="utf-8")
    assert START in workflow and END in workflow, "missing actual production policy markers"
    extracted = workflow.split(START, 1)[1].split(END, 1)[0]
    namespace = {"re": re}
    exec(compile(textwrap.dedent(extracted), "attestation-policy", "exec"),
         namespace)
    return namespace


class MainPushAttestationPolicyTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        functions = load_embedded_contract()
        cls.verify = staticmethod(functions["attest_live_main_binding"])
        cls.eligible = staticmethod(functions["eligible_main_push_run"])

    def fixture(self):
        event = {"number": 7, "pull_request": {
            "base": {"sha": MAIN, "ref": "main"},
            "head": {"sha": HEAD}}}
        pr = {"number": 7, "state": "open", "base": {
            "sha": MAIN, "ref": "main"}, "head": {"sha": HEAD}}
        comparison = {"behind_by": 0, "ahead_by": 3}
        return event, pr, comparison

    def check_pr(self, event=None, pr=None, comparison=None, required=MAIN, live=MAIN):
        good = self.fixture()
        return self.verify("pull_request", event if event is not None else good[0],
                           required, live, pr if pr is not None else good[1],
                           comparison if comparison is not None else good[2], None, None)

    def test_exact_main_head_and_ancestry_succeeds(self):
        self.assertEqual(self.check_pr(), {
            "main_sha": MAIN, "head_sha": HEAD, "pr_number": 7})

    def test_rejects_prior_main_even_if_old_ci_passed(self):
        event, pr, compare = self.fixture()
        event["pull_request"]["base"]["sha"] = OLD
        with self.assertRaisesRegex(ValueError, "STALE_PR_BASE"):
            self.check_pr(event, pr, compare)

    def test_rejects_stale_required_main_sha(self):
        with self.assertRaisesRegex(ValueError, "STALE_MAIN_BASE"):
            self.check_pr(required=OLD)

    def test_rejects_stale_live_pr_base(self):
        event, pr, compare = self.fixture()
        pr["base"]["sha"] = OLD
        with self.assertRaisesRegex(ValueError, "STALE_PR_BASE"):
            self.check_pr(event, pr, compare)

    def test_rejects_moved_head_on_same_pr(self):
        event, pr, compare = self.fixture()
        pr["head"]["sha"] = MOVED
        with self.assertRaisesRegex(ValueError, "STALE_PR_HEAD"):
            self.check_pr(event, pr, compare)

    def test_rejects_head_without_live_main_in_ancestry(self):
        event, pr, compare = self.fixture()
        compare["behind_by"] = 1
        with self.assertRaisesRegex(ValueError, "PR_HEAD_NOT_DESCENDED"):
            self.check_pr(event, pr, compare)

    def test_rejects_another_target_branch(self):
        event, pr, compare = self.fixture()
        event["pull_request"]["base"]["ref"] = "another"
        with self.assertRaisesRegex(ValueError, "PR_BASE_BRANCH_NOT_MAIN"):
            self.check_pr(event, pr, compare)

    def test_allows_main_dispatch_for_recheck_on_same_sha(self):
        result = self.verify("workflow_dispatch", {}, MAIN, MAIN, None, None,
                             "refs/heads/main", MAIN)
        self.assertEqual(result["main_sha"], MAIN)
        self.assertIsNone(result["head_sha"])

    def test_dispatch_from_pr_branch_or_stale_sha_is_blocked(self):
        for ref, sha in [("refs/heads/feature", MAIN), ("refs/heads/main", OLD)]:
            with self.subTest(ref=ref, sha=sha):
                with self.assertRaisesRegex(ValueError, "DISPATCH_NOT_LIVE_MAIN"):
                    self.verify("workflow_dispatch", {}, MAIN, MAIN, None, None, ref, sha)

    def test_only_full_exact_completed_successful_main_push_is_eligible(self):
        good = {"head_sha": MAIN, "event": "push", "head_branch": "main",
                "status": "completed", "conclusion": "success"}
        self.assertTrue(self.eligible(good, MAIN))
        for change in [
            {"status": "queued"}, {"status": "in_progress"},
            {"conclusion": "failure"}, {"conclusion": "skipped"},
            {"event": "pull_request"}, {"head_branch": "feature"},
            {"head_sha": OLD}]:
            with self.subTest(change=change):
                self.assertFalse(self.eligible({**good, **change}, MAIN))


if __name__ == "__main__":
    unittest.main()

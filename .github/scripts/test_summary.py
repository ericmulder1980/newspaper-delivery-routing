#!/usr/bin/env python3
"""Writes a Markdown test summary from JUnit XML results to $GITHUB_STEP_SUMMARY (or stdout).

Groups results by module and top-level package (e.g. core · domain, core · data) and lists every
failing test with its message. Uses only the Python standard library.
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict

PACKAGE_ROOT = "nl.ericmulder.krantenwijk."


def group_name(path: str, class_name: str) -> str:
    module = path.split(os.sep)[0]
    name = class_name.split("[")[0]
    rest = name[len(PACKAGE_ROOT):] if name.startswith(PACKAGE_ROOT) else name
    area = rest.split(".")[0] if "." in rest else "(root)"
    return f"{module} · {area}"


def main() -> int:
    files = sorted(glob.glob("*/build/test-results/**/*.xml", recursive=True))
    totals = defaultdict(lambda: {"tests": 0, "failed": 0, "skipped": 0})
    failures = []

    for path in files:
        suite = ET.parse(path).getroot()
        for case in suite.iter("testcase"):
            # KMP suites drop the package from the suite name, so group on each case's class name.
            group = group_name(path, case.get("classname", ""))
            totals[group]["tests"] += 1
            problem = case.find("failure")
            if problem is None:
                problem = case.find("error")
            if problem is not None:
                totals[group]["failed"] += 1
                message = (problem.get("message") or "").strip().splitlines()
                failures.append((
                    case.get("classname", "").split(".")[-1],
                    case.get("name", "").replace("[jvm]", ""),
                    message[0] if message else "",
                ))
            elif case.find("skipped") is not None:
                totals[group]["skipped"] += 1

    tests = sum(t["tests"] for t in totals.values())
    failed = sum(t["failed"] for t in totals.values())
    skipped = sum(t["skipped"] for t in totals.values())

    lines = ["## Test results", ""]
    if tests == 0:
        lines.append("⚠️ No test results found. Did the build fail before the tests ran?")
    else:
        icon = "❌" if failed else "✅"
        lines.append(f"{icon} **{tests - failed - skipped} of {tests} tests passed**" + (f", {failed} failed" if failed else "") + (f", {skipped} skipped" if skipped else ""))
        lines += ["", "| Module · area | Tests | Failed | Skipped |", "|---|---:|---:|---:|"]
        for group in sorted(totals):
            t = totals[group]
            lines.append(f"| {group} | {t['tests']} | {t['failed']} | {t['skipped']} |")
        if failures:
            lines += ["", "### Failing tests", ""]
            for cls, name, message in failures:
                lines.append(f"- **{cls}** › {name}" + (f"  \n  `{message[:300]}`" if message else ""))

    output = "\n".join(lines) + "\n"
    target = os.environ.get("GITHUB_STEP_SUMMARY")
    if target:
        with open(target, "a", encoding="utf-8") as fh:
            fh.write(output)
    else:
        sys.stdout.write(output)
    return 0


if __name__ == "__main__":
    sys.exit(main())

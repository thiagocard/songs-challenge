#!/usr/bin/env python3
"""
Reads per-module Kover XML reports and prints a markdown coverage table.
Usage: python3 scripts/coverage_report.py [--output <file>]
"""

import xml.etree.ElementTree as ET
import os
import sys
import argparse

# Map of display name -> path to the module's report XML
MODULES = {
    ":core:common":           "core/common/build/reports/kover/report.xml",
    ":core:ui":               "core/ui/build/reports/kover/report.xml",
    ":core:navigation":       "core/navigation/build/reports/kover/report.xml",
    ":core:networking":       "core/networking/build/reports/kover/report.xml",
    ":core:database":         "core/database/build/reports/kover/report.xml",
    ":feature:home":          "feature/home/build/reports/kover/report.xml",
    ":feature:player":        "feature/player/build/reports/kover/report.xml",
}

def parse_coverage(xml_path: str) -> dict | None:
    """Parse a Kover XML report and return coverage counters."""
    if not os.path.exists(xml_path):
        return None
    try:
        root = ET.parse(xml_path).getroot()
    except ET.ParseError:
        return None

    result = {}
    for counter_type in ("INSTRUCTION", "BRANCH", "LINE", "METHOD", "CLASS"):
        el = root.find(f'counter[@type="{counter_type}"]')
        if el is not None:
            covered = int(el.get("covered", 0))
            missed  = int(el.get("missed", 0))
            total   = covered + missed
            result[counter_type] = (covered, total)
        else:
            result[counter_type] = None
    return result

def pct(covered: int, total: int) -> str:
    if total == 0:
        return "—"
    value = covered / total * 100
    if value == 100.0:
        return "100%"
    return f"{value:.1f}%"

def badge(value_str: str) -> str:
    """Prepend a colored emoji based on coverage percentage."""
    if value_str == "—":
        return f"⬜ {value_str}"
    try:
        v = float(value_str.rstrip("%"))
    except ValueError:
        return value_str
    if v >= 50:
        return f"🟢 {value_str}"
    if v >= 35:
        return f"🟡 {value_str}"
    return f"🔴 {value_str}"

def build_table(workspace_root: str = ".") -> str:
    rows = []
    for module, rel_path in MODULES.items():
        full_path = os.path.join(workspace_root, rel_path)
        data = parse_coverage(full_path)
        if data is None:
            rows.append((module, "—", "—"))
            continue

        instr  = data.get("INSTRUCTION")
        line   = data.get("LINE")

        rows.append((
            module,
            badge(pct(*instr))  if instr  else "—",
            badge(pct(*line))   if line   else "—",
        ))

    lines = [
        "## 📊 Test Coverage Report",
        "",
        "| Module | Instructions | Lines |",
        "|--------|:------------:|:-----:|",
    ]
    for module, instr, line in rows:
        lines.append(f"| `{module}` | {instr} | {line} |")

    # Aggregated total from root report
    root_path = os.path.join(workspace_root, "build/reports/kover/report.xml")
    root_data = parse_coverage(root_path)
    if root_data:
        instr  = root_data.get("INSTRUCTION")
        line   = root_data.get("LINE")
        lines.append(f"| **Total** | **{pct(*instr) if instr else '—'}** | **{pct(*line) if line else '—'}** |")

    lines.append("")
    lines.append(f"_Generated from Kover XML reports. 🟢 ≥50% · 🟡 ≥35% · 🔴 <35%_")
    return "\n".join(lines)

def main():
    parser = argparse.ArgumentParser(description="Generate a Kover coverage markdown table.")
    parser.add_argument("--output", "-o", default=None, help="Write output to file instead of stdout")
    parser.add_argument("--root", default=".", help="Workspace root directory (default: cwd)")
    args = parser.parse_args()

    table = build_table(workspace_root=args.root)

    if args.output:
        with open(args.output, "w") as f:
            f.write(table)
        print(f"Coverage table written to {args.output}")
    else:
        print(table)

if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
generate_config_docs.py

Scans all modules in SIP Framework project for generated
spring-configuration-metadata.json files and produces a consolidated
docs/configuration.md with all properties grouped by module and prefix.

Usage:
    python scripts/generate-config-docs.py
    python scripts/generate-config-docs.py --root /path/to/project
    python scripts/generate-config-docs.py --root . --output docs/configuration.md
"""

import argparse
import json
import os
import re
from collections import defaultdict
from datetime import datetime
from pathlib import Path


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

METADATA_RELATIVE = Path("target") / "classes" / "META-INF" / "spring-configuration-metadata.json"


def find_metadata_files(root: Path) -> list[tuple[str, Path]]:
    """
    Return a list of (module_name, metadata_path) for every module that has a
    generated metadata file. Modules without the file are silently skipped.
    """
    results = []
    for candidate in sorted(root.iterdir()):
        meta = candidate / METADATA_RELATIVE
        if candidate.is_dir() and meta.exists():
            results.append((candidate.name, meta))
    return results


def load_metadata(path: Path) -> dict:
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def short_type(java_type: str | None) -> str:
    """Strip the package prefix from a Java type for readability."""
    if not java_type:
        return ""
    return java_type.split(".")[-1]


def is_deprecated(prop: dict) -> bool:
    """A property is deprecated if it has a non-null 'deprecation' key."""
    return prop.get("deprecation") is not None


def deprecation_badge(prop: dict) -> str:
    """Return a markdown badge/note for deprecated properties."""
    dep = prop.get("deprecation", {})
    if not dep:
        return ""
    parts = ["⚠️ **Deprecated**"]
    reason = dep.get("reason")
    replacement = dep.get("replacement")
    if reason:
        parts.append(f"— {reason}")
    if replacement:
        parts.append(f"Use `{replacement}` instead.")
    return " ".join(parts)


def anchor(text: str) -> str:
    """Convert a heading to a GitHub-compatible markdown anchor."""
    text = text.lower()
    text = re.sub(r"[^a-z0-9\s-]", "", text)
    text = re.sub(r"\s+", "-", text.strip())
    return text


def format_default(value) -> str:
    if value is None:
        return ""
    if isinstance(value, bool):
        return str(value).lower()
    return str(value)


# ---------------------------------------------------------------------------
# Markdown rendering
# ---------------------------------------------------------------------------

def render_property_table(properties: list[dict]) -> list[str]:
    lines = []
    lines.append("| Property | Type | Default | Description |")
    lines.append("|----------|------|---------|-------------|")
    for prop in properties:
        name = f"`{prop.get('name', '')}`"
        ptype = short_type(prop.get("type"))
        default = f"`{format_default(prop['defaultValue'])}`" if prop.get("defaultValue") is not None else ""
        desc = prop.get("description", "").replace("\n", " ").replace("|", "\\|")

        dep = deprecation_badge(prop)
        if dep:
            desc = f"{dep}<br>{desc}" if desc else dep

        lines.append(f"| {name} | {ptype} | {default} | {desc} |")
    return lines


def render_module_section(module_name: str, metadata: dict) -> list[str]:
    """Render one H2 section for a module, with H3 sub-sections per prefix group."""
    properties: list[dict] = metadata.get("properties", [])
    groups: list[dict] = metadata.get("groups", [])

    if not properties:
        return []

    # Build a lookup: prefix -> group display name (sourceType or name)
    group_names: dict[str, str] = {}
    for g in groups:
        group_names[g["name"]] = g.get("sourceType") or g["name"]

    # Bucket properties by their top-level prefix (first two segments)
    prefix_map: dict[str, list[dict]] = defaultdict(list)
    for prop in sorted(properties, key=lambda p: p.get("name", "")):
        name = prop.get("name", "")
        # Find the longest matching group prefix
        matched_prefix = ""
        for gname in group_names:
            if name.startswith(gname) and len(gname) > len(matched_prefix):
                matched_prefix = gname
        if not matched_prefix:
            # Fall back to first two dot-separated segments
            parts = name.split(".")
            matched_prefix = ".".join(parts[:2]) if len(parts) >= 2 else parts[0]
        prefix_map[matched_prefix].append(prop)

    lines = []
    lines.append(f"## {module_name}")
    lines.append("")

    for prefix in sorted(prefix_map.keys()):
        props = prefix_map[prefix]
        display = group_names.get(prefix, prefix)
        lines.append(f"### `{prefix}`")
        lines.append("")
        if display != prefix:
            lines.append(f"*Defined in `{display}`*")
            lines.append("")

        # Deprecation summary callout if ALL props in the group are deprecated
        all_deprecated = all(is_deprecated(p) for p in props)
        if all_deprecated:
            lines.append("> ⚠️ All properties in this group are deprecated.")
            lines.append("")

        lines.extend(render_property_table(props))
        lines.append("")

    return lines


def build_toc(modules_with_content: list[tuple[str, list[str]]]) -> list[str]:
    lines = ["## Table of Contents", ""]
    for module_name, _ in modules_with_content:
        lines.append(f"- [{module_name}](#{anchor(module_name)})")
    lines.append("")
    return lines


def render_document(modules_with_content: list[tuple[str, list[str]]]) -> str:
    now = datetime.now().strftime("%Y-%m-%d %H:%M")
    header = [
        "# SIP Foundation Configuration Properties",
        "",
        "> Auto-generated from `spring-configuration-metadata.json`."
        f" Last updated: {now}.",
        "",
        "---",
        "",
    ]

    toc = build_toc(modules_with_content)

    body = []
    for idx, (_, section_lines) in enumerate(modules_with_content):
        body.extend(section_lines)
        if idx < len(modules_with_content) - 1:
            body.append("---")
            body.append("")

    all_lines = header + toc + body
    return "\n".join(all_lines) + "\n"


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="Generate consolidated Spring Boot config docs.")
    parser.add_argument("--root", default=".", help="Project root directory (default: current directory)")
    parser.add_argument("--output", default="docs/configuration.md", help="Output markdown file path")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    output_path = root / args.output

    print(f"🔍  Scanning modules under: {root}")

    metadata_files = find_metadata_files(root)

    if not metadata_files:
        print("⚠️  No spring-configuration-metadata.json files found.")
        print("   Make sure you have run `mvn package` first.")
        return

    modules_with_content: list[tuple[str, list[str]]] = []

    for module_name, meta_path in metadata_files:
        metadata = load_metadata(meta_path)
        section = render_module_section(module_name, metadata)
        if section:
            modules_with_content.append((module_name, section))
            prop_count = len(metadata.get("properties", []))
            print(f"   ✅  {module_name} — {prop_count} properties")
        # modules with no properties are silently skipped

    if not modules_with_content:
        print("ℹ️  No properties found across any module. Nothing to write.")
        return

    output_path.parent.mkdir(parents=True, exist_ok=True)
    document = render_document(modules_with_content)
    output_path.write_text(document, encoding="utf-8")

    print(f"\n✨  Written to: {output_path}")


if __name__ == "__main__":
    main()
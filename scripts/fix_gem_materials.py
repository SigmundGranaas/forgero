#!/usr/bin/env python3
"""
Script to add upgrade_material role tag to gem materials that are missing it.
"""

import json
import os
from pathlib import Path

# Directory to process
GEM_MATERIALS_DIR = "content/forgero-gems/src/main/resources/data/forgero/materials"

UPGRADE_MATERIAL_TAG = "forgero:materials/roles/upgrade_material"

def fix_gem_material(file_path: Path) -> bool:
    """
    Add upgrade_material tag to gem material if missing.
    Returns True if file was modified.
    """
    with open(file_path, 'r') as f:
        data = json.load(f)

    # Check if it's a material type
    if data.get("type") != "forgero:material":
        return False

    # Get tags list
    tags = data.get("tags", [])

    # Check if already has upgrade_material
    if UPGRADE_MATERIAL_TAG in tags:
        print(f"  Skipping (already has upgrade_material): {file_path.name}")
        return False

    # Add upgrade_material tag
    tags.append(UPGRADE_MATERIAL_TAG)
    data["tags"] = tags

    # Write back with nice formatting
    with open(file_path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')  # Add trailing newline

    print(f"  Added upgrade_material to: {file_path.name}")
    return True

def main():
    # Get project root (parent of scripts directory)
    project_root = Path(__file__).parent.parent

    dir_path = project_root / GEM_MATERIALS_DIR

    if not dir_path.exists():
        print(f"Directory not found: {dir_path}")
        return

    total_modified = 0
    total_skipped = 0

    print(f"Processing: {GEM_MATERIALS_DIR}")

    # Find all .json files
    for material_file in dir_path.rglob("*.json"):
        if material_file.is_file():
            if fix_gem_material(material_file):
                total_modified += 1
            else:
                total_skipped += 1

    print(f"\n{'='*50}")
    print(f"Total modified: {total_modified}")
    print(f"Total skipped: {total_skipped}")

if __name__ == "__main__":
    main()

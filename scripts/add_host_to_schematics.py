#!/usr/bin/env python3
"""
Script to add host.create field to new format schematics that are missing it.
"""

import json
import os
from pathlib import Path

# Directories to process (relative to project root)
SCHEMATIC_DIRS = [
    "content/forgero-schematics/src/main/resources/data/forgero/schematics",
    "content/forgero-extended/src/main/resources/data/forgero/schematics",
    "content/forgero-extended-weapons/src/main/resources/data/forgero/schematics",
    "content/forgero-mining/src/main/resources/data/forgero/schematics",
]

def get_schematic_id(filename: str) -> str:
    """
    Convert filename to schematic ID.
    Example: rapier_blade_schematic.json -> forgero:rapier_blade-schematic
    """
    # Remove .json extension
    name = filename.replace(".json", "")
    # Replace _schematic with -schematic
    name = name.replace("_schematic", "-schematic")
    return f"forgero:{name}"

def add_host_to_schematic(file_path: Path) -> bool:
    """
    Add host.create field to schematic if missing.
    Returns True if file was modified.
    """
    with open(file_path, 'r') as f:
        data = json.load(f)

    # Check if it's a forgero:schematic type
    if data.get("type") != "forgero:schematic":
        return False

    # Check if host field already exists
    if "host" in data:
        print(f"  Skipping (already has host): {file_path.name}")
        return False

    # Generate schematic ID from filename
    schematic_id = get_schematic_id(file_path.name)

    # Add host.create field
    data["host"] = {
        "create": {
            "id": schematic_id,
            "class_name": "forgero:part_item",
            "item_group": "forgero:ingredients"
        }
    }

    # Write back with nice formatting
    with open(file_path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write('\n')  # Add trailing newline

    print(f"  Added host to: {file_path.name} -> {schematic_id}")
    return True

def main():
    # Get project root (parent of scripts directory)
    project_root = Path(__file__).parent.parent

    total_modified = 0
    total_skipped = 0

    for schematic_dir in SCHEMATIC_DIRS:
        dir_path = project_root / schematic_dir

        if not dir_path.exists():
            print(f"Directory not found: {dir_path}")
            continue

        print(f"\nProcessing: {schematic_dir}")

        # Find all *_schematic.json files
        for schematic_file in dir_path.rglob("*_schematic.json"):
            if schematic_file.is_file():
                if add_host_to_schematic(schematic_file):
                    total_modified += 1
                else:
                    total_skipped += 1

    print(f"\n{'='*50}")
    print(f"Total modified: {total_modified}")
    print(f"Total skipped: {total_skipped}")

if __name__ == "__main__":
    main()

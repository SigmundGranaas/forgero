#!/usr/bin/env python3
"""
Script to rename JSON files from underscore format to hyphen format.
"""

import os
from pathlib import Path

# Directory to process
PARTS_DIR = "content/vanilla-upgrades-base/src/main/resources/data/forgero/parts"

def rename_files(dir_path: Path) -> int:
    """
    Rename all .json files from underscore to hyphen format.
    Returns count of renamed files.
    """
    count = 0

    for json_file in dir_path.rglob("*.json"):
        if "_" in json_file.name:
            old_name = json_file.name
            new_name = old_name.replace("_", "-")
            new_path = json_file.parent / new_name

            json_file.rename(new_path)
            print(f"  Renamed: {old_name} -> {new_name}")
            count += 1

    return count

def main():
    # Get project root (parent of scripts directory)
    project_root = Path(__file__).parent.parent

    dir_path = project_root / PARTS_DIR

    if not dir_path.exists():
        print(f"Directory not found: {dir_path}")
        return

    print(f"Processing: {PARTS_DIR}")

    count = rename_files(dir_path)

    print(f"\n{'='*50}")
    print(f"Total files renamed: {count}")

if __name__ == "__main__":
    main()

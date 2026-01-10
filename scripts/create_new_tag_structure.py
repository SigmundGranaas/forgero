#!/usr/bin/env python3
"""
Creates new path-based tag directory structure and files.
Reads from tag_migration_mapping.json and creates new tag files
in the appropriate content modules with proper parent relationships.
"""

import json
import os
from pathlib import Path
from collections import defaultdict


def load_existing_tags(project_root: Path) -> dict:
    """Load all existing tag files to preserve their data."""
    content_dir = project_root / "content"
    existing_tags = {}

    for module_dir in content_dir.iterdir():
        if not module_dir.is_dir():
            continue

        tags_dir = module_dir / "src" / "main" / "resources" / "data" / "forgero" / "tags"
        if not tags_dir.exists():
            continue

        # Find all JSON files
        for tag_file in tags_dir.rglob("*.json"):
            try:
                with open(tag_file, 'r') as f:
                    tag_data = json.load(f)

                # Get tag ID from path
                rel_path = tag_file.relative_to(tags_dir)
                tag_name = str(rel_path.with_suffix('')).split('/')[-1]
                tag_id = f"forgero:{tag_name}"

                existing_tags[tag_id] = {
                    "data": tag_data,
                    "module": module_dir.name,
                    "old_path": str(tag_file.relative_to(project_root))
                }

            except Exception as e:
                print(f"Warning: Could not load {tag_file}: {e}")

    return existing_tags


def determine_module_for_tag(new_name: str, old_module: str) -> str:
    """Determine which content module should contain a tag based on its new path."""

    # Extract category from new path
    path_parts = new_name.replace("forgero:", "").split("/")

    if len(path_parts) == 1:
        # Root tags - keep in their original modules or move to base
        category = path_parts[0]
    else:
        category = path_parts[0]

    # Module assignment based on category
    module_mapping = {
        "materials": "forgero-base",
        "parts": "forgero-base",
        "upgrades": "forgero-base",
        "tools": "forgero-base",
        "armors": "forgero-armor-content",
        "armor": "forgero-armor-content",
        "weapons": "forgero-extended-weapons",
        "blocks": "forgero-extended",
        "schematics": "forgero-base",
        "uncategorized": "forgero-base"
    }

    return module_mapping.get(category, old_module)


def get_parent_tags(new_name: str) -> list:
    """
    Determine parent tags for a tag based on its new path.
    E.g., forgero:materials/types/metal -> parent is forgero:materials
    """
    path_parts = new_name.replace("forgero:", "").split("/")

    if len(path_parts) == 1:
        # Root tag - no parent
        return []
    elif len(path_parts) == 2:
        # Direct child of root (e.g., materials/types)
        # Parent is the root category
        root = path_parts[0]
        return [f"forgero:{root}"]
    else:
        # Deeper nesting (e.g., materials/types/metal)
        # Parent is the immediate parent path
        parent_path = "/".join(path_parts[:-1])
        return [f"forgero:{parent_path}"]


def create_tag_file(tag_id: str, new_name: str, existing_data: dict, output_path: Path):
    """Create a new tag JSON file with proper parent relationships."""

    # Preserve description from old tag
    old_tag_data = existing_data.get("data", {})
    description = old_tag_data.get("description", "")

    # Determine parent tags based on new path
    parents = get_parent_tags(new_name)

    # Build new tag structure
    new_tag_data = {}

    if parents:
        new_tag_data["parents"] = parents

    if description:
        new_tag_data["description"] = description
    elif not parents:
        # Root tags should have descriptions
        category = new_name.replace("forgero:", "")
        new_tag_data["description"] = f"Root tag for all {category} items"
    else:
        # Intermediate category tags should have descriptions
        path_parts = new_name.replace("forgero:", "").split("/")
        if len(path_parts) >= 2:
            # E.g., "forgero:materials/types" -> "Material type classifications"
            category = path_parts[0].replace("_", " ").title()
            subcategory = path_parts[-1].replace("_", " ")
            new_tag_data["description"] = f"{category} {subcategory}"

    # Preserve other fields if needed (values, etc.)
    # For now, we don't preserve "values" as they'll be regenerated

    # Ensure output directory exists
    output_path.parent.mkdir(parents=True, exist_ok=True)

    # Write the new tag file
    with open(output_path, 'w') as f:
        json.dump(new_tag_data, f, indent=2)
        f.write('\n')  # Add trailing newline


def collect_intermediate_tags(unique_new_tags: dict) -> set:
    """Collect all intermediate path tags that need to be created."""
    intermediate_tags = set()

    for new_name in unique_new_tags.keys():
        path_parts = new_name.replace("forgero:", "").split("/")

        # For paths with 3+ parts, we need intermediate tags
        # E.g., forgero:materials/types/metal needs forgero:materials/types
        if len(path_parts) >= 3:
            for i in range(2, len(path_parts)):
                intermediate_path = "/".join(path_parts[:i])
                intermediate_tag = f"forgero:{intermediate_path}"
                intermediate_tags.add(intermediate_tag)

    return intermediate_tags


def main():
    # Find project root
    script_dir = Path(__file__).parent
    project_root = script_dir.parent

    # Load migration mapping
    mapping_file = script_dir / "tag_migration_mapping.json"
    with open(mapping_file, 'r') as f:
        tag_mapping = json.load(f)

    # Load existing tag data
    print("Loading existing tag files...")
    existing_tags = load_existing_tags(project_root)
    print(f"Loaded {len(existing_tags)} existing tags")

    # Group tags by new name (handle duplicates)
    unique_new_tags = {}
    for old_name, tag_info in tag_mapping.items():
        new_name = tag_info["new_name"]
        if new_name not in unique_new_tags:
            unique_new_tags[new_name] = {
                "old_name": old_name,
                "category": tag_info["category"],
                "existing_data": existing_tags.get(old_name, {}),
                "old_module": existing_tags.get(old_name, {}).get("module", "forgero-base")
            }

    # Collect intermediate tags that need to be created
    intermediate_tags = collect_intermediate_tags(unique_new_tags)
    print(f"Found {len(intermediate_tags)} intermediate category tags to create")

    # Add intermediate tags to the set
    for intermediate_tag in intermediate_tags:
        if intermediate_tag not in unique_new_tags:
            # Determine module and category for intermediate tag
            path_parts = intermediate_tag.replace("forgero:", "").split("/")
            category = path_parts[0]

            unique_new_tags[intermediate_tag] = {
                "old_name": intermediate_tag,
                "category": f"{category}/intermediate",
                "existing_data": {},
                "old_module": "forgero-base"
            }

    print(f"\nCreating {len(unique_new_tags)} total tag files (including intermediate)...")

    # Create new tag files
    created_count = 0
    for new_name, info in unique_new_tags.items():
        # Determine which module this tag should go in
        module_name = determine_module_for_tag(new_name, info["old_module"])

        # Build output path
        tag_path = new_name.replace("forgero:", "")
        output_path = project_root / "content" / module_name / "src" / "main" / "resources" / "data" / "forgero" / "tags" / f"{tag_path}.json"

        # Create the tag file
        try:
            create_tag_file(
                info["old_name"],
                new_name,
                info["existing_data"],
                output_path
            )
            created_count += 1

            if created_count % 10 == 0:
                print(f"  Created {created_count} tags...")

        except Exception as e:
            print(f"Error creating {new_name}: {e}")

    print(f"\n✓ Created {created_count} new tag files")

    # Print summary by module
    module_counts = defaultdict(int)
    for new_name, info in unique_new_tags.items():
        module_name = determine_module_for_tag(new_name, info["old_module"])
        module_counts[module_name] += 1

    print("\nTags created per module:")
    for module, count in sorted(module_counts.items()):
        print(f"  {module}: {count} tags")


if __name__ == "__main__":
    main()

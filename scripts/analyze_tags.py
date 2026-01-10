#!/usr/bin/env python3
"""
Analyzes all Forgero tag JSON files and generates a migration mapping
from flat naming to path-based naming.
"""

import json
import os
from pathlib import Path
from typing import Dict, Set, Tuple
from collections import defaultdict


def get_tag_id_from_path(file_path: Path, content_root: Path) -> str:
    """
    Extract tag ID from file path.
    E.g., content/forgero-base/src/main/resources/data/forgero/tags/materials/wood.json
    -> forgero:wood
    """
    # Get relative path from data/forgero/tags/
    try:
        rel_path = file_path.relative_to(content_root / "data" / "forgero" / "tags")
        # Remove .json extension and convert path to ID
        tag_name = str(rel_path.with_suffix(''))
        # Replace path separators with namespace separator for nested paths
        # But keep the final component as the tag name
        parts = Path(tag_name).parts
        if len(parts) > 1:
            # For now, just use the filename as the tag name (flat structure)
            tag_name = parts[-1]
        return f"forgero:{tag_name}"
    except ValueError:
        return None


def categorize_tag(tag_id: str, file_path: Path, tag_data: dict) -> Tuple[str, str]:
    """
    Categorize a tag and determine its new path-based name.
    Returns (category, new_name)

    Categories:
    - materials/types/ - Material types (metal, wood, stone, etc.)
    - materials/roles/ - Material roles (tool_material, armor_material, etc.)
    - materials/properties/ - Material properties (hard, soft, magical, etc.)
    - parts/categories/ - Part categories (head, handle, blade, etc.)
    - parts/types/ - Part types (pickaxe_head, sword_blade, etc.)
    - parts/shapes/ - Part shapes
    - upgrades/types/ - Upgrade types (gem, reinforcement, cosmetic, etc.)
    - tools/types/ - Tool types (pickaxe, sword, axe, etc.)
    - weapons/types/ - Weapon types (katana, dagger, etc.)
    - armor/types/ - Armor types (helmet, chestplate, etc.)
    - schematics/categories/ - Schematic categories
    - blocks/categories/ - Block categories
    """

    tag_name = tag_id.replace("forgero:", "")
    path_parts = file_path.parts

    # Root category tags - keep as-is (no path change)
    ROOT_TAGS = {
        "materials": "forgero:materials",
        "parts": "forgero:parts",
        "upgrades": "forgero:upgrades",
        "tools": "forgero:tools",
        "armors": "forgero:armors",
        "weapons": "forgero:weapons"
    }

    if tag_name in ROOT_TAGS:
        return ("ROOT", ROOT_TAGS[tag_name])

    # Special cases: consolidate singular/plural variants to plural (root tags)
    if tag_name == "armor":
        return ("ROOT", "forgero:armors")
    if tag_name == "tool":
        return ("ROOT", "forgero:tools")

    # Determine category from file path
    tags_index = path_parts.index("tags")
    if tags_index + 1 < len(path_parts):
        category_dir = path_parts[tags_index + 1]
    else:
        category_dir = "unknown"

    # Material tags
    if category_dir == "materials" or "material" in tag_name:
        # Check if it's a property, role, or type
        if tag_name in ["hard", "soft", "magical", "hybrid"]:
            return ("materials/properties", f"forgero:materials/properties/{tag_name}")
        elif "material" in tag_name and tag_name != "material":
            # Role-based materials (tool_material, armor_lining_material, etc.)
            return ("materials/roles", f"forgero:materials/roles/{tag_name}")
        elif tag_name == "secondary":
            return ("materials/roles", f"forgero:materials/roles/{tag_name}")
        else:
            # Type-based materials (metal, wood, stone, etc.)
            return ("materials/types", f"forgero:materials/types/{tag_name}")

    # Part tags
    elif category_dir == "parts":
        # Generic part categories vs specific part types
        if tag_name in ["head", "handle", "blade", "binding", "guard", "pommel"]:
            return ("parts/categories", f"forgero:parts/categories/{tag_name}")
        elif "type" in tag_name:
            return ("parts/types", f"forgero:parts/types/{tag_name}")
        elif "default_" in tag_name:
            return ("parts/defaults", f"forgero:parts/defaults/{tag_name}")
        else:
            # Specific part types (pickaxe_head, sword_blade, etc.)
            return ("parts/types", f"forgero:parts/types/{tag_name}")

    # Shape tags
    elif category_dir == "shape":
        return ("parts/shapes", f"forgero:parts/shapes/{tag_name}")

    # Upgrade tags
    elif category_dir == "upgrade" or "upgrade" in tag_name:
        return ("upgrades/types", f"forgero:upgrades/types/{tag_name}")

    # Tool tags
    elif category_dir == "tools" or tag_name == "tool":
        if tag_name in ["pickaxe", "axe", "shovel", "hoe", "sword"]:
            return ("tools/types", f"forgero:tools/types/{tag_name}")
        elif tag_name == "tool":
            return ("tools", "forgero:tools")
        elif tag_name == "mining":
            return ("tools/categories", f"forgero:tools/categories/{tag_name}")
        else:
            return ("tools/types", f"forgero:tools/types/{tag_name}")

    # Weapon tags
    elif category_dir == "weapons":
        if tag_name == "weapons":
            return ("weapons", "forgero:weapons")
        elif tag_name in ["extended", "swordlike"]:
            return ("weapons/categories", f"forgero:weapons/categories/{tag_name}")
        else:
            return ("weapons/types", f"forgero:weapons/types/{tag_name}")

    # Armor tags
    elif category_dir == "armor":
        if tag_name == "armor":
            return ("armor", "forgero:armors")
        else:
            return ("armor/types", f"forgero:armor/types/{tag_name}")

    # Schematic tags
    elif category_dir == "schematics":
        return ("schematics/categories", f"forgero:schematics/categories/{tag_name}")

    # Block tags
    elif category_dir == "blocks":
        return ("blocks/categories", f"forgero:blocks/categories/{tag_name}")

    # Default fallback
    else:
        return ("uncategorized", f"forgero:uncategorized/{tag_name}")


def main():
    # Find project root
    script_dir = Path(__file__).parent
    project_root = script_dir.parent

    # Find all content modules
    content_dir = project_root / "content"

    all_tags = {}
    categories = defaultdict(list)
    orphaned_tags = []

    # Scan all content modules
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
                content_root = module_dir / "src" / "main" / "resources"
                tag_id = get_tag_id_from_path(tag_file, content_root)

                if not tag_id:
                    continue

                # Categorize and determine new name
                category, new_name = categorize_tag(tag_id, tag_file, tag_data)

                all_tags[tag_id] = {
                    "old_name": tag_id,
                    "new_name": new_name,
                    "category": category,
                    "file_path": str(tag_file.relative_to(project_root)),
                    "parents": tag_data.get("parents", []),
                    "description": tag_data.get("description", "")
                }

                categories[category].append(tag_id)

                # Track orphaned tags
                if not tag_data.get("parents"):
                    orphaned_tags.append(tag_id)

            except Exception as e:
                print(f"Error processing {tag_file}: {e}")

    # Generate report
    print("=" * 80)
    print("TAG MIGRATION MAPPING")
    print("=" * 80)
    print(f"\nTotal tags found: {len(all_tags)}")
    print(f"Orphaned tags: {len(orphaned_tags)}")
    print(f"\nCategories: {len(categories)}")

    # Print by category
    for category in sorted(categories.keys()):
        print(f"\n{category.upper()} ({len(categories[category])} tags):")
        for tag_id in sorted(categories[category]):
            tag_info = all_tags[tag_id]
            print(f"  {tag_info['old_name']:40} -> {tag_info['new_name']}")

    # Save mapping to JSON
    output_file = project_root / "scripts" / "tag_migration_mapping.json"
    with open(output_file, 'w') as f:
        json.dump(all_tags, f, indent=2, sort_keys=True)

    print(f"\n✓ Mapping saved to: {output_file}")

    # Save Python dict version for easy import
    output_py = project_root / "scripts" / "tag_migration_mapping.py"
    with open(output_py, 'w') as f:
        f.write("# Auto-generated tag migration mapping\n")
        f.write("# Maps old tag names to new path-based names\n\n")
        f.write("TAG_MIGRATIONS = {\n")
        for old_name in sorted(all_tags.keys()):
            new_name = all_tags[old_name]['new_name']
            f.write(f'    "{old_name}": "{new_name}",\n')
        f.write("}\n")

    print(f"✓ Python mapping saved to: {output_py}")


if __name__ == "__main__":
    main()

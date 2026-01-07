# Content Pack Usage by Mods

This document describes which content packs are used by `mods:forgero` and `mods:vanilla-upgrades`, and how they connect through the tag system.

For detailed architecture of individual content packs, see: `content/CONTENT_PACK_ARCHITECTURE.md`

---

## Overview

The rearchitected mod system uses a modular content pack approach where:
- **Tags define contracts** - Content packs reference tags by ID, not specific items
- **Packs are loosely coupled** - Any pack can extend tags from another pack
- **Mods select packs** - Each mod includes only the packs it needs

---

## Mod Content Pack Dependencies

### mods:forgero (Full Forgero Experience)

**13 content packs included:**

```
mods/forgero/build.gradle

FOUNDATION (Core system)
├── forgero-base           [runtimeOnly]  Tags, templates, upgrade slots
├── forgero-materials      [runtimeOnly]  41 materials (metal, wood, stone, etc.)
├── forgero-upgrades       [runtimeOnly]  Upgrade material definitions
└── minecraft-tools        [runtimeOnly]  Vanilla tool integration

EQUIPMENT (Item definitions)
├── forgero-tools          [runtimeOnly]  Tool templates (pickaxe, axe, etc.)
├── forgero-armor          [runtimeOnly]  Armor texture templates
└── forgero-armor-content  [runtimeOnly]  Armor models

SCHEMATICS
├── forgero-schematics           [runtimeOnly]  Quality schematics (refined, mastercrafted)
└── forgero-extended-schematics  [runtimeOnly]  Extended schematics (weapon blades, tool heads, guards, bindings)

EXTENDED CONTENT
├── forgero-secondary-materials  [runtimeOnly]  Secondary materials (soft, hard, hybrid, dyes)
├── forgero-extended-weapons     [runtimeOnly]  Extended weapon parts & equipment (katana, rapier, etc.)
├── forgero-mining               [runtimeOnly]  Mining tool parts & equipment (hammer, spade, etc.)
└── forgero-gems                 [runtimeOnly]  Gem upgrades (diamond_gem, etc.)

VANILLA INTEGRATION
└── vanilla-upgrades-base  [include + runtimeOnly]  Static parts for vanilla items
```

### mods:vanilla-upgrades (Lightweight Vanilla Enhancement)

**4 content packs included:**

```
mods/vanilla-upgrades/build.gradle

├── forgero-base           [include + runtimeOnly]  Tags & core definitions
├── forgero-materials      [include + runtimeOnly]  Material definitions
├── forgero-upgrades       [include + runtimeOnly]  Upgrade materials
└── vanilla-upgrades-base  [include + runtimeOnly]  Static vanilla parts
```

**Key Difference**: `vanilla-upgrades` is designed to add upgrade capabilities to vanilla tools without the full Forgero crafting system. It includes only 4 packs vs 11.

---

## Content Pack Dependency Graph

```
                           forgero-base
                    (tags, templates, slots)
                               |
        +----------------------+----------------------+
        |                      |                      |
        v                      v                      v
  forgero-materials    forgero-upgrades      minecraft-tools
  (41 materials)       (upgrade mats)        (vanilla integration)
        |                      |                      |
        +----------+-----------+                      |
                   |                                  |
        +----------+----------+----------+------------+
        |          |          |          |
        v          v          v          v
  forgero-   forgero-   forgero-      forgero-armor
  tools      schematics secondary-    forgero-armor-content
             (quality)  materials
        |          |          |
        +----+-----+----------+
             |
        +----+----+
        |         |
        v         v
  forgero-   forgero-
  extended-  extended-
  schematics weapons
        |         |
        +----+----+----+----+
             |         |    |
             v         v    v
       forgero-   forgero-  vanilla-
       mining     gems      upgrades-
                            base

Legend:
  [forgero-base] = Included by BOTH mods
  forgero-* = Included by mods:forgero only
  vanilla-upgrades-base = Included by BOTH mods
```

---

## Tag System Connection Points

### How Content Packs Share Tags

**forgero-base defines all root tags:**
```
tags/
├── materials/materials.json     (root)
├── parts/parts.json             (root)
├── tools/tools.json             (root)
├── upgrades/upgrades.json       (root)
└── schematics/categories.json   (root)
```

**Other packs populate these tags:**

| Pack | Defines | References |
|------|---------|------------|
| forgero-base | All root tags + hierarchy | - |
| forgero-materials | 41 materials | `materials/types/*`, `materials/roles/*` |
| forgero-tools | Tool templates | `tools/types/*`, `parts/types/*` |
| forgero-schematics | Refined/mastercrafted schematics | `schematics/categories/*` |
| forgero-extended-schematics | Extended schematics (blades, heads, guards, bindings) | `schematics/*` |
| forgero-secondary-materials | Soft/hard/hybrid/dye materials | `materials/properties/*` |
| vanilla-upgrades-base | Static parts | `tools/*`, `uncategorized/vanilla_tool` |

### Example: Tag Flow for Iron Pickaxe

```
1. forgero-base defines:
   - forgero:tools/types/pickaxe (equipment tag)
   - forgero:parts/types/pickaxe_head (part tag)
   - forgero:materials/roles/tool_material (role tag)

2. forgero-materials provides:
   - Iron material with tags:
     ["forgero:materials/types/metal", "forgero:materials/roles/tool_material"]

3. forgero-base provides:
   - pickaxe.json equipment template
   - pickaxe_head.json part template

4. At runtime:
   - Template slots reference tags
   - Iron fills "tool_material" slots
   - Result: iron-pickaxe_head composed into iron-pickaxe
```

---

## Content Pack File Structure

Each content pack follows this structure:

```
content/{pack-name}/
└── src/main/resources/data/forgero/
    ├── equipment/          # Equipment templates (pickaxe, sword, etc.)
    ├── materials/          # Material definitions
    ├── parts/              # Part templates
    ├── schematics/         # Schematic blueprints
    ├── tags/               # Tag hierarchy definitions
    ├── recipe_generators/  # Recipe configs
    └── forgero_loot/       # Loot table configs
```

### Key Files by Pack

**forgero-base** (Foundation)
```
equipment/tools/pickaxe.json, axe.json, shovel.json, hoe.json, sword.json
parts/heads/pickaxe_head.json, axe_head.json, etc.
parts/handles/handle.json
parts/bindings/binding.json
tags/ (96 tag definition files)
```

**forgero-materials** (Materials)
```
materials/metal/iron.json, gold.json, copper.json, netherite.json
materials/wood/oak.json, birch.json, spruce.json, etc.
materials/stone/stone.json, granite.json, diorite.json, etc.
materials/mineral/diamond.json, emerald.json, coal.json, etc.
materials/bone/bone.json
materials/leather/leather.json
materials/glass/glass.json, crying_obsidian.json
```

**vanilla-upgrades-base** (Vanilla Wrapping)
```
parts/pickaxes/wooden_pickaxe.json, stone_pickaxe.json, iron_pickaxe.json, etc.
parts/axes/wooden_axe.json, stone_axe.json, iron_axe.json, etc.
parts/shovels/, parts/hoes/, parts/swords/ (6 tiers each)
```

---

## Static Parts vs Dynamic Parts

### Dynamic Parts (forgero-base)

Templates that generate items from materials:

```json
// parts/heads/pickaxe_head.json
{
  "type": "forgero:part_template",
  "name": "pickaxe_head",
  "tags": ["forgero:parts/head", "forgero:parts/pickaxe_head"],
  "structure": {
    "slots": {
      "material": {
        "type": "forgero:tool_material",
        "default_tag": "forgero:tool_material"
      }
    }
  }
}
```

**Result**: `iron-pickaxe_head`, `diamond-pickaxe_head`, `oak-pickaxe_head`, etc.

### Static Parts (vanilla-upgrades-base)

Wraps existing vanilla items as Forgero components:

```json
// parts/pickaxes/iron_pickaxe.json
{
  "type": "forgero:static_part",
  "host": [{ "type": "item", "id": "minecraft:iron_pickaxe" }],
  "tags": ["forgero:tools/pickaxe", "forgero:uncategorized/vanilla_tool"],
  "attributes": [
    { "type": "forgero:durability", "computation": { "value": 250 } },
    { "type": "forgero:attack_damage", "computation": { "value": 4 } }
  ],
  "upgrades": [
    { "id": "iron_pickaxe-binding", "type": "forgero:materials/types/binding" }
  ]
}
```

**Result**: Vanilla `minecraft:iron_pickaxe` gains upgrade slots while preserving original behavior.

---

## Active vs Legacy Packs

### Active (Used by mods - Modern Format)

| Pack | Used By | Purpose |
|------|---------|---------|
| forgero-base | Both | Core tags, templates |
| forgero-materials | Both | Material definitions (41 primary materials) |
| forgero-upgrades | Both | Upgrade materials |
| vanilla-upgrades-base | Both | Vanilla item wrapping |
| forgero-tools | forgero | Tool definitions |
| forgero-armor | forgero | Armor templates |
| forgero-armor-content | forgero | Armor models |
| forgero-schematics | forgero | Quality schematics (refined, mastercrafted) |
| forgero-extended-schematics | forgero | Extended schematics (weapon blades, tool heads, guards, bindings) |
| forgero-secondary-materials | forgero | Secondary materials (soft, hard, hybrid, dyes) |
| forgero-extended-weapons | forgero | Extended weapon parts & equipment |
| forgero-mining | forgero | Mining tool parts & equipment |
| forgero-gems | forgero | Gem upgrades |
| minecraft-tools | forgero | Vanilla integration |

### Legacy (Old Pack Format - Not Used by Modern Mods)

| Pack | Status | Notes |
|------|--------|-------|
| forgero-vanilla-legacy-read-only | Legacy | Old pack format - recipe generators (renamed) |
| forgero-extended-legacy-read-only | Legacy | Old pack format - kept for reference only (renamed) |
| forgero-compat-legacy-read-only | Available | Mod compatibility (old format, renamed) |
| forgero-structures | Available | World generation |
| forgero-deprecated | Legacy | Old content |

**Note**: Legacy modules have been renamed with `-legacy-read-only` suffix to clearly indicate they are preserved for reference but not actively maintained or included in modern builds.

---

## Tag Inheritance in Action

When a component is queried for tags, the TagResolver performs BFS traversal up the hierarchy:

```
Query: Does iron_pickaxe have tag "forgero:tools"?

iron_pickaxe direct tags:
  - forgero:tools/types/pickaxe

BFS traversal:
  1. Check: tools/types/pickaxe == tools? NO
  2. Get parents of tools/types/pickaxe: [forgero:tools/types]
  3. Check: tools/types == tools? NO
  4. Get parents of tools/types: [forgero:tools]
  5. Check: tools == tools? YES!

Result: TRUE (inherited through 2 levels)
```

This enables queries like "find all tools" to return pickaxes, axes, swords, etc. without each item explicitly listing the root tag.

---

## Adding Content to Existing Mods

### Adding a New Material

1. Create material JSON in your content pack:
```json
// materials/custom/mythril.json
{
  "type": "forgero:material",
  "name": "Mythril",
  "tags": [
    "forgero:materials/types/metal",
    "forgero:materials/roles/tool_material"
  ],
  "host": { "identifiers": [{ "type": "item", "id": "mymod:mythril_ingot" }] },
  "attributes": [
    { "type": "forgero:durability", "computation": { "value": 2000 } }
  ]
}
```

2. The material automatically:
   - Can fill any slot requiring `tool_material`
   - Generates all tool variants (mythril-pickaxe, mythril-axe, etc.)
   - Inherits tag hierarchy (is also tagged as `materials/types` and `materials`)

### Adding a New Upgrade Type

1. Define the tag:
```json
// tags/upgrades/types/enchanter.json
{
  "parents": ["forgero:upgrades/types"],
  "description": "Enchanting upgrade slot"
}
```

2. Add upgrade slot to equipment template:
```json
"upgrades": [
  { "type": "forgero:upgrades/types/enchanter", "description": "Enchanter slot" }
]
```

3. Create materials that can fill this slot by tagging them with `forgero:upgrades/types/enchanter`.

---

## Summary

The content pack system provides:

1. **Modularity** - Mods include only needed packs
2. **Extensibility** - New content extends existing tags
3. **Loose coupling** - Packs reference tags, not specific items
4. **Data-driven** - All content in JSON, no code changes needed
5. **Inheritance** - Tag hierarchy enables broad queries

For implementation details, see:
- `docs/TAG_SYSTEM_ARCHITECTURE.md` - Tag graph implementation
- `content/CONTENT_PACK_ARCHITECTURE.md` - Content pack internals

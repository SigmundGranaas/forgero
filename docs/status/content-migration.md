# Forgero Content Migration Status

> **Status: ~80% COMPLETE** (January 2026)
>
> The migration from legacy pack format to modern JSON format is substantially complete.
> Recipe generators, schematics, materials, and equipment are fully migrated.
> Remaining gaps: loot tables (8 files) and block category tags (6 files).

---

## Quick Status

| Category | Status | Files | Notes |
|----------|--------|-------|-------|
| **Equipment Templates** | ✅ Complete | 9 | All tools/weapons migrated |
| **Part Templates** | ✅ Complete | 11 | All parts migrated |
| **Materials** | ✅ Complete | 126 | 41 primary + 50 secondary + 35 gems |
| **Schematics** | ✅ Complete | 61 | Core + quality + extended |
| **Recipe Generators** | ✅ Complete | 57 | All critical recipes migrated |
| **Conditions/Upgrades** | ✅ Complete | 20 | All conditions migrated |
| **Loot Tables** | ❌ Gap | 2/10 | Only bow loot migrated |
| **Block Category Tags** | ❌ Gap | 0/6 | Vein mining tags missing |

---

## Related Documentation

- **[Content Pack Usage](CONTENT_PACK_USAGE.md)** - How content packs are used by mods
- **[Resource Best Practices](RESOURCE_BEST_PRACTICES.md)** - Current patterns for new content
- **[Tag System Architecture](TAG_SYSTEM_ARCHITECTURE.md)** - Tag hierarchy design

---

## Remaining Gaps

### 1. Loot Tables (HIGH Priority)

**Status**: Only 2 of 10 loot tables migrated (bow content only)

| File | Legacy Location | Target | Purpose |
|------|-----------------|--------|---------|
| `mineshaft_parts.json` | `forgero-vanilla-legacy/forgero_loot/` | `forgero-base` | Parts in mineshaft chests |
| `mineshaft_schematics.json` | `forgero-vanilla-legacy/forgero_loot/` | `forgero-base` | Schematics in mineshaft chests |
| `diamond_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Diamond gem drops |
| `echo_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Echo gem drops |
| `emerald_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Emerald gem drops |
| `lapis_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Lapis gem drops |
| `prismarine_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Prismarine gem drops |
| `quartz_gem.json` | `forgero-extended-legacy/forgero_loot/` | `forgero-gems` | Quartz gem drops |

**Impact**: Gems and parts won't appear as world loot, reducing discoverability.

**Already Migrated** (in `forgero-bows`):
- `mineshaft_arrows.json` - Arrow heads in mineshaft loot
- `mineshaft_bow_schematics.json` - Bow schematics in mineshaft loot

### 2. Block Category Tags (HIGH Priority)

**Status**: 0 of 6 files migrated

| Tag | Legacy Location | Target | Purpose |
|-----|-----------------|--------|---------|
| `blocks.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Base block categorization |
| `categories.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Category hierarchy |
| `plants.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Plant block category |
| `vein_mining_dirt.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Vein mining for dirt blocks |
| `vein_mining_logs.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Vein mining for log blocks |
| `vein_mining_ores.json` | `forgero-extended-legacy/tags/block_categories/` | `forgero-base` | Vein mining for ore blocks |

**Impact**: Vein mining feature will not function correctly.

---

## Completed Migrations

### Recipe Generators (57 files - COMPLETE)

All recipe generators have been migrated to modern format:

**forgero-base** (22 files):
- `tool/tool.json` - Core tool assembly (head + handle = tool)
- `tool/tool_with_binding.json` - Tool assembly with binding upgrade
- `weapon/sword.json` - Core sword assembly (blade + handle = sword)
- `weapon/sword_with_guard.json` - Sword assembly with guard upgrade
- `simple_wood/*.json` (8 files) - Early-game wood crafting
- `schematic_recipes/*.json` (9 files) - Schematic-based crafting
- `wood_to_stone_upgrade.json` - Material tier upgrade path

**forgero-extended-weapons** (13 files):
- Extended weapon recipes (katana, rapier, dagger, etc.)

**forgero-mining** (8 files):
- Mining tool part generators and schematic recipes

**forgero-bows** (14 files):
- Arrow head, bow limb generators, and bow assembly recipes

### Schematics (61 files - COMPLETE)

**forgero-base** (11 files):
- Core bindings, blades, guards, handles, heads, pommels

**forgero-schematics** (16 files):
- Refined tier (8): axe_head, handle, hoe_head, pickaxe_head, pommel, shovel_head, sword_blade, sword_guard
- Mastercrafted tier (8): Same parts at higher quality

**forgero-extended-schematics** (26 files):
- Bindings (2): scrappy, string
- Blades (8): broadsword, cutlass, dagger, katana, knife, kunai, rapier, scythe_head
- Guards (7): cruciform, half_basket, mechanized, rounded, shell, swept, tsuba
- Heads (9): battle_axe, club, felling_axe, hammer, mace, mandrill_pickaxe, spade, spear, war_hammer

**forgero-bows** (8 files):
- Arrow heads (3 quality levels)
- Bow limbs (4 variants)

### Materials (126 files - COMPLETE)

**forgero-materials** (41 files):
- Metal: copper, gold, iron, netherite
- Wood: oak, birch, spruce, jungle, acacia, dark_oak, crimson, warped, cherry, mangrove
- Stone: stone, cobblestone, granite, diorite, andesite, deepslate, blackstone, basalt, end_stone, netherrack, tuff, calcite
- Mineral: diamond, emerald, amethyst, coal, charcoal, flint, lapis_lazuli, nether_quartz, prismarine, echo
- Other: bone, leather, blaze_rod, obsidian, crying_obsidian

**forgero-secondary-materials** (50 files):
- Dye: All 16 Minecraft dye colors
- Hard: bamboo, end_crystal, ender_eye, ender_pearl, nautilus_shell, popped_chorus_fruit, prismarine_crystals, scute, shulker_shell
- Hybrid: blaze_powder, mushrooms, fermented_spider_eye, fire_charge, ghast_tear, glowstone, honeycomb, magma_cream, redstone, slime_ball
- Soft: chorus_fruit, feather, kelp, lily_pad, nether_wart, phantom_membrane, rabbit_hide, string, sugar_cane, vines

**forgero-gems** (35 files):
- 7 gem types × 5 quality tiers: amethyst, diamond, echo, emerald, lapis, prismarine, quartz

### Equipment Templates (9 files - COMPLETE)

**forgero-base** (5 files):
- Tools: axe, hoe, pickaxe, shovel, sword

**forgero-extended-weapons** (2 files):
- Weapons: mace, spear

**forgero-bows** (2 files):
- Ranged: bow variants

---

## Modern Content Pack Inventory

| Pack | Purpose | File Count |
|------|---------|------------|
| `forgero-base` | Core tags, templates, equipment, models | ~270 |
| `forgero-materials` | Primary tool materials | 41 |
| `forgero-secondary-materials` | Soft/hard/hybrid/dye materials | 50 |
| `forgero-schematics` | Quality schematics (refined, mastercrafted) | 16 |
| `forgero-extended-schematics` | Extended schematics (blades, heads, guards) | 26 |
| `forgero-gems` | Gem materials with quality tiers | 35 |
| `forgero-upgrades` | Condition definitions | 20 |
| `forgero-tools` | Tool definitions with armor tags | ~10 |
| `forgero-armor` | Armor texture templates | ~20 |
| `forgero-armor-content` | Armor models | ~30 |
| `forgero-extended-weapons` | Extended weapon equipment & recipes | ~30 |
| `forgero-mining` | Mining tool equipment & recipes | ~20 |
| `forgero-bows` | Bow/arrow parts, equipment, recipes | ~40 |
| `vanilla-upgrades-base` | Static parts for vanilla items | ~30 |

---

## Legacy Packs (Reference Only)

These packs are preserved for reference but not included in modern builds:

| Pack | Content | Notes |
|------|---------|-------|
| `forgero-vanilla-legacy-read-only` | Original vanilla content | Migration source |
| `forgero-extended-legacy-read-only` | Extended content | Migration source |
| `forgero-compat-legacy-read-only` | 332 materials for 36 mods | Migrate per-mod as needed |
| `forgero-deprecated` | 68 deprecated files | Archive only |

---

## Migration Patterns

### Equipment Template: Legacy → Modern

**Old Format (Legacy)**
```json
{
  "type": "PICKAXE",
  "name": "pickaxe",
  "resource_type": "CONSTRUCT_TEMPLATE",
  "construct": {
    "slots": [
      {"type": "PICKAXE_HEAD", "order": 0},
      {"type": "HANDLE", "order": 1}
    ]
  }
}
```

**New Format (Modern)**
```json
{
  "type": "forgero:equipment_template",
  "name": "pickaxe",
  "tags": ["forgero:tools/pickaxe", "forgero:tool"],
  "host_template": {
    "class": "forgero:pickaxe_item",
    "id": "forgero:{head.material.name}-pickaxe"
  },
  "structure": {
    "id": "forgero:{head.material.name}-pickaxe",
    "slots": {
      "head": {"type": "forgero:pickaxe_head", "default_tag": "forgero:parts/pickaxe_head"},
      "handle": {"type": "forgero:handle", "default_tag": "forgero:parts/handle"}
    }
  },
  "attributes": [
    {"id": "forgero:pickaxe-attack_speed", "type": "forgero:attack_speed", "computation": {"value": -2.8}}
  ]
}
```

**Key Changes:**
- `type`: `PICKAXE` → `forgero:equipment_template`
- `resource_type` removed, type indicates resource
- `construct.slots` → `structure.slots` with named keys
- Slot types namespaced: `PICKAXE_HEAD` → `forgero:pickaxe_head`
- Added `tags` array for categorization
- Added `host_template` for item class binding

### Material: Legacy → Modern

**Old Format (Legacy)**
```json
{
  "type": "FEATHER",
  "name": "feather",
  "parent": "hide_base",
  "resource_type": "DEFAULT",
  "container": {"type": "HOST", "id": "minecraft:feather"},
  "properties": {
    "grouped_attributes": [
      {
        "context": "COMPOSITE",
        "order": "BASE",
        "operation": "ADDITION",
        "attributes": [
          {"id": "hide-durability", "type": "DURABILITY", "value": 40}
        ]
      }
    ]
  }
}
```

**New Format (Modern)**
```json
{
  "type": "forgero:material",
  "name": "Feather",
  "tags": [
    "forgero:materials/secondary",
    "forgero:materials/soft",
    "forgero:upgrade/binding"
  ],
  "host": {
    "identifiers": [{"type": "item", "id": "minecraft:feather"}]
  },
  "attributes": [
    {
      "id": "forgero:feather-durability",
      "type": "forgero:durability",
      "computation": {"value": 40},
      "context": "forgero:part-composite"
    }
  ]
}
```

**Key Changes:**
- `type`: `FEATHER` → `forgero:material`
- `parent` inheritance replaced with `tags`
- `container` → `host.identifiers`
- `grouped_attributes` with `context` → `attributes` with `context` field
- Context mapping: `COMPOSITE` → `forgero:part-composite`

### Attribute: Legacy → Modern

**Old Format**
```json
{
  "type": "DURABILITY",
  "order": "BASE",
  "operation": "ADDITION",
  "value": 100
}
```

**New Format**
```json
{
  "id": "forgero:material-durability",
  "type": "forgero:durability",
  "computation": {"value": 100},
  "context": "forgero:part-composite"
}
```

**Key Changes:**
- Required unique `id` field
- `type` namespaced: `DURABILITY` → `forgero:durability`
- `value` + `operation` wrapped in `computation` object
- Context-based application → explicit `context` field

---

## Asset Locations

### Palettes (Material Colors)
| Module | Path | Count |
|--------|------|-------|
| forgero-materials | `assets/forgero/textures/palette/` | 78 |
| forgero-materials | `assets/forgero/textures/palette/dye/` | 16 |

### Part Texture Templates
| Module | Path | Count |
|--------|------|-------|
| forgero-base | `assets/forgero/texture_template/parts/` | 47 |

### Item Textures
| Module | Path | Count |
|--------|------|-------|
| forgero-base | `assets/forgero/textures/item/` | 37 |

---

## Deleted Modules

These modules were duplicates and have been removed:
- `minecraft-vanilla-materials` (duplicate of forgero-materials)
- `shared-materials` (duplicate of forgero-materials)
- `forgero-equipment` (migrated to forgero-base)
- `forgero-models` (migrated to forgero-base)
- `forgero-tools-content` (migrated to forgero-base)

---

## Next Steps

1. **Migrate loot tables** (8 files) → `forgero-base` and `forgero-gems`
2. **Migrate block category tags** (6 files) → `forgero-base/tags/block_categories/`
3. **Run test suite** to verify functionality
4. **Consider mod compatibility** migrations based on user demand

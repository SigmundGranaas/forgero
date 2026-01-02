# Forgero Content Migration Status

> **Status: COMPLETE** - This document is historical reference. The migration from legacy pack format to modern JSON format is complete. See `docs/RESOURCE_BEST_PRACTICES.md` for current patterns.

This document tracks the migration of content from legacy pack format to modern JSON format.

---

## Migration Patterns

### Equipment Template Migration

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
- `attributes` uses explicit condition structure

---

### Part Template Migration

**Old Format (Legacy)**
```json
{
  "type": "PICKAXE_HEAD",
  "name": "pickaxe_head",
  "resource_type": "SCHEMATIC",
  "schematic": {
    "materialTarget": "HEAD"
  }
}
```

**New Format (Modern)**
```json
{
  "type": "forgero:part_template",
  "name": "pickaxe_head",
  "tags": ["forgero:parts/head", "forgero:parts/pickaxe_head"],
  "host_template": {
    "class": "forgero:part_item",
    "id": "forgero:{material.name}-pickaxe_head"
  },
  "structure": {
    "id": "forgero:{material.name}-pickaxe_head",
    "slots": {
      "material": {"type": "forgero:tool_material", "default_tag": "forgero:materials/tool_material"}
    }
  },
  "attributes": [
    {"id": "forgero:pickaxe_head-rarity", "type": "forgero:rarity", "computation": {"value": 10, "operation": "addition"}}
  ]
}
```

**Key Changes:**
- `type`: `PICKAXE_HEAD` → `forgero:part_template`
- `schematic.materialTarget` → `structure.slots.material`
- Material slot references tags instead of enums

---

### Material Migration

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
      },
      {
        "context": "UPGRADE",
        "attributes": [
          {"id": "hide-rarity-upgrade", "type": "RARITY", "value": 10}
        ]
      }
    ]
  },
  "palette": {"name": "feather"}
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
    "forgero:materials/hide",
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
      "condition": {
        "static": [{"type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot"}]
      }
    },
    {
      "id": "forgero:feather-rarity",
      "type": "forgero:rarity",
      "computation": {"value": 10}
    }
  ]
}
```

**Key Changes:**
- `type`: `FEATHER` → `forgero:material`
- `parent` inheritance replaced with `tags`
- `container` → `host.identifiers`
- `grouped_attributes` with `context` → `attributes` with explicit `condition`
- Context mapping: `COMPOSITE` → root attributes, `UPGRADE` → slot conditions
- `palette` removed (palette determined by material name)

---

### Schematic Migration

**Old Format (Legacy)**
```json
{
  "type": "SWORD_GUARD",
  "name": "cruciform_sword_guard",
  "resource_type": "SCHEMATIC",
  "schematic": {
    "unique": true
  },
  "properties": {
    "attributes": [
      {"type": "RARITY", "operation": "ADDITION", "value": 75}
    ]
  }
}
```

**New Format (Modern)**
```json
{
  "type": "forgero:schematic",
  "name": "Cruciform Sword Guard",
  "tags": ["forgero:schematics/guards", "forgero:schematics/extended"],
  "attributes": [
    {
      "id": "forgero:cruciform-schematic-rarity-local",
      "type": "forgero:rarity",
      "computation": {"value": 75, "operation": "addition"}
    },
    {
      "id": "forgero:cruciform-schematic-rarity-composite",
      "type": "forgero:rarity",
      "computation": {"value": 1.4, "operation": "multiplication"},
      "condition": {"static": [{"type": "forgero:is_root"}]}
    }
  ]
}
```

**Key Changes:**
- `type`: `SWORD_GUARD` → `forgero:schematic`
- `schematic.unique` replaced with tag-based filtering
- Attributes have explicit `id` and `computation` structure
- Composite effects use `condition: {"static": [{"type": "forgero:is_root"}]}`

---

### Model Migration

**Old Format (Legacy)**
```json
{
  "type": "PICKAXE_HEAD",
  "name": "pickaxe_head",
  "resource_type": "GENERATE",
  "model": {
    "modelType": "BASED",
    "offset": {"x": 1, "y": 2}
  }
}
```

**New Format (Modern)**
```json
{
  "type": "forgero:part_model_template",
  "name": "pickaxe_head",
  "model_type": "forgero:based",
  "offset": {"x": 1, "y": 2}
}
```

**Key Changes:**
- `type`: Part name → `forgero:part_model_template`
- `resource_type: GENERATE` removed
- `model.modelType` → `model_type` at root level
- Enum values namespaced: `BASED` → `forgero:based`

---

### Attribute Format Changes

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
  "computation": {
    "value": 100,
    "operation": "addition"
  },
  "condition": {
    "static": [{"type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot"}]
  }
}
```

**Key Changes:**
- Required unique `id` field
- `type` namespaced: `DURABILITY` → `forgero:durability`
- `value` + `operation` wrapped in `computation` object
- `order` replaced with conditional application
- Context-based application → explicit `condition` block

---

### Tag Format Changes

**Old Format** (implicit inheritance)
```json
{
  "type": "TOOL_MATERIAL",
  "name": "iron",
  "parent": "metal_base"
}
```

**New Format** (explicit tags)
```json
{
  "parent": "forgero:materials/metal"
}
```

Or with values:
```json
{
  "values": ["forgero:iron", "forgero:gold", "forgero:copper"]
}
```

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

### Item Textures (Schematics, UI)
| Module | Path | Count |
|--------|------|-------|
| forgero-base | `assets/forgero/textures/item/` | 37 |

### Extended Content Textures
| Module | Path | Content |
|--------|------|---------|
| forgero-extended | `assets/forgero/templates/textures/exotic/` | Exotic weapons (katana, rapier, etc.) |
| forgero-extended | `assets/forgero/templates/textures/lightweight/` | Lightweight weapons (knife, kunai) |
| forgero-extended | `assets/forgero/templates/textures/guards/` | Sword guard variants |
| forgero-extended | `assets/forgero/templates/textures/gems/` | Gem upgrade visuals |
| forgero-extended | `assets/forgero/templates/textures/mastercrafted/` | Mastercrafted variants |
| forgero-extended | `assets/forgero/templates/textures/refined/` | Refined variants |

### Models
| Module | Path | Content |
|--------|------|---------|
| forgero-base | `data/forgero/forgero_models/item/` | 15 item models |
| forgero-base | `data/forgero/forgero_models/upgrade/` | 2 upgrade models |
| forgero-extended | `assets/forgero/models/item/` | Scrappy binding models |

---

## Module Architecture

### Core Modules (Base Content)

| Module | Purpose | Status |
|--------|---------|--------|
| **forgero-base** | Base tools, parts, schematics, models, textures | Complete |
| **forgero-materials** | Base vanilla materials (41) + palettes (94) | Complete |
| **forgero-upgrades** | Base upgrade definitions | Complete |

### Extended Modules (Additional Content)

| Module | Purpose | Files | Status |
|--------|---------|-------|--------|
| **forgero-gems** | Gem materials (7 types, 5 tiers each) | 43 | Complete |
| **forgero-extended-weapons** | Extended weapons (13 types) | 68 | Complete |
| **forgero-schematics** | Schematic variants (mastercrafted, refined) | 18 | Complete |
| **forgero-mining** | Mining tools (4 types) | 22 | Complete |
| **forgero-extended** | Guard schematics, materials, bindings, loot | 71 | Complete |
| **forgero-compat** | Mod compatibility materials | 429 | Legacy |

### Other Active Modules

| Module | Purpose | Status |
|--------|---------|--------|
| **forgero-armor** | Armor system tags/equipment | Modern |
| **forgero-armor-content** | Armor parts and models | Modern |
| **forgero-structures** | Structure templates | Active |
| **vanilla-upgrades-base** | Vanilla upgrade system | Modern |

### Legacy Modules (Can Be Deprecated)

| Module | Content | Notes |
|--------|---------|-------|
| **forgero-vanilla** | Packs, conditions | Assets migrated, conditions remain |
| **forgero-tools** | 18 files | Test content, can be removed |
| **forgero-deprecated** | 68 packs | Archive only |
| **minecraft-tools** | 3 tags | Minimal, can be removed |

---

## Completed Migrations

### forgero-base
```
data/forgero/
├── equipment/tools/     # 5 tools (axe, hoe, pickaxe, shovel, sword)
├── parts/               # 9 parts (heads, handle, binding, blade, guard, pommel)
├── schematics/          # 11 base schematics
├── forgero_models/      # 17 models (15 item + 2 upgrade)
└── tags/                # 39 tag definitions

assets/forgero/
├── texture_template/parts/   # 47 part texture templates
└── textures/item/            # 37 item textures
```

### forgero-materials
```
data/forgero/
├── materials/           # 41 base materials
│   ├── metal/           # iron, gold, copper, netherite
│   ├── mineral/         # diamond, emerald, amethyst, etc.
│   ├── wood/            # oak, birch, spruce, etc. (10 types)
│   ├── stone/           # stone, cobblestone, deepslate, etc.
│   ├── bone/            # bone
│   ├── glass/           # glass
│   └── leather/         # leather
└── tags/materials/      # Material tags

assets/forgero/
└── textures/palette/    # 94 palette files (78 + 16 dyes)
```

### forgero-gems
```
data/forgero/
├── materials/           # 35 gem materials (7 gems × 5 tiers)
│   ├── amethyst/        # chipped, flawed, gem, flawless, perfect
│   ├── diamond/
│   ├── echo/
│   ├── emerald/
│   ├── lapis/
│   ├── prismarine/
│   └── quartz/
└── tags/materials/      # 8 tag files
```

### forgero-extended-weapons
```
data/forgero/
├── equipment/weapons/   # 13 weapons
│   ├── battle_axe, broadsword, dagger, scythe, spear, war_hammer
│   ├── katana, rapier, cutlass (exotic swordlike)
│   ├── mace, club (exotic blunt)
│   └── knife, kunai (lightweight)
├── parts/               # 13 parts (blades + heads)
├── schematics/          # 13 schematics
└── tags/                # 24 tags
```

### forgero-schematics
```
data/forgero/
├── schematics/
│   ├── mastercrafted/   # 8 mastercrafted schematics
│   └── refined/         # 8 refined schematics
└── tags/schematics/     # 2 tags
```

### forgero-mining
```
data/forgero/
├── equipment/tools/     # 4 tools (felling_axe, hammer, mandrill_pickaxe, spade)
├── parts/heads/         # 4 heads
├── schematics/heads/    # 4 schematics
└── tags/                # 10 tags
```

### forgero-extended
```
data/forgero/
├── materials/           # 50 secondary materials
│   ├── soft/            # 12 (feather, string, plants, etc.)
│   ├── hard/            # 9 (shells, crystals, ender items)
│   ├── hybrid/          # 13 (goo, dust, mushrooms)
│   └── dye/             # 16 dye materials
├── schematics/          # 9 schematics
│   ├── guards/          # 7 guard schematics
│   └── bindings/        # 2 binding schematics
├── tags/materials/      # 5 material tags
├── forgero_loot/        # 6 gem loot definitions
└── recipe_generators/   # Recipe generation configs

assets/forgero/
├── models/item/         # 2 scrappy binding models
└── templates/textures/  # 202 texture files
    ├── exotic/          # Exotic weapon textures
    ├── lightweight/     # Lightweight weapon textures
    ├── guards/          # Guard textures
    └── ...
```

---

## Pending Migration

| Source | Files | Content | Target | Status |
|--------|-------|---------|--------|--------|
| forgero-vanilla/conditions/ | 20 | Trait modifiers (durable, sharp, etc.) | forgero-upgrades | Pending |

---

## Legacy (No Migration Planned)

| Module | Files | Reason |
|--------|-------|--------|
| forgero-compat | 429 | Mod-specific, migrate per-mod as needed |
| forgero-deprecated | 68 | Deprecated content |
| forgero-trinkets | 5 | Deprecated feature |

---

## Migration Summary

| Category | Files | Status |
|----------|-------|--------|
| Core content (forgero-base) | ~80 | Complete |
| Materials (forgero-materials) | 41 | Complete |
| Gems (forgero-gems) | 43 | Complete |
| Extended weapons (forgero-extended-weapons) | 68 | Complete |
| Schematic variants (forgero-schematics) | 18 | Complete |
| Mining tools (forgero-mining) | 22 | Complete |
| Extended content (forgero-extended) | 71 | Complete |
| **Conditions** | 20 | **Pending** |
| Mod compatibility | 429 | Legacy |
| Deprecated | 73 | Skip |

**Total migrated:** ~350 files
**Total pending:** ~20 files (conditions only)
**Legacy (no migration):** ~502 files

---

## Deleted Modules

These modules were duplicates and have been removed:
- `minecraft-vanilla-materials` (duplicate of forgero-materials)
- `shared-materials` (duplicate of forgero-materials)
- `forgero-equipment` (migrated to forgero-base)
- `forgero-models` (migrated to forgero-base)
- `forgero-tools-content` (migrated to forgero-base)

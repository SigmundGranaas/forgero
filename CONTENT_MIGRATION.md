# Forgero Content Migration Status

This document tracks the migration of content from legacy pack format to modern JSON format.

## Format Comparison

### Old Format (Legacy)
```json
{
  "type": "PICKAXE",
  "name": "pickaxe",
  "resource_type": "CONSTRUCT_TEMPLATE",
  "construct": { ... }
}
```

### New Format (Modern)
```json
{
  "type": "forgero:equipment_template",
  "name": "pickaxe",
  "host_template": { ... },
  "structure": { ... }
}
```

---

## Module Architecture

### Core Modules (Base Content)

| Module | Purpose | Status |
|--------|---------|--------|
| **forgero-base** | Base tools, parts, schematics, models, textures | Complete |
| **forgero-materials** | Base vanilla materials (41) + palettes | Complete |
| **forgero-upgrades** | Base upgrade definitions | Complete |

### Extended Modules (Additional Content)

| Module | Purpose | Status |
|--------|---------|--------|
| **forgero-extended** | Material variants, guards, misc extensions | Needs Migration |
| **forgero-extended-weapons** | Extended weapon types (spears, scythes, etc.) | Needs Migration |
| **forgero-gems** | Gem materials and upgrades | Needs Migration |
| **forgero-schematics** | Extended schematics (mastercrafted, refined) | Partial |
| **forgero-mining** | Mining-related content | Needs Migration |
| **forgero-compat** | Mod compatibility materials | Legacy (429 packs) |

### Other Active Modules

| Module | Purpose | Status |
|--------|---------|--------|
| **forgero-armor** | Armor system tags/equipment | Modern |
| **forgero-armor-content** | Armor parts and models | Modern |
| **forgero-structures** | Structure templates | Active |
| **vanilla-upgrades-base** | Vanilla upgrade system | Modern |

### Legacy Modules (To Be Removed)

| Module | Files | Notes |
|--------|-------|-------|
| **forgero-vanilla** | 185 packs | Core content migrated, variants remain |
| **forgero-tools** | 18 files | Test/partial content |
| **forgero-deprecated** | 68 packs | Archive only |
| **minecraft-tools** | 3 tags | Minimal |

---

## Core Module Content (Complete)

### forgero-base
```
data/forgero/
├── equipment/tools/     # 5 tools (axe, hoe, pickaxe, shovel, sword)
├── parts/
│   ├── bindings/        # binding.json
│   ├── blades/          # sword_blade.json
│   ├── guards/          # sword_guard.json
│   ├── handles/         # handle.json
│   ├── heads/           # axe_head, hoe_head, pickaxe_head, shovel_head
│   └── pommels/         # pommel.json
├── schematics/          # 11 base schematics
├── forgero_models/
│   ├── item/            # 15 item model templates
│   └── upgrade/         # 2 upgrade models
└── tags/                # 39 tag definitions

assets/forgero/
├── texture_template/parts/   # 47 part texture templates
└── textures/item/            # 37 item textures (outlines, schematics)
```

### forgero-materials
```
data/forgero/
├── materials/
│   ├── bone/
│   ├── glass/
│   ├── leather/
│   ├── metal/           # iron, copper, gold, netherite
│   ├── mineral/
│   ├── stone/
│   └── wood/
└── tags/

assets/forgero/
└── textures/palette/    # 186 material palettes
```

---

## Pending Migration

### From forgero-vanilla (106 files)

| Pack | Files | Target Module |
|------|-------|---------------|
| minecraft-material/secondary-materials/ | 36 | forgero-extended |
| minecraft-material/dye-variants/ | 16 | forgero-extended |
| minecraft-material/material-baseline-variants/ | 16 | forgero-extended |
| minecraft-material/type/ | 20 | forgero-extended |
| forgero-scrappy-variants/ | 6 | forgero-extended |
| Remaining tool-material variants | ~12 | forgero-extended |

### From forgero-extended (170 files)

| Pack | Files | Target Module |
|------|-------|---------------|
| forgero-gems | 39 | forgero-gems |
| forgero-weapon-types | 22 | forgero-extended-weapons |
| forgero-imperial | 21 | forgero-extended-weapons |
| forgero-mastercrafted | 17 | forgero-schematics |
| forgero-refined | 17 | forgero-schematics |
| forgero-guards | 17 | forgero-extended |
| forgero-exotic | 13 | forgero-extended |
| forgero-broad_mining | 11 | forgero-mining |
| forgero-lightweight | 8 | forgero-extended |
| forgero-trinkets | 5 | forgero-extended |

### From forgero-compat (429 files)

All mod compatibility packs remain in legacy format. These define materials for:
- Better End (50 files)
- Better Nether (32 files)
- Regions Unexplored (32 files)
- Modern Industrialization (29 files)
- Biomes We've Gone (24 files)
- Nature's Spirit (22 files)
- Mythic Metals (21 files)
- Tech Reborn (16 files)
- Create (11 files)
- 27 other mod packs (~192 files)

---

## Migration Summary

| Category | Files | Status |
|----------|-------|--------|
| Core content | ~80 | Complete |
| Extended content | ~276 | Pending |
| Mod compatibility | 429 | Legacy (keep as-is or migrate later) |
| Deprecated | 68 | Archive only |

**Total migrated:** ~80 files
**Total pending:** ~705 files (276 extended + 429 compat)

---

## Deleted Modules

These modules were duplicates and have been removed:
- `minecraft-vanilla-materials` (duplicate of forgero-materials)
- `shared-materials` (duplicate of forgero-materials)
- `forgero-equipment` (migrated to forgero-base)
- `forgero-models` (migrated to forgero-base)
- `forgero-tools-content` (migrated to forgero-base)

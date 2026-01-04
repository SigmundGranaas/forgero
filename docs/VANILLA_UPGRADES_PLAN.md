# Vanilla Upgrades Mod - Analysis and Completion Plan

## Executive Summary

This document analyzes the current state of the Forgero and Vanilla Upgrades mods, identifies what's required to finish the Vanilla Upgrades mod, and proposes design principles for independent yet complementary content packs.

---

## 1. Current Architecture Overview

### 1.1 Two Primary Mods

| Mod | Purpose | Target Users |
|-----|---------|--------------|
| **Forgero** | Full tool part crafting system with custom Forgero tools, weapons, armor | Players wanting deep customization and new content |
| **Vanilla Upgrades** | Add upgrade slots to vanilla MC tools/weapons | Players wanting to enhance vanilla items without new tool types |

### 1.2 Shared Engine Architecture

Both mods use the same underlying engine (`modules/core`, `modules/mc/*`) with different content packs:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Core Engine                               │
│  modules/core (Component system, tags, identifiers, pipeline)   │
│  modules/mc/loader (ForgeroApi, plugin system)                  │
│  modules/mc/properties (Effects, handlers, selectors)           │
│  modules/mc/tools (Tool item implementations)                   │
│  modules/mc/armor (Armor implementations)                       │
│  modules/mc/bows (Bow/arrow implementations)                    │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
        ┌───────▼────────┐              ┌──────▼────────┐
        │    Forgero     │              │Vanilla Upgrades│
        │      Mod       │              │      Mod       │
        ├────────────────┤              ├───────────────┤
        │ Content Packs: │              │ Content Packs:│
        │ - forgero-base │              │ - forgero-base│
        │ - forgero-     │              │ - forgero-    │
        │   materials    │              │   materials   │
        │ - forgero-     │              │ - forgero-    │
        │   upgrades     │              │   upgrades    │
        │ - forgero-     │              │ - vanilla-    │
        │   tools        │              │   upgrades-   │
        │ - forgero-     │              │   base        │
        │   extended     │              └───────────────┘
        │ - forgero-     │
        │   armor        │
        │ - etc...       │
        └────────────────┘
```

### 1.3 Content Pack Dependencies

**Current vanilla-upgrades dependency chain:**
```
vanilla-upgrades-base
  └── forgero-materials (material definitions)
      └── (no dependencies)

forgero-upgrades (upgrade conditions like "sharp", "durable")
  └── (no dependencies)

forgero-base (tags, base schematics)
  └── (no dependencies)
```

---

## 2. Current State Analysis

### 2.1 Vanilla Upgrades - What Exists

#### Tools Defined (30 total)
| Tool Type | Tiers Covered | File Count |
|-----------|---------------|------------|
| Pickaxes | Wooden, Stone, Iron, Golden, Diamond, Netherite | 6 |
| Axes | Wooden, Stone, Iron, Golden, Diamond, Netherite | 6 |
| Swords | Wooden, Stone, Iron, Golden, Diamond, Netherite | 6 |
| Shovels | Wooden, Stone, Iron, Golden, Diamond, Netherite | 6 |
| Hoes | Wooden, Stone, Iron, Golden, Diamond, Netherite | 6 |

#### Upgrade Slot Distribution
| Tier | Binding Slot | Tip Reinforcement | Gem Slot | Total Slots |
|------|--------------|-------------------|----------|-------------|
| Wooden | ✓ | - | - | 1 |
| Stone | ✓ | - | - | 1 |
| Iron | ✓ | - | - | 1 |
| Golden | ✓ | - | - | 1 |
| Diamond | ✓ | ✓ | - | 2 |
| Netherite | ✓ | ✓ | ✓ | 3 |

### 2.2 Vanilla Upgrades - What's Missing

#### Missing Item Categories

1. **Vanilla Armor (20 items)**
   - Leather: Helmet, Chestplate, Leggings, Boots
   - Chainmail: Helmet, Chestplate, Leggings, Boots
   - Iron: Helmet, Chestplate, Leggings, Boots
   - Golden: Helmet, Chestplate, Leggings, Boots
   - Diamond: Helmet, Chestplate, Leggings, Boots
   - Netherite: Helmet, Chestplate, Leggings, Boots (not in 1.20.1 vanilla)
   - **Actually 24 items** (6 tiers × 4 pieces)

2. **Vanilla Ranged Weapons (4 items)**
   - Bow
   - Crossbow
   - Trident
   - Shield

3. **Vanilla Utility Tools (4 items)**
   - Shears
   - Flint and Steel
   - Fishing Rod
   - Carrot on a Stick

#### Missing Features

1. **No icon/branding assets** (`assets/vanilla-upgrades/icon.png` missing)
2. **No language files** (lang/*.json)
3. **No recipe integration** for applying upgrades
4. **No CurseForge/Modrinth publishing config**
5. **No documentation/wiki content**

### 2.3 Forgero Mod - Current State

The main Forgero mod is more mature with:
- Full tool/weapon/armor crafting system
- Template-based part generation
- Recipe generators
- Model generation
- Multiple content packs (extended weapons, mining tools, gems)
- Publishing to CurseForge and Modrinth
- Localization (en_us, ko_kr, zh_cn)

---

## 3. Design Principles for Content Pack Independence

### 3.1 Principle: Minimal Shared Dependencies

Content packs should only depend on what they need:

```
                    ┌─────────────────┐
                    │  forgero-base   │  (Core tags, base schematics)
                    │  (Foundation)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
     ┌────────▼─────┐  ┌─────▼──────┐  ┌───▼────────────┐
     │forgero-      │  │forgero-    │  │forgero-upgrades│
     │materials     │  │tools       │  │(conditions)    │
     │(iron, oak...)│  │(templates) │  └────────────────┘
     └──────┬───────┘  └─────┬──────┘
            │                │
            │    ┌───────────┘
            │    │
       ┌────▼────▼────┐           ┌────────────────────┐
       │ Forgero Mod  │           │  vanilla-upgrades- │
       │  (Full)      │           │  base              │
       └──────────────┘           │  (depends ONLY on  │
                                  │   forgero-materials│
                                  │   and forgero-base)│
                                  └────────────────────┘
```

### 3.2 Principle: Use `static_part` for External Items

Items from vanilla MC or other mods should use `forgero:static_part` type:

```json
{
  "type": "forgero:static_part",
  "name": "Iron Pickaxe",
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:iron_pickaxe" }
    ]
  },
  "attributes": [...],
  "upgrades": [...]
}
```

This allows adding Forgero capabilities to any existing item without replacing it.

### 3.3 Principle: Tag-Based Slot Validation

Upgrade slots should validate via tags, not hard-coded types:

```json
"upgrades": [
  {
    "id": "forgero:binding-slot",
    "type": "forgero:materials/roles/upgrade_material",
    "tags": ["forgero:materials/types/binding"],
    "description": "slot.forgero.binding"
  }
]
```

This allows any material tagged with `forgero:materials/types/binding` to be used.

### 3.4 Principle: Tier-Based Slot Progression

Higher-tier items should have more upgrade slots:

| Tier | Slots | Reasoning |
|------|-------|-----------|
| 0 (Wood/Leather) | 0-1 | Basic tier, minimal customization |
| 1 (Stone/Chainmail) | 1 | Early game progression |
| 2 (Iron/Gold) | 1 | Mid-game |
| 3 (Diamond) | 2 | Late-game, more customization |
| 4 (Netherite) | 3 | End-game, maximum customization |

### 3.5 Principle: Namespace Separation

- Forgero tools: `forgero:iron-pickaxe_head`, `forgero:oak-handle`
- Vanilla upgrades: Uses `minecraft:iron_pickaxe` via host identifier

This prevents ID conflicts and makes compatibility clear.

---

## 4. Completion Roadmap for Vanilla Upgrades

### Phase 1: Complete Tool Definitions (Current State: DONE)

- [x] All 30 vanilla tools defined
- [x] Proper attribute values matching vanilla
- [x] Upgrade slots by tier

### Phase 2: Add Vanilla Armor Support

#### 4.1 Create Armor Static Parts

Create 24 armor definition files in `vanilla-upgrades-base/src/main/resources/data/forgero/parts/armor/`:

**Structure:**
```
parts/armor/
├── leather/
│   ├── leather_helmet.json
│   ├── leather_chestplate.json
│   ├── leather_leggings.json
│   └── leather_boots.json
├── chainmail/
│   ├── chainmail_helmet.json
│   ├── chainmail_chestplate.json
│   ├── chainmail_leggings.json
│   └── chainmail_boots.json
├── iron/
│   ├── iron_helmet.json
│   ├── iron_chestplate.json
│   ├── iron_leggings.json
│   └── iron_boots.json
├── golden/
│   ├── golden_helmet.json
│   ├── golden_chestplate.json
│   ├── golden_leggings.json
│   └── golden_boots.json
├── diamond/
│   ├── diamond_helmet.json
│   ├── diamond_chestplate.json
│   ├── diamond_leggings.json
│   └── diamond_boots.json
└── netherite/
    ├── netherite_helmet.json
    ├── netherite_chestplate.json
    ├── netherite_leggings.json
    └── netherite_boots.json
```

**Example armor definition (`iron_chestplate.json`):**
```json
{
  "type": "forgero:static_part",
  "name": "Iron Chestplate",
  "tags": [
    "forgero:armor/chestplate",
    "forgero:armor/armor",
    "forgero:uncategorized/vanilla_armor"
  ],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:iron_chestplate" }
    ]
  },
  "attributes": [
    { "id": "forgero:iron-chestplate-durability", "type": "forgero:durability", "computation": 240 },
    { "id": "forgero:iron-chestplate-armor", "type": "forgero:armor", "computation": 6 },
    { "id": "forgero:iron-chestplate-armor_toughness", "type": "forgero:armor_toughness", "computation": 0 }
  ],
  "upgrades": [
    {
      "id": "forgero:lining-slot",
      "type": "forgero:materials/roles/upgrade_material",
      "tags": ["forgero:materials/roles/armor_lining_material"],
      "description": "slot.forgero.lining"
    }
  ]
}
```

#### Vanilla Armor Attributes Reference

| Armor | Durability | Armor Points | Toughness | Knockback Resistance |
|-------|------------|--------------|-----------|---------------------|
| **Leather** |||||
| Helmet | 55 | 1 | 0 | 0 |
| Chestplate | 80 | 3 | 0 | 0 |
| Leggings | 75 | 2 | 0 | 0 |
| Boots | 65 | 1 | 0 | 0 |
| **Chainmail** |||||
| Helmet | 165 | 2 | 0 | 0 |
| Chestplate | 240 | 5 | 0 | 0 |
| Leggings | 225 | 4 | 0 | 0 |
| Boots | 195 | 1 | 0 | 0 |
| **Iron** |||||
| Helmet | 165 | 2 | 0 | 0 |
| Chestplate | 240 | 6 | 0 | 0 |
| Leggings | 225 | 5 | 0 | 0 |
| Boots | 195 | 2 | 0 | 0 |
| **Golden** |||||
| Helmet | 77 | 2 | 0 | 0 |
| Chestplate | 112 | 5 | 0 | 0 |
| Leggings | 105 | 3 | 0 | 0 |
| Boots | 91 | 1 | 0 | 0 |
| **Diamond** |||||
| Helmet | 363 | 3 | 2 | 0 |
| Chestplate | 528 | 8 | 2 | 0 |
| Leggings | 495 | 6 | 2 | 0 |
| Boots | 429 | 3 | 2 | 0 |
| **Netherite** |||||
| Helmet | 407 | 3 | 3 | 0.1 |
| Chestplate | 592 | 8 | 3 | 0.1 |
| Leggings | 555 | 6 | 3 | 0.1 |
| Boots | 481 | 3 | 3 | 0.1 |

#### Armor Upgrade Slots by Tier

| Tier | Lining Slot | Reinforcement | Gem Slot |
|------|-------------|---------------|----------|
| Leather | ✓ | - | - |
| Chainmail | ✓ | - | - |
| Iron | ✓ | - | - |
| Golden | ✓ | ✓ | - |
| Diamond | ✓ | ✓ | - |
| Netherite | ✓ | ✓ | ✓ |

### Phase 3: Add Ranged Weapons Support

#### Bow Definition (`bow.json`)
```json
{
  "type": "forgero:static_part",
  "name": "Bow",
  "tags": [
    "forgero:weapons/bow",
    "forgero:uncategorized/vanilla_weapon"
  ],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:bow" }
    ]
  },
  "attributes": [
    { "id": "forgero:bow-durability", "type": "forgero:durability", "computation": 384 },
    { "id": "forgero:bow-draw_speed", "type": "forgero:draw_speed", "computation": 1.0 }
  ],
  "upgrades": [
    {
      "id": "forgero:string-slot",
      "type": "forgero:materials/roles/upgrade_material",
      "tags": ["forgero:materials/types/string"],
      "description": "slot.forgero.string"
    }
  ]
}
```

#### Crossbow Definition (`crossbow.json`)
```json
{
  "type": "forgero:static_part",
  "name": "Crossbow",
  "tags": [
    "forgero:weapons/crossbow",
    "forgero:uncategorized/vanilla_weapon"
  ],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:crossbow" }
    ]
  },
  "attributes": [
    { "id": "forgero:crossbow-durability", "type": "forgero:durability", "computation": 465 },
    { "id": "forgero:crossbow-draw_speed", "type": "forgero:draw_speed", "computation": 1.25 }
  ],
  "upgrades": [
    {
      "id": "forgero:mechanism-slot",
      "type": "forgero:materials/roles/upgrade_material",
      "tags": ["forgero:upgrades/types/mechanism"],
      "description": "slot.forgero.mechanism"
    }
  ]
}
```

### Phase 4: Add Utility Items

- Trident (with loyalty/channeling upgrade potential)
- Shield (with reinforcement slots)
- Shears (optional, low priority)
- Fishing Rod (optional, low priority)

### Phase 5: Mod Infrastructure

#### 5.1 Add Branding Assets

Create `mods/vanilla-upgrades/src/main/resources/assets/vanilla-upgrades/`:
- `icon.png` (128x128 mod icon)

#### 5.2 Add Language Files

Create `mods/vanilla-upgrades/src/main/resources/assets/vanilla-upgrades/lang/en_us.json`:
```json
{
  "slot.forgero.binding": "Binding Slot",
  "slot.forgero.tip_reinforcement": "Tip Reinforcement Slot",
  "slot.forgero.gem": "Gem Slot",
  "slot.forgero.lining": "Armor Lining Slot",
  "slot.forgero.string": "String Slot",
  "slot.forgero.mechanism": "Mechanism Slot",

  "modmenu.summaryTranslation.vanilla-upgrades": "Add upgrade slots to vanilla tools, weapons, and armor"
}
```

#### 5.3 Add Publishing Configuration

Update `mods/vanilla-upgrades/build.gradle` with CurseForge and Modrinth publishing:
```gradle
plugins {
    id "com.modrinth.minotaur" version "2.+"
    id 'com.matthewprenger.cursegradle' version '1.4.0'
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_API_KEY") ?: ""
    project {
        id = 'XXXXXX'  // Get from CurseForge
        // ... config
    }
}

modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "vanilla-upgrades"
    // ... config
}
```

### Phase 6: Testing

#### 6.1 Expand Test Coverage

Add tests for:
- All armor conversions and attributes
- Bow/crossbow conversions
- Upgrade application to armor
- Armor attribute modification via upgrades

#### 6.2 Integration Tests

- Test that Forgero and Vanilla Upgrades can run together
- Test that vanilla items aren't broken
- Test upgrade application through crafting UI

---

## 5. Content Pack Design for Mod Compatibility

### 5.1 Template: Adding Upgradeability to Any Mod's Items

Vanilla Upgrades serves as a template for adding Forgero capabilities to other mods. Here's the pattern:

#### Step 1: Create Content Pack Module

```
content/mythicmetals-upgrades/
├── build.gradle
└── src/main/resources/
    ├── fabric.mod.json
    └── data/forgero/parts/
        └── tools/
            ├── adamantite_pickaxe.json
            ├── celestium_sword.json
            └── ...
```

#### Step 2: Define Static Parts

```json
{
  "type": "forgero:static_part",
  "name": "Adamantite Pickaxe",
  "tags": [
    "forgero:tools/pickaxe",
    "forgero:uncategorized/mod_tool",
    "mythicmetals:adamantite_tool"
  ],
  "host": {
    "identifiers": [
      { "type": "item", "id": "mythicmetals:adamantite_pickaxe" }
    ]
  },
  "attributes": [
    { "id": "mythicmetals:adamantite-pickaxe-durability", "type": "forgero:durability", "computation": 2500 },
    { "id": "mythicmetals:adamantite-pickaxe-attack_damage", "type": "forgero:attack_damage", "computation": 5 },
    { "id": "mythicmetals:adamantite-pickaxe-mining_speed", "type": "forgero:mining_speed", "computation": 10 },
    { "id": "mythicmetals:adamantite-pickaxe-mining_level", "type": "forgero:mining_level", "computation": 4 }
  ],
  "upgrades": [
    {
      "id": "forgero:binding-slot",
      "type": "forgero:materials/roles/upgrade_material",
      "tags": ["forgero:materials/types/binding"],
      "description": "slot.forgero.binding"
    },
    {
      "id": "forgero:gem-slot",
      "type": "forgero:materials/roles/upgrade_material",
      "tags": ["forgero:upgrades/types/gem"],
      "tier": 2,
      "description": "slot.forgero.gem"
    }
  ]
}
```

#### Step 3: Set Up Dependencies

```json
// fabric.mod.json
{
  "depends": {
    "forgero-core": "*",
    "forgero-materials": "*",
    "mythicmetals": "*"
  }
}
```

### 5.2 Compatibility Matrix

| Scenario | Behavior |
|----------|----------|
| Only Forgero installed | Full Forgero tools, no vanilla upgrades |
| Only Vanilla Upgrades installed | Vanilla tools get upgrade slots |
| Both installed | Both systems work, can mix |
| Vanilla Upgrades + Mod Compat Pack | Mod items get upgrade slots |

---

## 6. Task Checklist

### Immediate Tasks (High Priority)

- [ ] Create armor static part definitions (24 files)
- [ ] Add armor attribute values matching vanilla
- [ ] Create bow/crossbow definitions
- [ ] Add mod icon asset
- [ ] Add en_us language file

### Medium Priority

- [ ] Add tests for armor conversion
- [ ] Add tests for ranged weapon conversion
- [ ] Update fabric.mod.json with better metadata
- [ ] Add ModMenu integration

### Low Priority (Future)

- [ ] Trident definition
- [ ] Shield definition
- [ ] Shears definition
- [ ] Publishing configuration
- [ ] Additional language translations

### Documentation Tasks

- [ ] Write user-facing documentation
- [ ] Create wiki pages
- [ ] Document upgrade slot types and valid materials
- [ ] Create modpack maker guide for adding support to other mods

---

## 7. File Inventory

### Files to Create

```
content/vanilla-upgrades-base/src/main/resources/data/forgero/parts/armor/
├── leather/
│   ├── leather_helmet.json
│   ├── leather_chestplate.json
│   ├── leather_leggings.json
│   └── leather_boots.json
├── chainmail/
│   ├── chainmail_helmet.json
│   ├── chainmail_chestplate.json
│   ├── chainmail_leggings.json
│   └── chainmail_boots.json
├── iron/
│   ├── iron_helmet.json
│   ├── iron_chestplate.json
│   ├── iron_leggings.json
│   └── iron_boots.json
├── golden/
│   ├── golden_helmet.json
│   ├── golden_chestplate.json
│   ├── golden_leggings.json
│   └── golden_boots.json
├── diamond/
│   ├── diamond_helmet.json
│   ├── diamond_chestplate.json
│   ├── diamond_leggings.json
│   └── diamond_boots.json
└── netherite/
    ├── netherite_helmet.json
    ├── netherite_chestplate.json
    ├── netherite_leggings.json
    └── netherite_boots.json

content/vanilla-upgrades-base/src/main/resources/data/forgero/parts/ranged/
├── bow.json
└── crossbow.json

mods/vanilla-upgrades/src/main/resources/assets/vanilla-upgrades/
├── icon.png
└── lang/
    └── en_us.json
```

### Files to Modify

```
mods/vanilla-upgrades/build.gradle           # Add publishing config
mods/vanilla-upgrades/src/main/resources/fabric.mod.json  # Update metadata
content/vanilla-upgrades-base/src/main/resources/fabric.mod.json  # Update deps
```

---

## 8. Estimated Scope

| Category | Items | Est. Files | Complexity |
|----------|-------|------------|------------|
| Armor Definitions | 24 | 24 | Low (templated) |
| Ranged Weapons | 2-4 | 2-4 | Medium |
| Utility Items | 2-4 | 2-4 | Low |
| Assets | 2 | 2 | Low |
| Build Config | 1 | 1 | Low |
| Tests | 3-5 | 3-5 | Medium |
| **Total** | **~35** | **~40** | - |

---

## 9. Conclusion

The Vanilla Upgrades mod is well-architected with a clear separation from the main Forgero mod. The foundation is solid with all 30 vanilla tools defined. To complete the mod:

1. **Add armor support** (24 items) - highest priority
2. **Add ranged weapons** (2-4 items) - medium priority
3. **Add mod infrastructure** (icon, lang, publishing) - medium priority
4. **Expand test coverage** - ongoing

The design principles established here can serve as a template for creating compatibility packs for any mod's tools and weapons, making Vanilla Upgrades a demonstration of Forgero's extensibility.

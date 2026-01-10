# Vanilla Upgrades Mod - Implementation Plan

> **Status: 67% COMPLETE** (January 2026)
>
> Tools and armor are fully implemented. Remaining: ranged weapons, utility items, and mod infrastructure.

---

## Quick Status

| Category | Planned | Complete | Status |
|----------|---------|----------|--------|
| Tools | 30 | 30 | ✅ Done |
| Armor | 24 | 24 | ✅ Done |
| Ranged Weapons | 4 | 0 | ❌ Pending |
| Utility Items | 4 | 0 | ❌ Pending |
| Infrastructure | 5 | 0 | ❌ Pending |

---

## 1. Architecture Overview

### Two Primary Mods

| Mod | Purpose | Target Users |
|-----|---------|--------------|
| **Forgero** | Full tool part crafting system with custom Forgero tools, weapons, armor | Players wanting deep customization and new content |
| **Vanilla Upgrades** | Add upgrade slots to vanilla MC tools/weapons | Players wanting to enhance vanilla items without new tool types |

### Shared Engine

Both mods use the same underlying engine (`modules/core`, `modules/mc/*`) with different content packs:

```
┌─────────────────────────────────────────────────────────────────┐
│                        Core Engine                               │
│  modules/core, modules/mc/loader, modules/mc/properties, etc.   │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
        ┌───────▼────────┐              ┌──────▼────────┐
        │    Forgero     │              │Vanilla Upgrades│
        │      Mod       │              │      Mod       │
        ├────────────────┤              ├───────────────┤
        │ forgero-base   │              │ forgero-base  │
        │ forgero-*      │              │ forgero-      │
        │ (all packs)    │              │   materials   │
        └────────────────┘              │ vanilla-      │
                                        │   upgrades-   │
                                        │   base        │
                                        └───────────────┘
```

---

## 2. Completed Work

### Phase 1: Tools ✅ COMPLETE

All 30 vanilla tools defined in `content/vanilla-upgrades-base/src/main/resources/data/forgero/parts/`:

| Tool Type | Files | Location |
|-----------|-------|----------|
| Pickaxes | 6 | `parts/pickaxes/` |
| Axes | 6 | `parts/axes/` |
| Swords | 6 | `parts/swords/` |
| Shovels | 6 | `parts/shovels/` |
| Hoes | 6 | `parts/hoes/` |

**Upgrade Slots by Tier:**
| Tier | Binding | Tip Reinforcement | Gem | Total |
|------|---------|-------------------|-----|-------|
| Wooden | ✓ | - | - | 1 |
| Stone | ✓ | - | - | 1 |
| Iron | ✓ | - | - | 1 |
| Golden | ✓ | - | - | 1 |
| Diamond | ✓ | ✓ | - | 2 |
| Netherite | ✓ | ✓ | ✓ | 3 |

### Phase 2: Armor ✅ COMPLETE

All 24 armor pieces defined in `content/vanilla-upgrades-base/src/main/resources/data/forgero/parts/armor/`:

| Material | Helmet | Chestplate | Leggings | Boots |
|----------|--------|------------|----------|-------|
| Leather | ✅ | ✅ | ✅ | ✅ |
| Chainmail | ✅ | ✅ | ✅ | ✅ |
| Iron | ✅ | ✅ | ✅ | ✅ |
| Golden | ✅ | ✅ | ✅ | ✅ |
| Diamond | ✅ | ✅ | ✅ | ✅ |
| Netherite | ✅ | ✅ | ✅ | ✅ |

---

## 3. Remaining Work

### Phase 3: Ranged Weapons ❌ PENDING

Create static parts for vanilla ranged weapons in `parts/ranged/`:

| Item | File | Priority |
|------|------|----------|
| Bow | `bow.json` | High |
| Crossbow | `crossbow.json` | High |
| Trident | `trident.json` | Low |
| Shield | `shield.json` | Low |

**Example Bow Definition:**
```json
{
  "type": "forgero:static_part",
  "name": "Bow",
  "tags": ["forgero:weapons/bow", "forgero:uncategorized/vanilla_weapon"],
  "host": {
    "identifiers": [{ "type": "item", "id": "minecraft:bow" }]
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

### Phase 4: Utility Items ❌ PENDING (Low Priority)

| Item | File | Priority |
|------|------|----------|
| Shears | `shears.json` | Low |
| Fishing Rod | `fishing_rod.json` | Low |
| Flint and Steel | `flint_and_steel.json` | Low |

### Phase 5: Mod Infrastructure ❌ PENDING

| Task | File | Status |
|------|------|--------|
| Mod icon | `assets/vanilla-upgrades/icon.png` | Missing |
| Language file | `assets/vanilla-upgrades/lang/en_us.json` | Missing |
| Publishing config | `build.gradle` | Not configured |
| ModMenu integration | `fabric.mod.json` | Not configured |

**Language file content needed:**
```json
{
  "slot.forgero.binding": "Binding Slot",
  "slot.forgero.tip_reinforcement": "Tip Reinforcement Slot",
  "slot.forgero.gem": "Gem Slot",
  "slot.forgero.lining": "Armor Lining Slot",
  "slot.forgero.string": "String Slot",
  "modmenu.summaryTranslation.vanilla-upgrades": "Add upgrade slots to vanilla tools, weapons, and armor"
}
```

### Phase 6: Testing ❌ PENDING

| Test | Description | Status |
|------|-------------|--------|
| Armor conversion | Test all armor pieces convert correctly | Missing |
| Armor attributes | Test attribute values match vanilla | Missing |
| Ranged conversion | Test bow/crossbow conversion | Missing |
| Upgrade application | Test upgrades can be applied | Missing |

---

## 4. Design Principles

### Use `static_part` for External Items

Items from vanilla MC use `forgero:static_part` type to add Forgero capabilities without replacing the item:

```json
{
  "type": "forgero:static_part",
  "name": "Iron Pickaxe",
  "host": {
    "identifiers": [{ "type": "item", "id": "minecraft:iron_pickaxe" }]
  },
  "attributes": [...],
  "upgrades": [...]
}
```

### Tag-Based Slot Validation

Upgrade slots validate via tags, allowing any material with the right tag:

```json
"upgrades": [
  {
    "id": "forgero:binding-slot",
    "type": "forgero:materials/roles/upgrade_material",
    "tags": ["forgero:materials/types/binding"]
  }
]
```

### Tier-Based Slot Progression

| Tier | Slots | Items |
|------|-------|-------|
| 0-2 | 1 | Wood, Stone, Iron, Gold, Leather, Chainmail |
| 3 | 2 | Diamond |
| 4 | 3 | Netherite |

---

## 5. Vanilla Attribute Reference

### Armor Durability & Protection

| Armor | Durability | Armor | Toughness | Knockback Res |
|-------|------------|-------|-----------|---------------|
| Leather Helmet | 55 | 1 | 0 | 0 |
| Leather Chestplate | 80 | 3 | 0 | 0 |
| Leather Leggings | 75 | 2 | 0 | 0 |
| Leather Boots | 65 | 1 | 0 | 0 |
| Iron Helmet | 165 | 2 | 0 | 0 |
| Iron Chestplate | 240 | 6 | 0 | 0 |
| Iron Leggings | 225 | 5 | 0 | 0 |
| Iron Boots | 195 | 2 | 0 | 0 |
| Diamond Helmet | 363 | 3 | 2 | 0 |
| Diamond Chestplate | 528 | 8 | 2 | 0 |
| Diamond Leggings | 495 | 6 | 2 | 0 |
| Diamond Boots | 429 | 3 | 2 | 0 |
| Netherite Helmet | 407 | 3 | 3 | 0.1 |
| Netherite Chestplate | 592 | 8 | 3 | 0.1 |
| Netherite Leggings | 555 | 6 | 3 | 0.1 |
| Netherite Boots | 481 | 3 | 3 | 0.1 |

### Ranged Weapon Stats

| Weapon | Durability | Draw Speed |
|--------|------------|------------|
| Bow | 384 | 1.0 |
| Crossbow | 465 | 1.25 |
| Trident | 250 | - |

---

## 6. Task Checklist

### High Priority
- [ ] Create `bow.json` static part
- [ ] Create `crossbow.json` static part
- [ ] Add mod icon
- [ ] Add language file

### Medium Priority
- [ ] Create `trident.json` static part
- [ ] Create `shield.json` static part
- [ ] Add conversion tests
- [ ] Add attribute tests

### Low Priority
- [ ] Create utility item definitions (shears, fishing rod)
- [ ] Publishing configuration
- [ ] Additional translations
- [ ] Wiki documentation

---

## 7. Summary

The Vanilla Upgrades mod has a solid foundation with all tools and armor complete. The remaining work is primarily:

1. **Ranged weapons** (bow, crossbow) - High priority, extends functionality
2. **Mod infrastructure** (icon, lang) - Medium priority, improves polish
3. **Utility items** - Low priority, nice to have

Total completion: **54 of 62 items** (87% of content), but infrastructure and testing still needed.

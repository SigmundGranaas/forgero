# Forgero Content Pack Architecture Overview

## Module Hierarchy

```
content/
├── FOUNDATION LAYER
│   ├── forgero-base/          # Base templates, tags, upgrade slots
│   ├── forgero-materials/     # Core material definitions (metal, wood, stone, etc.)
│   └── forgero-schematics/    # Schematic blueprints (refined, mastercrafted)
│
├── COMPONENT LAYER
│   ├── forgero-tools/         # Tool equipment templates (pickaxe, axe, shovel, hoe)
│   ├── forgero-armor/         # Armor equipment templates (helmet, chestplate, etc.)
│   └── forgero-armor-content/ # Armor models and extended definitions
│
├── ENHANCEMENT LAYER
│   ├── forgero-secondary-materials/   # Secondary materials (soft, hard, hybrid, dyes)
│   ├── forgero-schematics/            # Quality schematics (refined, mastercrafted)
│   ├── forgero-extended-schematics/   # Extended schematics (blades, heads, guards, bindings)
│   ├── forgero-mining/                # Mining tools (hammer, mandrill, spade)
│   ├── forgero-extended-weapons/      # Weapon variants (katana, rapier, etc.)
│   ├── forgero-gems/                  # Gem materials with quality tiers
│   └── forgero-upgrades/              # Upgrade material definitions
│
├── INTEGRATION LAYER
│   ├── vanilla-upgrades-base/         # Wraps vanilla tools as static parts
│   └── minecraft-tools/               # Minecraft tool integrations
│
├── LEGACY LAYER (Read-Only Reference)
│   ├── forgero-vanilla-legacy-read-only/   # Old pack format - recipe generators
│   ├── forgero-extended-legacy-read-only/  # Old pack format - reference only
│   └── forgero-compat-legacy-read-only/    # Old mod compatibility
│
└── SPECIALTY LAYER
    ├── forgero-structures/        # World generation (forges, smithies)
    └── forgero-deprecated/        # Legacy content
```

---

## Tag System Architecture

### Hierarchical Tag Structure

```
                            ┌─────────────────┐
                            │  ROOT TAGS {}   │
                            │  (empty parent) │
                            └────────┬────────┘
                                     │
        ┌────────────────────────────┼────────────────────────────┐
        │                            │                            │
        ▼                            ▼                            ▼
┌───────────────┐          ┌─────────────────┐          ┌─────────────────┐
│   materials/  │          │     tools/      │          │     armor/      │
│   material    │          │      tool       │          │     armor       │
└───────┬───────┘          └────────┬────────┘          └────────┬────────┘
        │                           │                            │
   ┌────┴────┬────┬────┐      ┌─────┼─────┐              ┌───────┼───────┐
   │         │    │    │      │     │     │              │       │       │
   ▼         ▼    ▼    ▼      ▼     ▼     ▼              ▼       ▼       ▼
┌──────┐ ┌─────┐ ┌────┐     pickaxe axe sword       helmet  chestplate  boots
│metal │ │wood │ │stone│     shovel  hoe                    leggings
└──────┘ └─────┘ └─────┘
 bone    glass   leather
 mineral
```

### Material Tags (`forgero:materials/`)

```
materials/material  (root)
    │
    ├── Category Tags (physical type)
    │   ├── metal      → iron, gold, copper, netherite
    │   ├── wood       → oak, birch, spruce, acacia, etc.
    │   ├── stone      → stone, granite, diorite, andesite
    │   ├── bone       → bone
    │   ├── glass      → glass
    │   ├── leather    → leather
    │   └── mineral    → coal, redstone, glowstone
    │
    ├── Functional Tags (what it can craft)
    │   ├── tool_material        → can craft tool heads
    │   ├── armor_material       → can craft armor plates
    │   └── armor_lining_material → can craft armor linings
    │
    └── Secondary/Modifier Tags
        ├── soft         → flexible upgrade materials
        ├── hard         → rigid upgrade materials
        ├── hybrid       → mixed behavior materials
        ├── binding      → binding upgrade materials
        ├── plant        → plant-based materials
        └── upgrade_material → any upgrade material
```

### Tool Tags (`forgero:tools/`)

```
tool (root) ─┬─► pickaxe
             ├─► axe
             ├─► shovel
             ├─► hoe
             └─► sword
```

### Upgrade Tags (`forgero:upgrade/`)

```
upgrade/
    ├── reinforcement      → head reinforcement slots
    ├── tip_reinforcement  → tip-specific reinforcements
    ├── binding            → handle bindings
    ├── grip               → handle grips
    ├── gem                → gem socketing
    └── cosmetic           → visual modifications (dyes)
```

### Part Tags (`forgero:parts/`)

```
parts/
    ├── Type Tags (what kind of part)
    │   ├── pickaxe_head_type
    │   ├── handle_type
    │   ├── binding_type
    │   ├── armor_plate_type
    │   └── armor_lining_type
    │
    └── Default Tags (fallback selection)
        ├── default_pickaxe_head
        ├── default_handle
        ├── default_binding
        ├── default_armor_plate
        └── default_armor_lining
```

### Schematic Tags (`forgero:schematics/`)

```
schematics/
    ├── Category Tags
    │   ├── heads      → head schematics
    │   ├── blades     → blade schematics
    │   ├── handles    → handle schematics
    │   └── bindings   → binding schematics
    │
    └── Quality Tags
        ├── refined       → +25% stats, +1 upgrade slot
        └── mastercrafted → +50% stats, +2 upgrade slots
```

---

## Component Relationships

### Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EQUIPMENT TEMPLATE                              │
│                            (forgero:equipment_template)                      │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  Example: pickaxe_template.json                                      │   │
│  │                                                                      │   │
│  │  slots: {                                                            │   │
│  │    head: {type: "forgero:parts/pickaxe_head_type"}  ───────────┐    │   │
│  │    handle: {type: "forgero:parts/handle_type"}      ─────────┐ │    │   │
│  │  }                                                           │ │    │   │
│  │  upgrades: [{tags: ["forgero:parts/binding_type"]}] ───────┐ │ │    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                         │         │ │
                                         ▼         ▼ ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               PART TEMPLATES                                 │
│                            (forgero:part_template)                           │
│                                                                             │
│  ┌─────────────────────────────┐     ┌─────────────────────────────┐       │
│  │  pickaxe_head.json          │     │  handle.json                 │       │
│  │                             │     │                              │       │
│  │  slots: {                   │     │  slots: {                   │       │
│  │    material: {              │     │    material: {              │       │
│  │      type: "tool_material" ─┼──┐  │      type: "handle_material"│       │
│  │    }                        │  │  │    }                        │       │
│  │    shape: {                 │  │  │    shape: {...}             │       │
│  │      type: "pickaxe_shape" ─┼──┼──│  }                          │       │
│  │    }                        │  │  │                              │       │
│  │  }                          │  │  └─────────────────────────────┘       │
│  └─────────────────────────────┘  │                                        │
└───────────────────────────────────┼────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               MATERIALS                                      │
│                            (forgero:material)                                │
│                                                                             │
│  ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐ │
│  │  iron.json          │  │  oak.json           │  │  diamond.json       │ │
│  │                     │  │                     │  │                     │ │
│  │  tags: [            │  │  tags: [            │  │  tags: [            │ │
│  │    "metal",         │  │    "wood",          │  │    "mineral",       │ │
│  │    "tool_material"  │  │    "tool_material"  │  │    "tool_material"  │ │
│  │  ]                  │  │  ]                  │  │  ]                  │ │
│  │                     │  │                     │  │                     │ │
│  │  attributes:        │  │  attributes:        │  │  attributes:        │ │
│  │    durability: 240  │  │    durability: 59   │  │    durability: 1561 │ │
│  │    mining_level: 2  │  │    mining_level: 0  │  │    mining_level: 3  │ │
│  │    ...              │  │    ...              │  │    ...              │ │
│  └─────────────────────┘  └─────────────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               SCHEMATICS                                     │
│                            (forgero:schematic)                               │
│                                                                             │
│  Modify part stats via shapes:                                              │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  refined_pickaxe_head.json                                           │   │
│  │                                                                      │   │
│  │  attributes: [                                                       │   │
│  │    {type: "rarity", operation: "multiplication", value: 1.25}        │   │
│  │    {type: "mining_speed", operation: "multiplication", value: 1.25}  │   │
│  │    {type: "durability", operation: "multiplication", value: 1.25}    │   │
│  │  ]                                                                   │   │
│  │                                                                      │   │
│  │  upgrades: [                                                         │   │
│  │    {tags: ["upgrade/reinforcement"]}  ← Adds extra upgrade slot     │   │
│  │    {tags: ["upgrade/cosmetic"]}       ← Adds cosmetic slot          │   │
│  │  ]                                                                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Attribute System

### Attribute Context Pattern (Recommended)

```json
{
  "id": "forgero:iron-attack_damage",
  "type": "forgero:attack_damage",
  "computation": { "value": 4.0 },
  "context": "forgero:part-composite"
}
```

### Available Attributes

| Attribute | Description | Use Context |
|-----------|-------------|-------------|
| `attack_damage` | Base damage dealt | Weapons |
| `attack_speed` | Attack cooldown rate | Weapons |
| `mining_speed` | Block breaking speed | Tools |
| `mining_level` | Harvestable block tier | Tools |
| `durability` | Item lifespan | All |
| `rarity` | Visual/sort ranking | All |
| `weight` | Physics modifier | All |
| `draw_speed` | Bow draw rate | Bows |
| `draw_power` | Arrow velocity | Bows |
| `armor` | Damage reduction | Armor |

---

## Static Part System (Vanilla Integration)

Wraps existing Minecraft items as Forgero components:

```
┌────────────────────────────────────────────────────────────────┐
│                    STATIC PART                                  │
│                 (forgero:static_part)                          │
│                                                                │
│  host: { identifiers: [{id: "minecraft:iron_pickaxe"}] }       │
│           │                                                    │
│           ▼                                                    │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │  Minecraft Item ←→ Forgero Component                     │  │
│  │                                                          │  │
│  │  attributes: [                                           │  │
│  │    {type: "durability", computation: 250}                │  │
│  │    {type: "attack_damage", computation: 4}               │  │
│  │    {type: "mining_speed", computation: 6}                │  │
│  │    {type: "mining_level", computation: 2}                │  │
│  │  ]                                                       │  │
│  │                                                          │  │
│  │  upgrades: [                                             │  │
│  │    {tags: ["upgrade/binding"]}  ← Can add binding        │  │
│  │  ]                                                       │  │
│  └─────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

---

## Design Principles

### 1. Composition Over Inheritance
- Components compose from smaller pieces
- Tags provide behavioral grouping without class hierarchies
- Materials + Shapes + Upgrades = Final Item

### 2. Data-Driven Configuration
- All content defined in JSON
- No code changes needed to add materials/tools
- Conditional logic expressed declaratively

### 3. Layered Modularity
```
Foundation → Components → Enhancements → Integration
    ↓            ↓              ↓             ↓
  Tags        Templates     Modifiers     Vanilla
  Materials   Parts         Gems          Wrap
  Base        Equipment     Extended      Compat
```

### 4. Tag-Based Slot Typing
- Slots reference tags, not specific items
- Any item with matching tags can fill slot
- Enables easy extension without modifying templates

### 5. Attribute Context and Conditions
- Use `context` field on attributes to control application:
  - `forgero:part-composite`: Apply during material + schematic composition
  - `forgero:local`: Apply only to the component itself
  - `forgero:upgrade`: Apply only when installed as upgrade
- Use conditions for slot-based restrictions:
  - `in_slot_type`: Apply based on where component is used
  - `is_root`: Apply only at top-level component

### 6. Template Variable Expansion
```
ID Pattern: "forgero:{material.name}-{shape.name}"
     ↓
Expanded:   "forgero:iron-refined_pickaxe_head"
```

---

## Pack Dependencies

```
                    ┌──────────────┐
                    │ forgero-base │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
     ┌────────────┐ ┌───────────┐ ┌──────────┐
     │ materials  │ │schematics │ │ upgrades │
     └─────┬──────┘ └─────┬─────┘ └────┬─────┘
           │              │            │
           └──────────────┼────────────┘
                          │
              ┌───────────┼───────────┐
              ▼           ▼           ▼
        ┌─────────┐ ┌─────────┐ ┌─────────┐
        │  tools  │ │  armor  │ │  gems   │
        └────┬────┘ └────┬────┘ └────┬────┘
             │           │           │
             └───────────┼───────────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
   ┌──────────┐   ┌───────────┐   ┌──────────────┐
   │ extended │   │  mining   │   │ ext-weapons  │
   └──────────┘   └───────────┘   └──────────────┘
```

---

This architecture enables Forgero's key feature: **infinitely combinable tools and equipment** through a clean separation of materials, shapes, parts, and upgrades - all orchestrated by a hierarchical tag system.

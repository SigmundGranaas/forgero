# Forgero Resource Pack Developer Guide

This comprehensive guide covers the **new modules-based architecture** for creating Forgero resource packs. All JSON APIs documented here are from the `:modules` package.

## Table of Contents

1. [Introduction](#introduction)
2. [File Structure](#file-structure)
3. [Materials](#materials)
4. [Shapes](#shapes)
5. [Schematics](#schematics)
6. [Static Parts](#static-parts)
7. [Part Templates](#part-templates)
8. [Equipment Templates](#equipment-templates)
9. [Extensions](#extensions)
10. [The Attribute System](#the-attribute-system)
11. [Upgrade Slots](#upgrade-slots)
12. [Properties System](#properties-system)
13. [Event Properties](#event-properties)
14. [Effect Handlers](#effect-handlers)
15. [Entity Selectors](#entity-selectors)
16. [Entity Filters](#entity-filters)
17. [Block Breaking Properties](#block-breaking-properties)
18. [Loot Properties](#loot-properties)
19. [Conditions](#conditions)
20. [Tags System](#tags-system)
21. [Generation Filters](#generation-filters)
22. [Host Mapping](#host-mapping)

---

## Introduction

Forgero is a data-driven mod where everything is defined through JSON files. The core concepts:

- **Components**: Building blocks (materials, shapes, parts, equipment)
- **Templates**: Define how components combine to auto-generate items
- **Properties**: Behaviors attached to components (effects, events)
- **Attributes**: Numeric stats with conditions and operators
- **Extensions**: Add properties to existing definitions without modifying them

All type IDs use the `forgero:` namespace.

---

## File Structure

```
data/forgero/
├── materials/           # Material definitions
├── shapes/              # Shape definitions
├── schematics/          # Schematic definitions
├── parts/               # Part templates and static parts
├── equipment/           # Equipment templates
├── extensions/          # Extension definitions
├── tags/                # Tag definitions
│   ├── materials/
│   ├── parts/
│   └── shapes/
└── properties/          # Custom property definitions
```

---

## Materials

Materials define base resources with stats and properties. Type: `forgero:material`

### Complete Material Structure

```json
{
  "type": "forgero:material",
  "name": "Iron",
  "include": ["forgero:materials/base_metal"],
  "tags": ["forgero:materials/tool_material", "forgero:materials/metal"],
  "local_tags": ["forgero:local_only"],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:iron_ingot" },
      { "type": "tag", "id": "c:iron_ingots" }
    ],
    "create": {
      "id": "forgero:iron_material_item",
      "class_name": "forgero:material_item",
      "item_group": "minecraft:materials"
    }
  },
  "attributes": [
    {
      "id": "forgero:iron-durability",
      "type": "forgero:durability",
      "computation": 250
    },
    {
      "id": "forgero:iron-attack_damage",
      "type": "forgero:attack_damage",
      "computation": 4
    },
    {
      "id": "forgero:iron-mining_speed",
      "type": "forgero:mining_speed",
      "computation": 6
    },
    {
      "id": "forgero:iron-mining_level",
      "type": "forgero:mining_level",
      "computation": 2
    }
  ],
  "local_attributes": [
    {
      "type": "forgero:weight",
      "computation": { "multiply": 1.2 }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:fire", "duration": 3 }
        ]
      }
    ]
  }
}
```

### Material Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | Yes | Always `"forgero:material"` |
| `name` | string | Yes | Unique material name |
| `include` | array | No | IDs to inherit from (cascades tags/attributes) |
| `tags` | array | No | Tags inherited via include |
| `local_tags` | array | No | Tags NOT inherited via include |
| `host` | object | No | Maps to vanilla/platform items |
| `attributes` | array | No | Inherited attributes |
| `local_attributes` | array | No | Attributes NOT inherited |
| `properties` | object | No | Event handlers and behaviors |

### Minimal Material Example

```json
{
  "type": "forgero:material",
  "name": "Diamond",
  "tags": ["forgero:materials/tool_material", "forgero:materials/mineral"],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:diamond" }
    ]
  },
  "attributes": [
    { "id": "forgero:diamond-durability", "type": "forgero:durability", "computation": 1561 },
    { "id": "forgero:diamond-attack_damage", "type": "forgero:attack_damage", "computation": 5 },
    { "id": "forgero:diamond-mining_speed", "type": "forgero:mining_speed", "computation": 8 },
    { "id": "forgero:diamond-mining_level", "type": "forgero:mining_level", "computation": 3 }
  ]
}
```

---

## Shapes

Shapes define the geometric form of parts with attribute multipliers. Type: `forgero:shape`

### Shape Structure

```json
{
  "type": "forgero:shape",
  "name": "Pickaxe Head",
  "tags": ["forgero:pickaxe_head_shape", "forgero:default_pickaxe_head"],
  "local_tags": ["forgero:base_shape"],
  "attributes": [
    {
      "id": "forgero:pickaxe-durability-mult",
      "type": "forgero:durability",
      "computation": { "multiply": 1.5 }
    },
    {
      "id": "forgero:pickaxe-mining_speed-mult",
      "type": "forgero:mining_speed",
      "computation": { "multiply": 1.2 }
    }
  ]
}
```

Shapes typically use multiplication operators to modify base material stats.

---

## Schematics

Schematics are enhanced shape variants with crafting bonuses. Type: `forgero:schematic`

### Schematic Structure

```json
{
  "type": "forgero:schematic",
  "name": "Master Crafted Pickaxe Head",
  "tags": ["forgero:pickaxe_head_shape"],
  "local_tags": ["forgero:mastercrafted"],
  "target": "forgero:shapes/pickaxe_head_shape",
  "attributes": [
    {
      "id": "forgero:schematic-durability-bonus",
      "type": "forgero:durability",
      "computation": { "multiply": 2.0 }
    },
    {
      "id": "forgero:schematic-rarity",
      "type": "forgero:rarity",
      "computation": { "add": 50 }
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `target` | string | Base shape being enhanced (optional) |

---

## Static Parts

Static parts are non-generated components with fixed properties. Type: `forgero:static_part`

### Static Part Structure

```json
{
  "type": "forgero:static_part",
  "name": "Oak Handle",
  "tags": ["forgero:parts/handle_type"],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:stick" }
    ]
  },
  "attributes": [
    {
      "id": "forgero:oak-handle-durability",
      "type": "forgero:durability",
      "computation": 50
    },
    {
      "id": "forgero:oak-handle-weight",
      "type": "forgero:weight",
      "computation": 2
    }
  ],
  "upgrades": [
    {
      "id": "forgero:grip-slot",
      "type": "forgero:upgrade_material",
      "tags": ["forgero:materials/binding"],
      "tier": 1,
      "description": "Customize the grip"
    }
  ]
}
```

---

## Part Templates

Part templates define how composite parts are assembled from materials and shapes. Type: `forgero:part_template`

### Part Template Structure

```json
{
  "type": "forgero:part_template",
  "name": "Pickaxe Head",
  "tags": ["forgero:parts/pickaxe_head_type"],
  "include": ["forgero:parts/heads/base_head_template"],
  "host_template": {
    "create": {
      "id": "forgero:{material.name}-{shape.shape_name}",
      "class_name": "forgero:part_item",
      "item_group": "minecraft:tools"
    }
  },
  "structure": {
    "id": "forgero:{material.name}-{shape.shape_name}",
    "slots": {
      "material": {
        "type": "forgero:tool_material",
        "count": 1,
        "description": "The primary material"
      },
      "shape": {
        "type": "forgero:pickaxe_head_shape",
        "count": 1,
        "description": "The shape of the head"
      }
    }
  },
  "upgrades": [
    {
      "id": "forgero:head-enhancement",
      "type": "forgero:upgrade_material",
      "tags": ["forgero:materials/metal"],
      "tier": 2,
      "description": "Add enhancement material"
    }
  ],
  "attributes": [
    {
      "id": "forgero:head-bonus",
      "type": "forgero:durability",
      "computation": { "add": 10 }
    }
  ],
  "generation": {
    "slots": {
      "shape": {
        "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"],
        "exclude_any_tags": ["forgero:schematic"]
      }
    }
  }
}
```

### Part Template Fields

| Field | Type | Description |
|-------|------|-------------|
| `host_template` | object | Template for item creation |
| `structure` | object | Defines slot composition |
| `structure.slots` | object | Map of slot_name -> slot definition |
| `upgrades` | array | Upgrade slot definitions |
| `generation` | object | Controls which combinations generate |

---

## Equipment Templates

Equipment templates define complete tools/armor from parts. Type: `forgero:equipment_template`

### Equipment Template Structure

```json
{
  "type": "forgero:equipment_template",
  "name": "Pickaxe",
  "tags": ["forgero:tools/pickaxe"],
  "include": ["forgero:equipment/tool_base"],
  "host_template": {
    "create": {
      "id": "forgero:{head.material.name}-pickaxe",
      "class_name": "forgero:pickaxe_item",
      "item_group": "minecraft:tools"
    }
  },
  "structure": {
    "id": "forgero:{head.material.name}-pickaxe",
    "slots": {
      "head": {
        "type": "forgero:parts/pickaxe_head_type",
        "default_tag": "forgero:default_pickaxe_head"
      },
      "handle": {
        "type": "forgero:parts/handle_type",
        "default": "forgero:static_oak_handle"
      }
    }
  },
  "upgrades": [
    {
      "id": "forgero:binding-slot",
      "type": "forgero:upgrade_material",
      "tags": ["forgero:materials/binding"],
      "description": "Tool binding slot"
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:knockback", "force": 0.5, "direction": "push" }
        ]
      }
    ]
  }
}
```

### Structure Slot Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Slot type tag (required) |
| `default_tag` | string | Tag identifying default parts pool |
| `default` | string | Specific default part ID |
| `count` | int | Amount needed |
| `description` | string | Translatable description key |

### Armor Equipment Template

```json
{
  "type": "forgero:equipment_template",
  "name": "Helmet",
  "tags": ["forgero:armor/helmet"],
  "host_template": {
    "create": {
      "id": "forgero:{plate.material.name}-helmet",
      "class_name": "forgero:armor_item",
      "item_group": "forgero:combat"
    }
  },
  "structure": {
    "id": "forgero:{plate.material.name}-helmet",
    "slots": {
      "plate": {
        "type": "forgero:parts/armor_plate_type",
        "default_tag": "forgero:default_armor_plate"
      },
      "lining": {
        "type": "forgero:parts/armor_lining_type",
        "default_tag": "forgero:default_armor_lining"
      }
    }
  }
}
```

---

## Extensions

Extensions add properties to existing definitions without modifying the original files. Forgero supports two extension systems: **Data Extensions** for component behavior and **Model Extensions** for visual representation.

### Extension Priority System

All extensions are applied in priority order (lowest first). When multiple extensions target the same definition:
- Priority 0 is applied first
- Priority 100 is applied last
- Negative priorities are supported

### Data Extensions

Data extensions modify component data (materials, templates, etc.). Type: `forgero:extension`

#### Data Extension Structure

```json
{
  "type": "forgero:extension",
  "target": "forgero:materials/iron",
  "priority": 0,
  "tags": ["forgero:additional_tag"],
  "attributes": [
    {
      "id": "forgero:extension-bonus",
      "type": "forgero:durability",
      "computation": { "add": 20 }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:fire", "duration": 2 }
        ]
      }
    ]
  },
  "upgrades": [
    {
      "id": "forgero:custom-slot",
      "type": "forgero:upgrade_material",
      "tags": ["forgero:materials/gem"],
      "tier": 1
    }
  ]
}
```

#### Data Extension Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `type` | string | Yes | - | Always `"forgero:extension"` |
| `target` | string | Yes | - | ID of definition to extend |
| `priority` | int | No | 0 | Merge order (lower applied first) |
| `tags` | array | No | - | Tags to add to target |
| `attributes` | array | No | - | Attributes to add to target |
| `properties` | object | No | - | Properties to merge into target |
| `upgrades` | array | No | - | Upgrade slots to add/override |

### Model Extensions

Model extensions add visual elements (layers, slots, mount points) to existing models. Type: `forgero:model_extension`

**File Location**: `assets/<namespace>/forgero_models/` or `assets/<namespace>/model_templates/`

#### Model Extension Structure

```json
{
  "type": "forgero:model_extension",
  "target": "forgero:parts/iron-pickaxe_head",
  "priority": 100,
  "layers": [
    {
      "order": 50,
      "textures": {
        "default": "forgero:item/overlays/dye_overlay"
      }
    }
  ],
  "slots": [
    {
      "id": "dye_slot",
      "order": 5,
      "renderer": { "type": "forgero:component" }
    }
  ],
  "mount_points": [
    {
      "name": "charm_mount",
      "position": [8, 2]
    }
  ]
}
```

#### Model Extension Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `type` | string | Yes | - | Always `"forgero:model_extension"` |
| `target` | string | Yes | - | Model ID to extend |
| `priority` | int | No | 0 | Merge order (lower applied first) |
| `layers` | array | No | - | Layers to add (appended after target layers) |
| `slots` | array | No | - | Slots to add (by ID, extension wins) |
| `mount_points` | array | No | - | Mount points to add (by name, extension wins) |

#### Model Extension Example: Adding Dye Overlay

Add a dye overlay layer to all iron pickaxe heads:

```json
{
  "type": "forgero:model_extension",
  "target": "forgero:parts/iron-pickaxe_head",
  "priority": 100,
  "layers": [
    {
      "order": 50,
      "textures": {
        "default": "forgero:item/overlays/dye_overlay",
        "variants": [
          {
            "predicate": [{ "type": "forgero:root_tag", "tag": "forgero:dyed" }],
            "texture": "forgero:item/overlays/dye_overlay_active"
          }
        ]
      }
    }
  ]
}
```

#### Model Extension Example: Adding Slot

Add a custom slot to equipment models:

```json
{
  "type": "forgero:model_extension",
  "target": "forgero:equipment/iron-pickaxe",
  "slots": [
    {
      "id": "charm",
      "order": 40,
      "renderer": { "type": "forgero:component" },
      "mount": "charm_mount"
    }
  ]
}
```

### Merge Semantics Comparison

| System | Field | Strategy |
|--------|-------|----------|
| **Data** | `tags` | Union (extension tags added) |
| **Data** | `attributes` | Concatenate (appended) |
| **Data** | `properties` | Deep merge (objects merged, arrays concatenated) |
| **Data** | `upgrades` | By ID: extension wins |
| **Model** | `layers` | Concatenate (appended after target layers) |
| **Model** | `slots` | By ID: extension wins (warning logged) |
| **Model** | `mount_points` | By name: extension wins (warning logged) |

### Extension Use Cases

**Data Extensions**:
- Add properties to vanilla materials without modifying original files
- Add upgrade slots to existing templates
- Add conditional effects to materials from other mods

**Model Extensions**:
- Add overlay textures for dye/enchantment effects
- Add new render slots for accessories/charms
- Override mount point positions for custom part alignment

---

## The Attribute System

Attributes define numeric stats with conditions and operators.

### Available Attribute Types

| Type | ID | Description |
|------|-----|-------------|
| Durability | `forgero:durability` | Item durability |
| Attack Damage | `forgero:attack_damage` | Base attack damage |
| Attack Speed | `forgero:attack_speed` | Attack speed modifier |
| Mining Speed | `forgero:mining_speed` | Block breaking speed |
| Mining Level | `forgero:mining_level` | Tool tier (0-4) |
| Armor | `forgero:armor` | Armor points |
| Armor Toughness | `forgero:armor_toughness` | Armor toughness |
| Rarity | `forgero:rarity` | Item rarity value |
| Weight | `forgero:weight` | Item weight |
| Draw Speed | `forgero:draw_speed` | Bow draw speed |
| Draw Power | `forgero:draw_power` | Bow power |

### Attribute Structure

```json
{
  "id": "forgero:iron-durability",
  "type": "forgero:durability",
  "computation": 250,
  "condition": {
    "static": [
      {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:tool_material"
      }
    ]
  }
}
```

### Attribute Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | No | Unique identifier (recommended) |
| `type` | string | Yes | Attribute type from table above |
| `computation` | number/object | Yes | Value and operation |
| `condition` | object | No | When attribute applies |

### Computation Formats

**Simple number (addition):**
```json
"computation": 250
```

**Operator syntax:**
```json
"computation": { "add": 10 }
"computation": { "subtract": 5 }
"computation": { "multiply": 1.5 }
"computation": { "divide": 2.0 }
```

**Full format:**
```json
"computation": {
  "value": 250,
  "operator": "forgero:addition",
  "order": "forgero:base"
}
```

### Operators

| Operator | Shorthand | Description |
|----------|-----------|-------------|
| `forgero:addition` | `add` | Add to base value |
| `forgero:subtraction` | `subtract` | Subtract from value |
| `forgero:multiplication` | `multiply` | Multiply value |
| `forgero:division` | `divide` | Divide value |

### Calculation Orders

| Order | When Applied |
|-------|--------------|
| `forgero:base` | Initial value (default) |
| `forgero:middle` | After base operations |
| `forgero:end` | Final application |

### Conditional Attributes

Apply attributes only in specific slots:

```json
{
  "attributes": [
    {
      "id": "forgero:leather-binding-durability",
      "type": "forgero:durability",
      "computation": { "add": 100 },
      "condition": {
        "static": [
          {
            "type": "forgero:in_slot_type",
            "slot_type": "forgero:binding_slot"
          }
        ]
      }
    },
    {
      "id": "forgero:leather-handle-attack_speed",
      "type": "forgero:attack_speed",
      "computation": { "multiply": 1.1 },
      "condition": {
        "static": [
          {
            "type": "forgero:in_slot_type",
            "slot_type": "forgero:handle_slot"
          }
        ]
      }
    }
  ]
}
```

---

## Upgrade Slots

Upgrade slots allow optional component enhancements.

### Upgrade Slot Structure

```json
{
  "id": "forgero:slot-id",
  "type": "forgero:upgrade_material",
  "tags": ["forgero:materials/binding", "forgero:enhancement"],
  "tier": 2,
  "description": "slot.forgero.description.key"
}
```

### Upgrade Slot Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique slot identifier (required) |
| `type` | string | What type of component fits (required) |
| `tags` | array | Tags that upgrade must have |
| `tier` | int | Tier requirement |
| `description` | string | Translatable description key |

---

## Properties System

Properties are stored in a `properties` map with string keys and array/object values.

### Property Map Structure

```json
{
  "properties": {
    "minecraft:on_hit": [...],
    "minecraft:on_tick": [...],
    "minecraft:on_kill": [...],
    "minecraft:on_damage_received": [...],
    "minecraft:on_sneak_toggle": [...],
    "minecraft:on_loot_drop": [...],
    "forgero:block_breaking": [...],
    "minecraft:use_interaction": {...},
    "forgero:tooltip": [...],
    "custom:key": { "arbitrary": "data" }
  }
}
```

---

## Event Properties

Event properties trigger effects on specific game events.

### On Hit (`minecraft:on_hit`)

Triggers when an entity is hit with the item:

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 3,
          "filters": [
            { "type": "forgero:is_hostile" },
            { "type": "forgero:is_alive" }
          ]
        },
        "effects": [
          { "type": "forgero:lightning" },
          { "type": "forgero:fire", "duration": 5 },
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:slowness",
            "duration": 100,
            "amplifier": 2
          }
        ],
        "condition": {
          "type": "forgero:target_has_tag",
          "tag": "minecraft:undead"
        }
      }
    ]
  }
}
```

### On Tick (`minecraft:on_tick`)

Triggers periodically while item is held/worn:

```json
{
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 5,
          "filters": [
            { "type": "forgero:is_hostile" }
          ]
        },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:slowness",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20,
        "condition": {
          "type": "forgero:is_sneaking"
        }
      }
    ]
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `interval` | int | Ticks between activations (20 = 1 second) |

### On Kill (`minecraft:on_kill`)

Triggers when an entity is killed:

```json
{
  "properties": {
    "minecraft:on_kill": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:life_steal", "amount": 3.0 },
          {
            "type": "forgero:spawn_entity",
            "entity": "minecraft:experience_orb",
            "count": 5,
            "on_target": false
          }
        ],
        "condition": {
          "type": "forgero:random_chance",
          "chance": 0.5
        }
      }
    ]
  }
}
```

### On Damage Received (`minecraft:on_damage_received`)

Triggers when wielder takes damage (thorns-like):

```json
{
  "properties": {
    "minecraft:on_damage_received": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:knockback", "force": 1.5, "direction": "push" },
          { "type": "forgero:fire", "duration": 2 }
        ]
      }
    ]
  }
}
```

### On Sneak Toggle (`minecraft:on_sneak_toggle`)

Triggers when player starts sneaking:

```json
{
  "properties": {
    "minecraft:on_sneak_toggle": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 3,
          "filters": [{ "type": "forgero:is_hostile" }]
        },
        "effects": [
          { "type": "forgero:knockback", "force": 2.0, "direction": "push" }
        ]
      }
    ]
  }
}
```

---

## Effect Handlers

Effect handlers define what happens when an event triggers.

### Fire (`forgero:fire`)

```json
{ "type": "forgero:fire", "duration": 5 }
```

### Lightning (`forgero:lightning`)

```json
{ "type": "forgero:lightning" }
```

### Status Effect (`forgero:status_effect`)

```json
{
  "type": "forgero:status_effect",
  "effect": "minecraft:slowness",
  "duration": 100,
  "amplifier": 2
}
```

### Life Steal (`forgero:life_steal`)

```json
{ "type": "forgero:life_steal", "amount": 2.0 }
```

### Knockback (`forgero:knockback`)

```json
{
  "type": "forgero:knockback",
  "force": 2.0,
  "direction": "push"
}
```

### Explosion (`forgero:explosion`)

```json
{
  "type": "forgero:explosion",
  "power": 2.0,
  "create_fire": false,
  "destruction_type": "none"
}
```

| destruction_type | Description |
|------------------|-------------|
| `none` | No block destruction |
| `block` | Normal block destruction |
| `mob` | Mob-like destruction |
| `tnt` | TNT-like destruction |

### Disarm (`forgero:disarm`)

```json
{ "type": "forgero:disarm" }
```

### Convert (`forgero:convert`)

```json
{ "type": "forgero:convert", "convert_to": "minecraft:zombie_villager" }
```

### Freeze (`forgero:freeze`)

```json
{ "type": "forgero:freeze", "duration": 5 }
```

### Spawn Entity (`forgero:spawn_entity`)

```json
{
  "type": "forgero:spawn_entity",
  "entity": "minecraft:zombie",
  "count": 2,
  "offset": {"x": 1.0, "y": 0.0, "z": 0.0},
  "on_target": false,
  "relative_offset": false,
  "set_owner": true
}
```

| Field | Type | Description |
|-------|------|-------------|
| `entity` | identifier | Entity type to spawn |
| `count` | int | Number to spawn (1-10) |
| `offset` | vec3d | Position offset |
| `on_target` | boolean | Spawn at target vs source |
| `relative_offset` | boolean | Offset relative to rotation |
| `set_owner` | boolean | Set spawner as owner |

### Velocity (`forgero:velocity`)

```json
{
  "type": "forgero:velocity",
  "target": "target",
  "amount": 2.0,
  "mode": "away_from_source",
  "vertical_bias": 0.5
}
```

| Field | Values | Description |
|-------|--------|-------------|
| `target` | `self`, `target` | Who to affect |
| `mode` | `add`, `set`, `away_from_source` | How to apply velocity |

### Magnet (`forgero:magnet`)

```json
{
  "type": "forgero:magnet",
  "radius": 5.0,
  "speed": 0.3
}
```

### Modify Block (`forgero:modify_block`)

```json
{
  "type": "forgero:modify_block",
  "action": "replace",
  "target_block": "minecraft:water",
  "replacement": "minecraft:ice",
  "consume_item": false,
  "handle_multiblock": true
}
```

| action | Description |
|--------|-------------|
| `replace` | Replace block with replacement |
| `destroy` | Break the block |

---

## Entity Selectors

Selectors determine which entities are affected.

### Shared selector options

These optional fields are accepted by every selector:

| Field | Type | Default | Meaning |
|-------|------|---------|---------|
| `filters` | array | `[]` | Filters applied after geometric selection (see [Entity Filters](#entity-filters)). |
| `match` | `"all"` \| `"any"` | `"all"` | How the `filters` list is combined. `"all"` = a candidate must pass every filter (AND); `"any"` = it must pass at least one (OR). This lets you express OR without wrapping filters in a `forgero:or` composite. |
| `maxTargets` | int (> 0) | unlimited | Caps the result to the N entities nearest the selector's anchor (the target for AOE, the source for cone). Use it to bound effect cost and gameplay impact. *(Not applicable to `single_target`; `chain` is already bounded by `maxChains`.)* |

> **Range ceiling:** `radius`, `range`, and `chainRange` are capped at **64 blocks**, and `maxChains` at **256**. Values above these (or `<= 0`) are rejected at data-load time with a clear error rather than silently issuing a pathological per-hit world query — keep ranges sane, especially on `on_tick` properties.

### Single Target (`forgero:single_target`)

```json
{ "type": "forgero:single_target" }
```

### Area of Effect (`forgero:aoe`)

Selects entities within a **spherical** radius (true distance) of the target — entities in the
corners of the bounding box but outside the sphere are excluded.

```json
{
  "type": "forgero:aoe",
  "radius": 5,
  "match": "all",
  "maxTargets": 8,
  "filters": [
    { "type": "forgero:is_hostile" },
    { "type": "forgero:is_alive" }
  ]
}
```

### Cone (`forgero:cone`)

```json
{
  "type": "forgero:cone",
  "angle": 90,
  "range": 10,
  "match": "any",
  "filters": [
    { "type": "forgero:is_hostile" },
    { "type": "forgero:is_burning" }
  ]
}
```

### Chain (`forgero:chain`)

```json
{
  "type": "forgero:chain",
  "maxChains": 5,
  "chainRange": 4,
  "allowRepeats": false,
  "filters": [
    { "type": "forgero:is_alive" }
  ]
}
```

---

## Entity Filters

Filters narrow down which entities are affected.

### Basic Filters

```json
{ "type": "forgero:is_alive" }
{ "type": "forgero:is_hostile" }
{ "type": "forgero:is_player" }
{ "type": "forgero:is_burning" }
{ "type": "forgero:is_in_water" }
{ "type": "forgero:is_teammate", "invert": false }
```

### Entity Type Filter

```json
{ "type": "forgero:entity_type", "entity_type": "minecraft:zombie" }
```

### Has Tag Filter

```json
{ "type": "forgero:has_tag", "tag": "minecraft:undead" }
```

### Has Effect Filter

```json
{ "type": "forgero:has_effect", "effect": "minecraft:poison" }
```

### Health Threshold Filter

```json
{
  "type": "forgero:health_threshold",
  "threshold": 0.5,
  "comparator": "less_than"
}
```

| comparator | Description |
|------------|-------------|
| `less_than` | Health < threshold |
| `less_than_or_equal` | Health <= threshold |
| `greater_than` | Health > threshold |
| `greater_than_or_equal` | Health >= threshold |
| `equal` | Health == threshold |

### Distance Filter

```json
{ "type": "forgero:distance", "min": 0, "max": 5 }
```

### Random Chance Filter

```json
{ "type": "forgero:random_chance", "chance": 0.25 }
```

### Environment Filter

```json
{ "type": "forgero:environment", "condition": "is_raining" }
```

### Composite Filters

```json
// AND
{
  "type": "forgero:and",
  "filters": [
    { "type": "forgero:is_hostile" },
    { "type": "forgero:is_alive" }
  ]
}

// OR
{
  "type": "forgero:or",
  "filters": [
    { "type": "forgero:is_burning" },
    { "type": "forgero:is_in_water" }
  ]
}

// NOT
{
  "type": "forgero:not",
  "filter": { "type": "forgero:is_teammate" }
}
```

---

## Block Breaking Properties

Custom block breaking behaviors (`forgero:block_breaking`):

```json
{
  "properties": {
    "forgero:block_breaking": [
      {
        "selector": {
          "type": "forgero:radius",
          "radius": 2,
          "filter": {
            "type": "forgero:filter_wrapper",
            "filters": [
              { "type": "forgero:can_mine" },
              { "type": "forgero:same_block" }
            ]
          }
        },
        "speed": { "type": "forgero:all" },
        "condition": {
          "type": "forgero:is_sneaking"
        }
      }
    ]
  }
}
```

### Block Selectors

```json
// Radius/Vein mining
{
  "type": "forgero:radius",
  "radius": 3,
  "filter": { "type": "forgero:same_block" }
}

// Column mining
{
  "type": "forgero:column",
  "depth": 3,
  "filter": { "type": "forgero:can_mine" }
}

// Pattern mining
{
  "type": "forgero:pattern",
  "pattern": ["###", "#X#", "###"],
  "filter": { "type": "forgero:can_mine" }
}
```

### Block Filters

```json
{ "type": "forgero:can_mine" }
{ "type": "forgero:same_block" }
{ "type": "forgero:similar_block" }
{ "type": "forgero:is_block", "block": "minecraft:stone" }
{ "type": "forgero:block_tag", "tag": "minecraft:mineable/pickaxe" }
```

### Speed Calculators

```json
{ "type": "forgero:all" }      // Same speed for all
{ "type": "forgero:single" }   // Only root block normal speed
{ "type": "forgero:average" }  // Average of all blocks
{ "type": "forgero:instant" }  // Instant breaking
```

---

## Loot Properties

Modify loot drops (`minecraft:on_loot_drop`):

```json
{
  "properties": {
    "minecraft:on_loot_drop": [
      {
        "handler": {
          "type": "forgero:apply_functions",
          "functions": [
            {
              "type": "forgero:auto_smelt",
              "filter": { "type": "forgero:tag", "tag": "c:raw_ores" }
            },
            {
              "type": "forgero:item_transform",
              "input": { "type": "forgero:tag", "tag": "minecraft:logs" },
              "output": "minecraft:charcoal",
              "count": 2
            }
          ]
        },
        "condition": {
          "type": "forgero:is_sneaking"
        }
      }
    ]
  }
}
```

### Loot Functions

```json
// Auto smelt
{
  "type": "forgero:auto_smelt",
  "filter": { "type": "forgero:tag", "tag": "c:raw_ores" }
}

// Item transform
{
  "type": "forgero:item_transform",
  "input": { "type": "forgero:tag", "tag": "minecraft:logs" },
  "output": "minecraft:charcoal",
  "count": 2
}
```

### Loot Filters

```json
{ "type": "forgero:is_item", "item": "minecraft:iron_ore" }
{ "type": "forgero:tag", "tag": "c:raw_ores" }
```

---

## Conditions

Conditions determine when properties/attributes apply.

### Static Conditions (Component Structure)

Used in `condition.static` array:

```json
// In slot type
{ "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" }

// Is root component
{ "type": "forgero:is_root" }

// At specific depth
{ "type": "forgero:at_depth", "depth": 2 }

// Self has tag
{ "type": "forgero:self_has_tag", "tag": "forgero:sword" }

// Root has tag
{ "type": "forgero:root_has_tag", "tag": "forgero:pickaxe" }

// Has sibling
{ "type": "forgero:has_sibling", "sibling_id": "forgero:oak_handle" }

// NOTE: For composite attributes, use the "context" field on the attribute instead of conditions.
// See "Attribute Context" section below for the modern pattern.

// Slot contains (check if root has slot with specific content)
{ "type": "forgero:slot_contains", "slot_type": "forgero:material", "tag": "forgero:metal" }

// ID match
{ "type": "forgero:id_match", "id": "forgero:iron_pickaxe" }
```

### Attribute Context (Recommended Pattern)

Use the `context` field on attributes to control when they apply during composition.
This is the modern, preferred approach for material attributes:

```json
{
  "id": "forgero:iron-durability",
  "type": "forgero:durability",
  "computation": { "value": 240 },
  "context": "forgero:part-composite"
}
```

### Available Contexts

| Context | When Applied |
|---------|--------------|
| `forgero:part-composite` | Material + schematic intersection composition (recommended for materials) |
| `forgero:local` | Only applies to the component itself |
| `forgero:upgrade` | Only applies when installed as upgrade |

Using `context` is simpler and more reliable than condition-based approaches for controlling
attribute composition behavior.

### Dynamic Conditions (Game State)

Used directly in `condition` field:

```json
// Target has tag
{ "type": "forgero:target_has_tag", "tag": "minecraft:undead" }

// Is sneaking
{ "type": "forgero:is_sneaking" }

// Is raining
{ "type": "forgero:is_raining" }

// Random chance
{ "type": "forgero:random_chance", "chance": 0.25 }
```

### Composite Conditions

```json
// AND
{
  "type": "forgero:and",
  "conditions": [
    { "type": "forgero:target_has_tag", "tag": "minecraft:undead" },
    { "type": "forgero:is_raining" }
  ]
}

// OR
{
  "type": "forgero:or",
  "conditions": [
    { "type": "forgero:is_sneaking" },
    { "type": "forgero:is_raining" }
  ]
}

// NOT
{
  "type": "forgero:not",
  "condition": { "type": "forgero:is_sneaking" }
}
```

---

## Tags System

Tags group components for template matching and filtering.

### Tag Definition

```json
// data/forgero/tags/materials/metal.json
{
  "values": [
    "forgero:iron",
    "forgero:gold",
    "forgero:copper",
    "forgero:netherite"
  ]
}
```

### Common Tags

| Tag | Purpose |
|-----|---------|
| `forgero:materials/tool_material` | Materials for tool heads |
| `forgero:materials/armor_material` | Materials for armor |
| `forgero:materials/binding` | Materials usable as bindings |
| `forgero:parts/pickaxe_head_type` | Pickaxe head parts |
| `forgero:parts/handle_type` | Handle parts |
| `forgero:default_pickaxe_head` | Default pickaxe head shape |

---

## Generation Filters

Control which template combinations are generated:

```json
{
  "generation": {
    "slots": {
      "shape": {
        "require_all_tags": ["forgero:pickaxe_head_shape", "forgero:base_shape"],
        "require_any_tags": ["forgero:default_pickaxe_head"],
        "exclude_any_tags": ["forgero:schematic"],
        "exclude_all_tags": ["forgero:broken"],
        "explicit_list": ["forgero:shapes/oak_head"]
      }
    }
  }
}
```

| Filter | Description |
|--------|-------------|
| `require_all_tags` | All tags must be present |
| `require_any_tags` | At least one tag must be present |
| `exclude_any_tags` | None of these tags can be present |
| `exclude_all_tags` | Can't have all of these tags |
| `explicit_list` | Only these IDs are allowed |

---

## Host Mapping

Map Forgero components to vanilla/platform items.

### Static Host (for resources)

```json
"host": {
  "identifiers": [
    { "type": "item", "id": "minecraft:iron_ingot" },
    { "type": "tag", "id": "c:iron_ingots" }
  ],
  "create": {
    "id": "forgero:custom_item",
    "class_name": "forgero:material_item",
    "item_group": "minecraft:tools"
  }
}
```

### Template Host (for templates)

```json
"host_template": {
  "identifiers": [
    { "type": "item", "id": "minecraft:diamond_pickaxe" }
  ],
  "create": {
    "id": "forgero:{material.name}-{shape.name}",
    "class_name": "forgero:pickaxe_item",
    "item_group": "minecraft:tools"
  }
}
```

### Template Placeholders

| Placeholder | Description |
|-------------|-------------|
| `{material.name}` | Material name from slot |
| `{shape.name}` | Shape name from slot |
| `{shape.shape_name}` | Shape component name |
| `{head.material.name}` | Material from "head" slot |
| `{slot_name.field}` | Any nested slot reference |

---

## Complete Examples

### Vampiric Material with Chain Lightning

```json
{
  "type": "forgero:material",
  "name": "Vampiric Steel",
  "tags": ["forgero:materials/metal", "forgero:materials/tool_material"],
  "host": {
    "identifiers": [{ "type": "item", "id": "mymod:vampiric_steel" }]
  },
  "attributes": [
    { "id": "mymod:vampiric-durability", "type": "forgero:durability", "computation": 350 },
    { "id": "mymod:vampiric-attack_damage", "type": "forgero:attack_damage", "computation": 5 }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:life_steal", "amount": 2.0 }
        ]
      },
      {
        "selector": {
          "type": "forgero:chain",
          "maxChains": 3,
          "chainRange": 4,
          "filters": [
            { "type": "forgero:is_hostile" },
            { "type": "forgero:is_alive" }
          ]
        },
        "effects": [
          { "type": "forgero:lightning" }
        ],
        "condition": { "type": "forgero:is_raining" }
      }
    ],
    "minecraft:on_kill": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:life_steal", "amount": 5.0 },
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:strength",
            "duration": 200,
            "amplifier": 1
          }
        ]
      }
    ]
  }
}
```

### Vein Mining Shape

```json
{
  "type": "forgero:shape",
  "name": "Vein Miner Head",
  "tags": ["forgero:pickaxe_head_shape"],
  "local_tags": ["forgero:vein_mining"],
  "attributes": [
    {
      "id": "forgero:vein-mining_speed",
      "type": "forgero:mining_speed",
      "computation": { "multiply": 0.8 }
    }
  ],
  "properties": {
    "forgero:block_breaking": [
      {
        "selector": {
          "type": "forgero:radius",
          "radius": 3,
          "filter": {
            "type": "forgero:filter_wrapper",
            "filters": [
              { "type": "forgero:can_mine" },
              { "type": "forgero:same_block" }
            ]
          }
        },
        "speed": { "type": "forgero:all" },
        "condition": { "type": "forgero:is_sneaking" }
      }
    ]
  }
}
```

### Auto-Smelt Binding Material

```json
{
  "type": "forgero:material",
  "name": "Blaze Rod",
  "tags": ["forgero:materials/binding", "forgero:upgrade/tip_reinforcement"],
  "host": {
    "identifiers": [{ "type": "item", "id": "minecraft:blaze_rod" }]
  },
  "properties": {
    "minecraft:on_loot_drop": [
      {
        "handler": {
          "type": "forgero:apply_functions",
          "functions": [
            {
              "type": "forgero:auto_smelt",
              "filter": { "type": "forgero:tag", "tag": "c:raw_ores" }
            },
            {
              "type": "forgero:auto_smelt",
              "filter": { "type": "forgero:tag", "tag": "c:cookable_meats" }
            }
          ]
        }
      }
    ]
  }
}
```

### Extension for Vanilla Iron

```json
{
  "type": "forgero:extension",
  "target": "forgero:materials/iron",
  "priority": 10,
  "attributes": [
    {
      "id": "mymod:iron-bonus-durability",
      "type": "forgero:durability",
      "computation": { "add": 50 }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:knockback", "force": 0.5, "direction": "push" }
        ],
        "condition": { "type": "forgero:random_chance", "chance": 0.1 }
      }
    ]
  }
}
```

---

## Type Reference

### Resource Types

| Type ID | Description |
|---------|-------------|
| `forgero:material` | Base material definition |
| `forgero:shape` | Shape definition |
| `forgero:schematic` | Enhanced shape variant |
| `forgero:cast` | Alternative shape variant |
| `forgero:static_part` | Non-generated component |

### Template Types

| Type ID | Description |
|---------|-------------|
| `forgero:part_template` | Composite part template |
| `forgero:equipment_template` | Tool/armor template |

### Special Types

| Type ID | Description |
|---------|-------------|
| `forgero:extension` | Extends existing definitions |

### Event Property Keys

| Key | Triggers When |
|-----|---------------|
| `minecraft:on_hit` | Entity is hit |
| `minecraft:on_tick` | Periodically while held/worn |
| `minecraft:on_kill` | Entity is killed |
| `minecraft:on_damage_received` | Wielder takes damage |
| `minecraft:on_sneak_toggle` | Player starts sneaking |
| `minecraft:on_loot_drop` | Loot is dropped |
| `forgero:block_breaking` | Breaking blocks |
| `minecraft:use_interaction` | Right-click actions |

### Effect Types

| Type ID | Description |
|---------|-------------|
| `forgero:fire` | Set on fire |
| `forgero:lightning` | Summon lightning |
| `forgero:status_effect` | Apply potion effect |
| `forgero:life_steal` | Heal attacker |
| `forgero:knockback` | Push/pull entities |
| `forgero:explosion` | Create explosion |
| `forgero:disarm` | Remove held items |
| `forgero:convert` | Convert entity type |
| `forgero:freeze` | Apply freeze effect |
| `forgero:spawn_entity` | Spawn entities |
| `forgero:velocity` | Manipulate velocity |
| `forgero:magnet` | Attract items |
| `forgero:modify_block` | Modify blocks |

### Selector Types

| Type ID | Description |
|---------|-------------|
| `forgero:single_target` | Only hit target |
| `forgero:aoe` | Radius around target |
| `forgero:cone` | Cone in front |
| `forgero:chain` | Chain between entities |

---

This guide covers the complete **modules-based** Forgero JSON API. All legacy content formats will be deprecated.

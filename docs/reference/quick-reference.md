# Forgero JSON API Quick Reference

Quick lookup tables for Forgero's JSON APIs. For detailed documentation, see:
- [Resource Pack Developer Guide](../RESOURCE_PACK_DEVELOPER_GUIDE.md) - Full data API documentation
- [Model Module README](../modules/model/README.md) - Model system documentation

---

## Type Identifiers

### Data Types (Server-Side)

| Type | File Location | Description |
|------|---------------|-------------|
| `forgero:material` | `data/forgero/materials/` | Material definitions |
| `forgero:shape` | `data/forgero/shapes/` | Shape definitions |
| `forgero:schematic` | `data/forgero/schematics/` | Schematic definitions |
| `forgero:static_part` | `data/forgero/parts/` | Non-generated parts |
| `forgero:part_template` | `data/forgero/parts/` | Part template definitions |
| `forgero:equipment_template` | `data/forgero/equipment/` | Equipment templates |
| `forgero:extension` | `data/forgero/extensions/` | Data extensions |

### Model Types (Client-Side)

| Type | File Location | Description |
|------|---------------|-------------|
| `forgero:texture_model` | `assets/forgero/forgero_models/` | Simple texture model |
| `forgero:composite_model` | `assets/forgero/forgero_models/` | Layered composite model |
| `forgero:contextual_model` | `assets/forgero/forgero_models/` | Context-variant model |
| `forgero:model_extension` | `assets/forgero/forgero_models/` | Model extension |
| `forgero:part_model_template` | `assets/forgero/model_templates/` | Part model template |
| `forgero:equipment_model_template` | `assets/forgero/model_templates/` | Equipment model template |
| `forgero:upgrade_model_template` | `assets/forgero/model_templates/` | Upgrade model template |
| `forgero:armor_model_template` | `assets/forgero/model_templates/` | Armor model template |

---

## Common Fields

### All Data Types

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Type identifier (required) |
| `name` | string | Unique name within type |
| `include` | string[] | IDs to inherit from |
| `tags` | string[] | Tags (inherited via include) |
| `local_tags` | string[] | Tags (not inherited) |
| `attributes` | object[] | Attributes (inherited) |
| `local_attributes` | object[] | Attributes (not inherited) |
| `properties` | object | Event handlers and behaviors |

### Extension Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `target` | string | - | ID of definition to extend (required) |
| `priority` | int | 0 | Merge priority (lower first) |

---

## Attribute System

### Attribute Types

| Type ID | Description |
|---------|-------------|
| `forgero:durability` | Item durability |
| `forgero:attack_damage` | Base attack damage |
| `forgero:attack_speed` | Attack speed modifier |
| `forgero:mining_speed` | Block breaking speed |
| `forgero:mining_level` | Tool tier (0-4) |
| `forgero:armor` | Armor points |
| `forgero:armor_toughness` | Armor toughness |
| `forgero:rarity` | Item rarity value |
| `forgero:weight` | Item weight |
| `forgero:draw_speed` | Bow draw speed |
| `forgero:draw_power` | Bow power |

### Computation Operators

| Operator | Shorthand | Example |
|----------|-----------|---------|
| Addition | `add` | `{ "add": 10 }` or `10` |
| Subtraction | `subtract` | `{ "subtract": 5 }` |
| Multiplication | `multiply` | `{ "multiply": 1.5 }` |
| Division | `divide` | `{ "divide": 2.0 }` |

### Calculation Orders

| Order | When Applied |
|-------|--------------|
| `forgero:base` | Initial value (default) |
| `forgero:middle` | After base operations |
| `forgero:end` | Final application |

---

## Property Events

| Event Key | Trigger |
|-----------|---------|
| `minecraft:on_hit` | Entity is hit |
| `minecraft:on_tick` | Periodically while held/worn |
| `minecraft:on_kill` | Entity is killed |
| `minecraft:on_damage_received` | Wielder takes damage |
| `minecraft:on_sneak_toggle` | Player starts sneaking |
| `minecraft:on_loot_drop` | Loot is dropped |
| `forgero:block_breaking` | Breaking blocks |
| `minecraft:use_interaction` | Right-click actions |

---

## Effect Types

| Type ID | Parameters | Description |
|---------|------------|-------------|
| `forgero:fire` | `duration` | Set on fire |
| `forgero:lightning` | - | Summon lightning |
| `forgero:status_effect` | `effect`, `duration`, `amplifier` | Apply potion |
| `forgero:life_steal` | `amount` | Heal attacker |
| `forgero:knockback` | `force`, `direction` | Push/pull |
| `forgero:explosion` | `power`, `create_fire`, `destruction_type` | Create explosion |
| `forgero:disarm` | - | Remove held items |
| `forgero:convert` | `convert_to` | Convert entity type |
| `forgero:freeze` | `duration` | Apply freeze |
| `forgero:spawn_entity` | `entity`, `count`, `offset`, `on_target` | Spawn entities |
| `forgero:velocity` | `target`, `amount`, `mode` | Manipulate velocity |
| `forgero:magnet` | `radius`, `speed` | Attract items |
| `forgero:modify_block` | `action`, `target_block`, `replacement` | Modify blocks |

---

## Selector Types

| Type ID | Parameters | Description |
|---------|------------|-------------|
| `forgero:single_target` | - | Only hit target |
| `forgero:aoe` | `radius`, `filters` | Radius around target |
| `forgero:cone` | `angle`, `range`, `filters` | Cone in front |
| `forgero:chain` | `maxChains`, `chainRange`, `filters` | Chain between entities |

---

## Entity Filters

| Type ID | Parameters | Description |
|---------|------------|-------------|
| `forgero:is_alive` | - | Entity is alive |
| `forgero:is_hostile` | - | Entity is hostile |
| `forgero:is_player` | - | Entity is player |
| `forgero:is_burning` | - | Entity is on fire |
| `forgero:is_in_water` | - | Entity is in water |
| `forgero:is_teammate` | `invert` | Team check |
| `forgero:entity_type` | `entity_type` | Match entity type |
| `forgero:has_tag` | `tag` | Has entity tag |
| `forgero:has_effect` | `effect` | Has potion effect |
| `forgero:health_threshold` | `threshold`, `comparator` | Health comparison |
| `forgero:distance` | `min`, `max` | Distance check |
| `forgero:random_chance` | `chance` | Random probability |
| `forgero:environment` | `condition` | Environment check |
| `forgero:and` | `filters` | All must match |
| `forgero:or` | `filters` | Any must match |
| `forgero:not` | `filter` | Invert filter |

---

## Model Predicate Types

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:root_tag` | `tag` | Root component has tag |
| `forgero:child_tag` | `tag` | Child component has tag |
| `forgero:bow_pulling` | `pulling` | Bow is being pulled |
| `forgero:bow_pull` | `pull` (0.0-1.0) | Bow pull percentage |

---

## Block Breaking

### Block Selectors

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:radius` | `radius`, `filter` | Radius/vein mining |
| `forgero:column` | `depth`, `filter` | Column mining |
| `forgero:pattern` | `pattern`, `filter` | Pattern mining |

### Block Filters

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:can_mine` | - | Tool can mine block |
| `forgero:same_block` | - | Same block type |
| `forgero:similar_block` | - | Similar block |
| `forgero:is_block` | `block` | Specific block |
| `forgero:block_tag` | `tag` | Block has tag |

### Speed Calculators

| Type | Description |
|------|-------------|
| `forgero:all` | Same speed for all |
| `forgero:single` | Only root block normal speed |
| `forgero:average` | Average of all blocks |
| `forgero:instant` | Instant breaking |

---

## Loot Functions

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:auto_smelt` | `filter` | Smelt matching items |
| `forgero:item_transform` | `input`, `output`, `count` | Transform items |

---

## Static Conditions

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:in_slot_type` | `slot_type` | In specific slot |
| `forgero:is_root` | - | Is root component |
| `forgero:at_depth` | `depth` | At specific depth |
| `forgero:self_has_tag` | `tag` | Self has tag |
| `forgero:root_has_tag` | `tag` | Root has tag |
| `forgero:has_sibling` | `sibling_id` | Has sibling |
| `forgero:slot_contains` | `slot_type`, `tag` | Slot content check |
| `forgero:id_match` | `id` | ID matches |

---

## Dynamic Conditions

| Type | Parameters | Description |
|------|------------|-------------|
| `forgero:target_has_tag` | `tag` | Target has tag |
| `forgero:is_sneaking` | - | Player sneaking |
| `forgero:is_raining` | - | Weather is rain |
| `forgero:random_chance` | `chance` | Random probability |
| `forgero:and` | `conditions` | All must match |
| `forgero:or` | `conditions` | Any must match |
| `forgero:not` | `condition` | Invert condition |

---

## File Locations

```
data/forgero/                    # Data files (server-side)
├── materials/                  # Material definitions
├── shapes/                     # Shape definitions
├── schematics/                 # Schematic definitions
├── parts/                      # Part templates and static parts
├── equipment/                  # Equipment templates
├── extensions/                 # Data extensions
├── tags/                       # Tag definitions
│   ├── materials/
│   ├── parts/
│   └── shapes/
└── properties/                 # Custom property definitions

assets/forgero/                  # Asset files (client-side)
├── forgero_models/             # Model definitions
│   ├── parts/                  # Part models
│   ├── equipment/              # Equipment models
│   └── upgrades/               # Upgrade models
├── model_templates/            # Model generation templates
│   ├── parts/
│   └── equipment/
├── texture_templates/          # Grayscale PNG templates
│   └── parts/
├── palettes/                   # Color palette PNGs
│   └── {material}.png
└── textures/                   # Generated/static textures
    └── item/
```

---

## Extension Merge Semantics

### Data Extensions (`forgero:extension`)

| Field | Strategy |
|-------|----------|
| `tags` | Union (appended) |
| `attributes` | Concatenate (appended) |
| `properties` | Deep merge |
| `upgrades` | By ID: extension wins |

### Model Extensions (`forgero:model_extension`)

| Field | Strategy |
|-------|----------|
| `layers` | Concatenate (appended) |
| `slots` | By ID: extension wins |
| `mount_points` | By name: extension wins |

---

## Template Placeholders

| Placeholder | Description |
|-------------|-------------|
| `{target}` | Target component name |
| `{target.name}` | Target name (explicit) |
| `{material}` | Material component name |
| `{material.name}` | Material name |
| `{head.material.name}` | Nested traversal |
| `{shape.name}` | Shape name |
| `{shape.shape_name}` | Shape component name |

**Special handling**: `_shape` suffix automatically stripped (`iron_shape` → `iron`)

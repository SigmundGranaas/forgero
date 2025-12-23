# Forgero Custom Properties Guide

Welcome to the Forgero property system! This data-driven framework allows you to add custom behaviors to tools, weapons, and armor using JSON files.

This guide is structured in two main parts:
1.  **Event Properties:** High-level events that are triggered in-game (e.g., breaking a block, hitting an entity). These events provide "slots" for you to plug in logic.
2.  **Reusable Logic Components:** The modular pieces of logic (Handlers, Selectors, Filters) that you can plug into the event slots. Many of these components can be reused across different events if they share a common interface.

---

## Part 1: Event Properties

These are the top-level property keys you can add to a component's `properties` object. Each one defines a contract for the kind of logic it accepts.

### On Entity Hit (`minecraft:on_hit`)

Triggers one or more effects when the wielder hits an entity. This property uses the full three-tier architecture, combining a selector with a list of effects.

**Interface Slots:**
*   `selector`: Expects an `EntitySelector`. Defines which entities to target (e.g., single target, area of effect).
*   `effects`: Expects a list of `OnHitEffect` components. These are the actual effects to apply to the selected targets.

**Example:**
```json
{
  "minecraft:on_hit": [
    {
      "selector": {
        "type": "forgero:aoe",
        "radius": 3
      },
      "effects": [
        {
          "type": "forgero:lightning"
        },
        {
          "type": "forgero:fire",
          "duration": 5
        }
      ]
    }
  ]
}
```

### Block Breaking (`forgero:block_breaking`)

Modifies how a tool breaks blocks, allowing for Area of Effect (AoE) mining patterns and custom breaking speeds.

**Interface Slots:**
*   `selector`: Expects a `BlockSelector`. Defines the shape and criteria for selecting multiple blocks.
*   `speed`: Expects a `BlockBreakSpeedCalculator`. Determines how breaking speed is calculated for all selected blocks.

**Example:**
```json
{
  "forgero:block_breaking": [
    {
      "selector": {
        "type": "forgero:pattern",
        "pattern": ["xxx", "xcx", "xxx"],
        "filter": {
          "type": "forgero:similar_block"
        }
      },
      "speed": {
        "type": "forgero:average"
      }
    }
  ]
}
```


### On Loot Drop (`minecraft:on_loot_drop`)

Modifies the loot dropped by blocks or entities.

**Interface Slots:**
*   `handler`: Expects a `LootHandler`. Defines how the list of all dropped items is processed.

**Example:**
```json
{
  "minecraft:on_loot_drop": [
    {
      "handler": {
        "type": "forgero:apply_functions",
        "functions": [
          {
            "type": "forgero:auto_smelt",
            "filter": {
              "type": "forgero:tag",
              "tag": "c:raw_ores"
            }
          }
        ]
      }
    }
  ]
}
```

---

## Part 2: Reusable Logic Components

These are the concrete implementations you can "plug into" the slots defined by the Event Properties above.

### On-Hit Selectors (`EntitySelector`)
*Used by: `minecraft:on_hit`, `minecraft:on_tick`*

Entity selectors define **who** gets targeted by effects. All selectors support an optional `filters` array to narrow down the selection.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:single_target`** | Selects only the entity that was directly hit. | `filters` (List<EntityFilter>, optional) |
| **`forgero:aoe`** | Selects all entities in a radius around the initial target. Excludes the source entity. | `radius` (int, **required**)<br>`filters` (List<EntityFilter>, optional) |
| **`forgero:cone`** | Selects entities within a cone-shaped area in front of the source entity. Perfect for sweep attacks or breath weapons. | `angle` (float, **required**, degrees, 0-360)<br>`range` (float, **required**)<br>`filters` (List<EntityFilter>, optional) |
| **`forgero:chain`** | Chains from the initial target to nearby entities, like chain lightning. | `maxChains` (int, **required**, 0+)<br>`chainRange` (float, **required**)<br>`allowRepeats` (boolean, optional, default: false)<br>`filters` (List<EntityFilter>, optional) |

**Example with filters:**
```json
{
  "selector": {
    "type": "forgero:aoe",
    "radius": 5,
    "filters": [
      {
        "type": "forgero:is_hostile"
      },
      {
        "type": "forgero:health_threshold",
        "threshold": 0.5,
        "comparator": "less_than"
      }
    ]
  }
}
```

### Entity Filters (`EntityFilter`)
*Used by: `EntitySelector` components*

Entity filters determine which entities from a selector's geometric/strategic selection should receive effects. Filters can be composed using logical operators (AND, OR, NOT).

#### Basic Filters

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:is_alive`** | Filters dead or removed entities. | *None* |
| **`forgero:is_hostile`** | Filters entities that extend `HostileEntity` (zombies, skeletons, etc.). | *None* |
| **`forgero:is_teammate`** | Filters based on team relationship with source. | `invert` (boolean, optional, default: false) |
| **`forgero:has_tag`** | Filters entities by entity type tag (e.g., `minecraft:undead`). | `tag` (Identifier, **required**) |
| **`forgero:entity_type`** | Filters by specific entity type (e.g., `minecraft:zombie`). | `entity_type` (Identifier, **required**) |
| **`forgero:is_player`** | Filters entities that are players. | *None* |

#### State-Based Filters

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:is_burning`** | Filters entities that are currently on fire. | *None* |
| **`forgero:is_in_water`** | Filters entities that are in or touching water. | *None* |
| **`forgero:has_effect`** | Filters entities with a specific status effect (e.g., `minecraft:poison`). | `effect` (Identifier, **required**) |

#### Advanced Filters

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:health_threshold`** | Filters entities based on health percentage (0.0-1.0). | `threshold` (float, **required**, 0.0-1.0)<br>`comparator` (String, **required**, options: `less_than`, `less_than_or_equal`, `greater_than`, `greater_than_or_equal`, `equal`) |
| **`forgero:distance`** | Filters entities within a distance range from source. | `min` (float, optional, default: 0.0)<br>`max` (float, **required**) |
| **`forgero:random_chance`** | Filters entities based on random chance (0.0-1.0). | `chance` (float, **required**, 0.0-1.0) |

#### Composite Filters

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:and`** | Requires ALL nested filters to pass. Short-circuits on first failure. | `filters` (List<EntityFilter>, **required**) |
| **`forgero:or`** | Requires ANY nested filter to pass. Short-circuits on first success. | `filters` (List<EntityFilter>, **required**) |
| **`forgero:not`** | Inverts the result of the nested filter. | `filter` (EntityFilter, **required**) |

**Example - Complex filter composition:**
```json
{
  "type": "forgero:and",
  "filters": [
    {
      "type": "forgero:is_hostile"
    },
    {
      "type": "forgero:or",
      "filters": [
        {
          "type": "forgero:is_burning"
        },
        {
          "type": "forgero:health_threshold",
          "threshold": 0.3,
          "comparator": "less_than"
        }
      ]
    }
  ]
}
```
*Selects hostile entities that are either burning OR have less than 30% health.*

### On-Hit Effects (`OnHitEffect`)
*Used by: `minecraft:on_hit`*

These components define an action to be performed on the targets chosen by an `EntitySelector`.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:convert`** | Converts the target entity into another entity. | `convert_to` (Identifier, **required**) |
| **`forgero:disarm`** | Forces the target to drop their main-hand item. | *None* |
| **`forgero:explosion`** | Creates an explosion at the target's location. | `power` (float, **required**)<br>`create_fire` (boolean, optional, default: `false`)<br>`destruction_type` (String, optional, default: "none", options: `none`, `block`, `mob`) |
| **`forgero:fire`** | Sets the target on fire. | `duration` (int, **required**, in seconds) |
| **`forgero:knockback`** | Applies knockback to the target. | `force` (float, **required**)<br>`direction` (String, **required**, options: `push`, `pull`) |
| **`forgero:life_steal`** | Damages the target and heals the attacker. | `amount` (float, **required**) |
| **`forgero:lightning`** | Strikes the target with a lightning bolt. | *None* |
| **`forgero:status_effect`** | Applies a status effect to the target. | `effect` (Identifier, **required**)<br>`duration` (int, **required**, in ticks)<br>`amplifier` (int, optional, default: 0) |

### Block Selectors (`BlockSelector`)
*Used by: `forgero:block_breaking`*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:single`** | Selects only the single target block. | *None* |
| **`forgero:column`** | Selects a column of blocks. The `depth` finds adjacent columns. | `depth` (int, optional, default: 1)<br>`height` (int, optional, default: 1)<br>`filter` (BlockFilter, **required**) |
| **`forgero:pattern`** | Selects blocks in a user-defined pattern relative to the player's facing. 'x' or 'c' are valid blocks. | `pattern` (List<String>, **required**)<br>`depth` (int, optional, default: 1)<br>`direction` (String, optional, default: "multi")<br>`filter` (BlockFilter, **required**) |
| **`forgero:radius`** | Selects blocks in a vein-mining radius. | `radius` (int, optional, default: 1)<br>`filter` (BlockFilter, **required**) |

### Block Breaking Speed Calculators (`BlockBreakSpeedCalculator`)
*Used by: `forgero:block_breaking`*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:single`** | Calculates speed based only on the primary target block. | *None* |
| **`forgero:all`** | The total time is the sum of the time to break every block individually. Breaks blocks very slowly. | *None* |
| **`forgero:average`** | Calculates the average breaking speed of all selected blocks. | *None* |
| **`forgero:instant`** | Breaks blocks instantly. | `can_break_unmineable` (boolean, optional, default: `false`) |

### Block Filters (`BlockFilter`)
*Used by: `BlockSelector` components*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:can_mine`** | Checks if the player can harvest the block with the current tool. | *None* |
| **`forgero:is_block`** | Checks if the position is a non-air block. | *None* |
| **`forgero:same_block`** | Checks if the block is the same type as the originally targeted block. | *None* |
| **`forgero:similar_block`** | Checks if the block shares a `forgero:similar_block/` tag with the original block (e.g., various stone types). | *None* |
| **`forgero:filter_wrapper`** | A container that requires all nested filters to be true. | `filters` (List<BlockFilter>, **required**) |

### Loot Handlers (`LootHandler`)
*Used by: `minecraft:on_loot_drop`*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:apply_functions`** | Applies a chain of `ItemFunction`s to each item in the loot list. | `functions` (List<ItemFunction>, **required**) |

### Item Functions (`ItemFunction`)
*Used by: `forgero:apply_functions` Loot Handler*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:auto_smelt`** | Smelts the item if a valid smelting recipe exists. | `filter` (ItemFilter, **required**) |
| **`forgero:item_transform`** | Replaces a filtered item with a different item. | `input` (ItemFilter, **required**)<br>`output` (Identifier, **required**)<br>`count` (int, optional, default: 1) |

### Item Filters (`ItemFilter`)
*Used by: `ItemFunction` components*

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:is_item`** | Checks if the item matches a specific item ID. | `item` (Identifier, **required**) |
| **`forgero:tag`** | Checks if the item belongs to a specific item tag. | `tag` (Identifier, **required**) |

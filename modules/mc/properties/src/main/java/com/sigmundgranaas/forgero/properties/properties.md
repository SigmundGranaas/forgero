# Forgero Custom Properties Guide

Welcome to the Forgero property system! This data-driven framework allows you to add custom behaviors to tools, weapons, and armor using JSON files.

This guide is structured in two main parts:
1.  **Event Properties:** High-level events that are triggered in-game (e.g., breaking a block, hitting an entity). These events provide "slots" for you to plug in logic.
2.  **Reusable Logic Components:** The modular pieces of logic (Handlers, Selectors, Filters) that you can plug into the event slots. Many of these components can be reused across different events if they share a common interface.

---

## Part 1: Event Properties

These are the top-level property keys you can add to a component's `properties` object. Each one defines a contract for the kind of logic it accepts.

### Block Breaking (`forgero:block_breaking`)

Modifies how a tool breaks blocks, allowing for Area of Effect (AoE) mining patterns and custom breaking speeds.

**Interface Slots:**
*   `selector`: Expects a `BlockSelector` component. Defines the shape and criteria for selecting multiple blocks.
*   `speed`: Expects a `BlockBreakSpeedCalculator` component. Determines how breaking speed is calculated for all selected blocks.

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

### On Entity Hit (`minecraft:on_hit`)

Triggers an effect when the wielder hits an entity with the item.

**Interface Slots:**
*   `handler`: Expects an `OnHitHandler` component. Defines the action to perform on hit.

**Example:**
```json
{
  "minecraft:on_hit": [
    {
      "handler": {
        "type": "forgero:lightning"
      }
    }
  ]
}
```

### On Loot Drop (`minecraft:on_loot_drop`)

Modifies the loot dropped by blocks or entities.

**Interface Slots:**
*   `handler`: Expects a `LootHandler` component. Defines how the list of all dropped items is processed.

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

These are the concrete implementations you can "plug into" the slots defined by the Event Properties above. They are grouped by the interface they implement.

### On-Hit Handlers (`OnHitHandler`)
*Used by: `minecraft:on_hit`*

These components define an action to be performed when an entity is hit.

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

These components define a selection of blocks for an operation. They often use `BlockFilter` components.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:single`** | Selects only the single target block. | *None* |
| **`forgero:column`** | Selects a column of blocks. The `depth` finds adjacent columns. | `depth` (int, optional, default: 1)<br>`height` (int, optional, default: 1)<br>`filter` (BlockFilter, **required**) |
| **`forgero:pattern`** | Selects blocks in a user-defined pattern relative to the player's facing. 'x' or 'c' are valid blocks. | `pattern` (List<String>, **required**)<br>`depth` (int, optional, default: 1)<br>`direction` (String, optional, default: "multi")<br>`filter` (BlockFilter, **required**) |
| **`forgero:radius`** | Selects blocks in a vein-mining radius. | `radius` (int, optional, default: 1)<br>`filter` (BlockFilter, **required**) |

### Block Breaking Speed Calculators (`BlockBreakSpeedCalculator`)
*Used by: `forgero:block_breaking`*

These components calculate the breaking speed for a selection of blocks.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:single`** | Calculates speed based only on the primary target block. | *None* |
| **`forgero:all`** | The total time is the sum of the time to break every block individually. Breaks blocks very slowly. | *None* |
| **`forgero:average`** | Calculates the average breaking speed of all selected blocks. | *None* |
| **`forgero:instant`** | Breaks blocks instantly. | `can_break_unmineable` (boolean, optional, default: `false`) |

### Block Filters (`BlockFilter`)
*Used by: `BlockSelector` components*

These components are used to validate if a block should be included in a selection.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:can_mine`** | Checks if the player can harvest the block with the current tool. | *None* |
| **`forgero:is_block`** | Checks if the position is a non-air block. | *None* |
| **`forgero:same_block`** | Checks if the block is the same type as the originally targeted block. | *None* |
| **`forgero:similar_block`** | Checks if the block shares a `forgero:similar_block/` tag with the original block (e.g., various stone types). | *None* |
| **`forgero:filter_wrapper`** | A container that requires all nested filters to be true. | `filters` (List<BlockFilter>, **required**) |

### Loot Handlers (`LootHandler`)
*Used by: `minecraft:on_loot_drop`*

These components process a list of dropped items.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:apply_functions`** | Applies a chain of `ItemFunction`s to each item in the loot list. | `functions` (List<ItemFunction>, **required**) |

### Item Functions (`ItemFunction`)
*Used by: `forgero:apply_functions` Loot Handler*

These components define a transformation to be applied to a single `ItemStack`. They use `ItemFilter`s to determine if they should run.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:auto_smelt`** | Smelts the item if a valid smelting recipe exists. | `filter` (ItemFilter, **required**) |
| **`forgero:item_transform`** | Replaces a filtered item with a different item. | `input` (ItemFilter, **required**)<br>`output` (Identifier, **required**)<br>`count` (int, optional, default: 1) |

### Item Filters (`ItemFilter`)
*Used by: `ItemFunction` components*

These components check if an `ItemFunction` should be applied to an item.

| `type` | Description | JSON Properties |
| :--- | :--- | :--- |
| **`forgero:is_item`** | Checks if the item matches a specific item ID. | `item` (Identifier, **required**) |
| **`forgero:tag`** | Checks if the item belongs to a specific item tag. | `tag` (Identifier, **required**) |

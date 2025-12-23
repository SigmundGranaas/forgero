# Vanilla Upgrades AI Context

## What is Vanilla Upgrades?

Vanilla Upgrades is a test mod that validates the new Forgero architecture by making **all vanilla Minecraft items** upgradeable through Forgero's component system. This allows players to use any vanilla item as an upgrade material, giving each item unique properties that create meaningful gameplay experiences.

**Core Innovation**: Override vanilla items with Forgero components, making them compatible with Forgero's dynamic attribute and property system without replacing the vanilla items themselves.

**Design Goal**: Create unique, fun, and explorable gameplay by giving each vanilla item fitting properties based on its nature, tier, and availability. Properties should feel thematically appropriate and encourage experimentation.

---

## Architectural Principles (CRITICAL)

When suggesting new properties or features, you MUST follow these principles:

### 1. **Modular and Reusable**
- Properties compose from generic, reusable components (Selectors, Effects, Filters, Handlers)
- New features should build on existing interfaces, not create parallel systems
- Prefer composition over specialized one-off implementations

### 2. **Data-Driven, Not Code-Heavy**
- Prefer JSON configuration over hardcoded logic
- Effects should be simple, focused, and declarative
- Avoid effects that require complex state machines or multi-tick coordination

### 3. **No Complex State Tracking**
- Avoid effects that require persistent state across server restarts
- Avoid effects that need client-server synchronization beyond what Minecraft provides
- Don't suggest effects that require tracking state over time (e.g., "count hits and explode on 5th hit")
- Simple per-entity or per-world state is acceptable (e.g., status effects, entity data)

### 4. **Feasible Implementation**
- If suggesting a new effect, you MUST define:
  - The generic event it hooks into (OnHit, OnTick, OnLootDrop, etc.)
  - The handler interface it implements (EntityEffectHandler, ContextualEffectHandler, etc.)
  - All required parameters and their types
  - Whether it's server-only or client-server
  - Any Minecraft APIs it relies on
- No hand-waving or "implementation left as exercise"

### 5. **Performance Conscious**
- OnTick effects must have reasonable intervals (minimum 10 ticks, prefer 20+)
- AoE effects should have sensible radius limits (max ~10 blocks for common effects)
- Avoid effects that scan large areas or iterate over many entities every tick

### 6. **Thematically Appropriate**
- Properties should match the vanilla item's nature and lore
- Example: Blaze Rod → auto-smelt (fire theme)
- Example: Bone → extra damage to undead (skeleton theme)
- Example: Feather → reduced fall damage or knockback resistance

### 7. **Create Exciting, Memorable Gameplay** (CRITICAL)
- **NEVER use negative stats** (negative damage, negative speed, etc.) - they're anti-fun and make players avoid your item
- **Multiple effects per item** - Combine 2-3 complementary effects for depth
- **Create memorable moments** - Effects that enhance Minecraft's core gameplay in meaningful ways
- **Synergies over simplicity** - How does this item combo with other systems?
- **Innovation over safety** - Don't just add "Status Effect X" - think bigger!
- **Player empowerment** - Items should make players feel powerful, not punished
- **Grounded in Minecraft's world** - Avoid over-the-top RPG/MMO language, keep it natural to Minecraft
- **Unique within item families** - If similar items exist (seeds, flowers, stone types), each must have DISTINCT effects

**Bad Design Philosophy:**
- "This item gives you +1 thing but -2 other things" ❌
- "Simple status effect, that's it" ❌
- "One-dimensional, single use case" ❌
- "Generic effect that could apply to any similar item" ❌ (e.g., all seeds doing the same thing)
- "Over-the-top marketing language ('POWER FANTASY: BE A GOD!')" ❌

**Good Design Philosophy:**
- "This item creates a unique playstyle grounded in Minecraft" ✅
- "Multiple complementary effects that synergize" ✅
- "Memorable moments that feel natural to the game" ✅
- "Strategic choices with meaningful tradeoffs (not punishments)" ✅
- "Distinct from similar items in the same family" ✅

---

## Component System Architecture

### Understanding Components and Hierarchies

Before diving into properties, understand how Forgero organizes items:

**Everything in Forgero is a Component**. Components compose into hierarchies where:
- **Materials** (iron, leather, blaze rod) are components
- **Parts** (pickaxe head, handle) are components built from materials
- **Tools** (pickaxe, sword) are components built from parts
- **Upgrades** (binding, gem) are components added to tools

**Three-Level Hierarchy:**
```
Root Tool (Iron Pickaxe)
├── Structure Slots (Required Parts)
│   ├── head: Iron Pickaxe Head
│   │   └── material: Iron Ingot
│   └── handle: Oak Handle
│       └── material: Oak Planks
└── Upgrade Slots (Optional Customizations)
    ├── binding_slot: Leather Binding (optional)
    └── reinforcement_slot: Diamond (optional)
```

**Key Concept**: When you place a vanilla item (like blaze rod) into a tool as an upgrade, it becomes part of this hierarchy and its properties/attributes flow up to the root tool.

---

### Component Types

#### 1. Material Components
**What they are:** Raw materials that define base stats and properties
**Examples:** Iron Ingot, Diamond, Blaze Rod, Leather, Feather

**JSON Structure:**
```json
{
  "type": "forgero:material",
  "name": "Iron",
  "tags": ["forgero:materials/metal", "forgero:upgrade/tip_reinforcement"],
  "host": {
    "identifiers": [{ "type": "item", "id": "minecraft:iron_ingot" }]
  },
  "attributes": [...],
  "properties": {...}
}
```

**Key Fields:**
- `tags`: Determines where this material can be used (as upgrade, in specific slots, etc.)
- `host.identifiers`: Maps to vanilla Minecraft item
- `attributes`: Stat bonuses this material provides
- `properties`: Behaviors this material adds (OnHit effects, etc.)

#### 2. Part Components (Structured)
**What they are:** Intermediate components with required structural slots
**Examples:** Pickaxe Head, Handle, Armor Plate

Parts have **structure slots** that MUST be filled:
```json
{
  "type": "forgero:schematic_part",
  "name": "Pickaxe Head",
  "structure": {
    "slots": {
      "material": {
        "type": "forgero:material_type",
        "default_tag": "forgero:materials/tool_material"
      }
    }
  },
  "upgrades": [
    {
      "id": "reinforcement_slot",
      "type": "forgero:upgrade_material",
      "tags": ["forgero:materials/tool_material"]
    }
  ]
}
```

#### 3. Equipment Components (Tools/Armor)
**What they are:** Final assembled items players use
**Examples:** Pickaxe, Sword, Chestplate

Equipment has both **structure slots** (required parts) and **upgrade slots** (optional customizations):
```json
{
  "type": "forgero:equipment_template",
  "name": "Pickaxe",
  "structure": {
    "slots": {
      "head": {
        "type": "forgero:parts/pickaxe_head_type",
        "default_tag": "forgero:parts/default_pickaxe_head"
      },
      "handle": {
        "type": "forgero:parts/handle_type",
        "default_tag": "forgero:parts/default_handle"
      }
    }
  },
  "upgrades": [
    {
      "id": "binding_slot",
      "type": "forgero:binding",
      "tags": ["forgero:upgrade/binding"]
    }
  ]
}
```

---

### The Slot System

Slots are **typed containers** where components fit. Two types exist:

#### Structure Slots (Required)
- **Always filled** - cannot be empty
- Define the fundamental structure of an item
- Example: A pickaxe MUST have a head and handle

#### Upgrade Slots (Optional)
- **Can be empty** - player choice to fill
- Customization points for tools/armor
- Validated by tags - only compatible items fit
- Example: A pickaxe MAY have a binding upgrade

**Common Upgrade Slot Types:**

| Slot Type | Accepts Tags | Use Case | Example Items |
|-----------|--------------|----------|---------------|
| `forgero:binding` | `forgero:upgrade/binding` | Wrapping/grip on handle | Leather, String, Phantom Membrane |
| `forgero:grip` | `forgero:upgrade/grip` | Handle grip enhancement | Leather, Wool, Slimeball |
| `forgero:tip_reinforcement` | `forgero:upgrade/tip_reinforcement` | Tool tip/edge hardening | Diamond, Netherite, Obsidian, Blaze Rod |
| `forgero:gem` | `forgero:upgrade/gem` | Socketed gems/special items | Emerald, Amethyst, Ender Pearl |
| `forgero:upgrade_material` | `forgero:materials/tool_material` | Generic material upgrade | Any tool material |

**Slot Validation:**
When you try to put an item in a slot, Forgero checks: "Does this component have ALL the tags the slot requires?"

Example:
- Blaze Rod has tags: `["forgero:upgrade/binding", "forgero:upgrade/tip_reinforcement"]`
- Binding slot requires: `["forgero:upgrade/binding"]`
- ✅ Valid - Blaze Rod can go in binding slot
- Reinforcement slot requires: `["forgero:upgrade/tip_reinforcement"]`
- ✅ Valid - Blaze Rod can go in reinforcement slot too!

---

### Creating New Slots

You can define new slot types dynamically in your JSON:

```json
{
  "upgrades": [
    {
      "id": "charm_slot",
      "type": "forgero:charm",
      "tags": ["forgero:upgrade/charm"],
      "description": "forgero.slot.charm"
    }
  ]
}
```

Then tag materials that should fit:
```json
{
  "type": "forgero:material",
  "name": "Rabbit Foot",
  "tags": ["forgero:upgrade/charm"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:rabbit_foot" }] }
}
```

**Result:** Rabbit Foot can now be placed in any `charm_slot`.

---

### Composite Attributes - How Stats Aggregate

When a tool has multiple components (head, handle, binding, etc.), their attributes **compose** into the final stats.

**Composite Key:** `"composite": "forgero:part-composite"`

This tells Forgero: "Include this attribute in the final tool's stats."

**Example Flow:**

**Iron Material:**
```json
{
  "attributes": [
    {
      "id": "forgero:iron-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 240 }
    }
  ]
}
```

**Oak Handle:**
```json
{
  "attributes": [
    {
      "id": "forgero:oak-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 50 }
    }
  ]
}
```

**Leather Binding (Conditional):**
```json
{
  "attributes": [
    {
      "id": "forgero:leather-binding-durability",
      "type": "forgero:durability",
      "computation": { "value": 100 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:binding_slot"
      }
    }
  ]
}
```

**Final Calculation:**
1. Iron Pickaxe Head contributes: 240 durability (from iron material)
2. Oak Handle contributes: 50 durability (from oak material)
3. Leather Binding contributes: 100 durability (ONLY if in binding_slot)
4. **Total Durability** = 240 + 50 + 100 = **390**

**Key Point:** Attributes with `"composite": "forgero:part-composite"` automatically aggregate across the entire component hierarchy.

---

### Slot-Conditional Attributes

Use the `in_slot_type` condition to make attributes apply ONLY when a component is in a specific slot:

```json
{
  "attributes": [
    {
      "id": "forgero:feather-grip-attack_speed",
      "type": "forgero:attack_speed",
      "computation": { "value": 0.5 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:handle_grip_slot"
      }
    },
    {
      "id": "forgero:feather-binding-weight",
      "type": "forgero:weight",
      "computation": { "value": -1.0 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:binding_slot"
      }
    }
  ]
}
```

**Meaning:**
- If Feather is used as a grip → +0.5 attack speed
- If Feather is used as a binding → -1.0 weight (lighter)
- If Feather is used elsewhere → neither attribute applies

This allows **the same material to behave differently in different slots**.

---

### The Host System - Vanilla Item Integration

The `host` system maps vanilla Minecraft items to Forgero components:

**Three Mapping Types:**

#### 1. Map Existing Vanilla Item
```json
{
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:blaze_rod" },
      { "type": "tag", "id": "c:blaze_rod" }
    ]
  }
}
```
**Result:** The vanilla blaze rod item becomes this Forgero component. When used as an upgrade, it brings its properties/attributes.

#### 2. Create New Forgero Item
```json
{
  "host": {
    "create": {
      "id": "forgero:reinforced-pickaxe",
      "class_name": "forgero:tool_item",
      "item_group": "forgero:tools"
    }
  }
}
```
**Result:** Creates a new item in Minecraft's registry.

#### 3. Template-Based Creation
```json
{
  "host_template": {
    "create": {
      "id": "forgero:{material.name}-{part.name}",
      "class_name": "forgero:part_item"
    }
  }
}
```
**Result:** Auto-generates items for all combinations (iron-pickaxe_head, diamond-pickaxe_head, etc.).

**For Vanilla Upgrades:** Always use type 1 (map existing vanilla item) so players can use vanilla items directly as upgrades.

---

### Slot-Conditional Properties

Just like attributes, **properties can also be conditional** based on slot placement:

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:fire",
            "duration": 5
          }
        ],
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:tip_reinforcement_slot"
        }
      }
    ],
    "minecraft:on_tick": [
      {
        "selector": { "type": "forgero:aoe", "radius": 3 },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:fire_resistance",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20,
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:binding_slot"
        }
      }
    ]
  }
}
```

**Meaning:**
- If Blaze Rod is in tip reinforcement → sets targets on fire when hitting
- If Blaze Rod is in binding → provides fire resistance aura
- This creates **different playstyles for the same material**

---

### Root-Conditional Properties

Properties can check what tool they're installed in:

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:aoe", "radius": 4 },
        "effects": [
          {
            "type": "forgero:knockback",
            "force": 2.0,
            "direction": "push"
          }
        ],
        "condition": {
          "type": "forgero:root_has_tag",
          "tag": "forgero:tool/sword"
        }
      }
    ]
  }
}
```

**Meaning:** This effect only works when the upgrade is installed in a sword (not pickaxe, axe, etc.).

**Use Case:** Create specialized upgrades that synergize with specific tool types.

---

## Current Forgero Architecture

### Property System Overview

Forgero's property system is **event-driven** and **composable**:

1. **Events** define when things happen (`minecraft:on_hit`, `minecraft:on_tick`, `minecraft:on_loot_drop`, `forgero:block_breaking`)
2. **Selectors** choose targets (which entities/blocks to affect)
3. **Effects/Handlers** define what happens to selected targets
4. **Conditions** determine if a property should activate (dynamic game state checks)
5. **Attributes** provide numeric stat bonuses (durability, attack damage, etc.)

**Example Flow:**
```
Player hits entity → OnHit event fires → Check conditions → Selector chooses targets → Effects apply to each target
```

---

## Available Events

### 1. On Entity Hit (`minecraft:on_hit`)

Triggers when the wielder hits an entity with the item.

**Three-Tier Structure:**
- `selector`: EntitySelector - Who to affect
- `effects`: List<OnHitEffect> - What happens to each target
- `condition`: Condition (optional) - When to trigger

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
      ],
      "condition": {
        "type": "forgero:target_has_tag",
        "tag": "minecraft:undead"
      }
    }
  ]
}
```

**When to use:** Combat effects, entity interactions, damage-based triggers

---

### 2. On Tick (`minecraft:on_tick`)

Periodic effect that triggers while the item is held/worn.

**Structure:**
- `selector`: EntitySelector - Who to affect
- `effects`: List<OnHitEffect> - What happens to each target
- `interval`: int (ticks, default 20) - How often to trigger
- `condition`: Condition (optional) - When to trigger

**Example:**
```json
{
  "minecraft:on_tick": [
    {
      "selector": {
        "type": "forgero:aoe",
        "radius": 5
      },
      "effects": [
        {
          "type": "forgero:status_effect",
          "effect": "minecraft:regeneration",
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
```

**When to use:** Auras, passive effects, conditional buffs/debuffs

**Performance Note:** Always set `interval` ≥ 20 (1 second) for common effects. Use higher intervals for expensive effects.

---

### 3. On Loot Drop (`minecraft:on_loot_drop`)

Modifies items dropped from blocks or entities.

**Structure:**
- `handler`: LootHandler - How to process drops

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

**When to use:** Fortune effects, item transformations, auto-processing

---

### 4. Block Breaking (`forgero:block_breaking`)

Modifies block breaking behavior (AoE mining, speed calculations).

**Structure:**
- `selector`: BlockSelector - Which blocks to break
- `speed`: BlockBreakSpeedCalculator - How fast to break them

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

**When to use:** AoE mining, vein mining, excavation patterns

---

## Available Components

### Entity Selectors (`EntitySelector`)

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:single_target` | Only the directly hit entity | None |
| `forgero:aoe` | All entities in radius around target | `radius` (int, required) |

**Usage:** Choose `single_target` for focused effects, `aoe` for area effects.

**Balance Guidance:**
- Radius 2-3: Moderate AoE (common for combat)
- Radius 4-6: Large AoE (rare/expensive materials)
- Radius 7+: Extreme AoE (very rare materials, high cost)

---

### On-Hit Effects (`OnHitEffect`)

These apply to targets selected by an EntitySelector.

| Type | Description | Parameters | Use Case |
|------|-------------|------------|----------|
| `forgero:fire` | Sets target on fire | `duration` (int, seconds) | Fire-themed items |
| `forgero:lightning` | Strikes with lightning bolt | None | Storm/electric theme |
| `forgero:status_effect` | Applies potion effect | `effect` (Identifier)<br>`duration` (int, ticks)<br>`amplifier` (int, optional, default 0) | Poison, slowness, weakness, etc. |
| `forgero:life_steal` | Heals attacker | `amount` (float, health) | Vampiric theme |
| `forgero:knockback` | Pushes or pulls target | `force` (float)<br>`direction` ("push" or "pull") | Force/wind theme |
| `forgero:explosion` | Creates explosion | `power` (float)<br>`create_fire` (bool, optional, default false)<br>`destruction_type` (string, "none"/"block"/"mob") | TNT/explosive theme |
| `forgero:convert` | Transforms entity type | `convert_to` (Identifier) | Transformation magic |
| `forgero:disarm` | Makes target drop held item | None | Combat utility |

**Implementation Notes:**
- All effects check `!world.isClient` before applying (server-side only)
- `EntityEffectHandler`: Needs only target entity
- `ContextualEffectHandler`: Needs both source and target (used by life_steal, explosion with context)

**Balance Guidance:**
- Fire duration: 2-5 seconds (common), 5-10 seconds (rare)
- Status effect duration: 60-100 ticks (3-5 sec, common), 100-200 ticks (5-10 sec, rare)
- Life steal: 1-2 HP (common), 3-4 HP (rare), 5+ HP (very rare)
- Explosion power: 1.0-2.0 (moderate), 3.0-4.0 (high, very rare)

---

### Block Selectors (`BlockSelector`)

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:single` | Only target block | None |
| `forgero:pattern` | 2D/3D pattern | `pattern` (List<String>)<br>`depth` (int, optional, default 1)<br>`filter` (BlockFilter) |
| `forgero:radius` | Vein mining radius | `radius` (int)<br>`filter` (BlockFilter) |
| `forgero:column` | Vertical column | `height` (int)<br>`depth` (int, optional)<br>`filter` (BlockFilter) |

**Patterns:** Use 'x' or 'c' for valid blocks, ' ' for empty space.
- `["xxx", "xcx", "xxx"]` = 3x3 square
- `["x", "c", "x"]` = vertical line

---

### Block Filters (`BlockFilter`)

| Type | Description | Use Case |
|------|-------------|----------|
| `forgero:can_mine` | Player can harvest block | Respect tool effectiveness |
| `forgero:is_block` | Position has non-air block | Basic validation |
| `forgero:same_block` | Exact block type match | Strict mining |
| `forgero:similar_block` | Shares `forgero:similar_block/` tag | Stone variants, ore variants |
| `forgero:filter_wrapper` | Combines multiple filters (AND) | Complex conditions |

---

### Block Break Speed Calculators (`BlockBreakSpeedCalculator`)

| Type | Description | Balance |
|------|-------------|---------|
| `forgero:single` | Speed based on target block only | Fastest AoE |
| `forgero:average` | Average speed of all blocks | Moderate AoE |
| `forgero:all` | Sum of all block times | Slowest AoE |
| `forgero:instant` | Breaks instantly | Very rare/creative only |

---

### Loot Handlers (`LootHandler`)

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:apply_functions` | Chain of ItemFunctions | `functions` (List<ItemFunction>) |

**Item Functions:**

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:auto_smelt` | Smelts items if recipe exists | `filter` (ItemFilter) |
| `forgero:item_transform` | Replaces item with another | `input` (ItemFilter)<br>`output` (Identifier)<br>`count` (int, optional, default 1) |

**Item Filters:**

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:is_item` | Matches specific item | `item` (Identifier) |
| `forgero:tag` | Matches item tag | `tag` (Identifier) |

---

### Conditions (Dynamic Game State)

These check runtime conditions, not component structure.

| Type | Description | Parameters | Use Case |
|------|-------------|------------|----------|
| `forgero:target_has_tag` | Target entity has entity type tag | `tag` (Identifier) | Target undead, aquatic, etc. |
| `forgero:is_sneaking` | Wielder is sneaking | None | Stealth abilities |
| `forgero:is_raining` | World is raining | None | Weather-based effects |

**Static Conditions (Component Structure):**
These evaluate during property resolution, not at runtime.

| Type | Description | Parameters |
|------|-------------|------------|
| `forgero:in_slot_type` | Component is in specific slot | `slot_type` (Identifier) |
| `forgero:is_root` | Component is root item | None |
| `forgero:self_has_tag` | Component has tag | `tag` (Identifier) |
| `forgero:root_has_tag` | Root component has tag | `tag` (Identifier) |

**Combining Conditions:**
```json
{
  "type": "forgero:and",
  "conditions": [
    {"type": "forgero:target_has_tag", "tag": "minecraft:undead"},
    {"type": "forgero:is_raining"}
  ]
}
```

---

## Attribute System

Attributes provide numeric stat bonuses. Unlike properties (event-driven), attributes are **passive stat modifiers**.

### Core Attribute Types

| Type | Description | Typical Use |
|------|-------------|-------------|
| `forgero:durability` | Item durability | All tools/armor |
| `forgero:attack_damage` | Melee damage | Weapons, tool heads |
| `forgero:attack_speed` | Attack speed | Weapons, handles |
| `forgero:mining_speed` | Block break speed | Tool heads |
| `forgero:mining_level` | Required mining tier | Tool heads |
| `forgero:armor` | Armor protection | Armor materials |
| `forgero:armor_toughness` | Toughness (high damage reduction) | High-tier armor |
| `forgero:rarity` | Rarity value | All materials |
| `forgero:weight` | Item weight | All materials |

### Attribute JSON Structure

**Simple:**
```json
{
  "id": "forgero:iron-durability",
  "type": "forgero:durability",
  "composite": "forgero:part-composite",
  "computation": { "value": 250 }
}
```

**With Operators:**
```json
{
  "id": "forgero:iron-mining_speed-multiplier",
  "type": "forgero:mining_speed",
  "computation": {
    "value": 1.5,
    "operator": "forgero:multiplication",
    "order": "forgero:middle"
  }
}
```

**With Conditions:**
```json
{
  "id": "forgero:leather-grip-attack_damage",
  "type": "forgero:attack_damage",
  "computation": { "value": 10 },
  "condition": {
    "type": "forgero:in_slot_type",
    "slot_type": "forgero:handle_grip_slot"
  }
}
```

### Operators

| Operator | Description | Use Case |
|----------|-------------|----------|
| `forgero:addition` | Add value (default) | Base stats |
| `forgero:subtraction` | Subtract value | Debuffs |
| `forgero:multiplication` | Multiply value | Percentage bonuses |
| `forgero:division` | Divide value | Penalties |

**Shorthand:**
```json
{ "add": 5 }
{ "multiply": 1.5 }
```

### Order/Priority Groups

Attributes are calculated in groups to ensure proper math order:

| Order | Value | Use Case |
|-------|-------|----------|
| `forgero:base` | 0 | Base values, additions |
| `forgero:middle` | 1 | Multipliers, percentage mods |
| `forgero:end` | 2 | Final adjustments |

**Example:** Base durability (order: base) is calculated before durability multiplier (order: middle).

---

## Material Tier System

When suggesting properties for vanilla items, consider their **tier** based on availability and value:

### Tier 1: Early Game (Abundant)
**Examples:** Wood, Stone, Leather, Bone, Feather, String, Paper
**Availability:** Crafted or common mob drops
**Attribute Range:**
- Durability: 50-100
- Attack Damage: 1-3
- Mining Speed: 2-4
- Armor: 1-2

**Property Guidelines:**
- Simple, single-target effects
- Short durations (2-3 seconds, 60-100 ticks)
- No AoE or minimal AoE (radius 2)
- Common status effects (slowness I, poison I)

**Example:** Feather → knockback resistance or reduced fall damage

---

### Tier 2: Mid Game (Moderate)
**Examples:** Iron, Copper, Amethyst, Quartz, Coal, Redstone
**Availability:** Mining, moderate exploration
**Attribute Range:**
- Durability: 150-300
- Attack Damage: 3-5
- Mining Speed: 5-7
- Armor: 2-4

**Property Guidelines:**
- Multi-target effects allowed (AoE radius 2-3)
- Moderate durations (5 seconds, 100-150 ticks)
- Conditional effects (based on target type, weather)
- Stronger status effects (slowness II, poison II)
- Auto-smelt for common items

**Example:** Copper → lightning during rain, Amethyst → status effect immunity

---

### Tier 3: Late Game (Rare)
**Examples:** Diamond, Gold, Emerald, Ender Pearl, Blaze Rod, Ghast Tear
**Availability:** Deep mining, Nether/End, rare mob drops
**Attribute Range:**
- Durability: 500-1500
- Attack Damage: 5-8
- Mining Speed: 8-10
- Armor: 4-6

**Property Guidelines:**
- Large AoE (radius 4-6)
- Long durations (10 seconds, 200-300 ticks)
- Multiple simultaneous effects
- Life steal (2-3 HP)
- Small explosions (power 1-2)
- Advanced loot transformations

**Example:** Blaze Rod → auto-smelt ores and meat, Ender Pearl → teleport on hit

---

### Tier 4: End Game (Very Rare)
**Examples:** Netherite, Nether Star, Dragon Egg, Echo Shard, Wither Skeleton Skull
**Availability:** Boss drops, deep exploration, extreme effort
**Attribute Range:**
- Durability: 1500-2500
- Attack Damage: 8-12
- Mining Speed: 10-15
- Armor: 6-10
- Armor Toughness: 2-4

**Property Guidelines:**
- Massive AoE (radius 7-10)
- Very long durations (15+ seconds, 300+ ticks)
- Multiple powerful effects combined
- Life steal (4-5 HP)
- Large explosions (power 3-4)
- Instant block breaking (with restrictions)
- Entity conversion, resurrection, or summoning

**Example:** Nether Star → massive AoE regeneration aura, Wither Skeleton Skull → wither effect

---

## Item Family Differentiation (CRITICAL)

### The Item Family Problem

**CRITICAL RULE:** If there are multiple similar vanilla items (seeds, flowers, stone variants, etc.), each one MUST have a DISTINCT effect. Never make them all do the same thing.

**Bad Example:**
```
Wheat Seeds: Regeneration in sunlight
Pumpkin Seeds: Regeneration in sunlight
Beetroot Seeds: Regeneration in sunlight
Melon Seeds: Regeneration in sunlight
```
**Problem:** Redundant, boring, no reason to choose one over another.

**Good Example:**
```
Wheat Seeds: Increases food restoration when eating
Pumpkin Seeds: Bonus damage vs undead (jack-o'-lantern theme)
Beetroot Seeds: Increased mining speed (root vegetables = digging)
Melon Seeds: Water breathing / aquatic affinity (watery fruit)
```
**Result:** Each seed has a unique identity and use case.

### Item Families to Consider

**Seeds:**
- Wheat, Pumpkin, Melon, Beetroot, Torchflower, Pitcher
- **Each must have different effects based on their unique properties**

**Flowers:**
- Dandelion, Poppy, Blue Orchid, Allium, Azure Bluet, Tulips, Oxeye Daisy, Cornflower, Lily of the Valley, Wither Rose, Sunflower, Lilac, Rose Bush, Peony
- **Group by color/theme, but each should be distinct**

**Stone Types:**
- Stone, Granite, Diorite, Andesite, Deepslate, Tuff, Calcite, Basalt, Blackstone
- **Mining/durability focused, but with different trade-offs**

**Wood Types:**
- Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Crimson, Warped
- **Overworld vs Nether, different biome themes**

**Coral:**
- Tube, Brain, Bubble, Fire, Horn (each color)
- **Aquatic theme, but different effects**

**When designing, ask:** "What makes THIS specific item different from its siblings?"

---

## Item-Specific Guidelines

### Hostile Mob Drops
Should relate to the mob's abilities:
- **Blaze Rod:** Fire/auto-smelt (fire theme)
- **Ghast Tear:** Explosion resistance or knockback (ghast attacks)
- **Phantom Membrane:** Slow falling or flight boost
- **Spider Eye:** Poison or wall climbing
- **Wither Skeleton Skull:** Wither effect

### Peaceful Mob Drops
Should relate to the mob's nature:
- **Feather:** Reduced fall damage, knockback resistance
- **Leather:** Durability boost, flexible (non-breaking)
- **Wool:** Damage absorption, silence (no sound)

### Minerals/Gems
Should feel valuable and match color/theme:
- **Diamond:** High stats, durability focus
- **Emerald:** Fortune/luck effects, trading theme
- **Amethyst:** Sound/vibration theme, status effect immunity
- **Lapis:** Enchantment boost, XP bonus
- **Redstone:** Efficiency, speed boosts
- **Quartz:** Smoothness, XP bonus

### Plants/Organic
Should relate to growth, nature, life:
- **Bamboo:** Fast growth (quick repair?), flexibility
- **Kelp:** Water breathing, aquatic affinity
- **Cactus:** Thorns, damage reflection
- **Sugar Cane:** Speed, efficiency

### End/Nether Items
Should feel powerful and exotic:
- **Ender Pearl:** Teleportation, void theme
- **Chorus Fruit:** Random teleport, unpredictable
- **Echo Shard:** Sound/vibration theme, sculk synergy
- **Crying Obsidian:** Respawn anchor synergy, durability

---

## Suggesting New Events/Handlers

If existing events/effects don't fit your idea, you can suggest new ones. However, you MUST provide complete implementation details.

### Required Information for New Effect

1. **Effect Name and Type String**
   - Example: `FreezeHandler` with type `forgero:freeze`

2. **Interface to Implement**
   - `EntityEffectHandler` (needs only target) OR
   - `ContextualEffectHandler` (needs source + target)

3. **Parameters (with types and defaults)**
   ```json
   {
     "type": "forgero:freeze",
     "duration": 5,  // int, required, seconds of freeze
     "amplifier": 0  // int, optional, default 0
   }
   ```

4. **Codec Implementation Pattern**
   - Singleton (no params): `Codec.unit(INSTANCE)`
   - Required params: `RecordCodecBuilder` with `fieldOf`
   - Optional params: `optionalFieldOf("field", defaultValue)`

5. **Minecraft API Usage**
   - What Minecraft methods/entities are used?
   - Example: `entity.setFrozenTicks(duration * 20)`

6. **Server/Client Handling**
   - Must include `!world.isClient` check for server-only effects
   - Specify if any client-side rendering is needed

7. **Performance Considerations**
   - Does it scan entities/blocks? What's the performance cost?
   - Should it have a cooldown or rate limit?

8. **Complete Code Skeleton**
```java
public record FreezeHandler(int duration) implements EntityEffectHandler {
    public static final String TYPE = "forgero:freeze";

    public static final Codec<FreezeHandler> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("duration").forGetter(FreezeHandler::duration)
        ).apply(instance, FreezeHandler::new)
    );

    @Override
    public void apply(Entity entity) {
        if (!entity.getWorld().isClient) {
            entity.setFrozenTicks(duration * 20);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

9. **Plugin Registration**
```java
// In OnHitPropertiesPlugin static block:
registerEffect(FreezeHandler.TYPE, FreezeHandler.CODEC);
```

10. **JSON Usage Example**
```json
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [
        {
          "type": "forgero:freeze",
          "duration": 5
        }
      ]
    }
  ]
}
```

### Required Information for New Event

If you need an entirely new event (beyond OnHit, OnTick, OnLootDrop, BlockBreaking):

1. **Event Name and When It Fires**
   - Example: `OnBlockPlace` - when wielder places a block

2. **Property Structure**
   - What slots does it have? (selector, effects, handler, etc.)
   - What parameters are needed?

3. **Mixin Location**
   - What Minecraft method to inject into?
   - Injection point (@At annotation)

4. **Manager Implementation**
   - How to resolve properties and execute logic

5. **Complete Implementation Skeleton** (like OnHit/OnTick examples in existing docs)

---

## Example Material Definitions

### Tier 1: Feather (Early Game) - Multi-Slot Design
```json
{
  "type": "forgero:material",
  "name": "Feather",
  "tags": ["forgero:upgrade/binding", "forgero:upgrade/grip"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:feather" }] },
  "attributes": [
    {
      "id": "forgero:feather-weight",
      "type": "forgero:weight",
      "composite": "forgero:part-composite",
      "computation": { "value": 0.5 }
    },
    {
      "id": "forgero:feather-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 50 }
    },
    {
      "id": "forgero:feather-grip-attack_speed",
      "type": "forgero:attack_speed",
      "computation": { "value": 0.3 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:handle_grip_slot"
      }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:knockback",
            "force": 1.0,
            "direction": "push"
          }
        ],
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:binding_slot"
        }
      }
    ]
  }
}
```

**Reasoning:**
- Feathers are light and associated with birds/flight
- **Can fit in both binding and grip slots** (multiple tags)
- In binding slot → knockback effect (push enemies away)
- In grip slot → attack speed bonus (lighter grip, faster swings)
- Low-tier stats for common item
- **Demonstrates slot-conditional design**

---

### Tier 2: Copper (Mid Game)
```json
{
  "type": "forgero:material",
  "name": "Copper",
  "tags": ["forgero:materials/metal"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:copper_ingot" }] },
  "attributes": [
    {
      "id": "forgero:copper-durability",
      "type": "forgero:durability",
      "computation": { "value": 200 }
    },
    {
      "id": "forgero:copper-mining_speed",
      "type": "forgero:mining_speed",
      "computation": { "value": 6.0 }
    },
    {
      "id": "forgero:copper-attack_damage",
      "type": "forgero:attack_damage",
      "computation": { "value": 4.0 }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 2
        },
        "effects": [
          {
            "type": "forgero:lightning"
          }
        ],
        "condition": {
          "type": "forgero:is_raining"
        }
      }
    ]
  }
}
```

**Reasoning:** Copper conducts electricity → lightning during rain. Mid-tier stats, moderate AoE, conditional trigger.

---

### Tier 3: Ender Pearl (Late Game) - Teleportation Theme
```json
{
  "type": "forgero:material",
  "name": "Ender Pearl",
  "tags": ["forgero:upgrade/gem", "forgero:upgrade/tip_reinforcement"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:ender_pearl" }] },
  "attributes": [
    {
      "id": "forgero:ender_pearl-rarity",
      "type": "forgero:rarity",
      "composite": "forgero:part-composite",
      "computation": { "value": 90 }
    },
    {
      "id": "forgero:ender_pearl-attack_damage",
      "type": "forgero:attack_damage",
      "composite": "forgero:part-composite",
      "computation": { "value": 6.0 }
    },
    {
      "id": "forgero:ender_pearl-tip-durability",
      "type": "forgero:durability",
      "computation": { "value": 800 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:tip_reinforcement_slot"
      }
    }
  ],
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:invisibility",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20,
        "condition": {
          "type": "forgero:and",
          "conditions": [
            { "type": "forgero:is_sneaking" },
            { "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" }
          ]
        }
      }
    ],
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:convert",
            "convert_to": "minecraft:endermite"
          }
        ],
        "condition": {
          "type": "forgero:and",
          "conditions": [
            {
              "type": "forgero:in_slot_type",
              "slot_type": "forgero:tip_reinforcement_slot"
            },
            {
              "type": "forgero:target_has_tag",
              "tag": "minecraft:arthropod"
            }
          ]
        }
      }
    ]
  }
}
```

**Reasoning:**
- Ender theme with teleportation/void association
- **Can fit in both gem and tip reinforcement slots**
- In gem slot + sneaking → invisibility (Enderman stealth behavior)
- In tip reinforcement + hitting arthropods → convert to endermite (ender pearl spawn behavior)
- High durability bonus when used as tip reinforcement
- Late-game rarity, multiple conditional effects
- **Demonstrates complex conditional logic with slot + game state**

**Note:** Could also suggest a new `TeleportHandler` effect for OnHit with complete implementation details (see "Suggesting New Events/Handlers" section).

---

### Tier 4: Nether Star (End Game)
```json
{
  "type": "forgero:material",
  "name": "Nether Star",
  "tags": ["forgero:upgrade/gem"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:nether_star" }] },
  "attributes": [
    {
      "id": "forgero:nether_star-rarity",
      "type": "forgero:rarity",
      "computation": { "value": 100 }
    },
    {
      "id": "forgero:nether_star-attack_damage",
      "type": "forgero:attack_damage",
      "computation": { "value": 10.0 }
    },
    {
      "id": "forgero:nether_star-durability",
      "type": "forgero:durability",
      "computation": { "value": 2500 }
    }
  ],
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 8
        },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:regeneration",
            "duration": 40,
            "amplifier": 1
          },
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:resistance",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20
      }
    ]
  }
}
```

**Reasoning:** Nether Star is beacon material → massive AoE regeneration/resistance aura (like beacon). End-game power level.

---

## Design Philosophy: Creating Excitement and Depth

### What Makes an Upgrade EXCITING?

**Exciting upgrades have:**
1. **Multiple complementary effects** that work together
2. **Clear power fantasy** - "I'm the fire mage!" or "I'm the tank!"
3. **Memorable moments** - Lightning strikes, explosions, dramatic transformations
4. **Strategic depth** - Choices about when/where/how to use it
5. **Build-around potential** - Players theory-craft builds around this item

**Boring upgrades have:**
1. Single, weak effect ("Apply Slowness I")
2. No synergy or combos
3. Forgettable, numbers-only bonuses
4. No strategic choices
5. Anti-fun mechanics (nerfs, punishments, penalties)

---

### The Memorable Gameplay Test

**Ask yourself:** Does this create interesting moments grounded in Minecraft's world?

**Bad:** "I used snowball upgrade and enemies moved slower"
- Forgettable, passive, weak

**Good:** "I used Blaze Rod and auto-smelted ores while mining, then the fire spread and lit up the cave"
- Useful, emergent gameplay, natural to Minecraft

**Great:** "I combined Ender Pearl with Phantom Membrane slow-falling to safely escape from a cliff-side fight"
- Combo potential, problem-solving, feels like smart play

**Design for memorable moments:**
- Visual feedback (fire, lightning, particles) that fits Minecraft's aesthetic
- Practical utility that solves real gameplay problems
- Emergent interactions (effects that combo naturally)
- Skill expression (timing, positioning, smart use of environment)

---

### Attribute-Focused Design (PRIMARY APPROACH)

**Good: Attribute-Only Item**
```json
{
  "type": "forgero:material",
  "name": "Iron Ingot",
  "tags": ["forgero:upgrade/tip_reinforcement"],
  "attributes": [
    {
      "id": "forgero:iron-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 250 }
    },
    {
      "id": "forgero:iron-attack_damage",
      "type": "forgero:attack_damage",
      "composite": "forgero:part-composite",
      "computation": { "value": 4.0 }
    },
    {
      "id": "forgero:iron-mining_speed",
      "type": "forgero:mining_speed",
      "composite": "forgero:part-composite",
      "computation": { "value": 6.0 }
    }
  ]
  // No properties - solid all-around stats
}
```
**Result:** Reliable, balanced upgrade. No flashy effects, just good stats. Perfect for players who want consistency.

---

**Great: Attributes + ONE Thematic Effect**
```json
{
  "type": "forgero:material",
  "name": "Blaze Rod",
  "tags": ["forgero:upgrade/tip_reinforcement", "forgero:upgrade/binding"],
  "attributes": [
    {
      "id": "forgero:blaze-rod-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 150 }
    },
    {
      "id": "forgero:blaze-rod-attack_damage",
      "type": "forgero:attack_damage",
      "composite": "forgero:part-composite",
      "computation": { "value": 3.0 }
    }
  ],
  "properties": {
    "minecraft:on_loot_drop": [
      {
        "handler": {
          "type": "forgero:apply_functions",
          "functions": [
            { "type": "forgero:auto_smelt", "filter": { "type": "forgero:tag", "tag": "c:raw_ores" } }
          ]
        }
      }
    ]
  }
}
```
**Result:** Solid stats + ONE perfect effect (auto-smelt for fire theme). Not overpowered, just focused.

---

### Effect-Attribute Synergy (BUILD CRAFTING)

**The Power of Modifier Attributes:**

Some items can provide **modifier attributes** that enhance effects from OTHER upgrades:

**Item A - Modifier Provider (No Effects):**
```json
{
  "type": "forgero:material",
  "name": "Ancient Tome",
  "tags": ["forgero:upgrade/gem"],
  "attributes": [
    {
      "id": "forgero:tome-effect_duration",
      "type": "forgero:effect_duration_multiplier",
      "composite": "forgero:part-composite",
      "computation": { "value": 1.5, "operator": "forgero:multiplication", "order": "forgero:middle" }
    },
    {
      "id": "forgero:tome-effect_power",
      "type": "forgero:effect_power_multiplier",
      "composite": "forgero:part-composite",
      "computation": { "value": 1.2, "operator": "forgero:multiplication", "order": "forgero:middle" }
    }
  ]
  // No properties - pure modifier item
}
```

**Item B - Effect Provider:**
```json
{
  "type": "forgero:material",
  "name": "Magma Cream",
  "tags": ["forgero:upgrade/binding"],
  "attributes": [
    {
      "id": "forgero:magma-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 100 }
    }
  ],
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:fire", "duration": 3 }  // Base: 3 seconds
        ]
      }
    ]
  }
}
```

**Combined Result:**
- Magma Cream alone: Sets target on fire for 3 seconds
- With Ancient Tome: Fire duration × 1.5 = **4.5 seconds**
- **Build Crafting:** Players combine items to enhance effects

**Why This is Excellent:**
- Not every item needs effects
- Items synergize to create builds
- Encourages experimentation
- Avoids power creep (individually balanced, powerful together)

---

### Anti-Fun Mechanics to AVOID

#### ❌ Negative Stats (The Worst Offender)

**NEVER suggest negative modifiers:**
```json
// DON'T DO THIS!
{
  "id": "forgero:snowball-damage",
  "type": "forgero:attack_damage",
  "computation": { "value": 1.0, "operator": "forgero:subtraction" }
}
```

**Why it's bad:**
- Makes players actively AVOID your item
- Creates "trap" upgrades that punish experimentation
- Anti-fun - players want to feel powerful, not weakened
- Breaks the fantasy - "Why would I downgrade my weapon?"

**Alternative - Tradeoffs through choice, not punishment:**
```json
// Instead of -damage, offer different playstyles via slots/conditions
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:aoe", "radius": 4 },
        "effects": [
          { "type": "forgero:status_effect", "effect": "minecraft:slowness", "duration": 100, "amplifier": 2 }
        ],
        "condition": { "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" }
      }
    ]
  }
}
```
**Why it's better:** Players CHOOSE crowd control over raw damage by slot selection. Not punished - empowered with options.

---

#### ❌ Effect Spam (Too Many Effects)

**DON'T create items that do everything:**
```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "effects": [
          { "type": "forgero:fire", "duration": 5 },
          { "type": "forgero:lightning" },
          { "type": "forgero:life_steal", "amount": 2.0 },
          { "type": "forgero:knockback", "force": 1.5 },
          { "type": "forgero:status_effect", "effect": "minecraft:weakness", ... }
        ]
      }
    ]
  }
}
```
**Problem:** Item does everything, no reason to use other items, no build crafting.

**DO focus on attributes with 0-1 effects:**
```json
{
  "attributes": [
    {
      "id": "forgero:iron-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 250 }
    },
    {
      "id": "forgero:iron-mining_speed",
      "type": "forgero:mining_speed",
      "composite": "forgero:part-composite",
      "computation": { "value": 6.0 }
    }
  ]
  // No properties - just solid stats
}
```
**OR, attribute-focused with ONE thematic effect:**
```json
{
  "attributes": [
    {
      "id": "forgero:blaze-rod-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 150 }
    },
    {
      "id": "forgero:blaze-rod-fire-resistance",
      "type": "forgero:fire_resistance",
      "composite": "forgero:part-composite",
      "computation": { "value": 1.0 }
    }
  ],
  "properties": {
    "minecraft:on_loot_drop": [
      {
        "handler": {
          "type": "forgero:apply_functions",
          "functions": [
            { "type": "forgero:auto_smelt", "filter": { "type": "forgero:tag", "tag": "c:raw_ores" } }
          ]
        }
      }
    ]
  }
}
```
**Result:** Solid stats + ONE perfect thematic effect (fire theme).

---

#### ❌ Weak Attributes

**DON'T:**
- "+1 armor"
- "+0.1 attack damage"
- "+5 durability"

These are meaningless. Attributes should be IMPACTFUL for their tier.

**DO:**
- Scale attributes appropriately (see Tier System)
- Use meaningful bonuses that players notice
- Create build-defining stat profiles
- Use slot-conditional attributes for variety

---

#### ❌ Overused Patterns to AVOID

The following patterns appear in EVERY design and are now BORING:

**Status Effects Without Creativity:**
```json
// BORING - Everyone does this
{ "type": "forgero:status_effect", "effect": "minecraft:regeneration", ... }
{ "type": "forgero:status_effect", "effect": "minecraft:speed", ... }
{ "type": "forgero:status_effect", "effect": "minecraft:weakness", ... }
```

**If you use status effects, they must be:**
1. Combined with OTHER interesting mechanics (not just more status effects)
2. Conditional on creative triggers (not just `is_sneaking` or `is_raining`)
3. Part of a larger thematic package

**Overused Conditions:**
- `is_sneaking` - Used in 90% of designs
- `is_raining` - Used constantly
- `target_has_tag: undead` - Every other design

**Instead, think about these CREATIVE mechanics:**

**Physics & Environmental:**
- Fall damage reduction/negation (feather, wool, slime)
- Knockback resistance (wool padding, heavy items)
- Fire walking (magma-related items)
- Water breathing / drowning prevention (aquatic items)
- Explosion resistance (blast-proof materials)
- Projectile deflection (hard surfaces)

**Resource & Progression:**
- Bonus drops from specific blocks/mobs (fortune-like)
- XP multipliers (learning/growth theme)
- Durability restoration over time (self-repair)
- Tool efficiency in specific biomes
- Harvesting speed bonuses

**Combat Innovation:**
- Damage scaling with conditions (low health = more damage, berserker theme)
- Attack reach extension
- Critical hit bonuses based on positioning
- Armor penetration
- Multi-hit combos

**Movement & Mobility:**
- Speed in specific biomes (home terrain advantage)
- Wall climbing / clinging
- Reduced fall speed (slow falling without status effect)
- Dash/dodge mechanics
- Jump height modifiers

**Utility & Interaction:**
- Block place distance
- Interaction speed (faster crafting, eating, etc.)
- Light emission while holding
- Mob detection radius
- Auto-pickup range extension

**When suggesting new handlers:**
Many of these require new effect handlers. When you suggest one, provide:
1. What Minecraft API it uses (e.g., `LivingEntity.handleFallDamage()`)
2. How it integrates with existing systems
3. Why existing effects can't achieve this

**CREATIVE Condition Combinations:**
```json
{
  "condition": {
    "type": "forgero:and",
    "conditions": [
      { "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" },
      { "type": "forgero:root_has_tag", "tag": "forgero:tool/pickaxe" },
      { "type": "forgero:target_has_tag", "tag": "minecraft:stone" }
    ]
  }
}
```
**Result:** Effect only triggers when using a pickaxe with this binding against stone blocks.

---

#### ❌ Overpowered Healing

**NEVER suggest:**
```json
// Instant Health is BROKEN for Tier 1
{
  "type": "forgero:status_effect",
  "effect": "minecraft:instant_health",
  "duration": 1
}
```

**Why it's broken:**
- Instant Health heals 4 HP instantly
- On a 100-tick interval (5 seconds), this is 48 HP per minute
- With sneaking trigger, players become invincible

**Alternatives for "protective" items like wool:**

**Example - Wool as Padding/Protection:**
```json
{
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          // Suggest FallDamageReductionHandler
          { "type": "forgero:reduce_fall_damage", "percentage": 50 }
        ],
        "interval": 1,
        "condition": { "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" }
      }
    ],
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          // Suggest KnockbackResistanceHandler
          { "type": "forgero:knockback_resistance", "amount": 0.5 }
        ],
        "condition": { "type": "forgero:in_slot_type", "slot_type": "forgero:grip_slot" }
      }
    ]
  }
}
```

**Why this is better:**
- Solves real problems (fall damage, getting pushed around)
- Thematically perfect (wool = soft padding)
- NOT overpowered (doesn't make you invincible)
- Creates interesting builds (parkour, cliff exploration)
- Requires new handlers, but they're generic and reusable

---

#### ❌ No Strategic Depth

**DON'T:**
```json
// Always does the same thing, no choices
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [{ "type": "forgero:fire", "duration": 3 }]
    }
  ]
}
```

**DO:**
```json
// Creates choices: slot placement, conditional activation, multiple effects
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:aoe", "radius": 3 },
      "effects": [
        { "type": "forgero:fire", "duration": 5 },
        { "type": "forgero:explosion", "power": 1.5, "destruction_type": "none" }
      ],
      "condition": {
        "type": "forgero:and",
        "conditions": [
          { "type": "forgero:in_slot_type", "slot_type": "forgero:tip_reinforcement_slot" },
          { "type": "forgero:target_has_tag", "tag": "minecraft:undead" }
        ]
      }
    }
  ],
  "minecraft:on_tick": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [
        { "type": "forgero:status_effect", "effect": "minecraft:fire_resistance", "duration": 40, "amplifier": 0 }
      ],
      "interval": 20,
      "condition": { "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" }
    }
  ]
}
```
**Result:** Different slot = different playstyle, conditional triggers, synergies

---

## Anti-Patterns (DO NOT SUGGEST)

### ❌ Complex State Tracking
```json
// DON'T: Track hit count across multiple attacks
{
  "type": "forgero:count_hits_and_explode",
  "hit_count": 5
}
```
**Why:** Requires persistent state storage, client-server sync, complex tracking.

**Alternative:** Simple per-hit effects or OnTick intervals.

---

### ❌ Multi-Tick Coordination
```json
// DON'T: Effects that span multiple ticks with complex logic
{
  "type": "forgero:charge_over_time",
  "charge_ticks": 60,
  "release_damage": 20
}
```
**Why:** Requires state machine, tick-by-tick updates, interruption handling.

**Alternative:** Use OnTick with intervals for periodic effects, or instant effects on OnHit.

---

### ❌ Overly Specific Effects
```json
// DON'T: Highly specific one-off effect
{
  "type": "forgero:summon_zombie_when_hit_creeper_at_night"
}
```
**Why:** Not reusable, violates modularity principle.

**Alternative:** Combine existing effects with conditions:
```json
{
  "selector": { "type": "forgero:single_target" },
  "effects": [
    { "type": "forgero:convert", "convert_to": "minecraft:zombie" }
  ],
  "condition": {
    "type": "forgero:and",
    "conditions": [
      { "type": "forgero:target_has_tag", "tag": "minecraft:creeper" },
      { "type": "forgero:is_night" }
    ]
  }
}
```
*(Note: `is_night` condition would need to be implemented, but it's a generic reusable condition)*

---

### ❌ Performance-Heavy Effects
```json
// DON'T: Scan huge area every tick
{
  "minecraft:on_tick": [
    {
      "selector": { "type": "forgero:aoe", "radius": 50 },
      "effects": [...],
      "interval": 1
    }
  ]
}
```
**Why:** Scans 50-block radius every tick → server lag.

**Alternative:** Smaller radius (max 10), higher interval (min 20 ticks).

---

## Workflow for AI Suggestions

When given a vanilla Minecraft item to design:

### Step 1: Identify Item Characteristics and Unique Identity
- What is the item's theme/lore? (fire, ice, nature, magic, etc.)
- What tier is it? (early, mid, late, end-game)
- How is it obtained? (crafted, mined, mob drop, boss drop)
- What's its vanilla use? (building, crafting, utility)
- **What slots could it fit in?** (binding, grip, reinforcement, gem, etc.)
- **What makes this item UNIQUE?** How is it different from similar items?
- **Item family consideration:** If there are similar items (seeds, flowers, stone types), how does this one stand out?
- **What memorable gameplay does this enable?** (grounded in Minecraft's world, not over-the-top)

### Step 2: Choose Appropriate Tags
- Decide which slots this item can fit into
- Add corresponding tags: `forgero:upgrade/binding`, `forgero:upgrade/tip_reinforcement`, etc.
- Can fit multiple slots? Add multiple tags (like Blaze Rod)

### Step 3: Choose Appropriate Attributes
- Assign base stats based on tier guidelines
- Use `in_slot_type` conditions if attributes should vary by slot
- Consider using multipliers for percentage-based bonuses (order: middle)
- Use `composite: "forgero:part-composite"` to make attributes aggregate

### Step 4: Design Attributes (PRIMARY FOCUS)
- **Start with attributes** - Most items should be attribute-focused, not effect-heavy
- What stats make sense? (durability, attack damage, mining speed, weight, etc.)
- Use `composite: "forgero:part-composite"` to make attributes aggregate
- **Slot-conditional attributes** create different playstyles without effects
- Consider **modifier attributes** that enhance other upgrades' effects (effect duration, effect power, etc.)
- **NO negative stats** - ever. Create choices through conditions/slots, not punishments

### Step 5: Design Properties (ONLY IF IT MAKES SENSE)
- **0-1 effects maximum** - Not every item needs effects, many should be attribute-only
- Does this item's theme justify an effect, or are attributes enough?
- If adding an effect, make it **thematically perfect** and **memorable**
- What event makes sense? (OnHit for combat, OnTick for auras, OnLootDrop for harvesting)
- **Think beyond status effects** - environmental interactions, physics, resource generation
- **Should the property change based on slot?** Use `in_slot_type` for different playstyles
- **Does it combo with attributes from other items?** (effect duration modified by other upgrades)
- **Avoid overused patterns** - see "Overused Patterns to Avoid" section

### Step 6: Validate Against Principles
- **Attribute Focus Check:** Does this have meaningful stat bonuses?
- **Effect Necessity Check:** If it has an effect, is it truly needed or are attributes enough?
- **Synergy Check:** Do attributes create build-crafting opportunities with other items?
- **Depth Check:** Strategic choices through attributes and slots? Build-around potential?
- **No Anti-Fun:** Zero negative stats? No punishing mechanics?
- **Innovation Check:** Is this boring/generic, or unique/exciting?
- Is it modular? (Composed from existing components?)
- Is it feasible? (No complex state tracking?)
- Is it performant? (Reasonable intervals and radii?)
- Is it thematically appropriate?

### Step 7: Provide Complete JSON
- Include all required fields
- Use proper formatting
- Add descriptions/comments explaining the design choices

### Step 8: Suggest New Features (If Needed)
- Only if existing components truly can't achieve the desired effect
- Provide complete implementation details (see "Suggesting New Events/Handlers")
- Explain why existing components are insufficient
- Define the generic, reusable interface it builds upon

---

## Example AI Response Format

### GOOD Example (High Quality, Exciting)

**Item:** Phantom Membrane

**Tier:** Tier 2-3 (Rare hostile mob drop - requires multiple nights without sleep)

**Theme:** Flight, slow falling, insomnia, phantoms

**Unique Identity:** Repairs elytra in vanilla; associated with flight and nighttime

**Slots:** Can fit in binding (wrapping material) OR gem (special item) slot

**Why This Design Works:**
- Builds on vanilla identity (elytra repair = flight theme)
- Distinct from other membrane/wing items
- Natural fit for Minecraft's world (phantoms spawn at night without sleep)

**Attributes:**
- Base durability: 150 (moderate)
- Rarity: 70 (rare drop from phantoms)
- Weight: 0.3 (very light, phantom theme)
- Slot-conditional attack speed: +0.4 when in binding slot (light wrapping)

**Properties:**
- In binding slot: Slow falling effect continuously (flight safety)
- In gem slot: Grants night vision when sneaking (phantom vision)
- Multiple complementary effects create utility-focused playstyle

**JSON:**
```json
{
  "type": "forgero:material",
  "name": "Phantom Membrane",
  "tags": ["forgero:upgrade/binding", "forgero:upgrade/gem"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:phantom_membrane" }] },
  "attributes": [
    {
      "id": "forgero:phantom_membrane-durability",
      "type": "forgero:durability",
      "composite": "forgero:part-composite",
      "computation": { "value": 150 }
    },
    {
      "id": "forgero:phantom_membrane-rarity",
      "type": "forgero:rarity",
      "composite": "forgero:part-composite",
      "computation": { "value": 70 }
    },
    {
      "id": "forgero:phantom_membrane-weight",
      "type": "forgero:weight",
      "composite": "forgero:part-composite",
      "computation": { "value": 0.3 }
    },
    {
      "id": "forgero:phantom_membrane-binding-attack_speed",
      "type": "forgero:attack_speed",
      "computation": { "value": 0.4 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:binding_slot"
      }
    }
  ],
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:slow_falling",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20,
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:binding_slot"
        }
      },
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:night_vision",
            "duration": 240,
            "amplifier": 0
          }
        ],
        "interval": 200,
        "condition": {
          "type": "forgero:and",
          "conditions": [
            { "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" },
            { "type": "forgero:is_sneaking" }
          ]
        }
      }
    ]
  }
}
```

**Design Rationale:**
- Phantom Membrane repairs elytra in vanilla → slow falling effect is natural extension
- Phantoms spawn from insomnia (night theme) → night vision fits thematically
- Rare mob drop requiring specific conditions → Tier 2-3 stats
- **Unique identity:** Only item that combines flight safety + night vision
- **Memorable gameplay:** Safely explore cliffs and caves, see in darkness
- **Multiple effects:** Attack speed + slow falling + night vision (3 complementary effects)
- **Multi-slot design creates two playstyles:**
  - Binding slot: Mobility focus (attack speed, slow falling for exploration)
  - Gem slot: Utility focus (night vision for cave exploration)
- Light weight fits phantom/flight theme naturally
- **NO negative stats** - only positive bonuses
- Uses existing effects → no new code needed
- **Grounded in Minecraft** - feels like it belongs in the game

---

### BAD Example (Low Quality, Boring) - DO NOT COPY THIS

**Item:** Snowball

**Tier:** Tier 1

**Theme:** Cold, frost

**Slots:** Binding only

**Attributes:**
- Attack Damage: **-1.0** ❌ (Negative stat - anti-fun!)
- Attack Speed: +0.5
- Durability: 25

**Properties:**
- OnHit: Apply Slowness II for 3 seconds ❌ (Single weak effect)

**Why This is BAD:**
- ❌ **Negative damage actively punishes players** for using it
- ❌ **One-dimensional** - only does one boring thing
- ❌ **No depth** - single slot, single effect, no choices
- ❌ **Forgettable** - no signature moments, no excitement
- ❌ **Anti-fun** - makes your weapon worse
- ❌ **No innovation** - just a basic status effect

**How to Fix It:**
```
Snowball (IMPROVED VERSION):

Theme: Cold, freezing, winter combat
Unique Identity: Infinite resource in snow biomes, cold-themed projectile
Distinct From: Ice (solid/hard) vs Snowball (soft/throwable)

Slots: Binding + Tip Reinforcement (multi-slot)

Attributes (NO negatives!):
- Weight: 0.1 (very light)
- Durability: 50
- Attack Speed: +0.3 (in binding slot only - light wrapping)

Properties:
Binding slot:
  - OnTick: Frost Resistance (immunity to freezing)
  - OnHit: Slowness II + small knockback (crowd control)

Tip Reinforcement slot:
  - OnHit to AoE radius 2: Slowness III for 5 seconds
  - OnHit vs slowed targets: +50% damage (shatter frozen enemies!)
  - Suggest new Freeze handler for visual frozen effect

Result:
- Multiple effects that SYNERGIZE (slow then shatter)
- Different playstyles per slot (defense vs offense)
- Exciting combo potential (freeze + crit = satisfying)
- NO negative stats
- Memorable "freeze and shatter" fantasy
```

---

## Quick Reference: Common Slot Types

| Slot ID | Slot Type | Required Tags | Theme/Purpose | Example Items |
|---------|-----------|---------------|---------------|---------------|
| `binding_slot` | `forgero:binding` | `forgero:upgrade/binding` | Wrapping/binding on handle | Leather, String, Phantom Membrane, Wool |
| `grip_slot` / `handle_grip_slot` | `forgero:grip` | `forgero:upgrade/grip` | Handle grip enhancement | Leather, Wool, Slimeball, Feather |
| `tip_reinforcement_slot` | `forgero:upgrade_material` | `forgero:upgrade/tip_reinforcement` | Tool tip/edge hardening | Diamond, Netherite, Blaze Rod, Obsidian |
| `gem_slot` | `forgero:gem` | `forgero:upgrade/gem` | Socketed gems/magic items | Emerald, Amethyst, Ender Pearl, Nether Star |
| `reinforcement_slot` | `forgero:upgrade_material` | `forgero:materials/tool_material` | Generic material upgrade | Any tool material |
| `charm_slot` (custom) | `forgero:charm` | `forgero:upgrade/charm` | Luck/utility items | Rabbit Foot, Four Leaf Clover (custom) |

**Key Insight:** Items can have MULTIPLE tags to fit in MULTIPLE slots, creating different playstyles based on placement.

---

## Summary

You now have everything needed to:
1. **Understand the component hierarchy** - how materials, parts, and tools compose
2. **Work with the slot system** - structure slots (required) vs upgrade slots (optional)
3. **Design multi-slot materials** - items that fit in different slots with different behaviors
4. **Use slot-conditional attributes** - stats that change based on placement
5. **Use slot-conditional properties** - effects that change based on placement
6. **Suggest vanilla items** with appropriate tiers and properties
7. **Design attributes** using the computation and composite system
8. **Compose properties** from existing events, selectors, effects, and conditions
9. **Suggest new features** with complete, feasible implementation details
10. **Maintain architectural principles** (modular, data-driven, performant, thematically appropriate)

**Golden Rule:** Every suggestion must be either:
- Composable from existing components, OR
- A new generic, reusable component with complete implementation details

**Slot-Aware Design Principle:**
When possible, design materials that can fit in MULTIPLE slots with DIFFERENT behaviors. This creates:
- **Player choice** - same item, different playstyles
- **Replayability** - experiment with different slot placements
- **Depth** - strategic decisions about where to place upgrades

**The Quality Checklist:**
Before finalizing a design, verify:

1. **Usability:** Would I actually use this item, or avoid it?
2. **Build Potential:** Is this worth theory-crafting around?
3. **Memorable Gameplay:** Does this create interesting moments?
4. **Fun Factor:** Does this add enjoyment, not frustration?
5. **Community Appeal:** Would players be excited to try this?
6. **Family Distinction:** If similar items exist, is this one unique?
7. **Grounded Design:** Does this fit Minecraft's world, or feel out of place?
8. **No Cringe:** Does the description avoid over-the-top marketing language?

**If any answer is "no" - redesign it.**

**Never suggest:**
- Negative stats or anti-fun mechanics
- Single weak effects with no depth
- Features requiring complex state tracking or client-server sync
- Boring, forgettable, one-dimensional designs

**Always include:**
- **Meaningful attributes** (focus on stats first, effects second)
- **0-1 effects maximum** (many items should be attribute-only)
- **Composite attribute aggregation** (`composite: "forgero:part-composite"` on attributes)
- **Strategic depth** (slot-conditional attributes, modifier attributes for synergy)
- **Memorable gameplay** (through smart attribute design and optional thematic effect)
- **Player empowerment** (feel powerful, not punished)
- **Item family distinction** (if similar items exist, make this one unique)
- **Grounded language** (avoid cringe marketing speak)
- Tier-appropriate balance
- Thematic fit with vanilla item
- Performance consciousness

**Design Philosophy Summary:**
Create upgrades that feel like natural extensions of Minecraft's existing items. Each upgrade should have a clear identity, solve real gameplay problems, and create interesting build choices. Avoid redundancy within item families - every vanilla item should have its own unique contribution.

Design with Minecraft's world in mind, not generic RPG tropes.

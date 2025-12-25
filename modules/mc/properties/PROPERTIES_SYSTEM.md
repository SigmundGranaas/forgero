# Forgero Properties System Documentation

## Table of Contents

1. [Overview](#overview)
2. [Core Concepts](#core-concepts)
3. [Event Properties Reference](#event-properties-reference)
4. [Handlers & Effects Reference](#handlers--effects-reference)
5. [Predicates & Conditions Reference](#predicates--conditions-reference)
6. [Entity Filters Reference](#entity-filters-reference)
7. [Entity Selectors Reference](#entity-selectors-reference)
8. [Usage Examples](#usage-examples)

---

## Overview

The Forgero Properties System is a flexible, data-driven framework for defining complex item behaviors through JSON configuration. It enables modpack creators and developers to create custom items with sophisticated mechanics without writing code.

### Key Features

- **Event-Driven Architecture**: React to game events (hitting entities, taking damage, placing blocks, etc.)
- **Composable Effects**: Combine multiple effects with selectors and conditions
- **Runtime Conditions**: Filter behaviors based on environment, entity state, or random chance
- **Two-Phase Resolution**: Optimize performance through static and dynamic evaluation
- **Full Reusability**: Share handlers, filters, and conditions across different property types

---

## Core Concepts

### Properties

A **Property** is a JSON-defined behavior that activates in response to game events. Properties are attached to items and evaluated when specific events occur.

All properties implement the `ConditionalProperty` interface, meaning they can have:
- **Conditions**: Requirements that must be met for the property to activate
- **Handlers/Effects**: Actions to perform when activated
- **Selectors** (for entity events): Choose which entities to affect

### Resolution Phases

The system evaluates properties in two phases for optimal performance:

1. **Static "Bake" Phase** (at component creation)
   - Evaluates `StaticCondition`s based on component structure
   - Tests tags, slot types, component depth, etc.
   - Pre-filters properties that can never activate

2. **Dynamic "Apply" Phase** (at runtime)
   - Evaluates `DynamicCondition`s based on game state
   - Tests entity type, weather, biome, etc.
   - Returns final list of active properties

### Property Flow

```
Game Event (e.g., entity hit)
  ↓
Event Manager intercepts
  ↓
Convert ItemStack → Component
  ↓
Resolve Properties with conditions
  ↓
Select target entities (if applicable)
  ↓
Apply filters to selected entities
  ↓
Execute effects on each target
```

---

## Event Properties Reference

### Entity-Based Events

Properties that trigger from entity interactions and apply effects to entities.

#### OnHitProperty

**Property Key**: `minecraft:on_hit`

Triggers when the wielder hits an entity with the item.

**Structure**:
```json
{
  "minecraft:on_hit": {
    "selector": <EntitySelector>,
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which entities to affect (default: single target)
- `effects`: List of effects to apply to selected entities
- `condition`: Optional conditions that must be met

**Use Cases**: Damage effects, life steal, knockback, status effects, area damage

**Example**:
```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:aoe",
      "radius": 3,
      "filters": [{"type": "forgero:is_hostile"}]
    },
    "effects": [
      {"type": "forgero:fire", "duration": 60},
      {"type": "forgero:knockback", "force": 1.5, "direction": "push"}
    ]
  }
}
```

---

#### OnDamageReceivedProperty

**Property Key**: `minecraft:on_damage_received`

Triggers when the wielder receives damage from an attacker. Enables defensive and counter-attack mechanics.

**Structure**:
```json
{
  "minecraft:on_damage_received": {
    "selector": <EntitySelector>,
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which attackers to affect
- `effects`: List of effects to apply to selected attackers
- `condition`: Optional conditions (e.g., only when health is low)

**Use Cases**: Thorns effects, damage reflection, counter-attacks, defensive buffs

**Example**:
```json
{
  "minecraft:on_damage_received": {
    "selector": {"type": "forgero:single_target"},
    "effects": [
      {"type": "forgero:knockback", "force": 2.0, "direction": "push"},
      {"type": "forgero:fire", "duration": 40}
    ],
    "condition": {
      "type": "forgero:random",
      "value": 0.3
    }
  }
}
```

---

#### OnKillProperty

**Property Key**: `minecraft:on_kill`

Triggers when the wielder kills an entity with the item.

**Structure**:
```json
{
  "minecraft:on_kill": {
    "selector": <EntitySelector>,
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which entities to affect (can select nearby entities, not just the killed one)
- `effects`: List of effects to apply
- `condition`: Optional conditions

**Use Cases**: Victory effects, loot bonuses, heal on kill, chain explosions

**Example**:
```json
{
  "minecraft:on_kill": {
    "selector": {"type": "forgero:single_target"},
    "effects": [
      {"type": "forgero:life_steal", "amount": 4.0},
      {"type": "forgero:explosion", "power": 2.0, "fire": false}
    ]
  }
}
```

---

#### OnTickProperty

**Property Key**: `minecraft:on_tick`

Triggers periodically while the item is equipped or in the player's inventory.

**Structure**:
```json
{
  "minecraft:on_tick": {
    "selector": <EntitySelector>,
    "effects": [<OnHitEffect>, ...],
    "interval": <int>,
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which entities to affect (often targets the wielder)
- `effects`: List of effects to apply each tick interval
- `interval`: Tick delay between activations (default: 20 ticks = 1 second)
- `condition`: Optional conditions

**Use Cases**: Passive effects, auras, regeneration, damage over time to wielder

**Example**:
```json
{
  "minecraft:on_tick": {
    "selector": {"type": "forgero:single_target"},
    "effects": [
      {"type": "forgero:status_effect", "effect": "minecraft:regeneration", "duration": 40, "amplifier": 0}
    ],
    "interval": 100,
    "condition": {
      "type": "forgero:dimension",
      "dimension": "minecraft:the_nether"
    }
  }
}
```

---

#### OnSneakToggleProperty

**Property Key**: `minecraft:on_sneak_toggle`

Triggers when the player starts sneaking (detects sneak press, not continuous sneaking).

**Structure**:
```json
{
  "minecraft:on_sneak_toggle": {
    "selector": <EntitySelector>,
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which entities to affect
- `effects`: List of effects to apply when sneaking starts
- `condition`: Optional conditions

**Use Cases**: Activate abilities, apply buffs, teleport, summon entities

**Example**:
```json
{
  "minecraft:on_sneak_toggle": {
    "selector": {"type": "forgero:single_target"},
    "effects": [
      {"type": "forgero:status_effect", "effect": "minecraft:invisibility", "duration": 200, "amplifier": 0}
    ]
  }
}
```

---

#### SwingHandProperty

**Property Key**: `minecraft:on_swing`

Triggers when the player performs a hand swing animation.

**Structure**:
```json
{
  "minecraft:on_swing": {
    "effects": [<SwingEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `effects`: List of swing effects to apply
- `condition`: Optional conditions

**Use Cases**: Particle effects, sound effects, visual feedback

---

#### EntityUseProperty

Triggers when using the item on an entity (e.g., right-clicking a mob).

**Structure**:
```json
{
  "minecraft:entity_use": {
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Use Cases**: Convert entities, heal mobs, apply effects on interaction

---

### Block-Based Events

Properties that trigger from block interactions.

#### OnHitBlockProperty

**Property Key**: `minecraft:on_hit_block`

Triggers when attacking a block (left-click, not breaking).

**Structure**:
```json
{
  "minecraft:on_hit_block": {
    "selector": <BlockSelector>,
    "effects": [<OnHitBlockEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which blocks to affect
- `effects`: List of block effects to apply
- `condition`: Optional conditions

**Use Cases**: Mining effects, block transmutation, particle effects

---

#### OnBlockPlaceProperty

**Property Key**: `minecraft:on_block_place`

Triggers when placing a block while holding the item.

**Structure**:
```json
{
  "minecraft:on_block_place": {
    "selector": <BlockSelector>,
    "effects": [<BlockEffect>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `selector`: Determines which blocks to affect
- `effects`: List of block effects to apply
- `condition`: Optional conditions

**Use Cases**: Auto-smelt blocks, modify placed blocks, area effects

---

#### BlockUseProperty

Triggers when using the item on a block (right-click).

**Structure**:
```json
{
  "minecraft:block_use": {
    "effects": [<OnHitEffect>, ...],
    "condition": <Condition>
  }
}
```

**Use Cases**: Till soil, strip logs, create paths

---

#### BlockBreakingProperty

Advanced multi-block mining system with pattern-based and vein-based selection.

**Structure**:
```json
{
  "minecraft:block_breaking": {
    "selector": <BlockSelector>,
    "condition": <Condition>
  }
}
```

**Selector Types**:
- `PatternSelector`: Break blocks in specific patterns (3x3, cross, etc.)
- `RadiusVeinSelector`: Break connected blocks of same type (vein mining)

**Use Cases**: Area mining, vein mining, pattern-based harvesting

---

### Use Interaction Events

Stateful item use with lifecycle phases (like bows, food, etc.).

#### UseInteractionProperty

**Property Key**: `minecraft:use_interaction`

Defines how an item behaves when used (right-click and hold). Supports charge-up mechanics with four lifecycle phases.

**Structure**:
```json
{
  "minecraft:use_interaction": {
    "use_action": <UseAction>,
    "max_use_time": <int>,
    "used_on_release": <boolean>,
    "on_start": [<UseHandler>, ...],
    "on_tick": [<UseHandler>, ...],
    "on_release": [<UseHandler>, ...],
    "on_finish": [<UseHandler>, ...],
    "condition": <Condition>
  }
}
```

**Fields**:
- `use_action`: Animation type (`NONE`, `EAT`, `DRINK`, `BLOCK`, `BOW`, `SPEAR`, `CROSSBOW`, `SPYGLASS`, `TOOT_HORN`, `BRUSH`)
- `max_use_time`: Maximum use duration in ticks (72000 = indefinite)
- `used_on_release`: If true, effects trigger when button is released (like bow)
- `on_start`: Handlers executed when use begins
- `on_tick`: Handlers executed each tick during use
- `on_release`: Handlers executed when button is released
- `on_finish`: Handlers executed when use completes naturally
- `condition`: Optional conditions

**Lifecycle Phases**:
1. **on_start**: Called once when right-click is pressed
2. **on_tick**: Called every tick while holding right-click
3. **on_release**: Called when right-click is released (if `used_on_release: true`)
4. **on_finish**: Called when use duration expires naturally

**Use Cases**: Throwing weapons, consumables, charging abilities, blocking

**Example (Throwable Spear)**:
```json
{
  "minecraft:use_interaction": {
    "use_action": "SPEAR",
    "max_use_time": 72000,
    "used_on_release": true,
    "on_start": [
      {"type": "forgero:start_use"}
    ],
    "on_release": [
      {"type": "forgero:throw", "velocity_multiplier": 2.5, "spin_type": "VERTICAL"},
      {"type": "forgero:damage_stack", "damage": 1},
      {"type": "forgero:sound", "sound": "minecraft:entity.arrow.shoot"}
    ]
  }
}
```

---

### Loot Modification

#### LootProperty

Modifies loot drops from blocks or entities.

**Structure**:
```json
{
  "minecraft:loot": {
    "modifier": <LootModifier>,
    "condition": <Condition>
  }
}
```

**Use Cases**: Fortune-like effects, bonus drops, loot modification

---

## Handlers & Effects Reference

Handlers are the actions performed when properties activate. They're organized by which property types can use them.

### Entity Effect Handlers (OnHitEffect)

These handlers work with **all entity-based events**: OnHit, OnDamageReceived, OnKill, OnTick, OnSneakToggle, EntityUse.

There are two types of entity effect handlers:

1. **EntityEffectHandler**: Simple effects that only need the target entity
2. **ContextualEffectHandler**: Complex effects that need both source and target

#### FireHandler

**Type**: `forgero:fire`
**Interface**: EntityEffectHandler
**Reusable**: Yes (all entity events)

Sets the target entity on fire.

**Fields**:
- `duration`: Fire duration in ticks (20 ticks = 1 second)

**Example**:
```json
{"type": "forgero:fire", "duration": 60}
```

---

#### LightningHandler

**Type**: `forgero:lightning`
**Interface**: EntityEffectHandler
**Reusable**: Yes (all entity events)

Strikes the target with lightning.

**Example**:
```json
{"type": "forgero:lightning"}
```

---

#### FreezeHandler

**Type**: `forgero:freeze`
**Interface**: EntityEffectHandler
**Reusable**: Yes (all entity events)

Freezes the target entity.

**Fields**:
- `duration`: Freeze duration in ticks

**Example**:
```json
{"type": "forgero:freeze", "duration": 100}
```

---

#### StatusEffectHandler

**Type**: `forgero:status_effect`
**Interface**: EntityEffectHandler
**Reusable**: Yes (all entity events)

Applies a potion effect to the target.

**Fields**:
- `effect`: Minecraft effect identifier (e.g., `minecraft:poison`)
- `duration`: Effect duration in ticks
- `amplifier`: Effect level (0 = level I, 1 = level II, etc.)
- `ambient`: Optional, makes particles less visible
- `showParticles`: Optional, show/hide particles
- `showIcon`: Optional, show/hide effect icon

**Example**:
```json
{
  "type": "forgero:status_effect",
  "effect": "minecraft:slowness",
  "duration": 200,
  "amplifier": 2,
  "showParticles": true
}
```

---

#### LifeStealHandler

**Type**: `forgero:life_steal`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Heals the source entity based on damage dealt or a fixed amount.

**Fields**:
- `amount`: Health points to restore
- `percentage`: Optional, heal based on damage percentage

**Example**:
```json
{"type": "forgero:life_steal", "amount": 2.0}
```

---

#### KnockbackHandler

**Type**: `forgero:knockback`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Applies knockback to the target.

**Fields**:
- `force`: Knockback strength
- `direction`: Direction type (`push` or `pull`)

**Example**:
```json
{"type": "forgero:knockback", "force": 2.5, "direction": "push"}
```

---

#### ExplosionHandler

**Type**: `forgero:explosion`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Creates an explosion at the target's location.

**Fields**:
- `power`: Explosion strength (4.0 = TNT)
- `fire`: Whether explosion creates fire
- `blockBreaking`: Optional, whether explosion breaks blocks

**Example**:
```json
{"type": "forgero:explosion", "power": 3.0, "fire": true}
```

---

#### ConvertHandler

**Type**: `forgero:convert`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Converts the target entity into another entity type.

**Fields**:
- `target`: Entity type identifier to convert to

**Example**:
```json
{"type": "forgero:convert", "target": "minecraft:zombie_villager"}
```

---

#### DisarmHandler

**Type**: `forgero:disarm`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Causes the target to drop their held item.

**Fields**:
- `chance`: Probability of disarming (0.0 to 1.0)

**Example**:
```json
{"type": "forgero:disarm", "chance": 0.3}
```

---

#### SoundHandler

**Type**: `forgero:sound`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (entity events AND use interactions)

Plays a sound effect.

**Fields**:
- `sound`: Minecraft sound identifier
- `volume`: Sound volume (default: 1.0)
- `pitch`: Sound pitch (default: 1.0)

**Example**:
```json
{"type": "forgero:sound", "sound": "minecraft:entity.lightning_bolt.thunder", "volume": 0.5}
```

---

#### ParticleHandler

**Type**: `forgero:particle`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (entity events AND use interactions)

Spawns particle effects.

**Fields**:
- `particle`: Minecraft particle identifier
- `count`: Number of particles to spawn
- `speed`: Particle movement speed
- `spread`: Particle spread area

**Example**:
```json
{"type": "forgero:particle", "particle": "minecraft:flame", "count": 20, "speed": 0.1}
```

---

#### VelocityHandler

**Type**: `forgero:velocity`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Modifies the target's velocity.

**Fields**:
- `x`, `y`, `z`: Velocity components
- `mode`: How to apply (`SET`, `ADD`, `MULTIPLY`)

**Example**:
```json
{"type": "forgero:velocity", "y": 1.5, "mode": "ADD"}
```

---

#### MagnetHandler

**Type**: `forgero:magnet`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Pulls items or entities toward the source.

**Fields**:
- `radius`: Pull radius
- `force`: Pull strength
- `itemsOnly`: Optional, only pull items

**Example**:
```json
{"type": "forgero:magnet", "radius": 5.0, "force": 0.3}
```

---

#### SpawnEntityHandler

**Type**: `forgero:spawn_entity`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Spawns an entity at the target's location.

**Fields**:
- `entity`: Entity type identifier
- `count`: Number of entities to spawn
- `offset`: Optional position offset

**Example**:
```json
{"type": "forgero:spawn_entity", "entity": "minecraft:lightning_bolt"}
```

---

#### ModifyBlockHandler

**Type**: `forgero:modify_block`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Modifies blocks near the target.

**Fields**:
- `radius`: Effect radius
- `from`: Source block state
- `to`: Target block state

**Example**:
```json
{
  "type": "forgero:modify_block",
  "radius": 3,
  "from": "minecraft:water",
  "to": "minecraft:ice"
}
```

---

#### TeleportHandler

**Type**: `forgero:teleport`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Teleports the target or source entity.

**Fields**:
- `target`: Who to teleport (`source`, `target`, or `both`)
- `distance`: Teleport distance
- `direction`: Direction to teleport

**Example**:
```json
{"type": "forgero:teleport", "target": "source", "distance": 10}
```

---

#### FunctionExecuteHandler

**Type**: `forgero:function_execute`
**Interface**: ContextualEffectHandler
**Reusable**: Yes (all entity events)

Executes a Minecraft function.

**Fields**:
- `function`: Function identifier

**Example**:
```json
{"type": "forgero:function_execute", "function": "mypack:custom_effect"}
```

---

### Use Interaction Handlers (UseHandler)

These handlers work **only with UseInteractionProperty**. They have access to use duration, charge progress, and hand information.

There are two types:

1. **SimpleUseHandler**: Basic operations without timing info
2. **ContextualUseHandler**: Advanced operations with charge time, pull progress, etc.

#### StartUseHandler

**Type**: `forgero:start_use`
**Interface**: SimpleUseHandler
**Reusable**: No (UseInteraction only)

Initiates the use action (required for use interactions to work properly).

**Example**:
```json
{"type": "forgero:start_use"}
```

---

#### ConsumeStackHandler

**Type**: `forgero:consume_stack`
**Interface**: SimpleUseHandler
**Reusable**: No (UseInteraction only)

Consumes the entire item stack.

**Fields**:
- `amount`: Number of items to consume (default: 1)

**Example**:
```json
{"type": "forgero:consume_stack", "amount": 1}
```

---

#### ConsumeUpgradeHandler

**Type**: `forgero:consume_upgrade`
**Interface**: SimpleUseHandler
**Reusable**: No (UseInteraction only)

Consumes a specific upgrade from the item.

**Fields**:
- `upgrade`: Upgrade identifier to consume

**Example**:
```json
{"type": "forgero:consume_upgrade", "upgrade": "forgero:ammo"}
```

---

#### DamageStackHandler

**Type**: `forgero:damage_stack`
**Interface**: SimpleUseHandler
**Reusable**: No (UseInteraction only)

Damages the item's durability.

**Fields**:
- `damage`: Damage amount (default: 1)

**Example**:
```json
{"type": "forgero:damage_stack", "damage": 1}
```

---

#### CooldownHandler

**Type**: `forgero:cooldown`
**Interface**: SimpleUseHandler
**Reusable**: No (UseInteraction only)

Applies a cooldown to the item.

**Fields**:
- `cooldown`: Cooldown duration in ticks

**Example**:
```json
{"type": "forgero:cooldown", "cooldown": 200}
```

---

#### ThrowHandler

**Type**: `forgero:throw`
**Interface**: ContextualUseHandler
**Reusable**: No (UseInteraction only)

Throws the item as a projectile. Charge time affects throw velocity.

**Fields**:
- `velocity_multiplier`: Base throw speed multiplier
- `spin_type`: Rotation style (`VERTICAL`, `HORIZONTAL`, `NONE`)
- `accuracy`: Optional, projectile accuracy (0.0 = perfect, higher = more spread)

**Example**:
```json
{"type": "forgero:throw", "velocity_multiplier": 2.5, "spin_type": "VERTICAL"}
```

---

### Summary: Handler Reusability Matrix

| Handler Type | OnHit | OnDamage | OnKill | OnTick | OnSneak | EntityUse | UseInteraction |
|--------------|-------|----------|--------|--------|---------|-----------|----------------|
| **Entity Effect Handlers** (23) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | - |
| **Use Handlers** (8) | - | - | - | - | - | - | ✓ |
| **Sound/Particle** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

**Cross-system handlers**: SoundHandler and ParticleHandler are registered in both systems and can be used with both entity events and use interactions.

---

## Predicates & Conditions Reference

Conditions determine when properties should activate. The system has two types based on evaluation timing.

### Static Conditions

**When**: Evaluated during component creation ("bake" phase)
**Purpose**: Filter based on component structure (tags, slots, hierarchy)
**Reusable**: Yes (all ConditionalProperty types)

These test the item's composition and structure, not runtime game state.

#### TagMatchCondition

**Type**: `forgero:tag_match`

Tests if the component has a specific tag.

**Fields**:
- `tag`: Tag to match

**Example**:
```json
{"type": "forgero:tag_match", "tag": "forgero:sword"}
```

---

#### AtDepthCondition

**Type**: `forgero:at_depth`

Tests the component's depth in the composition tree.

**Fields**:
- `depth`: Required depth level
- `comparator`: Optional (`EQUAL`, `LESS_THAN`, `GREATER_THAN`)

**Example**:
```json
{"type": "forgero:at_depth", "depth": 2, "comparator": "EQUAL"}
```

---

#### InSlotTypeCondition

**Type**: `forgero:in_slot_type`

Tests if the component is in a specific slot type.

**Fields**:
- `slot`: Slot type identifier

**Example**:
```json
{"type": "forgero:in_slot_type", "slot": "forgero:gem"}
```

---

#### IdMatchCondition

**Type**: `forgero:id_match`

Tests if the component matches a specific ID.

**Fields**:
- `id`: Component identifier

**Example**:
```json
{"type": "forgero:id_match", "id": "forgero:iron_sword"}
```

---

#### IsRootCondition

**Type**: `forgero:is_root`

Tests if the component is the root (top-level) component.

**Example**:
```json
{"type": "forgero:is_root"}
```

---

#### HasSiblingCondition

**Type**: `forgero:has_sibling`

Tests if the component has a sibling with specific properties.

**Fields**:
- `tag`: Optional tag the sibling must have
- `id`: Optional ID the sibling must match

**Example**:
```json
{"type": "forgero:has_sibling", "tag": "forgero:gem"}
```

---

#### SlotContainsCondition

**Type**: `forgero:slot_contains`

Tests if a specific slot contains a component matching criteria.

**Fields**:
- `slot`: Slot type to check
- `tag`: Optional tag to match
- `id`: Optional ID to match

**Example**:
```json
{"type": "forgero:slot_contains", "slot": "forgero:binding", "tag": "forgero:gold"}
```

---

### Dynamic Conditions

**When**: Evaluated at runtime ("apply" phase)
**Purpose**: Filter based on game state (weather, entity type, biome, etc.)
**Reusable**: Yes (all ConditionalProperty types)

These test the current game environment and entity states.

#### RandomCondition

**Type**: `forgero:random`

Random chance condition.

**Fields**:
- `value`: Probability (0.0 to 1.0)

**Example**:
```json
{"type": "forgero:random", "value": 0.25}
```

**Use Cases**: 25% chance to activate, proc-based effects

---

#### DamagePercentageCondition

**Type**: `forgero:damage_percentage`

Tests the damage amount as a percentage of max health.

**Fields**:
- `percentage`: Threshold percentage (0.0 to 1.0)
- `comparator`: Comparison type (`GREATER_THAN`, `LESS_THAN`, `EQUAL`)

**Example**:
```json
{"type": "forgero:damage_percentage", "percentage": 0.5, "comparator": "GREATER_THAN"}
```

**Use Cases**: Only activate on killing blows, only when taking heavy damage

---

#### WeatherCondition

**Type**: `forgero:weather`

Tests the current weather.

**Fields**:
- `weather`: Weather type (`clear`, `rain`, `thunder`)

**Example**:
```json
{"type": "forgero:weather", "weather": "thunder"}
```

**Use Cases**: Lightning effects during storms, bonuses in rain

---

#### EntityTypeCondition

**Type**: `forgero:entity_type`

Tests if the target entity is a specific type.

**Fields**:
- `entity`: Entity type identifier
- `blacklist`: Optional, invert the match

**Example**:
```json
{"type": "forgero:entity_type", "entity": "minecraft:zombie"}
```

**Use Cases**: Extra damage to undead, effects only on players

---

#### EntityFlagCondition

**Type**: `forgero:entity_flag`

Tests entity state flags.

**Fields**:
- `flag`: Flag type (`ON_FIRE`, `SNEAKING`, `SPRINTING`, `SWIMMING`, `INVISIBLE`, `GLOWING`, `FLYING`)
- `value`: Expected state (true/false)

**Example**:
```json
{"type": "forgero:entity_flag", "flag": "ON_FIRE", "value": true}
```

**Use Cases**: Extra damage to burning enemies, effects while sprinting

---

#### BlockMatchCondition

**Type**: `forgero:block_match`

Tests if the target block matches a specific type.

**Fields**:
- `block`: Block identifier

**Example**:
```json
{"type": "forgero:block_match", "block": "minecraft:stone"}
```

---

#### BlockTagCondition

**Type**: `forgero:block_tag`

Tests if the target block has a specific tag.

**Fields**:
- `tag`: Block tag identifier

**Example**:
```json
{"type": "forgero:block_tag", "tag": "minecraft:logs"}
```

---

#### DimensionCondition

**Type**: `forgero:dimension`

Tests the current dimension.

**Fields**:
- `dimension`: Dimension identifier

**Example**:
```json
{"type": "forgero:dimension", "dimension": "minecraft:the_nether"}
```

**Use Cases**: Bonuses in specific dimensions, Nether-only effects

---

#### BiomeCondition

**Type**: `forgero:biome`

Tests the current biome.

**Fields**:
- `biome`: Biome identifier or tag

**Example**:
```json
{"type": "forgero:biome", "biome": "minecraft:desert"}
```

**Use Cases**: Environmental bonuses, biome-specific effects

---

#### PositionCondition

**Type**: `forgero:position`

Tests position-based criteria.

**Fields**:
- `y`: Y-level threshold
- `comparator`: Comparison type
- `light_level`: Optional light level check

**Example**:
```json
{"type": "forgero:position", "y": 50, "comparator": "LESS_THAN"}
```

**Use Cases**: Deep mining bonuses, light-dependent effects

---

### Logical Conditions

Combine multiple conditions with boolean logic.

#### AndCondition

**Type**: `forgero:and`

All child conditions must pass.

**Fields**:
- `conditions`: List of conditions

**Example**:
```json
{
  "type": "forgero:and",
  "conditions": [
    {"type": "forgero:weather", "weather": "thunder"},
    {"type": "forgero:dimension", "dimension": "minecraft:overworld"}
  ]
}
```

---

#### OrCondition

**Type**: `forgero:or`

At least one child condition must pass.

**Fields**:
- `conditions`: List of conditions

**Example**:
```json
{
  "type": "forgero:or",
  "conditions": [
    {"type": "forgero:entity_type", "entity": "minecraft:zombie"},
    {"type": "forgero:entity_type", "entity": "minecraft:skeleton"}
  ]
}
```

---

#### NotCondition

**Type**: `forgero:not`

Inverts the child condition.

**Fields**:
- `condition`: Condition to invert

**Example**:
```json
{
  "type": "forgero:not",
  "condition": {"type": "forgero:is_player"}
}
```

---

### Condition Usage Matrix

| Condition Type | Evaluation Phase | Use Cases |
|----------------|------------------|-----------|
| **Static** | Component Creation | Structure, tags, slots, hierarchy |
| **Dynamic** | Runtime | Weather, entities, biomes, position |
| **Logical** | Both | Combine multiple conditions |

All conditions can be used with any ConditionalProperty (all 13 property types).

---

## Entity Filters Reference

**Used By**: EntitySelectors (for OnHit, OnDamage, OnKill, OnTick, OnSneak)
**Purpose**: Filter selected entities based on various criteria
**Interface**: `EntityFilter` with method `test(Entity source, Entity candidate)`
**Reusable**: Yes (all entity-based selectors)

Filters determine which entities from a selection should be affected.

### Basic Filters

#### IsAliveFilter

**Type**: `forgero:is_alive`

Filters to only living entities.

**Example**:
```json
{"type": "forgero:is_alive"}
```

---

#### IsHostileFilter

**Type**: `forgero:is_hostile`

Filters to only hostile mobs.

**Example**:
```json
{"type": "forgero:is_hostile"}
```

---

#### IsTeammateFilter

**Type**: `forgero:is_teammate`

Filters to only teammates (same team as source).

**Example**:
```json
{"type": "forgero:is_teammate"}
```

---

#### IsPlayerFilter

**Type**: `forgero:is_player`

Filters to only player entities.

**Example**:
```json
{"type": "forgero:is_player"}
```

---

#### HasTagFilter

**Type**: `forgero:has_tag`

Filters entities with a specific entity type tag.

**Fields**:
- `tag`: Entity type tag

**Example**:
```json
{"type": "forgero:has_tag", "tag": "minecraft:undead"}
```

---

#### EntityTypeFilter

**Type**: `forgero:entity_type`

Filters for a specific entity type.

**Fields**:
- `entity`: Entity type identifier
- `blacklist`: Optional, exclude this type instead

**Example**:
```json
{"type": "forgero:entity_type", "entity": "minecraft:creeper"}
```

---

### State-Based Filters

#### IsBurningFilter

**Type**: `forgero:is_burning`

Filters to only burning entities.

**Example**:
```json
{"type": "forgero:is_burning"}
```

---

#### IsInWaterFilter

**Type**: `forgero:is_in_water`

Filters to only entities in water.

**Example**:
```json
{"type": "forgero:is_in_water"}
```

---

#### HasEffectFilter

**Type**: `forgero:has_effect`

Filters entities with a specific potion effect.

**Fields**:
- `effect`: Effect identifier
- `amplifier`: Optional minimum amplifier level

**Example**:
```json
{"type": "forgero:has_effect", "effect": "minecraft:poison"}
```

---

### Advanced Filters

#### HealthThresholdFilter

**Type**: `forgero:health_threshold`

Filters based on health percentage.

**Fields**:
- `threshold`: Health percentage (0.0 to 1.0)
- `comparator`: Comparison type (`GREATER_THAN`, `LESS_THAN`, `EQUAL`)

**Example**:
```json
{"type": "forgero:health_threshold", "threshold": 0.3, "comparator": "LESS_THAN"}
```

**Use Cases**: Execute enemies below 30% health, only heal above 50%

---

#### DistanceFilter

**Type**: `forgero:distance`

Filters based on distance from source.

**Fields**:
- `distance`: Distance threshold in blocks
- `comparator`: Comparison type

**Example**:
```json
{"type": "forgero:distance", "distance": 5.0, "comparator": "LESS_THAN"}
```

---

#### RandomChanceFilter

**Type**: `forgero:random_chance`

Randomly includes entities with a probability.

**Fields**:
- `chance`: Probability (0.0 to 1.0)

**Example**:
```json
{"type": "forgero:random_chance", "chance": 0.5}
```

---

#### EnvironmentFilter

**Type**: `forgero:environment`

Filters based on environmental conditions.

**Fields**:
- `condition`: Environment type (`in_rain`, `in_sunlight`, `in_lava`, `underground`)

**Example**:
```json
{"type": "forgero:environment", "condition": "in_sunlight"}
```

---

#### EntityStateFilter

**Type**: `forgero:entity_state`

Filters based on complex entity state.

**Fields**:
- `state`: State type (various entity states)

**Example**:
```json
{"type": "forgero:entity_state", "state": "is_baby"}
```

---

### Composite Filters

#### AndFilter

**Type**: `forgero:and`

All child filters must pass.

**Fields**:
- `filters`: List of filters

**Example**:
```json
{
  "type": "forgero:and",
  "filters": [
    {"type": "forgero:is_hostile"},
    {"type": "forgero:health_threshold", "threshold": 0.5, "comparator": "LESS_THAN"}
  ]
}
```

---

#### OrFilter

**Type**: `forgero:or`

At least one child filter must pass.

**Fields**:
- `filters`: List of filters

**Example**:
```json
{
  "type": "forgero:or",
  "filters": [
    {"type": "forgero:is_burning"},
    {"type": "forgero:is_in_water"}
  ]
}
```

---

#### NotFilter

**Type**: `forgero:not`

Inverts the child filter.

**Fields**:
- `filter`: Filter to invert

**Example**:
```json
{
  "type": "forgero:not",
  "filter": {"type": "forgero:is_teammate"}
}
```

---

## Entity Selectors Reference

**Used By**: All entity-based properties (OnHit, OnDamage, OnKill, OnTick, OnSneak)
**Purpose**: Choose which entities to affect
**Interface**: `EntitySelector` with method `select(Entity source, Entity initialTarget)`

Selectors determine the geometric/spatial selection of entities, then apply filters.

### SingleTargetSelector

**Type**: `forgero:single_target`

Selects only the initial target entity.

**Fields**:
- `filters`: Optional list of EntityFilters

**Example**:
```json
{
  "type": "forgero:single_target",
  "filters": [
    {"type": "forgero:is_alive"}
  ]
}
```

**Use Cases**: Direct hits, single-target effects

---

### AreaOfEffectSelector

**Type**: `forgero:aoe`

Selects all entities within a radius of the target.

**Fields**:
- `radius`: Selection radius in blocks
- `filters`: Optional list of EntityFilters

**Example**:
```json
{
  "type": "forgero:aoe",
  "radius": 5,
  "filters": [
    {"type": "forgero:is_hostile"},
    {"type": "forgero:distance", "distance": 3.0, "comparator": "GREATER_THAN"}
  ]
}
```

**Use Cases**: Area damage, cleave attacks, splash effects

---

### ConeSelector

**Type**: `forgero:cone`

Selects entities in a cone shape in the direction the source is looking.

**Fields**:
- `radius`: Maximum distance in blocks
- `angle`: Cone angle in degrees
- `filters`: Optional list of EntityFilters

**Example**:
```json
{
  "type": "forgero:cone",
  "radius": 8,
  "angle": 45,
  "filters": [
    {"type": "forgero:is_alive"}
  ]
}
```

**Use Cases**: Frontal cleave, breath weapons, directional effects

---

### ChainSelector

**Type**: `forgero:chain`

Chains from target to target based on proximity, up to a maximum chain count.

**Fields**:
- `maxChain`: Maximum number of chain targets
- `maxDistance`: Maximum distance between chain links
- `filters`: Optional list of EntityFilters

**Example**:
```json
{
  "type": "forgero:chain",
  "maxChain": 3,
  "maxDistance": 5.0,
  "filters": [
    {"type": "forgero:is_hostile"},
    {"type": "forgero:not", "filter": {"type": "forgero:is_player"}}
  ]
}
```

**Use Cases**: Chain lightning, bouncing projectiles, spreading effects

---

### Selector Usage Pattern

1. **Geometric Selection**: Selector picks entities based on position (single, area, cone, chain)
2. **Filter Application**: EntityFilters refine the selection
3. **Effect Application**: Each selected entity receives all effects

**Example Flow**:
```
AOE Selector (radius 5)
  → Selects 10 entities within 5 blocks
  → Apply filter: is_hostile
    → 6 entities remain
  → Apply filter: health < 50%
    → 3 entities remain
  → Apply effects to 3 entities
```

---

## Usage Examples

### Example 1: Life-Stealing Vampiric Weapon

Heals the wielder when hitting hostile enemies at night.

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:single_target",
      "filters": [
        {"type": "forgero:is_hostile"}
      ]
    },
    "effects": [
      {"type": "forgero:life_steal", "amount": 3.0},
      {"type": "forgero:particle", "particle": "minecraft:damage_indicator", "count": 10}
    ],
    "condition": {
      "type": "forgero:and",
      "conditions": [
        {"type": "forgero:weather", "weather": "clear"},
        {"type": "forgero:position", "light_level": 7, "comparator": "LESS_THAN"}
      ]
    }
  }
}
```

---

### Example 2: Explosive Chain Lightning

25% chance to strike targets with lightning that chains to nearby enemies.

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:chain",
      "maxChain": 4,
      "maxDistance": 6.0,
      "filters": [
        {"type": "forgero:is_hostile"},
        {"type": "forgero:is_alive"}
      ]
    },
    "effects": [
      {"type": "forgero:lightning"},
      {"type": "forgero:explosion", "power": 1.5, "fire": false}
    ],
    "condition": {
      "type": "forgero:random",
      "value": 0.25
    }
  }
}
```

---

### Example 3: Thorns Counter-Attack

When damaged, knock back and burn the attacker.

```json
{
  "minecraft:on_damage_received": {
    "selector": {
      "type": "forgero:single_target"
    },
    "effects": [
      {"type": "forgero:knockback", "force": 3.0, "direction": "push"},
      {"type": "forgero:fire", "duration": 80},
      {"type": "forgero:sound", "sound": "minecraft:entity.blaze.hurt"}
    ]
  }
}
```

---

### Example 4: Area Cleave Attack

Hit creates a 3-block AOE that damages all nearby hostile mobs.

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:aoe",
      "radius": 3,
      "filters": [
        {"type": "forgero:is_hostile"},
        {"type": "forgero:not", "filter": {"type": "forgero:is_teammate"}}
      ]
    },
    "effects": [
      {"type": "forgero:knockback", "force": 1.0, "direction": "push"},
      {"type": "forgero:particle", "particle": "minecraft:sweep_attack", "count": 20}
    ]
  }
}
```

---

### Example 5: Throwable Javelin

Right-click and release to throw, with throw power based on charge time.

```json
{
  "minecraft:use_interaction": {
    "use_action": "SPEAR",
    "max_use_time": 72000,
    "used_on_release": true,
    "on_start": [
      {"type": "forgero:start_use"}
    ],
    "on_release": [
      {"type": "forgero:throw", "velocity_multiplier": 2.5, "spin_type": "VERTICAL", "accuracy": 1.0},
      {"type": "forgero:damage_stack", "damage": 1},
      {"type": "forgero:sound", "sound": "minecraft:entity.arrow.shoot", "volume": 1.0},
      {"type": "forgero:cooldown", "cooldown": 20}
    ]
  }
}
```

---

### Example 6: Nether Regeneration Aura

While in the Nether, periodically grants regeneration.

```json
{
  "minecraft:on_tick": {
    "selector": {
      "type": "forgero:single_target"
    },
    "effects": [
      {"type": "forgero:status_effect", "effect": "minecraft:regeneration", "duration": 120, "amplifier": 1}
    ],
    "interval": 100,
    "condition": {
      "type": "forgero:dimension",
      "dimension": "minecraft:the_nether"
    }
  }
}
```

---

### Example 7: Execute Below 30% Health

Instantly kills enemies below 30% health with a 20% chance.

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:single_target",
      "filters": [
        {"type": "forgero:health_threshold", "threshold": 0.3, "comparator": "LESS_THAN"},
        {"type": "forgero:not", "filter": {"type": "forgero:is_player"}}
      ]
    },
    "effects": [
      {"type": "forgero:explosion", "power": 2.0, "fire": false},
      {"type": "forgero:life_steal", "amount": 5.0}
    ],
    "condition": {
      "type": "forgero:random",
      "value": 0.2
    }
  }
}
```

---

### Example 8: Conditional Fire Sword

Only sets enemies on fire during thunderstorms in the Overworld.

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:single_target"
    },
    "effects": [
      {"type": "forgero:fire", "duration": 100},
      {"type": "forgero:lightning"}
    ],
    "condition": {
      "type": "forgero:and",
      "conditions": [
        {"type": "forgero:weather", "weather": "thunder"},
        {"type": "forgero:dimension", "dimension": "minecraft:overworld"}
      ]
    }
  }
}
```

---

### Example 9: Multi-Phase Charged Ability

Use interaction with effects on start, during use, and on release.

```json
{
  "minecraft:use_interaction": {
    "use_action": "BOW",
    "max_use_time": 72000,
    "used_on_release": true,
    "on_start": [
      {"type": "forgero:start_use"},
      {"type": "forgero:sound", "sound": "minecraft:block.beacon.power_select"}
    ],
    "on_tick": [
      {"type": "forgero:particle", "particle": "minecraft:flame", "count": 2, "speed": 0.05}
    ],
    "on_release": [
      {"type": "forgero:throw", "velocity_multiplier": 3.0, "spin_type": "NONE"},
      {"type": "forgero:damage_stack", "damage": 2},
      {"type": "forgero:cooldown", "cooldown": 100}
    ]
  }
}
```

---

### Example 10: Combo System with Static Conditions

Different effects based on component structure (gem slots).

```json
{
  "minecraft:on_hit": {
    "selector": {
      "type": "forgero:single_target"
    },
    "effects": [
      {"type": "forgero:fire", "duration": 60}
    ],
    "condition": {
      "type": "forgero:slot_contains",
      "slot": "forgero:gem",
      "tag": "forgero:fire_gem"
    }
  }
}
```

---

## Best Practices

### Performance Optimization

1. **Use Static Conditions When Possible**: Pre-filter properties at bake time
2. **Minimize Filter Complexity**: Simpler filters perform better in AOE scenarios
3. **Appropriate Selectors**: Don't use AOE when single target suffices
4. **Tick Intervals**: Use longer intervals for `on_tick` properties (100+ ticks)

### Design Patterns

1. **Layered Filtering**: Use both selector filters and conditions for fine control
2. **Composition**: Combine simple effects rather than creating complex custom handlers
3. **Probability Stacking**: Use both condition chance and filter chance for rare effects
4. **Context-Appropriate Handlers**: Use ContextualEffectHandler when you need source/target info

### Common Pitfalls

1. **Forgetting Filters**: AOE without hostile filter affects teammates
2. **Missing StartUseHandler**: Use interactions need `start_use` to function
3. **Infinite Chains**: ChainSelector without proper filters can infinite loop
4. **Condition Conflicts**: AND conditions that can never be satisfied together

### Testing Checklist

- [ ] Test with single targets
- [ ] Test with multiple entities
- [ ] Test with friendly/hostile mixes
- [ ] Verify conditions activate correctly
- [ ] Check performance with large entity counts
- [ ] Validate JSON syntax
- [ ] Test edge cases (underwater, void, dimension borders)

---

## Architecture Summary

```
ConditionalProperty (13 types)
  ├─ Conditions (static + dynamic)
  ├─ EntitySelector (for entity events)
  │   ├─ Geometric selection (single/aoe/cone/chain)
  │   └─ EntityFilters (18 types)
  └─ Handlers/Effects
      ├─ OnHitEffect (23 types) - for entity events
      ├─ UseHandler (8 types) - for use interactions
      ├─ BlockEffect - for block placement
      └─ OnHitBlockEffect - for block hitting
```

**Plugin Registration** → **Type Codecs** → **Runtime Resolution** → **Effect Application**

All components are JSON-configurable and reusable across the system where appropriate.

---

## Additional Resources

- **Source Code**: `/modules/mc/properties/src/main/java/com/sigmundgranaas/forgero/properties/minecraft/`
- **Plugin Registration**: Look for `*Plugin.java` files for complete handler lists
- **Codec Definitions**: Each handler has a static `CODEC` field for JSON structure
- **Manager Implementations**: `*Manager.java` files show how events are processed

For custom handlers, extend the appropriate interface and register via a DataPlugin implementation.

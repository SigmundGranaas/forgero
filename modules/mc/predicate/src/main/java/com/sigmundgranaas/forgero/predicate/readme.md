
# Forgero Predicate System Documentation

## 1. Overview

The Forgero Predicate System provides a data-driven way to define complex, game-context-aware conditions. These predicates are used within the `condition` block of properties (like `on_hit` or `block_breaking`) to determine if a property should be active at runtime.

This system is designed to be pluggable, allowing developers to easily add new types of checks without modifying core modules.

### Core Concepts

*   **Predicate / Dynamic Condition:** A data object, defined in JSON, that represents a specific check against the current game state. It implements the `DynamicCondition` interface.
*   **Dynamic Context:** A container (`DynamicContext`) that holds runtime information, such as the player entity, the target entity, the world, or a block position. Predicates use this context to perform their checks.
*   **Context Keys:** Static keys (`MinecraftContextKeys`) used to safely access data within the `DynamicContext`.
*   **Codec:** A `MoJang Codec` that defines how a predicate is deserialized from its JSON representation.
*   **Plugin:** A `DataPlugin` that registers the predicate's codec with Forgero's data loading system, making it available for use in data packs.

---

## 2. System Architecture

A single "predicate" type is composed of a few key classes:

1.  **The Predicate Record (`EntityPredicate.java`)**:
    *   An immutable data class that mirrors the structure of the predicate's JSON definition.
    *   Implements `DynamicCondition`.
    *   Contains an `OpenIdentifier` constant named `TYPE` which is its unique ID (e.g., `"minecraft:entity"`).
    *   Provides a static `Codec` for serialization.
    *   Its core logic is in the `test(DynamicContext context)` method, which reads from the context and returns `true` or `false`.

2.  **The Context Keys (`MinecraftContextKeys.java`)**:
    *   A class containing static `Key` objects that define the contract for what data is available in the `DynamicContext` at runtime.
    *   Examples include `ENTITY`, `TARGET_ENTITY`, `WORLD`, and `TARGET_BLOCK_POS`.

3.  **The Plugin (`MinecraftPredicatePlugin.java`)**:
    *   Implements `DataPlugin`.
    *   In its `register` method, it calls `context.registerDynamicConditionCodec()` to make the predicate type known to the system.

---

## 3. How to Create a New Predicate Type

This guide demonstrates how to create a new predicate that checks if the player is in water.

### Step 1: Define the Predicate Record

Create a new record that implements `DynamicCondition`. It should be simple, as it only needs a `type` field in the JSON.

```java
public record InWaterPredicate() implements DynamicCondition {
    public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "in_water");
    public static final Codec<InWaterPredicate> CODEC = Codec.unit(InWaterPredicate::new);

    @Override
    public boolean test(DynamicContext context) {
        // Get the primary entity from the context
        Optional<Entity> entityOpt = context.get(MinecraftContextKeys.ENTITY);
        
        // Check if the entity is present and in water
        return entityOpt.map(Entity::isInWater).orElse(false);
    }

    @Override
    public OpenIdentifier type() {
        return TYPE;
    }
}
```

### Step 2: Register the Predicate in a Plugin

Find the DataPlugin for your module (or create one) and add a line to register your new predicate's codec.

```
public class MinecraftPredicatePlugin implements DataPlugin {

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerDynamicConditionCodec(BlockPredicate.TYPE.toString(), BlockPredicate.CODEC);
		context.registerDynamicConditionCodec(EntityPredicate.TYPE.toString(), EntityPredicate.CODEC);
		
		// Register the new predicate
		context.registerDynamicConditionCodec(InWaterPredicate.TYPE.toString(), InWaterPredicate.CODEC);
	}

	@Override
	public String getId() {
		return "forgero-minecraft-predicates";
	}
}
```
  

### Step 3: Add Plugin to fabric.mod.json

If you created a new plugin, make sure to register it in your fabric.mod.json under the forgero:data_plugin entrypoint.

```        
{
  "entrypoints": {
    "forgero:data_plugin": [
      "com.your.package.YourDataPlugin"
    ]
  }
}
```
  

The new predicate is now ready to be used in JSON files.
### 4. JSON Schema and Examples

Predicates are used within the condition block of a property. A condition can be a single predicate or a list of them.

Example JSON for Predicates:

```    
{
  "properties": {
    "minecraft:on_hit": [
      {
        "handler": {
          "type": "forgero:explosion",
          "power": 3.0
        },
        "condition": {
          "type": "minecraft:entity",
          "target": "target_entity",
          "flags": {
            "is_on_fire": true
          }
        }
      },
      {
        "handler": {
          "type": "forgero:status_effect",
          "effect": "minecraft:strength",
          "duration": 100
        },
        "condition": {
            "type": "forgero:in_water"
        }
      }
    ]
  }
}
```

The condition block contains a JSON object (or an array of them).

The "type" field in the condition object (e.g., "minecraft:entity") selects which predicate implementation to use.

The rest of the fields are specific to that predicate, as defined by its Codec.

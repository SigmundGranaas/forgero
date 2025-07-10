# Component Object Format (COF) Module

## 1. Purpose

This module provides a robust and efficient system for serializing and deserializing Forgero `Component` objects. It is designed to convert live component instances into a persistent format (like JSON or NBT) and back again. This is crucial for saving tools and parts in inventories, sending item data over the network, or any other scenario where an item's state needs to be stored or transmitted.

## 2. Core Concept: Smart Serialization

The central feature of the COF module is its "smart" serialization strategy, which optimizes for both performance and data size by distinguishing between two states of a component:

1.  **Pristine Component:** A component that has not been modified and is identical to its default state as defined in the `ComponentRegistry`. These components are serialized into a highly compact format: a simple string containing their unique ID.

2.  **Mutated Component:** A component whose state has changed from its default. This includes:
    *   A tool with a part swapped out.
    *   A part with an upgrade gem socketed.
    *   A material with custom properties added at runtime.

    Mutated components are serialized into a full JSON object that captures their complete state, including all properties, tags, and any nested components within their structure or upgrade slots.

This dual approach ensures that common, unmodified items have a minimal data footprint, while complex, customized items can be fully and accurately represented.

## 3. JSON Output Format

The codec can serialize to any format supported by Mojang's `DynamicOps` (e.g., NBT), but here we will focus on the JSON representation.

### 3.1. Pristine Component Example

A pristine `forgero:iron-ingot` component is serialized as a simple string:

```json
"forgero:iron"
```

### 3.2. Mutated Component Example

Consider an `iron-pickaxe` that has had its default handle replaced with a `reinforced-oak-handle`, and a `diamond-gem` has been socketed into an upgrade slot. The `reinforced-oak-handle` is itself a composite part made of `oak` and `iron`.

The resulting JSON would be a full, recursive object.

```json
{
  "id": "forgero:iron-pickaxe",
  "component_type": "forgero:structured_extensible_equipment",
  "tags": [
    "forgero:pickaxe"
  ],
  "structure": {
    "slots": {
      "forgero:head": "forgero:iron-pickaxe_head",
      "forgero:handle": {
        "id": "forgero:oak-handle",
        "component_type": "forgero:structured_part",
        "structure": {
          "slots": {
            "forgero:material": "forgero:oak",
            "forgero:reinforcement": "forgero:iron"
          }
        },
        "cof_version": 1
      }
    }
  },
  "upgrades": {
    "slots": [
      {
        "id": "forgero:gem_slot",
        "type": "forgero:gem",
        "description": "A slot for a gem",
        "content": {
          "id": "forgero:diamond-gem",
          "component_type": "forgero:static_component",
          "tags": ["forgero:gem"],
          "attributes": [
            {
              "type": "forgero:attack_damage",
              "value": 10,
              "operator": "ADDITION"
            }
          ],
          "cof_version": 1
        }
      }
    ]
  },
  "cof_version": 1
}
```

**Key fields in the mutated format:**
- `id`: The unique identifier of the component.
- `component_type`: A string key that maps to the component's Java class. This is used during deserialization to construct the correct object type.
- `tags`, `attributes`, `features`: Optional fields that list any custom properties or tags that differ from the pristine version.
- `structure`: An object detailing the required parts of a composite component. Note how `forgero:head` is a pristine string ID, while the `forgero:handle` is another fully serialized object because it was mutated.
- `upgrades`: An array detailing the optional upgrade slots. Note how `gem_slot` contains a fully serialized gem. An empty slot would have its `content` field be absent.
- `cof_version`: An integer indicating the schema version. This ensures backward compatibility if the format changes in the future.

## 4. Extensibility for Addons

The COF module is designed to be extensible. Hardcoded logic for object construction is avoided by using the `ComponentConstructorRegistry`. Addon developers can register their own custom `Component` types:

```java
// In an addon's initialization code:
ComponentConstructorRegistry.getInstance().register(
  "my_addon:magic_wand",
  MagicWandComponent.class,
  (dto, props, struct, upgs) -> DataResult.success(new MagicWandComponent(...))
);
```
This allows the core COF codec to serialize and deserialize addon components without needing any direct knowledge of their implementation, fostering a modular and robust ecosystem.


# Component Object Format (COF) Module

## 1. Purpose

The COF module provides a robust and efficient system for **serializing and deserializing live Forgero `Component` objects at runtime**. Its primary purpose is to convert component instances into a persistent format (like JSON or NBT) and back again. This is crucial for:

*   Saving tools and parts in inventories.
*   Synchronizing item data between the server and client.
*   Any other scenario where a component's state needs to be stored or transmitted.

The DTOs (`CofComponent`, `CofSlot`, etc.) in this package define the **Canonical Object Format**—the single, unified intermediate representation used for this serialization process.

## 2. Core Concept: Smart Serialization with `ComponentCofCodec`

The `ComponentCofCodec` employs a "smart" serialization strategy to optimize for performance and data size by distinguishing between two states of a component:

1.  **Pristine Component:** A component that has not been modified and is identical to its default state as defined in the `ComponentRegistry`. These components are serialized into a highly compact format: **a simple string containing their unique ID**.

2.  **Mutated Component:** A component whose state has changed from its default. This includes a tool with a part swapped out, a part with an upgrade gem socketed, or a component with custom NBT data. These components are serialized into a **full, recursive JSON object** that captures their complete state, including all properties, tags, and any nested components within their structure or upgrade slots.

This dual approach ensures that common, unmodified items have a minimal data footprint, while complex, customized items can be fully and accurately represented.

## 3. JSON Output Format

### 3.1. Pristine Component Example

A pristine `forgero:iron-ingot` component is serialized as a simple string:

```json
"forgero:iron"
```

### 3.2. Mutated Component Example

Consider an iron-pickaxe that has had its default handle replaced with a reinforced-oak-handle. The resulting JSON would be a full, recursive object.

```json
{
  "id": "forgero:iron-pickaxe",
  "component_type": "forgero:structured_extensible_equipment",
  "tags": ["forgero:pickaxe", "forgero:tool"],
  "properties": {
    "forgero:attributes": [ 
        { "type": "forgero:attack_damage", "computation": { "value": 5.0, ... } }
    ]
  },
  "structure": {
    "slots": {
      "forgero:head": "forgero:iron-pickaxe_head",
      "forgero:handle": {
        "id": "forgero:oak-handle",
        "component_type": "forgero:extensible_part",
        "upgrades": {
          "slots": [
            { "id": "forgero:grip_slot", "type": "forgero:grip", "content": null }
          ]
        },
        "cof_version": 1
      }
    }
  },
  "upgrades": {
    "slots": [
      { "id": "forgero:binding_slot", "type": "forgero:binding", "content": null }
    ]
  },
  "cof_version": 1
}
```

Key fields in the mutated format:

id: The unique identifier of the component.

component_type: A string key that maps to the component's Java class, used by ComponentConstructor to build the correct object type on deserialization.

properties: An object containing any properties that differ from the pristine version.

structure: An object detailing the required parts. Note how forgero:head is a pristine string ID, while the forgero:handle is another fully serialized object because it was mutated.

upgrades: A list detailing the optional upgrade slots. An empty slot has its content field as null.

cof_version: An integer indicating the schema version, ensuring backward compatibility.

## 4. Extensibility with ComponentConstructor

The COF module is designed to be extensible. Hardcoded logic for object construction is avoided by using the ComponentConstructor service. This service maps a component_type identifier from the JSON to a factory function that knows how to create the correct Component instance. Addon developers can register their own custom component types, allowing the core COF codec to serialize and deserialize new component classes without needing any direct knowledge of their implementation.

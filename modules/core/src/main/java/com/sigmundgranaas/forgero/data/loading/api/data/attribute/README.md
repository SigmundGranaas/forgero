# Attribute Data Loading

DTOs and codecs for loading attributes from JSON.

## Attribute Formats

### Traditional Format

Individual attribute objects with full specification:

```json
{
  "id": "forgero:iron-attack_speed",
  "type": "forgero:attack_speed",
  "computation": {"value": 2.0},
  "condition": [
    {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
    {"type": "forgero:has_other_contributor", "attribute_type": "forgero:attack_speed"}
  ]
}
```

### Batch Format

Compact syntax for multiple attributes sharing conditions:

```json
{
  "id_prefix": "forgero:iron",
  "condition": [
    {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
    {"type": "forgero:has_other_contributor"}
  ],
  "values": {
    "forgero:attack_speed": 2.0,
    "forgero:attack_damage": 4.0,
    "forgero:mining_speed": 6.0
  }
}
```

**Expansion:** Each value becomes a separate `AttributeData` with:
- ID: `{id_prefix}-{attribute_name}`
- Type: The key from values map
- Condition: Cloned from batch, with `attribute_type` auto-injected into `has_other_contributor`
- Computation: Value from map or override

## Data Classes

### AttributeData

```java
public interface AttributeData {
    Optional<OpenIdentifier> id();
    OpenIdentifier type();
    ComputationData computation();
    Optional<Condition> condition();
}
```

DTO for a single attribute. Created either from:
- Traditional JSON parsing
- Batch expansion

### AttributeBatchData

```java
public interface AttributeBatchData {
    Optional<String> idPrefix();
    Optional<Condition> condition();
    Optional<ComputationData> computation();
    Map<String, AttributeValueData> values();
}
```

DTO for a batch of attributes. Expanded into multiple `AttributeData` instances during parsing.

**Fields:**
- `idPrefix`: Template for generated IDs (`{prefix}-{name}`)
- `condition`: Shared condition for all attributes
- `computation`: Default operator/order (value comes from values map)
- `values`: Map of attribute types to values

### AttributeValueData

```java
public record AttributeValueData(
    float value,
    Optional<ComputationData> computationOverride
) {}
```

Represents either a simple float or full computation override.

**Factory methods:**
```java
AttributeValueData.simple(2.0f)  // Just a value
AttributeValueData.withComputation(computation)  // Full override
```

### ComputationData

```java
public record ComputationData(
    float value,
    String operator,
    String order
) {}
```

Computation specification.

**Operators:** `forgero:addition`, `forgero:multiplication`, `forgero:subtraction`, `forgero:division`

**Orders:** `forgero:base`, `forgero:middle`, `forgero:end`

## JSON Usage

### In Materials

```json
{
  "type": "forgero:material",
  "name": "Iron",

  "attributes": [ /* traditional */ ],
  "attribute_batches": [ /* compact */ ],

  "local_attributes": [ /* traditional */ ],
  "local_attribute_batches": [ /* compact */ ]
}
```

Both formats are merged during parsing.

### Batch Examples

**Basic:**
```json
{
  "attribute_batches": [
    {
      "values": {
        "forgero:attack_speed": 2.0,
        "forgero:attack_damage": 4.0
      }
    }
  ]
}
```

**With conditions:**
```json
{
  "attribute_batches": [
    {
      "id_prefix": "forgero:iron",
      "condition": {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
      "values": {
        "forgero:attack_speed": 2.0
      }
    }
  ]
}
```

**Custom operators:**
```json
{
  "attribute_batches": [
    {
      "id_prefix": "forgero:sharpness",
      "computation": {
        "operator": "forgero:multiplication",
        "order": "forgero:end"
      },
      "values": {
        "forgero:attack_damage": 1.15,
        "forgero:mining_speed": 1.05
      }
    }
  ]
}
```

**Per-value overrides:**
```json
{
  "attribute_batches": [
    {
      "computation": {
        "operator": "forgero:multiplication",
        "order": "forgero:end"
      },
      "values": {
        "forgero:attack_damage": 1.2,
        "forgero:rarity": {
          "value": 5.0,
          "operator": "forgero:addition",
          "order": "forgero:base"
        }
      }
    }
  ]
}
```

## Auto-Injection

`has_other_contributor` conditions without `attribute_type` get it auto-injected:

**Input:**
```json
{
  "condition": [{"type": "forgero:has_other_contributor"}],
  "values": {
    "forgero:attack_speed": 2.0,
    "forgero:durability": 240.0
  }
}
```

**Expanded:**
```json
[
  {
    "type": "forgero:attack_speed",
    "condition": [
      {"type": "forgero:has_other_contributor", "attribute_type": "forgero:attack_speed"}
    ]
  },
  {
    "type": "forgero:durability",
    "condition": [
      {"type": "forgero:has_other_contributor", "attribute_type": "forgero:durability"}
    ]
  }
]
```

## Codec API

### AttributeCodecs

```java
// Single attribute codec
Codec<AttributeData> create(Codec<Condition> conditionCodec)
```

### AttributeBatchCodecs

```java
// Batch list codec (expands batches into AttributeData list)
Codec<List<AttributeData>> createBatchListCodec(Codec<Condition> conditionCodec)
```

### ResourceDataCodec

```java
// Material/shape/schematic codec with batch support
Codec<ResourceData> createWithBatchSupport(
    Codec<Condition> conditionCodec,
    Codec<List<UpgradeSlotData>> upgradeSlotCodec
)
```

Reads both `attributes`/`attribute_batches` and merges results.

## Benefits

**Space savings:** 85-90% reduction for materials with many attributes

**Tool materials:** 213 lines → 30 lines

**Complex upgrades:** 369 lines → 40 lines

**Backward compatible:** Traditional format still works

**Type safe:** Full codec validation

## See Also

- [Attribute System](../../../../core/attribute/README.md) - Runtime attribute handling
- [Data Pipeline](../../../readme.md) - Overall data loading architecture
- Tests: `AttributeBatchCodecTest.java` - Usage examples

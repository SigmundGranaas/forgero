# Forgero Resource Definition Best Practices

This document covers essential patterns for defining materials, properties, and attributes in Forgero's JSON data files.

## Properties Format

Properties must use a **map format** with the property type as key, not an array format.

### Correct (Map Format)
```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:fire", "duration": 5 }
        ],
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:upgrade_material"
        }
      }
    ]
  }
}
```

### Incorrect (Array Format) - DO NOT USE
```json
{
  "properties": [
    {
      "type": "minecraft:on_hit",
      "selector": { "type": "forgero:single_target" },
      "effects": [
        { "type": "forgero:fire", "duration": 5 }
      ]
    }
  ]
}
```

The array format will silently fail to parse, resulting in no properties being applied.

## Upgrade Material Tags

For a material to be installable in upgrade slots, it needs the correct tags:

### Required Tags for Upgrade Materials
```json
{
  "tags": [
    "forgero:materials/upgrade_material",  // Categorization
    "forgero:upgrade_material",            // Marks as upgrade material
    "forgero:upgrade/binding"              // Slot compatibility (binding slots)
  ]
}
```

### Available Slot Compatibility Tags
- `forgero:upgrade/binding` - Install in binding slots
- `forgero:upgrade/gem` - Install in gem slots
- `forgero:upgrade/grip` - Install in grip slots

Without the appropriate slot tag, `canInstallUpgrade()` returns false.

## Attribute Context

Use `context` instead of `has_other_contributor` conditions for attribute composition.

### Recommended Pattern
```json
{
  "id": "forgero:iron-attack_damage",
  "type": "forgero:attack_damage",
  "computation": { "value": 4.0 },
  "context": "forgero:part-composite"
}
```

### Available Contexts

| Context | When Applied |
|---------|--------------|
| `forgero:part-composite` | Material + schematic intersection composition (recommended for materials) |
| `forgero:local` | Only applies to the component itself |
| `forgero:upgrade` | Only applies when installed as upgrade |

## Property Conditions

Use the `in_slot_type` condition to restrict when properties apply:

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:status_effect", "effect": "minecraft:slowness", "duration": 60, "amplifier": 1 }
        ],
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:upgrade_material"
        }
      }
    ]
  }
}
```

This ensures the on-hit effect only triggers when the material is installed in an upgrade slot, not when used as primary material.

## Complete Upgrade Material Example

```json
{
  "type": "forgero:material",
  "name": "Slimeball",
  "tags": [
    "forgero:materials/soft",
    "forgero:materials/upgrade_material",
    "forgero:upgrade_material",
    "forgero:upgrade/binding"
  ],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:slime_ball" }
    ]
  },
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          { "type": "forgero:status_effect", "effect": "minecraft:slowness", "duration": 60, "amplifier": 1 }
        ],
        "condition": {
          "type": "forgero:in_slot_type",
          "slot_type": "forgero:upgrade_material"
        }
      }
    ]
  },
  "attributes": [
    {
      "id": "forgero:slime-durability-upgrade",
      "type": "forgero:durability",
      "computation": { "value": 105 },
      "context": "forgero:part-composite"
    }
  ]
}
```

## Equipment Template Properties

Equipment templates (tools, weapons) also use the map format for properties:

```json
{
  "type": "forgero:equipment_template",
  "name": "hammer",
  "properties": {
    "minecraft:block_breaking": [
      {
        "selector": {
          "type": "forgero:pattern",
          "pattern": ["XXX", "XXX", "XXX"],
          "filter": "forgero:can_mine"
        },
        "speed": "forgero:all",
        "predicate": { "type": "forgero:can_mine" },
        "title": "feature.forgero.pattern_mining.title",
        "description": "feature.forgero.pattern_mining.3x3-description"
      }
    ]
  }
}
```

## Common Mistakes

1. **Using array format for properties** - Properties are silently ignored
2. **Missing slot compatibility tags** - Material cannot be installed
3. **Using `has_other_contributor` condition** - Deprecated, use `context` instead
4. **Duplicate resource IDs across modules** - Only one definition loads
5. **Missing `in_slot_type` condition on properties** - Effects trigger when material is used as primary, not just upgrade
6. **Using array format in equipment templates** - Block breaking properties are ignored

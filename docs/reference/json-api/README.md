# JSON API Reference

Complete reference for Forgero's JSON data formats.

## Resource Types

| Type | File Pattern | Description |
|------|--------------|-------------|
| `forgero:material` | `materials/*.json` | Tool/armor materials |
| `forgero:part_template` | `parts/**/*.json` | Part definitions (heads, handles) |
| `forgero:equipment_template` | `equipment/**/*.json` | Tool/weapon definitions |
| `forgero:schematic` | `schematics/**/*.json` | Quality variants |
| `forgero:static_part` | `parts/**/*.json` | External item integration |

## Common Fields

All resources share these fields:

```json
{
  "type": "forgero:material",     // Resource type (required)
  "name": "Iron",                 // Display name (required)
  "tags": ["forgero:materials/metal"],  // Categorization tags
  "attributes": [...],            // Numeric attributes
  "properties": {...}             // Behaviors (effects, etc.)
}
```

## Attributes

Attributes define numeric properties:

```json
{
  "id": "forgero:iron-durability",           // Unique ID
  "type": "forgero:durability",              // Attribute type
  "computation": {"value": 250},             // Value and operation
  "context": "forgero:part-composite"        // When to apply
}
```

**Common attribute types:**
- `forgero:durability` - Tool durability
- `forgero:attack_damage` - Weapon damage
- `forgero:attack_speed` - Attack speed modifier
- `forgero:mining_speed` - Block breaking speed
- `forgero:mining_level` - Harvest level

## Properties

Properties define behaviors using a map format:

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": {"type": "forgero:single_target"},
        "effects": [
          {"type": "forgero:fire", "duration": 5}
        ]
      }
    ]
  }
}
```

**Property types:**
- `minecraft:on_hit` - Effects when hitting entities
- `minecraft:on_tick` - Periodic effects while held
- `minecraft:block_breaking` - Block breaking patterns

## Tags

Tags categorize and group resources:

```json
{
  "tags": [
    "forgero:materials/metal",        // Category
    "forgero:upgrade_material",       // Role marker
    "forgero:upgrade/binding"         // Slot compatibility
  ]
}
```

## See Also

- [Quick Reference](../quick-reference.md) - Cheat sheet
- [Best Practices](../../guides/best-practices.md) - Common patterns
- [Creating Content Packs](../../guides/creating-content-packs.md) - Full tutorial

# Reference Documentation

Complete reference for all Forgero JSON types and APIs.

## Quick Reference

- **[Quick Reference](quick-reference.md)** - Cheat sheet for common types and fields

## JSON API Reference

Detailed documentation for each JSON type:

| Type | Description | File |
|------|-------------|------|
| Materials | Define tool materials (iron, diamond, etc.) | `json-api/materials.md` |
| Parts | Part templates (heads, handles, blades) | `json-api/parts.md` |
| Equipment | Tool/weapon templates (pickaxe, sword) | `json-api/equipment.md` |
| Schematics | Quality variants (refined, mastercrafted) | `json-api/schematics.md` |
| Attributes | Numeric properties (durability, damage) | `json-api/attributes.md` |
| Properties | Behaviors (on_hit effects, block breaking) | `json-api/properties.md` |
| Tags | Categorization and grouping | `json-api/tags.md` |

*Note: Individual JSON API pages are being created. See [Quick Reference](quick-reference.md) for current documentation.*

## File Locations

Content pack resources go in:
```
data/forgero/
├── materials/       # Material definitions
├── parts/           # Part templates
├── equipment/       # Equipment templates
├── schematics/      # Schematic definitions
├── tags/            # Tag definitions
└── recipe_generators/  # Recipe templates
```

## See Also

- [Architecture Overview](../architecture/README.md) - How the systems work
- [Best Practices](../guides/best-practices.md) - Common patterns

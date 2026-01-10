# Guides

Step-by-step tutorials for creating Forgero content.

## Available Guides

### Getting Started
- **[Creating Content Packs](creating-content-packs.md)** - Comprehensive guide to JSON content creation
- **[Best Practices](best-practices.md)** - Common patterns and pitfalls to avoid

### Content Creation
- Creating Materials *(coming soon)*
- Creating Tools *(coming soon)*
- Creating Properties *(coming soon)*

## Prerequisites

Before creating content, you should understand:
- Basic JSON syntax
- Minecraft modding concepts (items, recipes, tags)
- [Forgero's tag system](../architecture/tag-system.md)

## Quick Example

Create a simple material in `data/forgero/materials/my_material.json`:

```json
{
  "type": "forgero:material",
  "name": "My Material",
  "tags": ["forgero:materials/metal"],
  "host": {
    "identifiers": [{"type": "item", "id": "minecraft:iron_ingot"}]
  },
  "attributes": [
    {
      "id": "my_mod:my_material-durability",
      "type": "forgero:durability",
      "computation": {"value": 500}
    }
  ]
}
```

See [Creating Content Packs](creating-content-packs.md) for the full guide.

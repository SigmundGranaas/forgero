# Forgero Documentation

Welcome to the Forgero documentation. Find guides, references, and architecture docs for creating custom tools, weapons, and armor.

## Quick Links

| I want to... | Go to... |
|--------------|----------|
| Create a content pack | [Getting Started Guide](guides/README.md) |
| Look up JSON format | [Quick Reference](reference/quick-reference.md) |
| Understand how it works | [Architecture Overview](architecture/README.md) |
| Contribute code | [Contributing Guide](contributing/README.md) |

---

## By Audience

### Content Pack Creators

Creating custom materials, tools, or weapons using JSON:

1. **[Guides](guides/README.md)** - Step-by-step tutorials
   - [Creating Content Packs](guides/creating-content-packs.md) - Comprehensive guide
   - [Modding API](guides/modding-api.md) - Building on Forgero from Java (read/mutate items, discovery, conditions, effects)
   - [Best Practices](guides/best-practices.md) - Patterns and pitfalls

2. **[Reference](reference/README.md)** - Complete JSON documentation
   - [Quick Reference](reference/quick-reference.md) - Cheat sheet
   - [JSON API](reference/json-api/README.md) - Detailed type reference

### Mod Developers

Extending Forgero with Java code:

1. **[Architecture](architecture/README.md)** - System design
   - [Attribute Resolution](architecture/attribute-resolution.md) - How attributes compute
   - [Tag System](architecture/tag-system.md) - Tag graph design
   - [Data Pipeline](architecture/data-pipeline.md) - Content loading

2. **[Contributing](contributing/README.md)** - Development setup
   - [Testing Guide](contributing/testing.md) - GameTest patterns

### Project Contributors

1. **[Contributing](contributing/README.md)** - How to contribute
2. **[Documentation Standards](contributing/documentation-standards.md)** - Writing docs
3. **[Project Status](status/README.md)** - Ongoing work tracking

---

## Documentation Structure

```
docs/
├── guides/           # How-to tutorials
├── reference/        # JSON API documentation
│   └── json-api/     # Detailed type references
├── architecture/     # System design docs
├── contributing/     # Contributor resources
└── status/           # Project tracking (temporary)
```

---

## Project Status

| Area | Progress | Details |
|------|----------|---------|
| Content Migration | ~80% | [status/content-migration.md](status/content-migration.md) |
| Vanilla Upgrades | ~67% | [status/vanilla-upgrades.md](status/vanilla-upgrades.md) |
| Model Validation | ~60% | [status/model-validation.md](status/model-validation.md) |

---

## External Resources

- **[GitHub Repository](https://github.com/sigmundgranaas/forgero)** - Source code
- **[Discord](https://discord.gg/3vK7ZwEDex)** - Community support
- **[CurseForge](https://www.curseforge.com/minecraft/mc-mods/forgero)** - Downloads
- **[Wiki](https://github.com/sigmundgranaas/forgero/wiki)** - Additional documentation

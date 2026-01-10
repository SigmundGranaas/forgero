# Architecture Documentation

Understanding how Forgero's systems work internally.

## Core Systems

| System | Description | Documentation |
|--------|-------------|---------------|
| **Attribute Resolution** | How numeric attributes are computed through the component hierarchy | [attribute-resolution.md](attribute-resolution.md) |
| **Tag System** | Graph-based tag inheritance and querying | [tag-system.md](tag-system.md) |
| **Data Pipeline** | How JSON content is loaded and processed | [data-pipeline.md](data-pipeline.md) |

## Key Concepts

### Component Hierarchy

Everything in Forgero is a **Component**:
- Materials, parts, tools, and upgrades are all components
- Components compose into hierarchies (tool → parts → materials)
- Properties and attributes flow through this hierarchy

### Attribute Computation

Attributes (durability, damage, etc.) are computed through a pipeline:
1. **Collection** - Gather attributes from all components
2. **Context filtering** - Apply context conditions (`part-composite`, `upgrade`, etc.)
3. **Computation** - Execute operations (addition, multiplication)
4. **Caching** - Store results for performance

See [Attribute Resolution](attribute-resolution.md) for details.

### Tag Graph

Tags form a directed acyclic graph (DAG):
- Tags can inherit from parent tags
- Queries traverse the graph for membership
- Used for slot compatibility, filtering, and categorization

See [Tag System](tag-system.md) for details.

## Module Structure

```
modules/
├── core/           # Platform-agnostic core (no MC dependencies)
├── model/          # Model/texture generation
└── mc/
    ├── loader/     # Data loading and plugin system
    ├── properties/ # Effect handlers
    ├── tools/      # Tool implementations
    ├── armor/      # Armor implementations
    └── bows/       # Bow/arrow implementations
```

## See Also

- [Reference Documentation](../reference/README.md) - JSON API details
- [Contributing](../contributing/README.md) - Development setup

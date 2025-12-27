# Forgero Tag System

## Overview

The Forgero tag system provides a hierarchical categorization mechanism for game resources. Tags form a DAG (Directed Acyclic Graph) where child tags inherit from parent tags, enabling flexible queries and model resolution.

## Core Concepts

### Tags

Tags are namespaced identifiers following the format `namespace:path`:
- Use **snake_case** for naming: `forgero:iron`, `forgero:weapon_head`
- Namespace is required: `forgero:material`, `minecraft:pickaxe`
- Tags are case-sensitive

### Inheritance

Tags can inherit from one or more parent tags:
```
iron → metal → material
     ↘
       mineable
```

Items tagged with `iron` automatically match queries for `metal`, `material`, and `mineable`.

### Core Interfaces

| Interface | Purpose |
|-----------|---------|
| `TagResolver` | Query tag relationships and check if items have tags |
| `Taggable` | Interface for items that can be tagged |
| `TaggedRegistry<T>` | Type-safe registry for storing and querying tagged resources |

## Tag Definition Files

Tags are defined in JSON files located at:
```
data/<namespace>/forgero/tags/<tag_name>.json
```

### File Format

```json
{
  "parent": "forgero:material"
}
```

Or with multiple parents:
```json
{
  "parents": ["forgero:metallic", "forgero:mineable"]
}
```

## API Usage

### Checking if an item has a tag

```java
TagResolver resolver = new TagGraphBuilder()
    .add(IRON, Set.of(METAL))
    .build();

Taggable item = () -> Set.of(IRON);
resolver.hasTag(item, METAL);  // true (inherited)
resolver.hasTag(item, IRON);   // true (direct)
```

### Using TaggedRegistry

```java
TaggedRegistry<Resource> registry = new TaggedRegistry.Builder<>(resolver)
    .add(ironPickaxe)
    .build();

// Find items with inherited tags
registry.findByTag(METAL);        // Returns ironPickaxe (via inheritance)

// Find items with direct tags only
registry.getDirectlyTagged(IRON); // Returns ironPickaxe
registry.getDirectlyTagged(METAL); // Returns empty (no direct METAL tag)
```

### Building a Tag Hierarchy

```java
TagGraphBuilder builder = new TagGraphBuilder();

// Define parent-child relationships
builder.add(IRON, Set.of(METAL, MINEABLE));
builder.add(GOLD, Set.of(METAL, MINEABLE));
builder.add(METAL, Set.of(MATERIAL));

TagResolver resolver = builder.build();
```

## Design Rules

1. **Immutability**: All tag structures are immutable after construction
2. **No Cycles**: Tag hierarchy must be acyclic (validated on build, throws `IllegalStateException`)
3. **Case Sensitivity**: Tags are case-sensitive; use snake_case consistently
4. **Namespace Required**: All tags must have a namespace prefix
5. **Validation**: Resources added to `TaggedRegistry` must only reference known tags

## Model Predicates

Tag predicates in model definitions support inheritance-aware matching:

```json
{
  "predicate": [
    {
      "type": "forgero:root_tag",
      "tag": "forgero:metal"
    }
  ]
}
```

This predicate matches components tagged with `iron`, `gold`, etc. (children of `metal`).

## Plugin Extension

Plugins can add custom tags via `TagSource`:

```java
public class MyPlugin implements ForgeroPlugin {
    @Override
    public void registerTags(TagSourceRegistry registry) {
        registry.addSource(() -> Map.of(
            "mymod:custom_material", "{ \"parent\": \"forgero:material\" }"
        ));
    }
}
```

## Package Structure

```
com.sigmundgranaas.forgero.common.tags/
├── api/
│   ├── TagResolver.java      # Main query interface
│   └── Taggable.java         # Interface for taggable items
└── engine/
    ├── TagGraph.java         # Default TagResolver implementation
    ├── TagGraphBuilder.java  # Builder for creating tag hierarchies
    ├── TaggedRegistry.java   # Type-safe registry for tagged resources
    ├── TagLoader.java        # Loads tags from TagSource
    ├── TagParser.java        # Parses tag JSON files
    └── TagLoadingService.java # Service for loading tags from resources
```

## Error Handling

| Error | Cause |
|-------|-------|
| `IllegalStateException` | Cycle detected in tag hierarchy |
| `IllegalArgumentException` | Invalid tag identifier format |
| `IllegalArgumentException` | Resource references unknown tag |

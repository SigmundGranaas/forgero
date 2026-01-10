# Forgero Graph-Based Tag System Architecture

This document describes the composition-based tag system used by the rearchitected Forgero and Vanilla Upgrades mods.

## Core Design Philosophy

The tag system implements a **Directed Acyclic Graph (DAG)** that enables:
1. **Composition-based inheritance** - Tags inherit from parent tags via BFS traversal
2. **Multi-dimensional classification** - Components can belong to multiple orthogonal hierarchies
3. **Slot validation** - Tags determine what can go where
4. **Conditional behavior** - Properties apply based on tag membership

---

## Tag Graph Visualization

```
                          +----------------------------------------------------------------------+
                          |                     ROOT TAGS (No Parents)                           |
                          +----------------------------------------------------------------------+
                                    |                    |                    |
         +--------------------------+--------------------+--------------------+--------------------------+
         |                          |                    |                    |                          |
         v                          v                    v                    v                          v
+-----------------+       +-----------------+   +-------------+   +-----------------+       +-----------------+
|  forgero:parts  |       |forgero:materials|   |forgero:tools|   |forgero:upgrades |       |forgero:weapons  |
|   (root tag)    |       |   (root tag)    |   |  (root tag) |   |   (root tag)    |       |   (root tag)    |
+--------+--------+       +--------+--------+   +------+------+   +--------+--------+       +--------+--------+
         |                         |                   |                   |                         |
    +----+----+----+----+     +----+----+----+    +----+----+         +----+----+             +------+------+
    |         |    |    |     |         |    |    |         |         |         |             |             |
    v         v    v    v     v         v    v    v         v         v         v             v             v
+-------+ +-----++----++----++----+ +-----++----++----+           +--------+           +-----------+ +----------+
|types  | |catg ||shap||defs||types| |roles||prop||types|          | types  |           | types     | |categories|
+---+---+ +--+--++--+-++--+-++--+--+ +--+--++--+-++--+--+          +---+----+           +-----+-----+ +----+-----+
    |        |      |     |     |       |      |     |                 |                     |            |
```

---

## Detailed Hierarchy Trees

### 1. PARTS Hierarchy
```
forgero:parts (ROOT)
|-- forgero:parts/categories
|   |-- forgero:parts/categories/head      <- pickaxe_head, axe_head, etc inherit this
|   |-- forgero:parts/categories/handle    <- wooden_handle, iron_handle inherit this
|   |-- forgero:parts/categories/blade     <- sword_blade, dagger_blade inherit this
|   +-- forgero:parts/categories/guard     <- sword_guard inherits this
|
|-- forgero:parts/types                    <- Specific part implementations
|   |-- forgero:parts/types/pickaxe_head
|   |-- forgero:parts/types/axe_head
|   |-- forgero:parts/types/shovel_head
|   |-- forgero:parts/types/hoe_head
|   |-- forgero:parts/types/sword_blade
|   |-- forgero:parts/types/handle_type
|   |-- forgero:parts/types/binding_type
|   +-- ... (all specific part types)
|
|-- forgero:parts/shapes                   <- Shape templates
|   |-- forgero:parts/shapes/pickaxe_head_shape
|   |-- forgero:parts/shapes/armor_plate_shape
|   +-- forgero:parts/shapes/armor_lining_shape
|
+-- forgero:parts/defaults                 <- Default part selections
    |-- forgero:parts/defaults/default_handle
    |-- forgero:parts/defaults/default_pickaxe_head
    |-- forgero:parts/defaults/default_binding
    +-- forgero:parts/defaults/default_armor_plate
```

### 2. MATERIALS Hierarchy
```
forgero:materials (ROOT)
|-- forgero:materials/types                <- Material substance classification
|   |-- forgero:materials/types/metal      <- iron, gold, copper, netherite
|   |-- forgero:materials/types/wood       <- oak, birch, spruce, etc.
|   |-- forgero:materials/types/stone      <- cobblestone, granite, etc.
|   |-- forgero:materials/types/mineral    <- diamond, emerald, amethyst
|   |-- forgero:materials/types/bone
|   |-- forgero:materials/types/glass
|   |-- forgero:materials/types/leather
|   |-- forgero:materials/types/binding    <- string, vines, etc.
|   |-- forgero:materials/types/dye        <- coloring materials
|   +-- forgero:materials/types/plant
|
|-- forgero:materials/roles                <- What materials can DO
|   |-- forgero:materials/roles/tool_material      <- Can make tool heads
|   |-- forgero:materials/roles/armor_material     <- Can make armor plates
|   |-- forgero:materials/roles/armor_lining_material
|   |-- forgero:materials/roles/upgrade_material   <- Can be used as upgrades
|   +-- forgero:materials/roles/secondary          <- Secondary components
|
+-- forgero:materials/properties           <- Material characteristics
    |-- forgero:materials/properties/hard
    |-- forgero:materials/properties/soft
    |-- forgero:materials/properties/hybrid
    +-- forgero:materials/properties/magical
```

### 3. TOOLS Hierarchy
```
forgero:tools (ROOT)
+-- forgero:tools/types
    |-- forgero:tools/types/pickaxe
    |-- forgero:tools/types/axe
    |-- forgero:tools/types/shovel
    |-- forgero:tools/types/hoe
    |-- forgero:tools/types/sword
    |-- forgero:tools/types/hammer
    |-- forgero:tools/types/mandrill_pickaxe
    |-- forgero:tools/types/felling_axe
    +-- forgero:tools/types/spade
```

### 4. WEAPONS Hierarchy
```
forgero:weapons (ROOT)
|-- forgero:weapons/types
|   |-- forgero:weapons/types/sword
|   |-- forgero:weapons/types/dagger
|   |-- forgero:weapons/types/katana
|   |-- forgero:weapons/types/rapier
|   |-- forgero:weapons/types/broadsword
|   |-- forgero:weapons/types/cutlass
|   |-- forgero:weapons/types/scythe
|   |-- forgero:weapons/types/knife
|   |-- forgero:weapons/types/kunai
|   |-- forgero:weapons/types/battle_axe
|   |-- forgero:weapons/types/mace
|   |-- forgero:weapons/types/club
|   +-- forgero:weapons/types/blunt
|
+-- forgero:weapons/categories
    +-- forgero:weapons/categories/extended
```

### 5. UPGRADES Hierarchy
```
forgero:upgrades (ROOT)
+-- forgero:upgrades/types
    |-- forgero:upgrades/types/gem
    |-- forgero:upgrades/types/reinforcement
    |-- forgero:upgrades/types/tip_reinforcement
    |-- forgero:upgrades/types/cosmetic
    |-- forgero:upgrades/types/grip
    |-- forgero:upgrades/types/guard
    +-- forgero:upgrades/types/pommel
```

### 6. ARMOR Hierarchy
```
forgero:armor/armors (ROOT)
+-- forgero:armor/types
    |-- forgero:armor/types/helmet
    |-- forgero:armor/types/chestplate
    |-- forgero:armor/types/leggings
    +-- forgero:armor/types/boots
```

### 7. SCHEMATICS Hierarchy
```
forgero:schematics (ROOT)
+-- forgero:schematics/categories
    |-- forgero:schematics/categories/heads
    |-- forgero:schematics/categories/handles
    |-- forgero:schematics/categories/blades
    |-- forgero:schematics/categories/bindings
    |-- forgero:schematics/categories/refined
    |-- forgero:schematics/categories/mastercrafted
    +-- forgero:schematics/categories/mining
```

---

## How Tags Enable Composition

### Slot Validation via Tags

```
+---------------------------------------------------------------------------------+
|                            PICKAXE EQUIPMENT TEMPLATE                           |
|                                                                                 |
|  tags: ["forgero:tools/pickaxe", "forgero:tools"]                               |
|                                                                                 |
|  +-------------------------------------------------------------------------+   |
|  | SLOT: head                                                               |   |
|  |   type: "forgero:pickaxe_head"                                          |   |
|  |   default_tag: "forgero:parts/pickaxe_head"  <- What can fill this slot |   |
|  |                                                                          |   |
|  |   +--------------------------------------------------------------+      |   |
|  |   | ACCEPTS: Any component with tag                              |      |   |
|  |   |   "forgero:parts/pickaxe_head"                               |      |   |
|  |   |   OR tags inheriting FROM it                                 |      |   |
|  |   +--------------------------------------------------------------+      |   |
|  +-------------------------------------------------------------------------+   |
|                                                                                 |
|  +-------------------------------------------------------------------------+   |
|  | SLOT: handle                                                             |   |
|  |   type: "forgero:parts/categories/handle"                               |   |
|  |   default: "forgero:wooden_handle"                                       |   |
|  |                                                                          |   |
|  |   +--------------------------------------------------------------+      |   |
|  |   | ACCEPTS: Any component tagged as handle                      |      |   |
|  |   |   (wood, bone, any material with handle capability)          |      |   |
|  |   +--------------------------------------------------------------+      |   |
|  +-------------------------------------------------------------------------+   |
|                                                                                 |
|  +-------------------------------------------------------------------------+   |
|  | UPGRADE SLOT: binding                                                    |   |
|  |   type: "forgero:materials/roles/upgrade_material"                       |   |
|  |   tags: ["forgero:materials/types/binding"]  <- Filter to binding types |   |
|  |                                                                          |   |
|  |   +--------------------------------------------------------------+      |   |
|  |   | ACCEPTS: Materials that are:                                 |      |   |
|  |   |   1. upgrade_material role  AND                              |      |   |
|  |   |   2. binding type                                            |      |   |
|  |   +--------------------------------------------------------------+      |   |
|  +-------------------------------------------------------------------------+   |
+---------------------------------------------------------------------------------+
```

### Multi-Dimensional Material Tagging

A material like **Iron** has multiple tag memberships across different dimensions:

```
                                  IRON MATERIAL
                                       |
            +--------------------------+---------------------------+
            |                          |                           |
            v                          v                           v
    +---------------+          +---------------+           +---------------+
    |  TYPE: metal  |          |ROLE: tool_mat |           | PROP: hard    |
    |               |          |               |           |               |
    | "What is it?" |          |"What can it   |           |"How does it   |
    |               |          | make?"        |           | behave?"      |
    +---------------+          +---------------+           +---------------+
            |                          |                           |
            v                          v                           v
    Can match queries      Can fill slots for          Conditional effects
    like "all metals"      tool_material roles         apply to "hard" items
```

**Iron's tags in JSON:**
```json
"tags": [
  "forgero:materials/types/metal",           // Type classification
  "forgero:materials/roles/tool_material",   // Role - can make tools
  "forgero:materials/properties/hard",       // Property - is hard
  "forgero:upgrades/types/tip_reinforcement" // Also an upgrade type
]
```

---

## Core Implementation

### Key Classes

| Class | Location | Purpose |
|-------|----------|---------|
| `TagResolver` | `modules/core/.../tags/api/` | Interface for tag queries |
| `TagGraph` | `modules/core/.../tags/engine/` | DAG implementation with BFS traversal |
| `TagGraphBuilder` | `modules/core/.../tags/engine/` | Builder with cycle detection |
| `TagLoadingService` | `modules/core/.../tags/engine/` | Loads tags from JSON resources |
| `TaggedRegistry` | `modules/core/.../tags/engine/` | Registry with tag-based lookups |
| `Taggable` | `modules/core/.../tags/api/` | Interface for taggable items |

### TagResolver BFS Algorithm

```
Query: resolver.hasTag(iron_pickaxe_head, "forgero:parts")

                 START
                   |
                   v
    +-----------------------------------------+
    | iron_pickaxe_head direct tags:          |
    | - forgero:parts/head                    |
    | - forgero:parts/pickaxe_head            |
    +-----------------------------------------+
                   |
            BFS Queue:
    [parts/head, parts/pickaxe_head]
                   |
                   v
    +-----------------------------------------+
    | Check: parts/head == parts?             | NO
    | Parents of parts/head:                  |
    |   -> forgero:parts/categories           |
    +-----------------------------------------+
                   |
            Queue: [parts/pickaxe_head, parts/categories]
                   |
                   v
    +-----------------------------------------+
    | Check: parts/pickaxe_head?              | NO
    | Parents of parts/pickaxe_head:          |
    |   -> forgero:parts/types                |
    +-----------------------------------------+
                   |
            Queue: [parts/categories, parts/types]
                   |
                   v
    +-----------------------------------------+
    | Check: parts/categories?                | NO
    | Parents of parts/categories:            |
    |   -> forgero:parts                      | <- TARGET!
    +-----------------------------------------+
                   |
                   v
              MATCH FOUND!
              Return TRUE
```

### Data Structures

```java
public class TagGraph implements TagResolver {
    // Child -> Parents mapping (for upward BFS traversal)
    private final Map<OpenIdentifier, Set<OpenIdentifier>> parentRelationships;

    // Parent -> Children mapping (pre-computed for fast descendant queries)
    private final Map<OpenIdentifier, Set<OpenIdentifier>> childRelationships;
}
```

---

## JSON File Structure

### File Path Convention

```
data/forgero/tags/{domain}/{subdomain}/{item}.json

Examples:
  tags/parts/types/pickaxe_head.json     -> forgero:parts/types/pickaxe_head
  tags/materials/roles/tool_material.json -> forgero:materials/roles/tool_material
  tags/tools/types/pickaxe.json          -> forgero:tools/types/pickaxe
```

### Tag Definition Format

**Root tag (no parents):**
```json
{
  "description": "Root tag for all part components"
}
```

**Child tag with inheritance:**
```json
{
  "parents": [
    "forgero:parts/categories"
  ],
  "description": "Parts head"
}
```

**Single parent shorthand:**
```json
{
  "parent": "forgero:materials/types",
  "description": "Materials metal"
}
```

### Component Tag Application

**Part template:**
```json
{
  "type": "forgero:part_template",
  "name": "pickaxe_head",
  "tags": [
    "forgero:parts/head",
    "forgero:parts/pickaxe_head"
  ],
  "structure": {
    "slots": {
      "material": {
        "type": "forgero:tool_material",
        "default_tag": "forgero:tool_material"
      }
    }
  }
}
```

**Material:**
```json
{
  "type": "forgero:material",
  "name": "Iron",
  "tags": [
    "forgero:materials/types/metal",
    "forgero:materials/roles/tool_material",
    "forgero:materials/properties/hard",
    "forgero:upgrades/types/tip_reinforcement"
  ]
}
```

---

## Key Design Goals

| Goal | Implementation |
|------|----------------|
| **Extensibility** | New tags can inherit from existing ones without code changes |
| **Multi-classification** | Items belong to multiple hierarchies (type, role, property) |
| **Slot compatibility** | Tags define what fits where - no hardcoded rules |
| **Conditional behavior** | Properties activate based on tag membership |
| **Data-driven** | All relationships defined in JSON, not code |
| **Performance** | Pre-computed child relationships for O(1) descendant lookups |
| **Safety** | Cycle detection prevents infinite loops |
| **Merge-ability** | Multiple tag graphs can be combined from different content packs |

---

## Test Coverage

The tag system has comprehensive test coverage:

### Unit Tests (`modules/core/src/test/java/`)

| Test Class | Coverage |
|------------|----------|
| `TagGraphTest` | Core graph traversal, inheritance, filtering, direct vs inherited |
| `TagGraphBuilderTest` | Builder pattern, direct cycle detection, transitive cycle detection |
| `TagLoadingServiceTest` | Loading from classpath resources, path resolution, hierarchy building |
| `TagLoaderTest` | JSON parsing, single/multi-parent, cycle propagation, identifier validation |
| `TaggedRegistryTest` | Registry with tag validation, direct vs inherited lookups, duplicate detection |

### Integration Tests (`fabric/forgero-fabric-core/src/test/java/`)

| Test Class | Coverage |
|------------|----------|
| `TagResolverIntegrationTest` | Full Minecraft integration with FabricGameTest |
| - `testEmptyResolverIsSingleton` | Null object pattern |
| - `testMergeWithEmpty` | Resolver merging |
| - `testTagInheritance` | Real component inheritance |
| - `testHasTagWithComponents` | Component tag queries |
| - `testFindTagged` | Filtering with inheritance |
| - `testFindDirectlyTagged` | Filtering without inheritance |
| - `testGetRelationships` | Hierarchy export |
| - `testFromRelationships` | Resolver recreation |

### Running Tests

```bash
# Run all tag-related tests
./gradlew :modules:core:test --tests "*Tag*"

# Run integration tests (requires Minecraft environment)
./gradlew :fabric:forgero-fabric-core:test
```

---

## Tag System Characteristics

| Aspect | Implementation |
|--------|-----------------|
| **Graph Type** | Directed Acyclic Graph (DAG) |
| **Traversal** | Breadth-First Search (BFS) |
| **Lookup Complexity** | O(n) BFS for inheritance, O(1) direct match |
| **Immutability** | Fully immutable after construction |
| **Thread Safety** | Safe for concurrent reads |
| **Merging** | Multiple resolvers can merge via `merge()` |
| **Cycle Detection** | DFS-based validation before graph creation |
| **Serialization** | JSON with Gson custom deserializer |
| **Platform Abstraction** | Via `ResourceProvider` interface |

---

## Content Pack Locations

Tags are defined in JSON files across content modules:

```
content/forgero-base/src/main/resources/data/forgero/tags/
    materials/
        materials.json          (root)
        types.json
        roles.json
        properties.json
        types/*.json            (metal, wood, stone, etc.)
        roles/*.json            (tool_material, armor_material, etc.)
        properties/*.json       (hard, soft, magical, etc.)
    parts/
        parts.json              (root)
        categories.json
        types.json
        shapes.json
        defaults.json
        categories/*.json       (head, handle, blade, etc.)
        types/*.json            (pickaxe_head, sword_blade, etc.)
    tools/
        tools.json              (root)
        types.json
        types/*.json            (pickaxe, axe, sword, etc.)
    upgrades/
        upgrades.json           (root)
        types.json
        types/*.json            (gem, reinforcement, etc.)
    schematics/
        categories.json
        categories/*.json       (heads, handles, refined, etc.)

content/forgero-armor-content/src/main/resources/data/forgero/tags/
    armor/
        armors.json             (root)
        types/*.json            (helmet, chestplate, etc.)

content/forgero-extended-weapons/src/main/resources/data/forgero/tags/
    weapons/
        weapons.json            (root)
        types/*.json            (dagger, katana, mace, etc.)
        categories/*.json       (extended, swordlike, etc.)
```

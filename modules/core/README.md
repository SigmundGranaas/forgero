# Forgero Core Module

Platform-agnostic foundation with **zero Minecraft dependencies**. Defines the component system, property resolution, and data pipeline.

## Key Concepts

**Components** are immutable, composable game objects. Everything is a Component - materials, parts, tools, upgrades.

**Properties** attach behaviors to components. Resolved dynamically from component hierarchies.

**Attributes** are numerical properties (attack damage, durability) with operators (ADD, MULTIPLY) and conditions.

**Tags** provide hierarchical categorization with inheritance.

## Package Structure

```
core/
├── component/     # Component interfaces and implementations
├── attribute/     # Numerical properties with computation
├── property/      # Property resolution framework
├── condition/     # Static and dynamic conditions
├── registry/      # Component storage and lookup
├── cof/           # Component Object Format (serialization)
├── data/          # JSON loading pipeline
└── model/         # 3D model definitions
```

## Component Hierarchy

```java
Component                         // Base interface
├── StructuredComponent           // Has required parts (blade + handle)
└── CustomizableComponent         // Has upgrade slots (gem sockets)
```

**Implementations:**

| Class | Structure | Upgrades | Example |
|-------|-----------|----------|---------|
| `StaticComponent` | No | No | Iron ingot, oak log |
| `ExtensiblePart` | No | Yes | Part with gem slot |
| `StructuredExtensibleEquipment` | Yes | Yes | Sword with parts + upgrade slots |

## Working with Components

```java
// Components are immutable - modifications create new instances
Component modified = component.with(builder ->
    builder.addProperty(attackDamage(5.0f))
);

// Check capabilities via instanceof
if (component instanceof CustomizableComponent cc) {
    List<ComponentUpgradeSlot> slots = cc.slots();
}

// Navigate structure
if (component instanceof StructuredComponent sc) {
    for (Component part : sc.parts()) {
        // Process each part
    }
}
```

## Attribute System

```java
// Default attributes
DefaultAttributes.ATTACK_DAMAGE.create(5.0f)
DefaultAttributes.DURABILITY.create(1000)
DefaultAttributes.MINING_SPEED.create(6.0f, Operator.MULTIPLICATION)

// Operators apply in order: ADD → MULTIPLY → MIN/MAX
```

## Conditions

**Static** (evaluated at load time): `tag_match`, `in_slot_type`, `is_root`, `at_depth`

**Dynamic** (evaluated at runtime): `random`, `weather`, `dimension`, `entity_type`

## Data Pipeline

JSON files in `data/forgero/` are processed through:
1. Raw loading → 2. Tag graph → 3. Static processing → 4. Template generation → 5. Component construction

## Detailed Documentation

- [Component System](src/main/java/com/sigmundgranaas/forgero/core/component/README.md)
- [Attribute System](src/main/java/com/sigmundgranaas/forgero/core/attribute/README.md)
- [COF Format](src/main/java/com/sigmundgranaas/forgero/cof/readme.md)
- [Data Pipeline](src/main/java/com/sigmundgranaas/forgero/data/readme.md)
- [Tag System](src/main/java/com/sigmundgranaas/forgero/common/tags/README.md)

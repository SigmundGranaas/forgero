# Component System

Everything in Forgero is a `Component` - materials, parts, tools, upgrades. Components are **immutable** and **composable**.

## Interface Hierarchy

```java
Component                              // Base: id, tags, properties
├── StructuredComponent                // Has required parts (structure)
└── CustomizableComponent              // Has optional upgrade slots
```

## Implementations

| Class | Structure | Upgrades | Use Case |
|-------|-----------|----------|----------|
| `StaticComponent` | No | No | Materials (iron, oak) |
| `ExtensiblePart` | No | Yes | Parts with gem slots |
| `StructuredPart` | Yes | No | Composite parts |
| `StructuredExtensiblePart` | Yes | Yes | Full-featured parts |
| `StructuredExtensibleEquipment` | Yes | Yes | Tools, weapons, armor |

## Component Interface

```java
public interface Component extends PropertyHolder, Taggable, Identifiable {
    OpenIdentifier id();              // "forgero:iron-sword"
    String name();                    // "Iron Sword"
    Set<String> tags();               // {"sword", "weapon", "iron"}
    Type type();                      // Type.SWORD
    List<Property> properties();      // All attached properties

    // Immutable modification
    Component with(Consumer<ComponentBuilder> modifier);
}
```

## Structure & Upgrades

**StructuredComponent** - required parts in slots:
```java
ComponentStructure structure = sword.structure();
Optional<Component> blade = structure.partInSlot("blade");
List<Component> allParts = sword.parts();
```

**CustomizableComponent** - optional upgrade slots:
```java
List<ComponentUpgradeSlot> slots = tool.slots();
List<Component> installedUpgrades = tool.installedUpgrades();

// Check slot availability
boolean canAccept = slot.canAccept(gemComponent);
```

## Example Structure

```
iron-sword (StructuredExtensibleEquipment)
├── structure:
│   ├── blade: iron-sword_blade
│   └── handle: oak-handle
└── upgrades:
    └── binding_slot: (empty)
```

## Modification (Immutable)

```java
// Add property
Component modified = original.with(b -> b.addProperty(attackDamage(5.0f)));

// Add tag
Component tagged = original.with(b -> b.addTag("enhanced"));
```

## Type Checking

```java
if (component instanceof StructuredComponent sc) {
    // Has parts
}

if (component instanceof CustomizableComponent cc) {
    // Has upgrade slots
}
```

## ComponentBuilder

```java
Component pickaxe = ComponentBuilder.create()
    .id("test:iron-pickaxe")
    .type(Type.PICKAXE)
    .part(pickaxeHead)
    .part(handle)
    .upgradeSlot(Type.GEM, 2)
    .build();  // Auto-selects correct implementation class
```

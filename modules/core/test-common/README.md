# Forgero Core Test Common

Platform-agnostic test utilities for Forgero's core module. Provides fixtures, builders, and assertions for testing Forgero components without any Minecraft dependencies.

## Features

- **Fixtures**: Pre-configured test data for materials, properties, and components
- **Builders**: Fluent APIs for constructing test components
- **Assertions**: Chainable assertion methods for validating component behavior
- **Zero Minecraft Dependencies**: Pure Java utilities that work in any testing environment

## Adding to Your Project

Add this module to your test dependencies:

```gradle
dependencies {
    testImplementation(project(":modules:core:test-common"))
}
```

## Usage Examples

### PropertyFixtures

Factory methods for creating test attributes with common values:

```java
import static com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures.*;

// Create individual attributes
Attribute damage = attackDamage(10.0f);
Attribute speed = miningSpeed(5.0f);
Attribute durability = durability(1000);
Attribute miningLevel = miningLevel(2);

// Attack attributes
Attribute knockback = knockback(2.0f);
Attribute critChance = critChance(0.15f);
Attribute critDamage = critDamage(1.5f);

// Mining attributes
Attribute range = range(3);
Attribute rarity = rarity(2);

// Convenience groups
Attribute[] ironStats = ironProperties();
Attribute[] diamondStats = diamondProperties();
```

### MaterialFixtures

Factory methods for creating material components:

```java
import static com.sigmundgranaas.forgero.testcommon.fixtures.MaterialFixtures.*;

// Pre-configured materials
Component iron = iron();
Component diamond = diamond();
Component oak = oak();
Component stone = stone();

// Custom material
Component custom = material("mythril", MaterialTypes.METAL,
    attackDamage(15.0f),
    durability(2000),
    miningLevel(3)
);

// With custom type
Component gem = material("ruby", MaterialTypes.GEM,
    OpenIdentifier.of("gem"),
    attackDamage(8.0f)
);
```

### ComponentBuilder

Fluent API for building test components:

```java
import com.sigmundgranaas.forgero.testcommon.builders.ComponentBuilder;

// Simple components
Component head = ComponentBuilder.create()
    .id("iron-pickaxe_head")
    .type(Type.PICKAXE_HEAD)
    .attributes(attackDamage(5.0f), durability(500))
    .build();

// Structured component with parts
Component pickaxe = ComponentBuilder.create()
    .id("iron-pickaxe")
    .type(Type.PICKAXE)
    .part(head)
    .part(handle)
    .build();

// Customizable component with slots
Component customTool = ComponentBuilder.create()
    .id("modular-sword")
    .type(Type.SWORD)
    .part(blade)
    .part(handle)
    .upgradeSlot(Type.GEM, 2)        // 2 gem slots
    .upgradeSlot(Type.BINDING, 1)    // 1 binding slot
    .build();

// Quick helpers
Component simpleHead = ComponentBuilder.simplePickaxeHead(iron());
Component tool = ComponentBuilder.pickaxeTool(head, handle);
```

### ComponentAssertions

Fluent assertion API for components:

```java
import static com.sigmundgranaas.forgero.testcommon.assertions.ComponentAssertions.*;

// Basic component assertions
assertThat(component)
    .hasId("iron-pickaxe_head")
    .hasNamespace("forgero")
    .hasPath("iron-pickaxe_head")
    .hasType(Type.PICKAXE_HEAD)
    .hasTag("pickaxe_head")
    .hasTag("tool_head");

// Property assertions
assertThat(component)
    .hasProperty(Attribute.KEY)
    .hasPropertyCount(Attribute.KEY, 3)
    .hasAttribute(attackDamage(5.0f));

// Structured component assertions
assertThat(pickaxe)
    .isStructured()
    .asStructured()
    .hasPartCount(2)
    .hasPartWithId("iron-pickaxe_head")
    .hasPart(head);

// Customizable component assertions
assertThat(customTool)
    .isCustomizable()
    .asCustomizable()
    .hasSlotCount(3)
    .hasSlotOfType(Type.GEM)
    .hasSlotWithCapacity(Type.GEM, 2);

// Chaining multiple assertions
assertThat(component)
    .hasId("test-component")
    .hasType(Type.MATERIAL)
    .hasTag("metal")
    .hasProperty(Attribute.KEY)
    .hasAttribute(durability(1000));
```

## Package Structure

```
com.sigmundgranaas.forgero.testcommon/
├── fixtures/          # Pre-built test data
│   ├── MaterialFixtures.java
│   └── PropertyFixtures.java
├── builders/          # Fluent builders
│   └── ComponentBuilder.java
└── assertions/        # Custom assertions
    └── ComponentAssertions.java
```

## Design Principles

1. **Factory Methods Over Constants**: Test data is created through factory methods instead of static constants, allowing for customization and avoiding shared mutable state.

2. **Fluent APIs**: Builders and assertions use method chaining for readable, expressive test code.

3. **Type Safety**: Strong typing prevents common mistakes and provides good IDE support.

4. **Modern Architecture**: All utilities work with Forgero's modern component system (StaticComponent, StructuredComponent, CustomizableComponent).

5. **No Minecraft**: This module has zero Minecraft dependencies, enabling fast pure-Java unit tests.

## Testing the Utilities

This module includes its own test suite to ensure the utilities work correctly:

```bash
./gradlew :modules:core:test-common:test
```

All 59 tests pass, verifying fixtures, builders, and assertions function correctly.

## Related Modules

- **modules/mc/test-common**: Minecraft-specific test utilities (GameTests, ItemStack assertions, etc.)
- **modules/core**: The core Forgero module being tested

## Contributing

When adding new test utilities:

1. Follow the factory method pattern for fixtures
2. Use fluent builder APIs for constructability
3. Provide fluent assertion methods for validation
4. Add comprehensive tests for new utilities
5. Update this README with usage examples

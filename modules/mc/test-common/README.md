# Forgero MC Test Common

Minecraft-specific test utilities for Forgero. Provides GameTest infrastructure, player factories, position utilities, and ItemStack assertions for testing Minecraft integration.

## Features

- **ForgeroGameTest Interface**: Base interface providing easy access to ForgeroServices in GameTests
- **ForgeroTestContext**: Enhanced TestContext wrapper with Forgero-specific utilities
- **PlayerFactory**: Fluent builder for creating test players with custom configurations
- **Position Utilities**: TestPos, TestPosCollection, and ContextSupplier for working with game coordinates
- **ItemStack Assertions**: Fluent assertion API for validating ItemStacks
- **Full Minecraft Integration**: Works with Fabric GameTest API

## Adding to Your Project

Add this module to your test dependencies:

```gradle
dependencies {
    testImplementation(project(path: ":modules:mc:test-common", configuration: 'namedElements'))
}
```

## Usage Examples

### ForgeroGameTest Interface

Base interface for GameTests that need Forgero services:

```java
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class MyComponentTests implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void test_component_conversion(TestContext context) {
        // Easy access to ForgeroServices
        var services = services();
        var registry = services.componentRegistry();

        // Enhanced context with Forgero utilities
        var ctx = forgero(context);

        // Look up a component
        var component = ctx.component("forgero:iron-pickaxe_head");
        ctx.assertTrue(component.isPresent(), "Component should exist");

        // Convert to ItemStack
        var stack = ctx.toStack(component.get());
        ctx.assertTrue(stack.isPresent(), "Should convert to stack");

        // Convert back to Component
        var roundTrip = ctx.toComponent(stack.get());
        ctx.assertTrue(roundTrip.isPresent(), "Should convert back");

        ctx.complete();
    }
}
```

### ForgeroTestContext

Enhanced TestContext with Forgero-specific methods:

```java
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;

@GameTest(templateName = EMPTY_STRUCTURE)
public void test_with_context(TestContext context) {
    ForgeroTestContext ctx = new ForgeroTestContext(context);

    // Component lookup
    Optional<Component> comp = ctx.component("forgero:diamond-sword");
    Optional<Component> comp2 = ctx.component(OpenIdentifier.of("iron-axe"));

    // Component <-> ItemStack conversion
    Optional<ItemStack> stack = ctx.toStack(comp.get());
    Optional<Component> converted = ctx.toComponent(stack.get());

    // Access to ForgeroServices
    ForgeroServices api = ctx.api();

    // Standard TestContext methods still work
    ctx.assertTrue(comp.isPresent(), "Component exists");
    ctx.getAbsolutePos(new BlockPos(1, 2, 3));
    ctx.setBlockState(pos, Blocks.STONE);
    ctx.complete();
}
```

### PlayerFactory

Fluent builder for creating test players:

```java
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;

@GameTest(templateName = EMPTY_STRUCTURE)
public void test_player_actions(TestContext context) {
    // Simple player at origin
    ServerPlayerEntity player = PlayerFactory.simple(context);

    // Player with custom configuration
    ServerPlayerEntity warrior = PlayerFactory.create(context)
        .withName("test-warrior")
        .at(new BlockPos(5, 64, 5))
        .facing(Direction.SOUTH)
        .withStack(new ItemStack(Items.DIAMOND_SWORD))
        .withOffHand(new ItemStack(Items.SHIELD))
        .survival()
        .build();

    // Player at specific position
    ServerPlayerEntity positioned = PlayerFactory.at(context, new BlockPos(10, 70, 10));

    // Full builder example
    ServerPlayerEntity custom = PlayerFactory.create(context)
        .withName("miner")
        .withUuid(UUID.randomUUID())
        .at(1, 64, 1)
        .facing(Direction.NORTH)
        .withPitch(-45.0f)
        .holding(new ItemStack(Items.DIAMOND_PICKAXE))
        .creative()
        .build();

    context.complete();
}
```

### Position Utilities

TestPos, TestPosCollection, and ContextSupplier for coordinate handling:

```java
import com.sigmundgranaas.forgero.mc.testcommon.gametest.*;

@GameTest(templateName = EMPTY_STRUCTURE)
public void test_positions(TestContext context) {
    // Single position
    TestPos pos = TestPos.of(new BlockPos(1, 2, 3), context);
    BlockPos relative = pos.relative();  // (1, 2, 3)
    BlockPos absolute = pos.absolute();  // World coordinates

    // Apply offsets
    TestPos offset = pos.offset(5, 0, 5);  // (6, 2, 8)
    TestPos offset2 = pos.offset(new BlockPos(1, 1, 1));

    // Collection of positions
    Set<BlockPos> positions = Set.of(
        new BlockPos(1, 1, 1),
        new BlockPos(2, 2, 2),
        new BlockPos(3, 3, 3)
    );
    TestPosCollection collection = TestPosCollection.of(positions, context);

    // Filter positions
    TestPosCollection high = collection.apply(p -> p.relative().getY() > 1);

    // Count matches
    long count = collection.count(p -> p.relative().getX() > 2);

    // Check for matches
    boolean hasHighY = collection.anyMatch(p -> p.relative().getY() > 5);

    // Context supplier for block states
    ContextSupplier ctx = ContextSupplier.of(context);
    BlockState state = ctx.relative(pos);
    BlockState absoluteState = ctx.absolute(pos);

    // Debug string
    String debug = collection.toString(ctx);  // Includes block IDs

    context.complete();
}
```

### ItemStack Assertions

Fluent assertion API for ItemStacks:

```java
import static com.sigmundgranaas.forgero.mc.testcommon.assertions.ItemStackAssertions.*;

@GameTest(templateName = EMPTY_STRUCTURE)
public void test_itemstack(TestContext context) {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);

    // Basic assertions
    assertThat(stack)
        .isNotEmpty()
        .hasItem(Items.DIAMOND_PICKAXE)
        .hasCount(1)
        .isDamageable();

    // NBT assertions
    stack.setNbt(new NbtCompound());
    stack.getNbt().putString("owner", "test");

    assertThat(stack)
        .hasTag()
        .tagContains("owner")
        .tagHasString("owner", "test");

    // Damage assertions
    stack.setDamage(100);
    assertThat(stack)
        .hasDamage(100)
        .hasMaxDamage(1561);

    // Forgero conversion
    assertThat(stack)
        .convertsToComponent();

    // Get the component for further testing
    Component component = assertThat(stack).asComponent();

    context.complete();
}
```

## Package Structure

```
com.sigmundgranaas.forgero.mc.testcommon/
├── gametest/         # GameTest infrastructure
│   ├── ForgeroGameTest.java
│   ├── ForgeroTestContext.java
│   ├── TestPos.java
│   ├── TestPosCollection.java
│   └── ContextSupplier.java
├── helpers/          # Test helpers
│   └── PlayerFactory.java
└── assertions/       # Minecraft assertions
    └── ItemStackAssertions.java
```

## Running Tests

### Unit Tests (JUnit)

Run standard JUnit tests (like ItemStackAssertionsTests):

```bash
./gradlew :modules:mc:test-common:test
```

### GameTests

Run Fabric GameTests (requires Minecraft server):

```bash
./gradlew :modules:mc:test-common:runGameTest
```

Note: GameTest infrastructure is configured in `build.gradle` with the Loom plugin.

## Design Principles

1. **GameTest First**: Designed primarily for Fabric GameTests with in-game validation

2. **Fluent APIs**: Builders and assertions use method chaining for readability

3. **Forgero Integration**: Tight integration with ForgeroServices for component testing

4. **Position Abstractions**: TestPos handles the complexity of relative vs absolute coordinates

5. **Type Safety**: Yarn mappings and strong typing prevent common mistakes

## Common Patterns

### Testing Component Conversion

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void test_conversion(TestContext context) {
    var ctx = forgero(context);

    // Component -> ItemStack -> Component round-trip
    var original = ctx.component("forgero:iron-pickaxe").orElseThrow();
    var stack = ctx.toStack(original).orElseThrow();
    var converted = ctx.toComponent(stack).orElseThrow();

    ctx.assertTrue(
        original.id().equals(converted.id()),
        "IDs should match after round-trip"
    );
    ctx.complete();
}
```

### Testing Player Interactions

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void test_player_mining(TestContext context) {
    // Create player with pickaxe
    ServerPlayerEntity player = PlayerFactory.create(context)
        .at(1, 64, 1)
        .holding(new ItemStack(Items.DIAMOND_PICKAXE))
        .survival()
        .build();

    // Set up test block
    BlockPos blockPos = new BlockPos(2, 64, 1);
    context.setBlockState(blockPos, Blocks.STONE);

    // Test mining logic here...

    context.complete();
}
```

### Testing Area Effects

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void test_area_effect(TestContext context) {
    // Define affected area
    Set<BlockPos> area = Set.of(
        new BlockPos(0, 1, 0),
        new BlockPos(1, 1, 0),
        new BlockPos(0, 1, 1)
    );
    TestPosCollection positions = TestPosCollection.of(area, context);

    // Apply effect to all positions...

    // Verify all positions affected
    ContextSupplier ctx = ContextSupplier.of(context);
    positions.positions().forEach(pos -> {
        BlockState state = ctx.relative(pos);
        context.assertTrue(state.isOf(Blocks.STONE), "Should be stone");
    });

    context.complete();
}
```

## Yarn vs Mojang Mappings

This module uses Yarn mappings (Fabric standard). Key differences:

- `TestContext` (not `GameTestHelper`)
- `ServerPlayerEntity` (not `ServerPlayer`)
- `hasNbt()` / `getNbt()` (not `hasTag()` / `getTag()`)
- `isDamageable()` (not `isDamageableItem()`)
- `getDamage()` (not `getDamageValue()`)

## Testing the Utilities

This module includes its own test suite:

```bash
# Unit tests (JUnit)
./gradlew :modules:mc:test-common:test

# GameTests (in Minecraft)
./gradlew :modules:mc:test-common:runGameTest
```

Test classes demonstrate proper usage of all utilities.

## Related Modules

- **modules/core/test-common**: Platform-agnostic test utilities for core components
- **modules/mc/loader**: ForgeroApi and services being tested
- **modules/mc/common**: Common Minecraft utilities

## Contributing

When adding new test utilities:

1. Use Yarn mappings consistently
2. Provide fluent builder/assertion APIs
3. Write both unit tests and GameTests where applicable
4. Document usage with examples
5. Update this README

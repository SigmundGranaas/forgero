# Forgero Testing Guide

Comprehensive guide to writing effective tests for Forgero using GameTest and ScenarioBuilder.

## Quick Start

### Writing a New GameTest

Use ScenarioBuilder for all new gameplay tests:

```java
import com.sigmundgranaas.forgero.mc.testcommon.scenario.ScenarioBuilder;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;

public class MyTest implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void myNewTest(TestContext context) {
        ScenarioBuilder.create(context)
            .at(1, 64, 1)
            .holding(myTestItem)
            .survival()
            .target()
                .entity(EntityType.ZOMBIE)
                .at(2, 64, 2)
            .action()
                .attack()
            .expect()
                .targetOnFire(100)
            .verify();
    }
}
```

## Test Categories

### 1. Deep Tests ✅ (What We Want)

Tests actual gameplay mechanics through full integration.

**Example: Fire sword sets target on fire**
- Creates player with fire sword
- Spawns target entity
- Player attacks target
- **Verifies target.isOnFire() and fireTicks**

**Why it's good:** Tests the full pipeline from mixin → property → handler → effect

### 2. Shallow Tests ⚠️ (Minimize These)

Tests only that objects exist or APIs don't crash.

**Example: Tool attribute query**
- Looks up component
- Calls getAttackDamage()
- Checks result >= 0
- **Does NOT test actual attack**

**Why it's limited:** Doesn't validate actual gameplay behavior

## Testing Philosophy

### Test Gameplay, Not Code

❌ **BAD** - Testing implementation details:
```java
@GameTest
public void testFireHandler() {
    FireHandler handler = new FireHandler(5);
    assertNotNull(handler);  // Just checks object exists
}
```

✅ **GOOD** - Testing actual behavior:
```java
@GameTest
public void fireSword_setsTargetOnFire(TestContext context) {
    ScenarioBuilder.create(context)
        .player().holding(fireSwordStack)
        .target().entity(EntityType.PIG).at(2, 64, 2)
        .action().attack()
        .expect()
            .targetOnFire(100)
            .targetHealth(13.0f, 1.0f)  // Also took damage
        .verify();
}
```

## Common Patterns

### Pattern 1: OnHit Effect Testing

Test that hitting entities with effect weapons applies effects correctly.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void poisonSword_appliesPoisonEffect(TestContext context) {
    // Create item with poison OnHit property
    ItemStack poisonSword = createSwordWithEffect(new PoisonHandler(2, 200));

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .holding(poisonSword)
        .target()
            .entity(EntityType.COW)
            .at(2, 64, 2)
        .action()
            .attack()
        .expect()
            .targetHasEffect(StatusEffects.POISON, 2, 200)  // Level II, 10 seconds
            .targetHealth(17.0f, 1.0f)  // Also took damage from attack
        .verify();
}
```

### Pattern 2: Armor Protection Testing

Test that armor reduces damage and triggers defensive effects.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void ironArmor_reducesDamage(TestContext context) {
    ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
    ItemStack ironChest = new ItemStack(Items.IRON_CHESTPLATE);
    ItemStack ironLegs = new ItemStack(Items.IRON_LEGGINGS);
    ItemStack ironBoots = new ItemStack(Items.IRON_BOOTS);

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .wearing(ironHelmet, ironChest, ironLegs, ironBoots)
        .health(20.0f)
        .survival()
        .target()
            .entity(EntityType.ZOMBIE)
            .at(2, 64, 2)
        .action()
            .targetAttacksPlayer()
        .expect()
            .captureInitialState()
            .playerTookDamage(20.0f, 1.0f, 1.5f)  // Reduced damage with armor
        .verify();
}
```

### Pattern 3: OnTick Passive Effects

Test that held/worn items apply periodic effects.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void regenerationArmor_healsPlayerOverTime(TestContext context) {
    ItemStack regenHelmet = createHelmetWithRegeneration();

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .helmet(regenHelmet)
        .health(10.0f)  // Start with low health
        .target()
            .entity(EntityType.PIG)  // Dummy target (not used)
            .at(2, 64, 2)
        .action()
            .none()  // No action, just wait
        .expect()
            .after(20)  // Wait 1 second for regeneration
            .playerHealth(12.0f, 1.0f)  // Should have healed
        .verify();
}
```

### Pattern 4: Projectile Effects

Test that arrows with effects apply them on hit.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void fireArrow_setsTargetOnFire(TestContext context) {
    // Bow that shoots fire arrows
    ItemStack fireBow = createBowWithFireEffect();

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .holding(fireBow)
        .target()
            .entity(EntityType.ZOMBIE)
            .at(5, 64, 5)  // Further away for arrow travel
        .action()
            .shootArrow()
        .expect()
            .after(5)  // Wait for arrow to hit
            .targetOnFire(100)
        .verify();
}
```

### Pattern 5: Multiple Property Interactions

Test that multiple properties on the same item work together correctly.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void fireAndFreezeSword_appliesBothEffects(TestContext context) {
    // Sword with BOTH fire and freeze OnHit
    ItemStack comboSword = createSwordWithMultipleEffects(
        new FireHandler(5),
        new FreezeHandler(100, false)
    );

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .holding(comboSword)
        .target()
            .entity(EntityType.PIG)
            .at(2, 64, 2)
        .action()
            .attack()
        .expect()
            .targetOnFire(100)
            .custom(() -> {
                // Custom assertion for frozen state
                assertTrue(target.getFrozenTicks() > 0, "Target should be frozen");
            })
        .verify();
}
```

### Pattern 6: Tool Durability and Breaking

Test that tools lose durability and eventually break.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void tool_losesdurability_onBlockBreak(TestContext context) {
    ItemStack pickaxe = getIronPickaxe();
    int initialDurability = pickaxe.getMaxDamage() - pickaxe.getDamage();

    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .holding(pickaxe)
        .target()
            .entity(EntityType.PIG)  // Dummy
            .at(5, 64, 5)
        .action()
            .breakBlock(new BlockPos(2, 63, 2))  // Break one block
        .expect()
            .custom(() -> {
                int finalDurability = pickaxe.getMaxDamage() - pickaxe.getDamage();
                assertTrue(finalDurability < initialDurability,
                    "Tool durability should decrease after mining");
            })
        .verify();
}
```

### Pattern 7: Conditional Properties

Test that properties only trigger under specific conditions.

```java
@GameTest(templateName = EMPTY_STRUCTURE)
public void lightningStrike_onlyWhenRaining(TestContext context) {
    // Sword with conditional lightning (only when raining)
    ItemStack weatherSword = createSwordWithConditionalLightning();

    // Test 1: No lightning when NOT raining
    ScenarioBuilder.create(context)
        .at(1, 64, 1)
        .holding(weatherSword)
        .target()
            .entity(EntityType.PIG)
            .at(2, 64, 2)
        .action()
            .attack()
        .expect()
            .custom(() -> {
                // No lightning entities should spawn
                var lightnings = context.getWorld().getEntitiesByType(
                    EntityType.LIGHTNING_BOLT,
                    Box.of(Vec3d.ZERO, 20, 20, 20),
                    e -> true
                );
                assertTrue(lightnings.isEmpty(), "No lightning when not raining");
            })
        .verify();

    // Test 2: Lightning DOES trigger when raining
    context.getWorld().setWeather(0, 100, true, true);  // Start rain

    ScenarioBuilder.create(context)
        .at(3, 64, 3)
        .holding(weatherSword)
        .target()
            .entity(EntityType.PIG)
            .at(4, 64, 4)
        .action()
            .attack()
        .expect()
            .after(3)
            .custom(() -> {
                // Lightning should have spawned
                var lightnings = context.getWorld().getEntitiesByType(
                    EntityType.LIGHTNING_BOLT,
                    Box.of(Vec3d.ZERO, 20, 20, 20),
                    e -> true
                );
                assertTrue(!lightnings.isEmpty(), "Lightning spawns when raining");
            })
        .verify();
}
```

## Best Practices

### 1. Test Gameplay, Not Just Code

- ❌ DON'T: Just check that APIs don't crash
- ✅ DO: Verify actual effects on entities and world state

### 2. Use ScenarioBuilder for Readability

- ❌ DON'T: Manual player creation, entity spawning, position calculation
- ✅ DO: Fluent, declarative ScenarioBuilder API

### 3. Test Edge Cases

- Broken tools (0 durability)
- Full inventory slots
- Conflicting effects (fire + freeze)
- Extreme values (1000 damage, 0 health)
- Missing components (no blade, no handle)

### 4. Test Property Interactions

- Multiple OnHit effects on same item
- Multiple OnTick effects (armor + held item auras)
- Conditional chains (sneaking + raining + low health)
- Deep component hierarchies (upgrades on upgraded items)

### 5. Document Test Intent

Use clear, descriptive test names that explain expected behavior:

```java
// ❌ BAD - Vague
@GameTest
public void test1() { ... }

// ✅ GOOD - Clear intent
@GameTest
public void fireSword_setsTargetOnFire_forAtLeast5Seconds() { ... }
```

### 6. Keep Tests Focused

Each test should verify ONE specific behavior:

```java
// ❌ BAD - Tests too many things
@GameTest
public void testEverything() {
    // Tests fire, poison, knockback, healing, durability...
}

// ✅ GOOD - Focused on one behavior
@GameTest
public void fireSword_setsTargetOnFire() {
    // Only tests fire effect
}

@GameTest
public void poisonSword_appliesPoisonEffect() {
    // Only tests poison effect
}
```

## Running Tests

### Run All GameTests

```bash
./gradlew :modules:mc:properties:runGameTest
```

### Run Specific Test Class

```bash
./gradlew :modules:mc:properties:runGameTest --tests "*PropertyIntegrationGametest*"
```

### Run All Unit Tests in Module

```bash
./gradlew :modules:mc:properties:test
```

### Run Single Test Method

```bash
./gradlew :modules:mc:properties:test --tests "*.MyTest.mySpecificTestMethod"
```

### Run Tests with Verbose Output

```bash
./gradlew :modules:mc:properties:test --info
```

## Examples to Study

### Gold Standard Tests (Models to Follow)

These tests demonstrate excellent deep gameplay testing:

1. **`modules/mc/blocks/src/test/java/.../AssemblyStationGameTest.java`**
   - Deep validation of actual disassembly mechanics
   - Tests real component extraction from tools
   - Verifies upgrade preservation

2. **`modules/mc/properties/src/test/java/.../PropertyIntegrationGametest.java`**
   - Comprehensive effect testing (fire, poison, lightning, knockback)
   - Tests selectors (single, AOE, cone, chain)
   - Tests filters and conditions

3. **`modules/mc/bows/src/test/java/.../DynamicArrowBehaviorTest.java`**
   - Complex arrow physics and effects
   - Tests projectile-specific properties
   - Validates trajectory and impact

4. **`fabric/forgero-fabric-core/src/test/java/.../VeinMiningToolTests.java`**
   - Realistic mining scenarios
   - Tests multi-block breaking patterns
   - Validates durability consumption

## Common Pitfalls

### 1. Testing Only Object Creation

```java
// ❌ SHALLOW
@GameTest
public void testFireSword() {
    var sword = new ItemStack(Items.DIAMOND_SWORD);
    assertNotNull(sword);  // Just checks object exists
}

// ✅ DEEP
@GameTest
public void fireSword_setsTargetOnFire(TestContext context) {
    ScenarioBuilder.create(context)
        .player().holding(fireSwordStack)
        .target().entity(EntityType.PIG)
        .action().attack()
        .expect().targetOnFire(100)
        .verify();
}
```

### 2. Not Waiting for Async Effects

Some effects take time to apply (OnTick, delayed damage, etc.). Use `.after(ticks)`:

```java
// ❌ MIGHT FAIL - Checks immediately
.expect()
    .targetOnFire(100)
.verify();

// ✅ CORRECT - Waits for effect to apply
.expect()
    .after(5)  // Wait 5 ticks
    .targetOnFire(100)
.verify();
```

### 3. Forgetting to Capture Initial State

For damage/healing tests, capture state BEFORE the action:

```java
// ❌ WRONG - No initial state
.expect()
    .playerTookDamage(3.0f, 1.0f)  // ERROR: initialPlayerHealth not captured
.verify();

// ✅ CORRECT - Capture state first
.expect()
    .captureInitialState()
    .playerTookDamage(3.0f, 1.0f)
.verify();

// ✅ ALSO CORRECT - Provide initial health explicitly
.expect()
    .playerTookDamage(20.0f, 3.0f, 1.0f)  // 20.0f = initial health
.verify();
```

### 4. Using Vanilla Items for Forgero-Specific Tests

When testing Forgero properties, use actual Forgero components:

```java
// ❌ WRONG - Vanilla item won't have Forgero properties
ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

// ✅ CORRECT - Use Forgero component system
var ctx = forgero(context);
Component swordComponent = ctx.component("forgero:iron-sword").orElseThrow();
ItemStack sword = ctx.toStack(swordComponent).orElseThrow();
```

## Advanced Topics

### Creating Test Items with Properties

```java
// Example: Create a sword with fire OnHit property
private ItemStack createFireSword() {
    var registry = ForgeroApi.componentRegistry();
    var converter = ForgeroApi.converter();

    // Find base sword component
    var sword = registry.find(OpenIdentifier.of("forgero:iron-sword")).orElseThrow();

    // Add fire property (depends on your data setup)
    // Properties are typically defined in JSON and attached via components

    return converter.toStack(sword).orElseThrow();
}
```

### Using ForgeroGameTest Interface

Simplifies access to Forgero services:

```java
public class MyTests implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void myTest(TestContext context) {
        // Easy access to ForgeroServices
        var registry = services().componentRegistry();
        var converter = services().converter();

        // Enhanced context with Forgero utilities
        var ctx = forgero(context);
        var component = ctx.component("forgero:iron-pickaxe").orElseThrow();
        var stack = ctx.toStack(component).orElseThrow();

        // ... test logic
    }
}
```

### Custom Assertions

For complex scenarios not covered by standard assertions:

```java
.expect()
    .custom(() -> {
        // Custom validation logic
        var world = context.getWorld();
        var entities = world.getEntitiesByType(...);

        context.assertTrue(entities.size() == 3,
            "Should spawn exactly 3 lightning bolts");
    })
.verify();
```

## Migration from Legacy Tests

### Before (Legacy Pattern)

```java
@GameTest
public void testFireHandler(TestContext context) {
    TestPos playerPos = TestPos.of(new BlockPos(3, 1, 3), context);
    createFloor(context);

    PlayerEntity player = PlayerFactory.builder(context)
        .pos(playerPos.absolute())
        .build()
        .createPlayer();

    PigEntity target = context.spawnEntity(EntityType.PIG,
        playerPos.offset(new BlockPos(1, 0, 1)).relative());

    FireHandler fireHandler = fireHandler();
    fireHandler.onHit(player, context.getWorld(), target);

    context.runAtTick(1, () -> {
        context.assertTrue(target.getFireTicks() > 1);
        context.assertTrue(target.getHealth() < target.getMaxHealth());
        context.complete();
    });
}
```

### After (ScenarioBuilder Pattern)

```java
@GameTest
public void testFireHandler(TestContext context) {
    ScenarioBuilder.create(context)
        .at(3, 1, 3)
        .target()
            .entity(EntityType.PIG)
            .at(4, 1, 4)
        .action()
            .custom(() -> {
                FireHandler fireHandler = fireHandler();
                fireHandler.onHit(player, context.getWorld(), target);
            })
        .expect()
            .targetOnFire(100)
            .targetHealth(17.0f, 3.0f)
        .verify();
}
```

**Benefits:**
- 18 lines → 13 lines (28% reduction)
- Removes boilerplate (PlayerFactory, TestPos, manual spawning)
- More readable and declarative
- Type-safe assertions

## Contributing New Tests

When adding new tests to the project:

1. **Use ScenarioBuilder** for all new gameplay tests
2. **Implement ForgeroGameTest** for access to Forgero services
3. **Write deep tests** that validate actual gameplay, not just API calls
4. **Test edge cases** and property interactions
5. **Document intent** with clear test names and comments
6. **Follow existing patterns** from gold standard tests
7. **Run tests locally** before committing

## Resources

- **Test Utilities README**: `modules/mc/test-common/README.md`
- **ScenarioBuilder Examples**: `modules/mc/test-common/src/test/java/.../ScenarioBuilderExampleTest.java`
- **EffectAssertions API**: `modules/mc/test-common/src/main/java/.../EffectAssertions.java`
- **Gold Standard Tests**: See examples above
- **Forgero API Documentation**: `FORGERO_AI_CONTEXT.md`

---

**Remember:** Good tests validate actual gameplay behavior, not just that code compiles and APIs don't crash. Use ScenarioBuilder to write clear, concise, deep tests that give confidence in the mod's functionality.

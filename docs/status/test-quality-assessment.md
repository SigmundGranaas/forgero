# Test Quality Assessment

**Status:** Assessment of current test coverage quality
**Date:** 2025-01-22
**Related Issue:** Guard durability not applied when installed as upgrade

## Executive Summary

Analysis of Forgero's test suite reveals approximately **70-75% of tests are "shallow"** - they verify attributes EXIST rather than that they APPLY correctly. This gap allowed the composite attribute resolution bug (guards not adding durability to swords) to go undetected.

## Test Quality Tiers

| Tier | Current | Target | Description |
|------|---------|--------|-------------|
| **A (Behavioral)** | ~25% | 80%+ | Verifies actual in-game/resolved behavior |
| **B (Integration)** | ~10% | 15% | Tests component interaction end-to-end |
| **C (Shallow)** | ~65% | <5% | Existence/raw value checks only |

## Anti-Patterns Found

### 1. Checking Existence Instead of Application

```java
// BAD: Only checks that properties exist
assertFalse(component.properties(Attribute.KEY).isEmpty());

// GOOD: Checks that properties APPLY via resolution
float resolved = attributeEngine().resolve(component).getValue(DURABILITY);
assertTrue(resolved > 0, "Durability should be positive after resolution");
```

### 2. Raw Values Instead of Resolved Values

```java
// BAD: Checks raw attribute value directly
float raw = component.properties(KEY).get(0).value();
assertEquals(100, raw);

// GOOD: Uses full resolution pipeline (what Minecraft actually sees)
float resolved = ForgeroApi.itemQuery().getAttribute(toStack(component), DURABILITY);
assertEquals(100, resolved);
```

### 3. Testing Implementation Details

```java
// BAD: Tests internal structure
assertEquals("forgero:part-composite", attr.context().get().toString());

// GOOD: Tests observable behavior
Component sword = getSword();
Component upgradedSword = installUpgrade(sword, guard);
assertTrue(getResolvedDurability(upgradedSword) > getResolvedDurability(sword),
    "Guard should increase sword durability");
```

### 4. Missing Negative Assertions

```java
// BAD: Only checks happy path
assertTrue(result.getValue(DURABILITY) > 0);

// GOOD: Validates both inclusion AND exclusion
assertEquals(108.0f, result.getValue(DURABILITY),
    "Should be base (91) + guard composed (17)");
assertNotEquals(91.0f, result.getValue(DURABILITY),
    "Should NOT be just base (bug filtered out guard)");
```

## Recommended Test Patterns

### Pattern 1: Behavioral Attribute Test

```java
@Test
void upgrade_increases_resolved_durability() {
    // Arrange
    Component base = getComponent("forgero:iron_sword");
    float baseDur = getResolvedAttribute(base, "forgero:durability");

    // Act
    Component upgraded = installUpgrade(base, "forgero:iron-sword_guard");
    float upgradedDur = getResolvedAttribute(upgraded, "forgero:durability");

    // Assert
    assertTrue(upgradedDur > baseDur,
        String.format("Guard must increase durability: %f -> %f", baseDur, upgradedDur));
}
```

### Pattern 2: Comparative Test (Different Materials)

```java
@Test
void different_materials_give_different_bonuses() {
    Component ironGuard = getComponent("forgero:iron-sword_guard");
    Component diamondGuard = getComponent("forgero:diamond-sword_guard");

    float ironDur = getResolvedAttribute(ironGuard, "forgero:durability");
    float diamondDur = getResolvedAttribute(diamondGuard, "forgero:durability");

    assertTrue(diamondDur > ironDur,
        "Diamond guard should have more durability than iron");
}
```

### Pattern 3: Full Pipeline Test (ItemStack Level)

```java
@Test
void tooltip_reflects_upgrade_bonus() {
    ItemStack sword = toStack(getComponent("forgero:golden_sword"));
    int baseDur = sword.getMaxDamage();

    ItemStack upgraded = ForgeroApi.itemMutation().installUpgrade(sword, guardStack);
    int upgradedDur = upgraded.getMaxDamage();

    assertTrue(upgradedDur > baseDur,
        "ItemStack max damage should reflect upgrade");
}
```

## Tests Identified for Refactoring

### High Priority (Directly Related to Bug)

| Test | Current Issue | Recommended Fix |
|------|---------------|-----------------|
| `UpgradeValueTest` | Checks raw values only | Add resolved value assertions |
| `UpgradeEffectivenessTest` | Missing actual stat verification | Test that upgrades actually improve performance |
| `MaxedOutToolTest` | Checks slot fill count | Verify tool stats after filling all slots |

### Medium Priority (Common Patterns)

| Test File | Issue Pattern |
|-----------|---------------|
| `PropertyResolverTest` | Tests existence not application |
| `ComponentRegistryTest` | Tests lookup not behavior |
| `TagGraphTest` | Tests structure not effect |

### Low Priority (Structure Tests)

These tests serve a purpose but should be supplemented with behavioral tests:
- Component builder tests
- Codec serialization tests
- Tag hierarchy tests

## Migration Checklist

For each test being refactored:

- [ ] Uses `attributeEngine().resolve()` or `ForgeroApi.itemQuery()`
- [ ] Tests RESOLVED values, not raw properties
- [ ] Includes both positive and negative assertions
- [ ] Documents expected values with calculation comments
- [ ] Uses descriptive assertion messages showing actual vs expected

## New Tests Added

### `UpgradeAttributeApplicationTest` (GameTest)
- `ender_pearl_actually_increases_pickaxe_durability_when_installed`
- `glowstone_increases_sword_durability_when_installed`
- `guard_increases_sword_durability_when_installed` (was failing, now fixed)
- `different_guard_materials_provide_different_bonuses`
- `guard_mining_speed_composition`
- `guard_composition_produces_non_zero_durability`
- `multiple_upgrades_stack_durability`
- `upgrade_attack_damage_is_applied`

### `UpgradeAttributeFilteringIntegrationTest` (Unit Test)
- `structuredUpgradeComposesInternally` (CRITICAL: validates the fix)
- `structuredUpgradeComposedAttributesHaveNoContext`
- `multipleStructuredUpgradesStack`

## Root Cause of the Bug

The bug occurred in `CompositeAttributeBakingStrategy.collectUpgradesFromComponent()`:

```java
// BEFORE (Bug): Read raw properties and filter by context
List<Attribute> upgradeAttrs = upgrade.properties(KEY).stream()
    .filter(attr -> AttributeContext.matchesSlotContext(attr.context(), slotContext))
    ...

// AFTER (Fix): Compose structured upgrades first, then merge
List<Attribute> composedAttrs = List.of();
if (upgrade instanceof StructuredComponent structuredUpgrade) {
    composedAttrs = composeStructuredComponent(structuredUpgrade, root);
}
// Composed attrs have no context, so they pass through
```

**Why existing tests didn't catch it:**
- Tests checked that guards HAVE durability attributes
- Tests didn't check that guards ADD durability to swords
- The filtering logic passed all "existence" tests while breaking "application"

## Conclusion

The test suite needs significant investment in **behavioral validation**. The fix for the composite attribute bug has been accompanied by new tests that follow the recommended patterns. Future work should prioritize converting shallow tests to behavioral tests, especially in the attribute resolution and upgrade application areas.

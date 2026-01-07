# Test Coverage Expansion Plan

## Testing Philosophy

### Core Principles

1. **Test through public APIs** - Never instantiate implementation classes directly in assertions
2. **Behavioral focus** - Test what the system DOES, not implementation details
3. **Developer feedback** - For JSON parsing, ensure error messages help resource pack developers
4. **No trivial tests** - Don't test Java operators, string parsing, boolean logic
5. **No bug-named tests** - Tests describe behavior, not reference issues

### API Surfaces for Testing

| API | Classes | Purpose |
|-----|---------|---------|
| **Component** | `Component`, `ComponentBuilder`, `ComponentFactory` | Component construction and navigation |
| **Resolver** | `Resolver`, `AttributeQueryResult` | Property/attribute resolution |
| **SlotManager** | `SlotManager`, `SlotQuery`, `InstallationResult` | Upgrade installation/removal |
| **Condition** | `Condition`, `StaticCondition`, `DynamicCondition` | Condition evaluation |
| **Codecs** | `*Codecs` classes | JSON parsing with developer feedback |

---

## Current Coverage Status

| Package | Coverage | Priority | Notes |
|---------|----------|----------|-------|
| `data.pipeline.impl` | 79% | Low | Good coverage |
| `common.tags.engine` | 89% | Low | Well tested |
| `core.component.api.slot.impl` | 87% | Low | Well tested |
| `core.component.api` | 5% | **HIGH** | Core API undertested |
| `core.condition.logical` | 5% | Medium | Logical AND/OR/NOT |
| `common.filter` | 0% | Medium | Filter composition |
| `common.tags.validation` | 0% | Low | Validation utilities |
| `validation` | 0% | Low | Tag validation runner |
| `utility.resource.loader.implementation` | 14% | Low | Internal loader details |

---

## Priority 1: Component Builder API (HIGH)

**Current**: 5% coverage on `core.component.api`  
**Target**: 80%+

### Test File: `ComponentBuilderBehaviorTest.java`

```
Location: modules/core/src/test/java/com/sigmundgranaas/forgero/core/component/api/
```

#### Behavioral Units

1. **Basic component construction**
   - Building static component with id, tags, properties
   - Building structured component with parts
   - Building customizable component with upgrade slots
   - Building structured + customizable component

2. **Type inference**
   - Builder auto-selects StaticComponent when no structure/upgrades
   - Builder auto-selects StructuredPart when structure present
   - Builder auto-selects ExtensiblePart when upgrades present
   - Builder auto-selects StructuredExtensibleEquipment when both + asEquipment()

3. **Property accumulation**
   - Multiple calls to `.property()` accumulate values
   - Properties from different keys don't interfere

4. **Tag management**
   - `.tag()` adds individual tags
   - `.tags()` adds collections
   - Duplicate tags are deduplicated

5. **Slot validation behavior**
   - Structure slots accept valid components
   - Structure slots reject invalid components based on SlotValidator
   - Upgrade slots validate by type

#### Test Pattern

```java
@Test
void builderCreatesStructuredComponentWhenPartsAdded() {
    Component material = ComponentBuilder.of("iron").tag("metal").build();
    
    Component tool = ComponentBuilder.of("pickaxe")
        .structureSlot("head", "pickaxe_head", material)
        .build();
    
    assertInstanceOf(StructuredComponent.class, tool);
    assertEquals(1, ((StructuredComponent) tool).structure().allParts().size());
}
```

---

## Priority 2: Logical Condition Composition (MEDIUM)

**Current**: 5% coverage on `core.condition.logical`  
**Target**: 70%+

### Test File: `LogicalConditionBehaviorTest.java`

```
Location: modules/core/src/test/java/com/sigmundgranaas/forgero/core/condition/
```

#### Behavioral Units (through Resolver API)

1. **AND condition behavior**
   - Attribute applies when ALL conditions pass
   - Attribute does NOT apply when ANY condition fails
   - Mixed static/dynamic conditions evaluate correctly

2. **OR condition behavior**
   - Attribute applies when ANY condition passes
   - Attribute does NOT apply when ALL conditions fail

3. **NOT condition behavior**
   - Inverts the wrapped condition result

4. **Nested logical conditions**
   - AND(OR(a, b), c) evaluates correctly
   - OR(AND(a, b), c) evaluates correctly

#### Test Pattern

```java
@Test
void attributeWithAndConditionRequiresAllToPass() {
    // Given: attribute with AND(tag_match:metal, in_slot_type:material)
    Condition andCondition = Condition.and(
        tagMatch("metal"),
        inSlotType("material")
    );
    Attribute attr = attribute(ATTACK_DAMAGE, 5f).withCondition(andCondition);
    
    // When: component has metal tag and is in material slot
    Component iron = part("iron").withTag("metal").withAttribute(attr).build();
    Component tool = part("tool")
        .withStructureSlot("material_slot", "material", iron)
        .build();
    
    // Then: attribute applies
    AttributeQueryResult result = resolver().resolve(tool, attributeEngine());
    assertEquals(5f, result.getValue(ATTACK_DAMAGE));
}

@Test
void attributeWithAndConditionFailsWhenOneConditionFails() {
    // Given: same attribute
    // When: component has metal tag but is NOT in material slot
    // Then: attribute does NOT apply
    assertEquals(0f, result.getValue(ATTACK_DAMAGE));
}
```

---

## Priority 3: Filter Composition (MEDIUM)

**Current**: 0% coverage  
**Target**: 60%+

### Test File: `FilterCompositionBehaviorTest.java`

```
Location: modules/core/src/test/java/com/sigmundgranaas/forgero/common/filter/
```

#### Behavioral Units

1. **Filter.alwaysTrue/alwaysFalse factory methods**
2. **Filter.and() composition**
3. **Filter.or() composition**
4. **Filter.negate()**
5. **CompositeFilter (AndFilter, OrFilter, NotFilter)**

#### Note on Testing Philosophy

Filters are simple functional interfaces. Direct unit testing is acceptable here since:
- They are small, focused utility classes
- No complex state or dependencies
- Testing through a higher-level API would be artificial

```java
@Test
void andFilterPassesWhenBothPass() {
    Filter<String, Void> startsWithA = (ctx, s) -> s.startsWith("A");
    Filter<String, Void> endsWithZ = (ctx, s) -> s.endsWith("Z");
    
    Filter<String, Void> combined = startsWithA.and(endsWithZ);
    
    assertTrue(combined.test(null, "ABCZ"));
    assertFalse(combined.test(null, "ABCD"));
    assertFalse(combined.test(null, "XYZZ"));
}
```

---

## Priority 4: Expanded Resource Pack Validation (HIGH)

**Current**: MaterialData, PartTemplateData, EquipmentTemplateData covered  
**Target**: All definition types

### Test File: `ResourcePackValidationTest.java` (expand existing)

#### Additional Behavioral Units

1. **SchematicData validation**
   - Valid schematic with slots
   - Missing required fields produce clear errors
   - Slot configuration validation

2. **StatusModifierData validation**
   - Valid status modifier
   - Missing type produces clear error
   - Invalid effect type produces clear error

3. **ExtensionData validation**
   - Valid extension targeting existing definition
   - Invalid target reference produces clear error

4. **Conditional attribute validation**
   - Valid condition block parses
   - Unknown condition type produces helpful error
   - Nested logical conditions parse correctly

5. **Cross-reference validation**
   - Invalid include reference detected
   - Circular include reference handling

---

## Priority 5: Slot Manager Edge Cases (LOW)

**Current**: ~70% (SlotManagerTest, SlotManagerIntegrationTest exist)  
**Target**: 85%+

### Expand existing: `SlotManagerIntegrationTest.java`

#### Additional Behavioral Units

1. **Slot capacity**
   - Installing into full slot fails gracefully
   - Installing after removal works

2. **Slot type validation**
   - Installing wrong type fails with clear reason
   - Custom SlotValidator behavior

3. **Query operations**
   - Query by type returns correct slots
   - Query empty/filled slots
   - Query nested slots in structured components

---

## Implementation Order

### Phase 1: High Priority (Week 1)
1. `ComponentBuilderBehaviorTest.java` - New file, 15-20 tests
2. Expand `ResourcePackValidationTest.java` - Add 10-15 tests

### Phase 2: Medium Priority (Week 2)
3. `LogicalConditionBehaviorTest.java` - New file, 10-15 tests
4. `FilterCompositionBehaviorTest.java` - New file, 8-10 tests

### Phase 3: Polish (Week 3)
5. Expand `SlotManagerIntegrationTest.java` - Add 5-8 tests
6. Review and consolidate any remaining impl/ tests

---

## Test Template

```java
package com.sigmundgranaas.forgero.core.[package];

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[Feature Area]")
class [Feature]BehaviorTest {

    @BeforeEach
    void setUp() {
        // Use factory methods from ForgeroTestFactory
    }

    @Nested
    @DisplayName("[Behavioral Unit]")
    class [BehavioralUnit]Tests {

        @Test
        @DisplayName("[Expected behavior description]")
        void [descriptive_method_name]() {
            // Given: [setup using public API builders]
            
            // When: [action through public API]
            
            // Then: [assertions on public API results]
        }
    }
}
```

---

## Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Instruction Coverage | 57% | 75%+ |
| Branch Coverage | 45% | 65%+ |
| Test Count | 527 | 600+ |
| `core.component.api` Coverage | 5% | 80%+ |
| `core.condition.logical` Coverage | 5% | 70%+ |

---

## Files to Create

1. `modules/core/src/test/java/com/sigmundgranaas/forgero/core/component/api/ComponentBuilderBehaviorTest.java`
2. `modules/core/src/test/java/com/sigmundgranaas/forgero/core/condition/LogicalConditionBehaviorTest.java`
3. `modules/core/src/test/java/com/sigmundgranaas/forgero/common/filter/FilterCompositionBehaviorTest.java`

## Files to Expand

1. `modules/core/src/test/java/com/sigmundgranaas/forgero/data/loading/ResourcePackValidationTest.java`
2. `modules/core/src/test/java/com/sigmundgranaas/forgero/core/component/api/slot/SlotManagerIntegrationTest.java`

---

## Commands

```bash
# Run all core tests with coverage
./gradlew :modules:core:test :modules:core:jacocoTestReport

# Run specific test class
./gradlew :modules:core:test --tests "*ComponentBuilderBehaviorTest"

# View coverage report
open modules/core/build/reports/jacoco/test/html/index.html
```

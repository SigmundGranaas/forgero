# Property and Attribute Resolution: Comprehensive Architectural Analysis

> **⚠️ HISTORICAL ANALYSIS.** Written before the compile-at-construction inversion and the
> `StatFold` kernel. The resolver/engine/scope-matching machinery it analyses has been deleted.
> See `docs/ADR-002-compiler-in-the-factory.md` and `docs/ADR-003-stat-contribution-kernel.md`
> for the current architecture.

**Date:** 2025-01-22
**Status:** Critical Assessment
**Scope:** modules/core, modules/mc/properties, legacy modules

---

## Executive Summary

Forgero's property and attribute resolution system is **architecturally sophisticated but terminologically confusing**. The two-phase BAKE/APPLY pattern is sound, the context-based composition is well-designed, and the condition system provides clean separation of concerns. However, significant naming inconsistencies create cognitive load, and several API design choices increase the risk of developer misuse.

**Verdict:** Fundamentally sound architecture with confusing terminology.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Strengths](#2-strengths)
3. [Weaknesses and Inconsistencies](#3-weaknesses-and-inconsistencies)
4. [Risks and Bug-Prone Areas](#4-risks-and-bug-prone-areas)
5. [Recommendations](#5-recommendations)
6. [Detailed Component Analysis](#6-detailed-component-analysis)
7. [Data Flow Diagrams](#7-data-flow-diagrams)

---

## 1. Architecture Overview

### 1.1 Core Concepts

| Concept | Purpose | Key Classes |
|---------|---------|-------------|
| **Property** | Base polymorphic type for all behaviors | `Property` (marker interface) |
| **Attribute** | Numerical property (durability, damage) | `Attribute`, `SimpleAttribute` |
| **Condition** | When properties apply | `Condition`, `StaticCondition`, `DynamicCondition` |
| **Context** | Where attributes participate in composition | `AttributeContext` constants |
| **Selector** | Who/what effects target | `EntitySelector`, `BlockSelector` |
| **Filter** | Narrow selection criteria | `EntityFilter`, `BlockFilter` |

### 1.2 Two-Phase Resolution Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│                     BAKE PHASE (Static, Cached)                  │
├─────────────────────────────────────────────────────────────────┤
│ 1. Component tree traversal (ComponentTraversal.traverse)        │
│ 2. ResolutionContext built (topological map of tree)             │
│ 3. Static conditions evaluated (in_slot_type, is_root, etc.)     │
│ 4. Attributes filtered by context (PART_COMPOSITE, UPGRADE)      │
│ 5. Composition applied (base × multiplier intersection)          │
│ 6. Result cached as BakedAttributes                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    APPLY PHASE (Dynamic, Per-Query)              │
├─────────────────────────────────────────────────────────────────┤
│ 1. DynamicContext provided (target, world state)                 │
│ 2. Dynamic conditions evaluated (is_sneaking, target_has_tag)    │
│ 3. Final resolved values computed                                │
│ 4. Result returned to caller                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 Component Type Hierarchy

```
Component
├── StaticComponent          - Leaf nodes (materials, simple parts)
├── StructuredComponent      - Has structure slots
│   ├── StructuredPart       - Composed parts (shape + material)
│   └── StructuredEquipment  - Terminal items (auto-baked)
└── CustomizableComponent    - Has upgrade slots
    └── CustomizableEquipment - Items with mutable upgrades
```

---

## 2. Strengths

### 2.1 Clean Two-Phase Resolution Architecture

**Rating: Excellent**

The BAKE → APPLY separation enables:
- O(1) attribute lookups for unconditional properties
- Complex conditional behaviors without performance penalties
- Clear debugging points (bake-time vs apply-time)

**Key Files:**
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/property/api/DataTypeEngine.java`

### 2.2 Well-Structured Condition System

**Rating: Excellent**

```java
public record Condition(
    List<StaticCondition> staticConditions,   // Evaluated once during bake
    List<DynamicCondition> dynamicConditions  // Evaluated per-query
) {}
```

The clean separation of static vs dynamic conditions enables:
- Compile-time verification of condition types
- Clear documentation of when each type applies
- Composable condition chains via AND semantics

**Key Files:**
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/condition/api/Condition.java`
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/condition/api/StaticCondition.java`
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/condition/api/DynamicCondition.java`

### 2.3 Comprehensive Documentation

**Rating: Good**

The existing architecture documentation in `docs/architecture/attribute-resolution.md` provides:
- Data flow diagrams
- Code examples
- Edge case handling
- Performance considerations

### 2.4 Extensive Test Coverage

**Rating: Good**

Tests thoroughly cover:
- Context matching rules (`AttributeContextMatchingTest`)
- Composition logic (`PartCompositeContextConditionTest`)
- Upgrade filtering (`UpgradeAttributeFilteringIntegrationTest`)
- Regression scenarios (`StructuredUpgradeCompositionRegressionTest`)

### 2.5 Modular Effect System

**Rating: Excellent**

The three-tier pattern for OnHit/OnTick:
1. **Selector** - WHO to affect (single, aoe, cone, chain)
2. **Filter** - NARROW the selection (is_hostile, health_threshold)
3. **Effect** - WHAT happens (fire, lightning, life_steal)

This enables complex behaviors through simple JSON composition.

---

## 3. Weaknesses and Inconsistencies

### 3.1 CRITICAL: "Context" is Overloaded (Three Different Meanings)

| Usage | Type | Meaning |
|-------|------|---------|
| `AttributeContext` | `OpenIdentifier` | WHERE/HOW attribute participates in composition |
| `ResolutionContext` | Class | Component tree topology during BAKE |
| `DynamicContext` | Class | Runtime game state during APPLY |

**Impact:** Developers must disambiguate based on static type alone. Code like:
```java
// These are completely unrelated concepts with the same word
attr.context()           // AttributeContext - composition scope
ResolutionContext ctx    // Tree topology
DynamicContext dctx      // Runtime state
```

**Key Files:**
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/attribute/api/AttributeContext.java`
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/property/context/ResolutionContext.java`
- `modules/core/src/main/java/com/sigmundgranaas/forgero/core/property/context/DynamicContext.java`

### 3.2 Attribute has `context()` AND `condition()` - Confusing Orthogonality

**File:** `modules/core/src/main/java/com/sigmundgranaas/forgero/core/attribute/api/Attribute.java`

```java
Optional<OpenIdentifier> context();  // Composition behavior (WHERE/HOW)
Optional<Condition> condition();     // Application criteria (WHEN)
```

**Confusion Points:**
1. Both control "when" the attribute applies, but in different phases
2. `context: "forgero:upgrade"` vs `condition: { in_slot_type: upgrade_slot }` - which to use?
3. The javadoc explains this, but the API doesn't make it obvious

**Decision Table (Currently Not Documented):**

| Goal | Use Context | Use Condition |
|------|-------------|---------------|
| Only compose in shape+material intersection | `part-composite` | - |
| Only apply when installed as upgrade | `upgrade` | - |
| Only apply in specific slot type | - | `in_slot_type` |
| Only apply when player sneaking | - | `is_sneaking` |
| Prevent propagation to parent | `local` | - |

### 3.3 Property vs Attribute Relationship is Structural but Non-Obvious

**Hierarchy:**
```
Property (marker interface)
    ↑
Attribute (sealed interface)
    ↑
SimpleAttribute (record implementation)
```

**Issue:** Attributes define their own `PropertyKey<Attribute>` and are stored separately:
```java
// Attributes stored under "forgero:attributes" key
PropertyKey<Attribute> KEY = new PropertyKey<>(Attribute.class, "forgero:attributes");

// Other properties under different keys
PropertyKey<OnHitProperty> ON_HIT_KEY = ...
```

Users cannot query all properties uniformly - must know the specific key.

### 3.4 Legacy vs Modern Codebase Coexistence

**Legacy:** `forgero-core-legacy-read-only/` - 312 Java files
**Modern:** `modules/core/` - 228 Java files

The legacy module contains:
- `PropertyStream` (different resolution model)
- `BaseAttribute` (different attribute model)
- `NumericOperation` enum vs `Operator` interface
- `AttributeCache` caching system

**Risk:** Accidental imports from legacy module could cause subtle bugs.

### 3.5 Composition Rules are Subtle and Underdocumented

**Rule in `PartCompositeContextHandler`:**
```java
// Need at least one base AND one multiplier from DIFFERENT sources
```

**Undocumented Edge Cases:**
1. Material has both base AND multiplier → treated as same source → discarded
2. Only base values, no multipliers → no composition output
3. Only multipliers, no bases → no composition output
4. Multiple bases from different sources → summed, then multiplied

### 3.6 SlotFiltering Logic in `matchesSlotContext()` is Complex

**File:** `modules/core/src/main/java/com/sigmundgranaas/forgero/core/attribute/api/AttributeContext.java`

Five different cases:
1. No context → always matches
2. UPGRADE context → always matches upgrade slots
3. Composite contexts → NEVER matches upgrade slots
4. Specific context + matching slot → matches
5. Specific context + non-matching/empty slot → doesn't match

This complexity led to the guard durability bug (part-composite was silently filtered).

---

## 4. Risks and Bug-Prone Areas

### 4.1 HIGH RISK: `matchesSlotContext()` Filtering Logic

**Location:** `AttributeContext.java:111-138`

**Why Risky:**
- Multiple special cases with different behaviors
- Silent filtering (attributes just disappear, no warning)
- Recent bug: guards not adding durability was caused by this

**Mitigation:**
- Comprehensive test coverage (exists)
- Logging when attributes are filtered (not implemented)

### 4.2 HIGH RISK: Recursive Collection in `CompositeAttributeBakingStrategy`

**Location:** `CompositeAttributeBakingStrategy.java`

Three separate recursive collection phases:
1. `collectContextAttributesRecursively()` - part-composite attrs
2. `collectDefaultAttributesRecursively()` - no-context attrs
3. `collectUpgradeAttributesRecursively()` - upgrade slot attrs

**Risks:**
- Deep hierarchies could cause stack overflow
- Phase interaction bugs (double-counting, missed attrs)
- Complex debugging when attributes don't appear

### 4.3 MEDIUM RISK: Intersection Composition Model

**Location:** `PartCompositeContextHandler.java`

The intersection model (base+multiplier from different sources) is:
- Powerful for intended use cases
- Confusing for edge cases
- Silently produces zero output when requirements not met

### 4.4 MEDIUM RISK: Tag Hierarchy Resolution

**Location:** Tag system (DAG structure)

Tags can inherit from other tags, creating complex hierarchies:
```
forgero:metal
  ├── forgero:iron
  ├── forgero:copper
  └── forgero:gold
```

**Risk:** Circular tag references could cause infinite loops (mitigated by cycle detection, but still a concern).

---

## 5. Recommendations

### 5.1 Terminology Refactoring (High Priority)

| Current | Recommended | Rationale |
|---------|-------------|-----------|
| `AttributeContext` | `CompositionScope` | Avoids confusion with `ResolutionContext` |
| `Attribute.context()` | `Attribute.compositionScope()` | Clear intent |
| `AttributeContext.LOCAL` | `CompositionScope.SELF_ONLY` | Self-documenting |
| `AttributeContext.PART_COMPOSITE` | `CompositionScope.SHAPE_MATERIAL` | Descriptive |

### 5.2 Documentation Improvements

1. **Add glossary** defining all "context" usages
2. **Add decision tree** for "context vs condition"
3. **Document edge cases** for composition rules
4. **Add logging guidance** for debugging attribute resolution

### 5.3 API Improvements

**Consider fluent builder:**
```java
Attribute.builder(DefaultAttributes.DURABILITY)
    .value(240f)
    .forPartComposition()      // Sets compositionScope = SHAPE_MATERIAL
    .onlyWhenInSlot(GEM_SLOT)  // Adds static condition
    .build();
```

### 5.4 Debug Tooling

Add resolution tracing:
```java
// Proposed API
AttributeResolutionTrace trace = attributeEngine.traceResolve(component);
trace.getFilteredAttributes();  // What was filtered out and why
trace.getComposedAttributes();  // What was composed and from which sources
trace.getFinalAttributes();     // Final result
```

### 5.5 Legacy Cleanup

1. Delete or archive `forgero-core-legacy-read-only/` after confirming no dependencies
2. Add lint rules preventing legacy imports
3. Document migration path for any remaining legacy code

---

## 6. Detailed Component Analysis

### 6.1 Attribute System

**Core Interface:** `Attribute.java`
```java
public sealed interface Attribute permits SimpleAttribute {
    PropertyKey<Attribute> KEY;
    Optional<OpenIdentifier> id();
    OpenIdentifier type();
    float value();
    Operator operator();
    int group();
    Optional<OpenIdentifier> context();   // Composition scope
    Optional<Condition> condition();       // Application conditions
}
```

**Operators:**
- `AdditionOperator` - `base + value`
- `MultiplicationOperator` - `base × value`
- `SubtractionOperator` - `base - value`
- `DivisionOperator` - `base / value`

**Groups:** Computation order (0 = base, 1 = middle, 2 = end)

### 6.2 Condition System

**StaticCondition Types:**
| Type | Key | Purpose |
|------|-----|---------|
| `IsRootCondition` | `forgero:is_root` | Component is tree root |
| `InSlotTypeCondition` | `forgero:in_slot_type` | In specific slot type |
| `AtDepthCondition` | `forgero:at_depth` | At specific tree depth |
| `TagMatchCondition` | `forgero:self_has_tag` | Component has tag |
| `HasSiblingCondition` | `forgero:has_sibling` | Has sibling with ID |
| `RootHasTagCondition` | `forgero:root_has_tag` | Root has tag |

**DynamicCondition Types:**
| Type | Key | Purpose |
|------|-----|---------|
| `IsSneakingCondition` | `forgero:is_sneaking` | Player sneaking |
| `TargetHasTagCondition` | `forgero:target_has_tag` | Target entity has tag |
| `IsRainingCondition` | `forgero:is_raining` | Weather is raining |
| `RandomChanceCondition` | `forgero:random_chance` | Random probability |

### 6.3 Resolution Contexts

**ResolutionContext** (BAKE phase):
```java
class ResolutionContext {
    Component self();                    // Current component
    Component root();                    // Root component
    Optional<Slot> getSlot();            // Slot containing self
    Optional<ComponentPart> getPart();   // Part containing self
    List<Component> getSiblings();       // Sibling components
    int getDepth();                      // Tree depth
    boolean isRoot();                    // Is root check
}
```

**DynamicContext** (APPLY phase):
```java
class DynamicContext {
    <T> Optional<T> get(Key<T> key);     // Type-safe value retrieval

    // Common keys:
    Key<Entity> TARGET_ENTITY;
    Key<World> WORLD;
    Key<PlayerEntity> PLAYER;
}
```

### 6.4 Effect System (OnHit/OnTick)

**Three-Tier Structure:**
```json
{
  "minecraft:on_hit": [{
    "selector": { "type": "forgero:aoe", "radius": 3 },
    "effects": [
      { "type": "forgero:fire", "duration": 5 },
      { "type": "forgero:lightning" }
    ],
    "condition": { "type": "forgero:is_sneaking" }
  }]
}
```

**Selectors:** `single_target`, `aoe`, `cone`, `chain`
**Effects:** `fire`, `lightning`, `life_steal`, `status_effect`, `knockback`, `explosion`
**Filters:** `is_alive`, `is_hostile`, `is_player`, `has_tag`, `health_threshold`

---

## 7. Data Flow Diagrams

### 7.1 Attribute Resolution Flow

```
JSON Definition
    │
    ▼
┌─────────────────┐
│ Attribute Codec │ ← Deserialization
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Component Tree  │ ← Store on components
│ (properties map)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Tree Traversal  │ ← Collect all components
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Context Filter  │ ← Filter by AttributeContext
│ (PART_COMPOSITE │    (matchesSlotContext)
│  UPGRADE, etc.) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Composition     │ ← Base × Multiplier
│ (intersection)  │    from different sources
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Static Condition│ ← in_slot_type, is_root
│ Filter          │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ BAKED RESULT    │ ← Cached
└────────┬────────┘
         │
         ▼ (Per Query)
┌─────────────────┐
│ Dynamic Condition│ ← is_sneaking, target_has_tag
│ Filter           │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ FINAL VALUE     │ ← AttributeQueryResult
└─────────────────┘
```

### 7.2 OnHit Effect Flow

```
Player Hits Entity
         │
         ▼
┌─────────────────┐
│ Get OnHit Props │ ← From component tree
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Check Condition │ ← is_sneaking, random_chance
└────────┬────────┘
         │ (pass)
         ▼
┌─────────────────┐
│ Selector.select │ ← Get entity list
│ (AOE/Cone/Chain)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Apply Filters   │ ← is_hostile, is_alive
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Apply Effects   │ ← fire, lightning, etc.
│ (foreach entity)│
└─────────────────┘
```

---

## Conclusion

The Forgero property and attribute resolution system is well-architected with strong separation of concerns. The main areas for improvement are:

1. **Terminology** - Rename `AttributeContext` to avoid confusion
2. **Documentation** - Add decision trees and edge case docs
3. **API** - Consider fluent builders for complex attribute definitions
4. **Debugging** - Add resolution tracing tools
5. **Cleanup** - Remove or archive legacy module

The recent guard durability bug demonstrates that the system's complexity requires vigilant testing and documentation. With the recommended improvements, the architecture would be significantly more approachable for new developers.

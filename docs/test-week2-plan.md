# Week 2 Test Plan: Variant Selection & Predicates

## Overview

Week 2 focuses on **variant selection** and **predicate evaluation** - the conditional rendering system that changes textures based on game state and component properties.

**Why Critical:**
- Variants power all dynamic visuals (bow pulling, gem colors, enchantment glows, etc.)
- Predicates determine when variants activate
- Wrong evaluation = wrong textures shown to players
- Complex boolean logic with multiple predicate types

## Planned Test Suites

### 1. VariantSelectionTest (6-8 tests)

Tests the core variant matching logic in `ModelLayer` and `TextureModel`.

#### Behaviors to Test

**Basic Matching:**
```java
testVariantSelectedWhenAllPredicatesMatch()
// Given: Variant with 3 predicates
// When: All 3 predicates match
// Then: Variant is selected

testNoVariantWhenAnyPredicateFails()
// Given: Variant with 3 predicates
// When: 2 match, 1 fails
// Then: Variant is NOT selected (fall back to default)
```

**Priority & Ordering:**
```java
testFirstMatchingVariantWins()
// Given: 3 variants, all match
// When: Resolve model
// Then: First variant in list is selected (deterministic)

testVariantOrderMatters()
// Given: Specific variant → General variant (both match)
// When: Resolve
// Then: Specific variant selected (order matters for specificity)
```

**Partial Override:**
```java
testVariantCanOverrideTextureOnly()
// Given: Variant with texture but no offset
// When: Variant matches
// Then: Use variant texture, keep base offset

testVariantCanOverrideOffsetOnly()
// Given: Variant with offset but no texture
// When: Variant matches
// Then: Use variant offset, keep base texture
```

**Model Replacement:**
```java
testVariantCanReplaceEntireModel()
// Given: Variant with model (not just texture)
// When: Variant matches (TextureModel.apply)
// Then: Entire model replaced with variant model
```

**Edge Cases:**
```java
testEmptyPredicateListAlwaysMatches()
// Given: Variant with empty predicate list
// When: Resolve
// Then: Variant always selected

testNoVariantsUsesDefaultTexture()
// Given: Model with no variants or no matching variants
// When: Resolve
// Then: Default texture used
```

#### Test Infrastructure Needed

```java
// Helper to create variants with specific predicates
Variant createVariant(String texture, Predicate... predicates);

// Helper to create models with variants
Model createModelWithVariants(Variant... variants);

// Mock predicates for testing
Predicate alwaysTrue();
Predicate alwaysFalse();
Predicate matchingPredicate(String key, Object value);
```

---

### 2. PredicateEvaluationTest (8-10 tests)

Tests all predicate types and their edge cases.

#### Predicate Types to Test

**BowPullPredicate:**
```java
testBowPullPredicateWithPullingTrue()
// State: {"pulling": true, "pull": 0.65}
// Predicate: {pulling: true, pull: 0.1}
// Result: MATCH (0.65 >= 0.1)

testBowPullPredicateWithPullingFalse()
// State: {"pulling": false, "pull": 0.9}
// Predicate: {pulling: true, pull: 0.1}
// Result: NO MATCH (pulling is false)

testBowPullPredicateAtZeroPull()
// State: {"pulling": true, "pull": 0.0}
// Predicate: {pulling: true, pull: 0.0}
// Result: MATCH (boundary case)

testBowPullPredicateMissingPullingKey()
// State: {"pull": 0.5}  // No "pulling" key
// Predicate: {pulling: true, pull: 0.0}
// Result: NO MATCH (defaults to !threshold)

testBowPullPredicateMissingPullKey()
// State: {"pulling": true}  // No "pull" key
// Predicate: {pulling: true, pull: 0.5}
// Result: NO MATCH (defaults to 0.0, 0.0 < 0.5)
```

**RootTagPredicate:**
```java
testRootTagPredicateWithMatchingTag()
// Component: Root with tag "forgero:ranged/bow"
// Predicate: {tag: "forgero:ranged/bow"}
// Result: MATCH

testRootTagPredicateWithoutMatchingTag()
// Component: Root with tag "forgero:ranged/arrow"
// Predicate: {tag: "forgero:ranged/bow"}
// Result: NO MATCH

testRootTagPredicateWithTagInheritance()
// Component: Root with tag "forgero:materials/metal/iron"
// Tag hierarchy: iron → metal → material
// Predicate: {tag: "forgero:materials/metal"}
// Result: MATCH (via inheritance, if TagResolver provided)

testRootTagPredicateWithoutResolver()
// Predicate: {tag: "forgero:some/tag"}
// Context: No TagResolver
// Result: Falls back to direct tag matching
```

**ChildTagPredicate:**
```java
testChildTagPredicateOnCurrentComponent()
// Component hierarchy: Bow → Arrow (current)
// Arrow has tag "forgero:ranged/arrow"
// Predicate: {tag: "forgero:ranged/arrow"}
// Result: MATCH (checks current, not root)

testChildTagPredicateVsRootTagPredicate()
// Hierarchy: Bow (root) → Head (child, current)
// Root tag: "ranged/bow", Child tag: "parts/head"
// RootTagPredicate("ranged/bow"): MATCH
// ChildTagPredicate("parts/head"): MATCH
// Different components checked
```

**Edge Cases:**
```java
testPredicateWithNullDynamicState()
// Dynamic state: null or missing
// Predicate: Requires dynamic state value
// Result: NO MATCH (graceful handling)

testPredicateWithWrongTypeInDynamicState()
// Dynamic state: {"pull": "not a number"}  // String instead of Float
// Predicate: BowPullPredicate
// Result: ClassCastException OR graceful NO MATCH
```

#### Test Infrastructure Needed

```java
// Helper to create contexts with specific state
ModelResolutionContext createContext(
    Component root,
    Component current,
    Map<String, Object> dynamicState
);

// Helper to create tagged components
Component createComponentWithTags(String... tags);

// Test predicates
testPredicate(Predicate predicate, ModelResolutionContext context);
```

---

## Test Models Needed

Create simple test models for variant testing:

**test-gem.json** (TextureModel with color variants):
```json
{
  "type": "forgero:texture_model",
  "textures": {
    "default": "forgero:item/gem/generic",
    "variants": [
      {
        "predicate": [
          { "type": "forgero:child_tag", "tag": "forgero:gem/ruby" }
        ],
        "texture": "forgero:item/gem/ruby"
      },
      {
        "predicate": [
          { "type": "forgero:child_tag", "tag": "forgero:gem/sapphire" }
        ],
        "texture": "forgero:item/gem/sapphire"
      }
    ]
  }
}
```

**test-enchanted_blade.json** (Model replacement variant):
```json
{
  "type": "forgero:texture_model",
  "textures": {
    "default": "forgero:item/blade/normal"
  },
  "variants": [
    {
      "predicate": [
        { "type": "forgero:root_tag", "tag": "forgero:enchanted" }
      ],
      "model": {
        "type": "forgero:texture_model",
        "textures": {
          "default": "forgero:item/blade/glowing"
        }
      }
    }
  ]
}
```

---

## Expected Outcomes

### Test Metrics

| Suite | Tests | Focus Area |
|-------|-------|------------|
| VariantSelectionTest | 6-8 | Variant matching logic |
| PredicateEvaluationTest | 8-10 | Predicate type correctness |
| **Total** | **14-18** | **Conditional rendering** |

### Coverage

**Variant Selection:**
- ✅ AND logic (all predicates must match)
- ✅ First-match-wins ordering
- ✅ Partial vs full override
- ✅ Model replacement
- ✅ Default fallback

**Predicate Evaluation:**
- ✅ All predicate types (BowPull, RootTag, ChildTag)
- ✅ Dynamic state access
- ✅ Tag matching (direct + inheritance)
- ✅ Edge cases (missing keys, wrong types, null state)

### Bugs Prevented

- **Wrong texture displayed**: Predicates evaluated incorrectly
- **Crashes from null state**: Missing dynamicState keys handled
- **Non-deterministic rendering**: Variant ordering ensures consistency
- **Tag inheritance bugs**: TagResolver integration tested

---

## Implementation Timeline

**Day 1-2:** VariantSelectionTest (6-8 tests)
- Core matching logic
- Ordering and priority
- Partial override
- Model replacement

**Day 3-4:** PredicateEvaluationTest setup (4-5 tests)
- BowPullPredicate tests
- RootTagPredicate tests
- Test infrastructure

**Day 5:** PredicateEvaluationTest completion (4-5 tests)
- ChildTagPredicate tests
- Edge cases (null, wrong types)
- Integration tests

---

## Success Criteria

- ✅ 14-18 tests passing (0 failures)
- ✅ All predicate types covered
- ✅ Variant selection logic validated
- ✅ Edge cases handled gracefully
- ✅ Test infrastructure reusable for Week 3

---

## Next: Week 3 & 4

After Week 2, we'll have:
- ✅ Dynamic slot resolution (Week 1)
- ✅ Mount point calculations (Week 1)
- ✅ Variant selection (Week 2)
- ✅ Predicate evaluation (Week 2)

Week 3 will cover:
- Rendering order & layering
- Slot mismatch detection
- Contextual model selection

Week 4 will cover:
- Error handling & robustness
- Dynamic state inheritance edge cases
- Performance & edge case testing

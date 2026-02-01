# Week 3 Test Plan: Rendering Order & Contextual Selection

## Overview

Week 3 focuses on **rendering order** and **contextual model selection** - the layering system that ensures textures render in correct z-order and the context-based model switching system.

**Why Critical:**
- Rendering order determines visual layering (gems on top, base textures below)
- Wrong order = visual glitches (textures behind/in front of wrong layers)
- Contextual selection enables same component to render differently based on usage context
- Slot type mismatches can cause crashes or missing textures

## Planned Test Suites

### 1. RenderingOrderTest (6-8 tests)

Tests the ordering/priority system that determines which textures render first (bottom layer) vs last (top layer).

#### Behaviors to Test

**Basic Ordering:**
```java
testSlotsRenderInDefinedOrder()
// Given: Composite model with slots ordered 1, 5, 10
// When: Resolve model
// Then: Textures appear in correct z-order (1 below, 10 on top)

testSlotOrderIndependentOfStructure()
// Given: Slots defined in arbitrary structure order
// When: Resolve model
// Then: Rendering respects order field, not structure order
```

**Layer Priority:**
```java
testHigherOrderRendersOnTop()
// Given: Gem slot (order=100), Base texture (order=1)
// When: Both resolve
// Then: Gem texture renders above base texture

testNegativeOrdersRenderFirst()
// Given: Background (order=-10), Normal (order=0), Overlay (order=10)
// When: Resolve
// Then: Layers stack in correct order (-10, 0, 10)
```

**Texture Layer Ordering:**
```java
testMultipleTexturesInSameSlotRespectOrder()
// Given: Slot with multiple texture layers
// When: Resolve
// Then: Textures within slot render in defined order

testDefaultOrderWhenUnspecified()
// Given: Slot without explicit order
// When: Resolve
// Then: Uses default order (likely 0 or insertion order)
```

**Edge Cases:**
```java
testTieBreakingWhenOrdersEqual()
// Given: Two slots with same order value
// When: Resolve
// Then: Deterministic tie-breaking (definition order or slot ID)

testOrderPreservedThroughDynamicSlots()
// Given: Dynamic slot with specific order
// When: Component loaded dynamically
// Then: Order respected in final render
```

#### Test Infrastructure Needed

```java
// Helper to extract render order from RenderableTexture list
List<String> getTextureOrder(List<RenderableTexture> textures);

// Helper to create components with specific slot orders
Component createComponentWithSlots(SlotDefinition... slots);

// Assertion helper
void assertTextureOrder(List<RenderableTexture> textures, String... expectedOrder);
```

---

### 2. ContextualModelSelectionTest (6-8 tests)

Tests the "context" field system that allows components to use different models based on where/how they're used.

#### Background: Contextual Models

The model system supports a "context" field that matches against the parent slot's "model_context":

```json
{
  "type": "forgero:texture_model",
  "target": "forgero:iron",
  "context": "arrow_shaft",  // ← Only applies in arrow_shaft context
  "textures": {...}
}
```

This allows the same material (e.g., "iron") to have different textures when used as arrow shaft vs tool handle.

#### Behaviors to Test

**Basic Context Matching:**
```java
testModelSelectedWhenContextMatches()
// Given: Component with context="arrow_shaft" model
// When: Inserted into slot with model_context="arrow_shaft"
// Then: Context-specific model is selected

testContextIgnoredWhenNoMatch()
// Given: Component with context="arrow_shaft" model
// When: Inserted into slot with model_context="tool_handle"
// Then: Default (no context) model is selected
```

**Context Priority:**
```java
testContextModelOverridesDefault()
// Given: Component with both default and context="X" models
// When: Slot has model_context="X"
// Then: Context model wins over default

testMultipleContextsSelectCorrectOne()
// Given: Component with context="A", context="B", and default models
// When: Slot has model_context="B"
// Then: Context="B" model selected (not A or default)
```

**Slot Type Matching:**
```java
testSlotTypeFilteringWorks()
// Given: Component with slot_type filters
// When: Inserted into slot of matching type
// Then: Type-filtered model selected

testSlotTypeMismatchFallsBackToDefault()
// Given: Component expects slot_type="GEM"
// When: Inserted into slot_type="SCHEMATIC"
// Then: Falls back to default model (or error)
```

**Nested Context:**
```java
testContextInheritsThroughSlots()
// Given: Parent slot with model_context="X"
// When: Child component has nested slots
// Then: Nested slots don't inherit parent context (unless explicitly set)

testDynamicSlotContextResolution()
// Given: Dynamic slot with model_context
// When: Component loaded from dynamicState
// Then: Context applied correctly
```

**Edge Cases:**
```java
testNoContextModelStillResolves()
// Given: Component without any context specification
// When: Inserted into slot with model_context
// Then: Uses default model (no crash)

testEmptyContextStringHandledGracefully()
// Given: Component with context=""
// When: Resolve
// Then: Treats as no context (not crash)
```

#### Test Infrastructure Needed

```java
// Helper to create components with multiple context models
Component createComponentWithContexts(ModelContext... contexts);

// Helper to create slots with specific model_context
ComponentPart createSlot(String id, String modelContext);

// Assertion helper
void assertModelContext(List<RenderableTexture> textures, String expectedContext);
```

---

## Test Models Needed

### Rendering Order Models

**test-layered-tool.json** (Composite model with ordered slots):
```json
{
  "type": "forgero:composite_model",
  "slots": [
    {
      "id": "base",
      "order": 1,
      "renderer": { "type": "forgero:component" }
    },
    {
      "id": "overlay",
      "order": 10,
      "renderer": { "type": "forgero:component" }
    },
    {
      "id": "gem",
      "order": 100,
      "renderer": { "type": "forgero:component" }
    }
  ]
}
```

**test-base.json**, **test-overlay.json**, **test-gem.json** (Simple texture models):
```json
{
  "type": "forgero:texture_model",
  "textures": {
    "default": "forgero:item/test/{name}"
  }
}
```

### Contextual Selection Models

**test-contextual-material.json** (Material with multiple contexts):
```json
{
  "type": "forgero:texture_model",
  "target": "forgero:oak",
  "textures": {
    "default": "forgero:item/material/oak"
  }
}
```

**test-contextual-arrow-shaft.json** (Context-specific model):
```json
{
  "type": "forgero:texture_model",
  "target": "forgero:oak",
  "context": "arrow_shaft",
  "textures": {
    "default": "forgero:item/arrow/oak_shaft"
  }
}
```

**test-contextual-handle.json** (Different context):
```json
{
  "type": "forgero:texture_model",
  "target": "forgero:oak",
  "context": "handle",
  "textures": {
    "default": "forgero:item/handle/oak"
  }
}
```

---

## Expected Outcomes

### Test Metrics

| Suite | Tests | Focus Area |
|-------|-------|------------|
| RenderingOrderTest | 6-8 | Texture z-order/layering |
| ContextualModelSelectionTest | 6-8 | Context-based model switching |
| **Total** | **12-16** | **Rendering & context** |

### Coverage

**Rendering Order:**
- ✅ Slot order determines z-order
- ✅ Higher order renders on top
- ✅ Negative orders supported
- ✅ Tie-breaking is deterministic
- ✅ Order preserved through dynamic slots

**Contextual Selection:**
- ✅ Context matching works
- ✅ Context overrides default
- ✅ Multiple contexts select correctly
- ✅ No-context models still render
- ✅ Slot type filtering works
- ✅ Mismatches handled gracefully

### Bugs Prevented

- **Incorrect layering**: Gems rendering behind base textures
- **Context failures**: Wrong model selected for context
- **Slot type crashes**: Mismatched slot types causing errors
- **Missing textures**: No fallback when context doesn't match

---

## Implementation Timeline

**Day 1-2:** RenderingOrderTest (6-8 tests)
- Basic ordering tests
- Layer priority tests
- Edge cases (tie-breaking, dynamic slots)

**Day 3-4:** ContextualModelSelectionTest setup (4 tests)
- Basic context matching
- Context priority
- Test infrastructure

**Day 5:** ContextualModelSelectionTest completion (4 tests)
- Slot type matching
- Nested context
- Edge cases

---

## Success Criteria

- ✅ 12-16 tests passing (0 failures)
- ✅ All rendering order scenarios covered
- ✅ Context-based model selection validated
- ✅ Edge cases handled gracefully
- ✅ Test infrastructure reusable for Week 4

---

## Combined Progress (Week 1-3)

After Week 3, we'll have:
- ✅ Dynamic slot resolution (Week 1) - 8 tests
- ✅ Mount point calculations (Week 1) - 7 tests
- ✅ Variant selection (Week 2) - 8 tests
- ✅ Predicate evaluation (Week 2) - 12 tests
- ✅ Rendering order (Week 3) - 6-8 tests
- ✅ Contextual selection (Week 3) - 6-8 tests

**Total: 47-51 behavioral tests covering core model resolution**

---

## Next: Week 4

Week 4 will cover:
- Error handling & robustness (malformed JSON, missing files)
- Dynamic state inheritance edge cases
- Performance regression detection
- Integration tests (full component hierarchies)

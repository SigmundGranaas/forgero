# Test Suite API Analysis

## User Questions

### 1. Do tests verify equipment-type-specific variants (e.g., handle with pickaxe override)?

**Current Status**: ❌ **NO** - Not explicitly tested

**What IS tested**:
- ✅ Context-based model selection (oak as arrow_shaft vs oak as handle)
- ✅ Same material in different contexts uses different models

**What is NOT tested**:
- ❌ Equipment-type-specific overrides (e.g., "oak handle when used in pickaxe" vs "oak handle when used in sword")
- ❌ Tool-type-specific material variants

**Example of missing coverage**:
```java
// NOT TESTED: Handle behaves differently in pickaxe vs sword
Component pickaxeHandle = oak_handle_in_pickaxe_context;  // Different texture
Component swordHandle = oak_handle_in_sword_context;      // Different texture
```

**Recommendation**: Add test if this is a real use case in your system.

---

### 2. Do tests verify different gems in different slots apply different textures?

**Current Status**: ⚠️ **PARTIAL** - Context differentiation tested, but not multi-slot gem scenarios

**What IS tested**:
- ✅ Same gem in different contexts (e.g., diamond as binding_gem vs guard_gem) - covered by ContextualModelSelectionTest
- ✅ Multiple dynamic slots receive state correctly - covered by DynamicSlotResolutionTest

**What is NOT tested**:
- ❌ Multi-gem equipment (e.g., sword with 3 gem slots, each containing different gems)
- ❌ Gem slot positioning (gem in slot A vs gem in slot B)

**Example of missing coverage**:
```java
// NOT TESTED: Tool with multiple gem slots
Component tool = createToolWithThreeGemSlots(
    slot1 -> diamond_gem,  // Should render at position 1
    slot2 -> emerald_gem,  // Should render at position 2
    slot3 -> ruby_gem      // Should render at position 3
);
// Expected: 3 different gem textures at different mount points
```

**Recommendation**: Add integration test if multi-gem equipment is a core feature.

---

### 3. Are tests using stable public APIs or brittle internal APIs?

**Current Status**: ⚠️ **MIXED** - Uses some implementation classes, but only through public interface methods

## API Usage Analysis

### Public Stable APIs (✅ Good)

Tests correctly use these stable public APIs:

1. **`ItemModelResolver`** (interface) - ✅ Public API
   - Method: `resolve(Component, Map<String, Object>)`
   - Location: `model.resolution.api.item.ItemModelResolver`
   - Stability: High (public interface, unlikely to change)

2. **`RenderableTexture`** (record) - ✅ Public API
   - Location: `model.api.RenderableTexture`
   - Stability: High (public data type)
   - Tests only use: `texture()`, `xOffset()`, `yOffset()`, `order()`

3. **`Component`** (interface) - ✅ Public API
   - Location: `core.component.api.Component`
   - Stability: High (core public interface)

4. **`OpenIdentifier`** (class) - ✅ Public API
   - Location: `common.identifier.api.OpenIdentifier`
   - Stability: High (core identifier system)

### Implementation Classes (⚠️ Potentially Brittle)

Tests directly instantiate these implementation classes:

1. **`RecursiveModelResolver`** (class) - ⚠️ Implementation class
   - Location: `model.resolution.impl.RecursiveModelResolver`
   - Issue: Direct instantiation couples tests to implementation
   - Impact: **Low** - Only uses public `ItemModelResolver` interface methods
   - Recommendation: Could use interface, but current usage is acceptable

2. **`MapBackedModelRegistry`** (class) - ⚠️ Implementation class
   - Location: `model.registry.impl.MapBackedModelRegistry`
   - Issue: Direct instantiation couples tests to implementation
   - Impact: **Low** - Only uses `ItemModelRegistry` interface methods
   - Recommendation: Acceptable for test setup

3. **`FileModelProvider`** (class) - ⚠️ Implementation class
   - Location: `model.loading.impl.FileModelProvider`
   - Issue: Direct instantiation couples tests to implementation
   - Impact: **Low** - Only used in test setup to load models
   - Recommendation: Acceptable for test infrastructure

4. **`TestComponentFactory`** (class) - ✅ Test utility
   - Location: Test package only
   - Issue: None (test-specific helper)
   - Impact: None
   - Recommendation: Good encapsulation

### Core Component APIs (⚠️ Low-level)

Tests use low-level component construction:

1. **`StaticComponent`** (class) - ⚠️ Low-level implementation
   - Location: `core.component.impl.StaticComponent`
   - Issue: Direct instantiation of core component implementation
   - Impact: **Medium** - Component structure changes could break tests
   - Recommendation: Encapsulate in `TestComponentFactory` (already done ✅)

2. **`ComponentPart`** (record) - ⚠️ Low-level structure
   - Location: `core.component.api.structure.ComponentPart`
   - Issue: Direct construction of component parts
   - Impact: **Medium** - Structure changes could affect tests
   - Recommendation: Encapsulate in `TestComponentFactory` (already done ✅)

3. **`ComponentStructure`** (class) - ⚠️ Low-level structure
   - Location: `core.component.api.structure.ComponentStructure`
   - Issue: Direct construction
   - Impact: **Medium** - Structure API changes could affect tests
   - Recommendation: Encapsulate in `TestComponentFactory` (already done ✅)

---

## Refactoring Risk Assessment

### High Risk (Would Break Tests)

❌ **Changing `ItemModelResolver.resolve()` signature**
- All 82 tests call this method
- Recommendation: Don't change public API

❌ **Changing `RenderableTexture` fields**
- Tests assert on `texture()`, `xOffset()`, `yOffset()`, `order()`
- Recommendation: Don't change public record

### Medium Risk (Would Require Test Updates)

⚠️ **Changing `ComponentPart` / `ComponentStructure` API**
- `TestComponentFactory` would need updates
- 82 tests use factory, but indirectly
- Recommendation: Update factory, tests unaffected

⚠️ **Changing component construction**
- `TestComponentFactory` would need updates
- Tests use factory, so impact is localized
- Recommendation: Update factory once, tests unaffected

### Low Risk (Would NOT Break Tests)

✅ **Changing `RecursiveModelResolver` internal implementation**
- Tests only use public `resolve()` method
- Internal changes don't affect tests
- Recommendation: Safe to refactor internals

✅ **Changing model loading logic**
- Tests load models in setup, but only use resolver
- Recommendation: Safe to change loading

✅ **Changing registry implementation**
- Tests use `ItemModelRegistry` interface
- Recommendation: Safe to swap implementations

---

## Recommendations

### Short Term (Improve Current Tests)

1. **Add missing test coverage**:
   - Equipment-type-specific variants (if this is a real feature)
   - Multi-gem equipment (if this is a real feature)

2. **Document TestComponentFactory better**:
   - Add comments explaining what each factory method creates
   - Add examples of how to create complex scenarios

### Medium Term (Reduce Brittleness)

1. **Use interface types in test fields**:
   ```java
   // Current (brittle):
   private RecursiveModelResolver resolver;

   // Better (resilient):
   private ItemModelResolver resolver;
   ```

2. **Consider factory methods for common scenarios**:
   ```java
   // Create test resolver with loaded models
   static ItemModelResolver createTestResolver() {
       ItemModelRegistry registry = new MapBackedModelRegistry();
       // ... load models ...
       return new RecursiveModelResolver(registry);
   }
   ```

### Long Term (Architectural)

1. **Consider dependency injection for tests**:
   - Create test module that provides pre-configured resolver
   - Tests don't need to know about implementation classes

2. **Create public test utilities module**:
   - `modules/model/test-utilities/` with reusable factories
   - Other modules can depend on it for testing

---

## Current Verdict

### Overall Brittleness: 🟡 **MEDIUM-LOW**

**Strengths**:
- ✅ Tests use public interface methods (`resolve()`)
- ✅ Tests assert on public API types (`RenderableTexture`)
- ✅ Low-level construction encapsulated in `TestComponentFactory`
- ✅ Tests are behavioral (don't check internal state)

**Weaknesses**:
- ⚠️ Tests instantiate implementation classes directly
- ⚠️ Component construction is low-level (but encapsulated)
- ⚠️ Missing coverage for some edge cases (multi-gem, equipment-type variants)

**Impact of Refactoring**:
- Changing **public APIs** (`ItemModelResolver`, `RenderableTexture`): ❌ **HIGH IMPACT** - All tests break
- Changing **internal implementation** (`RecursiveModelResolver` internals): ✅ **NO IMPACT** - Tests unaffected
- Changing **component structure** (`ComponentPart`, `ComponentStructure`): ⚠️ **MEDIUM IMPACT** - Update `TestComponentFactory`, tests unaffected
- Changing **registry/loading**: ✅ **LOW IMPACT** - Minor test setup changes

### Conclusion

The tests are **reasonably resilient** to internal refactoring:
- Internal resolver changes: **Safe** ✅
- Model loading changes: **Safe** ✅
- Component structure changes: **Localized to factory** ⚠️
- Public API changes: **High impact** ❌ (as expected)

**You can safely refactor internal implementations without touching tests**, as long as you maintain the public `ItemModelResolver` interface contract.

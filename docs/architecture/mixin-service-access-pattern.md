# Mixin Service Access Pattern - Implementation Plan

## Overview

This document outlines the architectural solution to replace the `AttributeManager` static singleton with a cleaner `MixinServiceAccessor` pattern that properly bridges Mixins to Forgero's service-oriented architecture.

## Goals

1. **Eliminate duplication**: Attribute calculation logic should live in one place (services)
2. **Improve testability**: Static accessor can be reset and injected with mocks
3. **Maintain fail-safety**: Returns vanilla/empty values if services unavailable
4. **Preserve performance**: No additional overhead compared to current solution
5. **Enable future refactoring**: Clear separation allows gradual migration

## Architecture Comparison

### Current (AttributeManager)
```
Mixin → AttributeManager (static singleton with business logic)
              ↓
    ComponentConverter (service)
              ↓
    AttributeEngine (calculation)
```

### New (MixinServiceAccessor)
```
Mixin → MixinServiceAccessor (static holder, no logic)
              ↓
    ForgeroServices (injected container)
              ↓
    ItemQueryApi / ComponentConverter (services)
              ↓
    AttributeEngine (calculation)
```

## Implementation Steps

### Phase 1: Create MixinServiceAccessor

**File**: `modules/mc/common/src/main/java/com/sigmundgranaas/forgero/common/api/MixinServiceAccessor.java`

**Key Design Decisions**:
- Single `AtomicReference<ForgeroServices>` field - no state duplication
- `initialize()` called once during ForgeroDataLoader initialization
- Convenience methods delegate 100% to services
- Fail-safe: returns empty/defaults if services unavailable

**Status**: ✅ Created

### Phase 2: Extend ItemQueryApi

**File**: `modules/mc/common/src/main/java/com/sigmundgranaas/forgero/common/api/item/ItemQueryApi.java`

**Add Methods**:
```java
Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
    ItemStack stack,
    Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap,
    EquipmentSlot slot
);

float getMiningSpeed(ItemStack stack, BlockState state);
```

**Status**: ⏳ To be implemented

### Phase 3: Implement ItemQueryApiImpl

**File**: `modules/mc/common/src/main/java/com/sigmundgranaas/forgero/common/api/item/ItemQueryApiImpl.java`

**Responsibilities**:
- Move all attribute modifier logic from AttributeManager
- Move mining speed calculation from ItemMiningMixin
- Handle armor UUIDs and vanilla attribute mapping
- Tool effectiveness checking (mining level + tool type)

**Status**: ⏳ To be implemented

### Phase 4: Update Mixins

**Files to Update**:
1. `ItemStackAttributeMixin.java` - Use MixinServiceAccessor
2. `ItemMiningMixin.java` - Use MixinServiceAccessor, simplify logic
3. `ItemStackDurabilityMixin.java` - Use MixinServiceAccessor

**Status**: ✅ Updated (simplified versions created)

### Phase 5: Update ForgeroDataLoader

**File**: `modules/mc/loader/src/main/java/com/sigmundgranaas/forgero/loader/ForgeroDataLoader.java`

**Changes**:
```java
// Replace:
AttributeManager.initialize(services.converter());

// With:
MixinServiceAccessor.initialize(services);
```

**Status**: ⏳ To be implemented

### Phase 6: Deprecate AttributeManager

**File**: `modules/mc/common/src/main/java/com/sigmundgranaas/forgero/common/attribute/AttributeManager.java`

**Changes**:
- Mark class as `@Deprecated(forRemoval = true)`
- Add Javadoc pointing to MixinServiceAccessor
- Keep for backward compatibility during migration period

**Status**: ⏳ To be implemented

## Testing Strategy

### Unit Tests

```java
@Test
void shouldReturnVanillaMapWhenServicesNotInitialized() {
    MixinServiceAccessor.reset();
    
    Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap = createVanillaMap();
    Multimap<EntityAttribute, EntityAttributeModifier> result = 
        MixinServiceAccessor.getAttributeModifiers(stack, vanillaMap, EquipmentSlot.MAINHAND);
    
    assertEquals(vanillaMap, result);
}

@Test
void shouldDelegateToServicesWhenInitialized() {
    ForgeroServices mockServices = mock(ForgeroServices.class);
    ItemQueryApi mockQuery = mock(ItemQueryApi.class);
    when(mockServices.itemQuery()).thenReturn(mockQuery);
    
    MixinServiceAccessor.initialize(mockServices);
    
    // Test that accessor delegates to services
    MixinServiceAccessor.getAttributeModifiers(stack, vanillaMap, EquipmentSlot.MAINHAND);
    verify(mockQuery).getAttributeModifiers(stack, vanillaMap, EquipmentSlot.MAINHAND);
}
```

### Integration Tests

- Verify all mixins work correctly with new accessor
- Verify attribute modifiers are applied correctly
- Verify mining speed calculations work
- Verify durability displays correctly

## Migration Path

### For Internal Code

1. **Immediate**: Update all mixins to use `MixinServiceAccessor`
2. **Short-term**: Mark `AttributeManager` as deprecated
3. **Medium-term**: Remove `AttributeManager` after one release cycle

### For External Modders

- No breaking changes - `AttributeManager` remains functional during deprecation period
- New code should use `ForgeroApi.services()` directly
- Mixins in external mods can use `MixinServiceAccessor` if needed

## Trade-offs

### Advantages

| Aspect | Improvement |
|--------|-------------|
| **Testability** | Can inject mock services for testing |
| **Clarity** | Clear separation between accessor and logic |
| **Maintainability** | Business logic lives in proper service classes |
| **Consistency** | Aligns with rest of service-oriented architecture |
| **Future-proof** | Easier to refactor services without touching mixins |

### Disadvantages

| Aspect | Impact | Mitigation |
|--------|--------|------------|
| **More classes** | Slightly more complex | Well-defined responsibilities |
| **Migration effort** | Need to update mixins | Can be done incrementally |
| **Temporary duplication** | Both systems exist during migration | Short deprecation period |

## Performance Considerations

The new pattern has **zero additional overhead**:

- `AtomicReference.get()` is lock-free and extremely fast
- No object creation in hot paths
- Same number of method calls as before
- Services are cached, not looked up each time

## Code Quality Metrics

### Before (AttributeManager)

| Metric | Value | Notes |
|--------|-------|-------|
| Lines of code | 167 | Business logic mixed with static state |
| Testability | Poor | Static state hard to mock |
| Coupling | High | Mixins coupled to implementation |
| Cohesion | Low | Multiple responsibilities |

### After (MixinServiceAccessor + Services)

| Metric | Value | Notes |
|--------|-------|-------|
| Lines of code (accessor) | ~100 | Pure delegation, no logic |
| Lines of code (services) | ~150 | Proper service implementation |
| Testability | Excellent | Injectable, mockable |
| Coupling | Low | Mixins depend on abstraction |
| Cohesion | High | Single responsibility per class |

## Conclusion

This architectural solution:

1. ✅ Eliminates the technical debt of `AttributeManager`
2. ✅ Maintains the same performance characteristics
3. ✅ Improves testability and maintainability
4. ✅ Provides a clean migration path
5. ✅ Aligns with Forgero's service-oriented architecture

The implementation is straightforward and can be completed in a single development cycle with minimal risk.

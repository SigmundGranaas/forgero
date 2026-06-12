# Forgero Attribute Resolution Architecture

> **⚠️ SUPERSEDED — do not treat as current.** This document describes the original
> runtime-resolution design. The architecture has since inverted: attributes are **compiled once
> at component construction** (the factory), not resolved at runtime, and all stat composition is
> done by the **`StatFold` kernel**. The runtime-traversal engine, the scope-handler/baking-strategy
> machinery, and `ScopeMatcher`/`matchesSlotScope` described below have been **deleted**.
> See **`docs/ADR-002-compiler-in-the-factory.md`** and **`docs/ADR-003-stat-contribution-kernel.md`**
> for the current design. Kept for historical context only.

## Core Design Principle

**Attributes in structured components are NOT pre-computed at generation time. They are lazily resolved at runtime by traversing the component structure tree.**

---

## The Complete Pipeline

### Stage 1: JSON Data Loading
- Materials, templates, and static components defined in JSON
- Each has attributes with optional `context` field (e.g., `"context": "forgero:part-composite"`)

### Stage 2: Template Generation (TemplateGenerator)

**Purpose:** Create component DTOs (CofComponent) by combining templates with materials

**Part Generation** (iron-pickaxe_head):
```java
// Input: pickaxe_head template + iron material
List<DefinitionData> dtoList = [pickaxe_head template, iron material];
PropertyMerger.merge(dtoList);

// Output: CofComponent {
//   id: "iron-pickaxe_head",
//   properties: {durability_mult: 1.0, mining_speed_mult: 1.2},  // ONLY template attributes
//   structure: {
//     material: CofSlot(content: iron CofComponent)  // iron attributes stay here
//   }
// }
```

**Equipment Generation** (iron-pickaxe):
```java
// Input: pickaxe template + parts (iron-pickaxe_head, wooden_handle)
List<DefinitionData> rawPartsDtoList = getSourceDtosForComponent(iron-pickaxe_head) + ...;
List<DefinitionData> fullDtoList = [pickaxe template] + rawPartsDtoList;
PropertyMerger.merge(fullDtoList);

// Output: CofComponent {
//   id: "iron-pickaxe",
//   properties: {attack_speed: -2.8},  // ONLY equipment template attributes
//   structure: {
//     head: CofSlot(content: iron-pickaxe_head CofComponent),
//     handle: CofSlot(content: wooden_handle CofComponent)
//   }
// }
```

### Stage 3: Component Building (ComponentBuilder)

**Purpose:** Convert CofComponent DTOs to runtime Component objects with proper structure tree

**Recursive Structure Building:**
```java
// For iron-pickaxe CofComponent:
buildComponentFromDto(iron-pickaxe) {
  buildStructureFromDto(cofStructure) {
    for each slot in cofStructure:
      childComponent = buildFromId(slot.content.id);  // RECURSIVELY build children
      parts.add(new ComponentPart(childComponent));
  }
  return StructuredEquipment(id, tags, properties, structure);
}
```

**Result - Component Tree:**
```
iron-pickaxe (StructuredEquipment)
  properties: {attack_speed: -2.8}  // From pickaxe template
  structure:
    head: ComponentPart ──→ iron-pickaxe_head (StructuredPart)
                              properties: {durability_mult: 1.0, mining_speed_mult: 1.2}  // From pickaxe_head template
                              structure:
                                material: ComponentPart ──→ iron (StaticComponent)
                                                              properties: {durability: 240, mining_speed: 6.0, ...}
    handle: ComponentPart ──→ wooden_handle (StaticComponent)
                                properties: {durability: 50, ...}
```

### Stage 4: Runtime Attribute Resolution (ResolverEngine)

**Purpose:** Dynamically compute effective attributes by traversing the structure tree

**Process:**
```java
// When querying attributes on iron-pickaxe:
ResolverEngine.resolve(iron-pickaxe, engine, context) {
  // 1. Traverse the entire component tree
  List<Component> allComponents = traverse(iron-pickaxe);

  // traverse() uses getChildren() recursively:
  // iron-pickaxe.getChildren() → [iron-pickaxe_head, wooden_handle]
  // iron-pickaxe_head.getChildren() → [iron]
  // wooden_handle.getChildren() → []

  // Result: [iron-pickaxe, iron-pickaxe_head, iron, wooden_handle]

  // 2. Pass all components to engine for composition
  B bakedResult = engine.bake(allComponents.stream());

  // engine.bake() composes attributes from all components:
  // - Applies context filtering (forgero:part-composite, etc.)
  // - Merges base values with multipliers
  // - Computes final values

  // 3. Apply dynamic context
  return engine.apply(bakedResult, context);
}
```

**Key Point:** The **engine.bake()** method receives ALL components in the tree and performs the actual attribute composition!

---

## PropertyMerger's Role

**PropertyMerger is ONLY for includes/extensions, NOT compositional attribute merging.**

### Template-Based Merge Behavior
```java
// PropertyMerger.merge(dtoList) where dtoList contains TemplateData:
if (isTemplateBasedMerge) {
  // ONLY include attributes from TemplateData
  if (dto instanceof TemplateData) {
    mergedAttributes.addAll(dto.attributes());
  }
  // Material/shape attributes are EXCLUDED
  // They stay on their respective components in the structure
}
```

**Why?**
- Template attributes (multipliers/modifiers) belong on the generated component
- Material attributes (base values) belong on the material component in structure
- Runtime resolution composes them by traversing the tree

---

## The Bug: My Incorrect Fix

### What I Did Wrong

```java
// My fix in getSourceDtosForComponent():
private List<DefinitionData> getSourceDtosForComponent(CofComponent component) {
    // ...
    RawDefinition templateDef = generatedComponentToTemplate.get(component.id());
    if (templateDef != null) {
        result.add(templateDef.data());  // ❌ WRONG!
    }
    // ...
}
```

**Why it's wrong:**
1. Added pickaxe_head template to iron-pickaxe's PropertyMerger input
2. This puts pickaxe_head template attributes on iron-pickaxe
3. **Violates separation:** pickaxe_head template attributes should be on iron-pickaxe_head (in structure), not on iron-pickaxe itself
4. **Breaks lazy resolution:** Attributes that should be runtime-resolved from structure are being pre-computed during generation

**Effect:**
```
WRONG (with my fix):
iron-pickaxe
  properties: {
    attack_speed: -2.8,           // Correct (from pickaxe template)
    durability_mult: 1.0,          // WRONG! Should be on iron-pickaxe_head
    mining_speed_mult: 1.2         // WRONG! Should be on iron-pickaxe_head
  }

CORRECT (without my fix):
iron-pickaxe
  properties: {attack_speed: -2.8}  // Only equipment template
  structure:
    head: iron-pickaxe_head
            properties: {durability_mult: 1.0, mining_speed_mult: 1.2}  // Template attributes here!
```

---

## Where Was The Actual Bug? ✅ RESOLVED

**Resolution Date:** 2026-01-03
**Commit:** 500aba5ad "Fix nested structure attribute composition bug"
**Test:** `CompositeAttributeResolutionBugTest.java`

The structure is correct, PropertyMerger is correct, ComponentBuilder is correct. The bug was in **runtime attribute composition**.

### Root Cause

**Location:** `CompositeAttributeBakingStrategy.composePartAttributes()` (lines 105-119)

When collecting part-composite attributes from structure slot children, the method only looked at the **direct child's properties**, missing attributes nested in the child's structure.

**Example:**
```
iron-pickaxe (StructuredEquipment)
  structure:
    head → iron-pickaxe_head (StructuredPart)
             properties: {durability_mult: 1.0}  ← Only template multiplier
             structure:
               material → iron (StaticComponent)
                            properties: {durability: 240}  ← Base value missed!
```

The old code did:
```java
List<Attribute> childAttrs = child.properties(KEY).stream()
    .filter(attr -> attr.context().equals(AttributeContext.PART_COMPOSITE))
    .toList();
```

This only saw `iron-pickaxe_head.properties()` (multiplier), not `iron.properties()` (base value).

### The Fix

Added `collectPartCompositeAttributesRecursively()` method that traverses the entire component tree:

```java
private List<Attribute> collectPartCompositeAttributesRecursively(Component component) {
    List<Attribute> result = new ArrayList<>();

    // Add component's own part-composite attributes
    result.addAll(component.properties(KEY).stream()
        .filter(attr -> attr.context().equals(AttributeContext.PART_COMPOSITE))
        .toList());

    // Recursively collect from children if structured
    if (component instanceof StructuredComponent structured) {
        for (ComponentPart part : structured.structure().allParts()) {
            result.addAll(collectPartCompositeAttributesRecursively(part.getContent()));
        }
    }

    return result;
}
```

Now when composing iron-pickaxe:
- head slot: `[durability_mult: 1.0 from iron-pickaxe_head, durability: 240 from iron]`
- handle slot: `[durability: 50 from wooden_handle]`
- Composition succeeds: 290 durability (240 from iron + 50 from oak)

### Lesson Learned

**Always write unit tests BEFORE fixing complex bugs.**

The bug was caught by integration tests (game tests), but:
- Integration tests are slow (15+ seconds)
- Hard to debug (full Minecraft environment)
- Fail for many reasons (noisy)

A focused unit test (`CompositeAttributeResolutionBugTest`) reproduced the bug in <1 second and made the fix obvious. This test now prevents regression.

---

## Action Items

1. ✅ Documented architecture
2. ⏳ Revert incorrect fix in TemplateGenerator
3. ⏳ Find where attributes are queried (ItemStack durability, etc.)
4. ⏳ Trace that code path to verify ResolverEngine is used
5. ⏳ Debug why runtime resolution isn't finding iron material attributes

---

## Key Files

- **TemplateGenerator:** `modules/core/src/main/java/com/sigmundgranaas/forgero/data/pipeline/impl/TemplateGenerator.java`
- **PropertyMerger:** `modules/core/src/main/java/com/sigmundgranaas/forgero/data/pipeline/impl/PropertyMerger.java`
- **ComponentBuilder:** `modules/core/src/main/java/com/sigmundgranaas/forgero/data/pipeline/impl/ComponentBuilder.java`
- **ResolverEngine:** `modules/core/src/main/java/com/sigmundgranaas/forgero/core/property/engine/ResolverEngine.java`
- **Component interfaces:** `modules/core/src/main/java/com/sigmundgranaas/forgero/core/component/api/`

# Model Validation Implementation Plan

> **Status: 60% COMPLETE** (January 2026)
>
> Core validation (reference, semantic, palette, animation) is implemented.
> Remaining: template chain validation and cross-reference validation.

---

## Quick Status

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Reference Validation | ✅ Complete |
| Phase 2 | Template Validation | ❌ Not implemented |
| Phase 3 | Semantic Validation | ✅ Complete |
| Phase 4 | Cross-Reference Validation | ❌ Not implemented |
| Phase 5 | Palette & Texture Quality | ✅ Complete |
| Phase 6 | Gradle Configuration | ⚠️ Partial |

---

## Implemented Classes

```
modules/model/src/main/java/.../model/validation/
├── api/
│   ├── ModelValidator.java
│   ├── ModelValidationResult.java
│   ├── TextureValidationResult.java
│   ├── ReferenceValidationResult.java
│   ├── SemanticValidationResult.java
│   ├── PaletteConformityResult.java
│   ├── AnimatedTextureResult.java
│   └── ComprehensiveModelValidationResult.java
├── impl/
│   ├── DefaultModelValidator.java
│   ├── ReferenceValidator.java
│   ├── SemanticValidator.java
│   ├── PaletteConformityValidator.java
│   └── AnimatedTextureValidator.java
└── util/
    ├── TexturePathResolver.java
    └── McmetaParser.java
```

---

## Completed Work

### Phase 1: Reference Validation ✅ COMPLETE

**Implemented in:** `ReferenceValidator.java`, `ReferenceValidationResult.java`

Features:
- Texture path resolution and existence validation
- Parent model reference validation
- Slot reference validation
- Error reporting with file paths and field locations

### Phase 3: Semantic Validation ✅ COMPLETE

**Implemented in:** `SemanticValidator.java`, `SemanticValidationResult.java`

Features:
- Layer order validation (unique, positive integers)
- Predicate validation (known types, required fields)
- Mount point validation (unique names, valid positions)

### Phase 5: Palette & Texture Quality ✅ COMPLETE

**Implemented in:** `PaletteConformityValidator.java`, `AnimatedTextureValidator.java`

Features:
- Palette conformity (height=1, width≥8, no transparency)
- Standard width checking (8, 16, or 32 pixels)
- Color progression validation (dark→light)
- Animated texture `.mcmeta` validation
- Frame count and frametime validation

---

## Remaining Work

### Phase 2: Template Validation ❌ NOT IMPLEMENTED

**Goal:** Validate template-palette-output chains and placeholder resolution.

**Missing Classes:**
- `TemplateChainValidator.java`
- `PlaceholderValidator.java`
- `TemplateValidationResult.java`

**Validation needed:**
1. Template file exists for each layer
2. Palette file exists (after placeholder resolution)
3. Placeholders (`{target.name}`, `{material.name}`) resolve correctly
4. Target tags exist and produce results

**Example validation:**
```json
{
  "template": "forgero:texture_templates/parts/handle",
  "palette": "forgero:palettes/{target.name}",
  "output": "forgero:item/{target.name}-handle"
}
```
- Verify `texture_templates/parts/handle.png` exists
- Verify `palettes/{target.name}.png` exists for all targets
- Verify no duplicate outputs

### Phase 4: Cross-Reference Validation ❌ NOT IMPLEMENTED

**Goal:** Verify models exist for all components and detect circular references.

**Missing Classes:**
- `CrossReferenceValidator.java`
- `ModelRegistry.java`

**Validation needed:**
1. All generated components have models
2. All materials have palettes
3. No circular parent references
4. Report orphaned models (no component)

---

## Asset Inventory

| Category | Count | Location |
|----------|-------|----------|
| Modern Model Templates | 63 | `assets/forgero/forgero_models/` |
| Armor Model Templates | 6 | `data/forgero/forgero_models/` |
| Texture Templates | 353 | `assets/forgero/templates/textures/` |
| Palettes | 95 | `assets/forgero/textures/palette/` |
| Static Textures | 152 | `assets/forgero/textures/item/` |

---

## Palette Specification

### Format Rules

| Property | Requirement | Severity |
|----------|-------------|----------|
| File format | PNG | ERROR |
| Height | Exactly 1 pixel | ERROR |
| Width | ≥ 8 pixels | ERROR |
| Width (recommended) | 8, 16, or 32 pixels | WARNING |
| Transparency | No transparent pixels (alpha = 255) | ERROR |
| Color order | Dark → Light, left to right | WARNING |

### Standard Palette Layout (16-pixel)

```
Pixel:  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
Color:  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██  ██
        ↑                           ↑                               ↑
      Shadow                     Midtone                        Highlight
```

### Animated Palette Rules

| Property | Requirement | Severity |
|----------|-------------|----------|
| Height | > 1 pixel (stacked frames) | - |
| `.mcmeta` file | Required alongside PNG | ERROR |
| Frame height | Image height divisible by frame height | ERROR |
| `frametime` | Positive integer | ERROR |

---

## Task Checklist

### Completed
- [x] Texture path resolution utility
- [x] Texture existence validation
- [x] Parent model reference validation
- [x] Layer order validation
- [x] Predicate validation
- [x] Mount point validation
- [x] Palette conformity validation
- [x] Animated texture validation
- [x] Comprehensive result aggregation

### Remaining (Medium Priority)
- [ ] Template-palette-output chain validation
- [ ] Placeholder resolution validation
- [ ] Target tag validation
- [ ] Model-component consistency check
- [ ] Circular reference detection
- [ ] Texture coverage validation

### Low Priority
- [ ] Enhanced Gradle task configuration
- [ ] Report formatting improvements
- [ ] CI integration documentation

---

## Success Criteria

1. **Zero false positives**: Valid content should never produce errors
2. **Helpful error messages**: Include file path, line context, and suggested fixes
3. **Fast execution**: Full validation < 30 seconds
4. **Incremental value**: Each phase independently useful
5. **CI integration**: Runs on every PR, blocks merge on errors

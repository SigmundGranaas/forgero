# Model Module Test Plan

## Current Coverage: 10 tests (~9%)
## Target Coverage: 35+ tests (~30%)

## Test Strategy

### Priority 1: Critical Path Coverage (Codecs & Loading)
**Why**: These are data entry points - failures here break everything downstream

1. **ModelCodecs** - JSON deserialization
2. **ModelTemplateCodecs** - Template deserialization
3. **ArmorModelCodecs** - Armor model deserialization
4. **ModelTranslator** - DTO → Domain conversion
5. **ArmorModelTranslator** - Armor DTO → Domain conversion

### Priority 2: Business Logic (Registry & Resolution)
**Why**: Core functionality - storage and retrieval

6. **MapBackedModelRegistry** - Basic model storage
7. **TargetKeyedModelRegistry** - Contextual lookup
8. **CompositeContextualModelRegistry** - Multi-registry composition
9. **RecursiveModelResolver** - Component tree resolution

### Priority 3: Data Validation (DTOs)
**Why**: Early detection of malformed data

10. **ModelDTO** - Validation tests
11. **LayerDTO** - Validation tests
12. **TexturesDTO** - Validation tests
13. **VariantDTO** - Validation tests

### Priority 4: Edge Cases (Utilities & Predicates)
**Why**: Prevent subtle bugs in edge cases

14. **BowPullPredicate** - Context matching
15. **RootTagPredicate** - Tag matching
16. **ChildTagPredicate** - Child tag matching

## Test Categories

### Unit Tests (New)
- Codecs: 3 test files
- Translators: 2 test files
- Registries: 3 test files
- DTOs: 4 test files
- Predicates: 3 test files
- Utilities: 2 test files

**Total New Tests: 17 files**

### Integration Tests (Existing)
- FullPickaxeRenderTest
- ModelPipelineFullIntegrationTest
- FileModelProviderTest
- ModelResolverTest
- AwtTextureCompositorTest
- DefaultAnimatedPalettizedTextureGeneratorTest
- VerticalStripFramedTextureTest
- RowBasedFramedPaletteTest
- AnimationMetadataDTOTest
- ModelRegistrationServiceTest

**Existing: 10 files**

### Total After Plan: 27 test files (~26% coverage)

## Implementation Order

1. ✅ Analyze gaps (DONE)
2. ⏳ Codec tests (HIGH PRIORITY)
3. ⏳ Translator tests (HIGH PRIORITY)
4. ⏳ Registry tests (MEDIUM PRIORITY)
5. ⏳ DTO validation tests (MEDIUM PRIORITY)
6. ⏳ Predicate tests (LOW PRIORITY)

## Success Criteria

- ✅ All new tests pass
- ✅ Coverage increases from 9% → 26%+
- ✅ Critical paths (codecs, loading, registry) have unit tests
- ✅ No regressions in existing tests
- ✅ Build time remains reasonable (<2min for model module)

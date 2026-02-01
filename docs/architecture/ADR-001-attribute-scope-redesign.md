# ADR-001: Attribute Scope Redesign

**Status:** Implemented
**Date:** 2026-01-22
**Authors:** Architecture Review
**Supersedes:** Previous AttributeContext system (removed)

---

## Context and Problem Statement

The current attribute system uses the term "context" for three unrelated concepts:
1. **`AttributeContext`** - WHERE/HOW an attribute participates in composition
2. **`ResolutionContext`** - Component tree topology during BAKE phase
3. **`DynamicContext`** - Runtime game state during APPLY phase

This overloading causes confusion. Additionally, the `Attribute.context()` method sounds like it controls WHEN an attribute applies, but it actually controls WHERE it participates in the composition pipeline.

### Current API Pain Points

```java
// CONFUSING: "context" sounds like "when", but means "where"
Optional<OpenIdentifier> context();   // Actually: composition scope
Optional<Condition> condition();       // Actually: when it applies

// CONFUSING: Three different "contexts"
AttributeContext.PART_COMPOSITE       // Composition scope
ResolutionContext ctx                  // Tree topology
DynamicContext dctx                    // Runtime state
```

### Real-World Confusion Example

```json
// User asks: "How do I make this attribute only apply when sneaking?"
// Wrong answer (context controls WHERE, not WHEN):
{ "type": "forgero:attack_damage", "context": "forgero:sneaking" }

// Correct answer (condition controls WHEN):
{ "type": "forgero:attack_damage", "condition": { "type": "forgero:is_sneaking" } }
```

---

## Decision

### 1. Rename `AttributeContext` → `AttributeScope`

The word "scope" clearly indicates WHERE something is visible/active, not WHEN.

### 2. Rename `Attribute.context()` → `Attribute.scope()`

Aligns with the class rename and clarifies intent.

### 3. Create Clear Separation of Concerns

| Concept | Class | Purpose | Question Answered |
|---------|-------|---------|-------------------|
| **Scope** | `AttributeScope` | WHERE attribute participates in composition | "In which composition phase?" |
| **Condition** | `Condition` | WHEN attribute applies at runtime | "Under what circumstances?" |
| **Bake Context** | `ResolutionContext` | Tree topology during bake | "What's the component structure?" |
| **Apply Context** | `DynamicContext` | Runtime state during apply | "What's the game state?" |

### 4. Introduce Fluent Builder API

Make the API self-documenting and hard to misuse.

---

## New API Design

### Core Types

```java
/**
 * Defines WHERE an attribute participates in the composition pipeline.
 *
 * <p>Scope is orthogonal to conditions:</p>
 * <ul>
 *   <li><b>Scope</b> - WHERE: Which composition phase? (part, equipment, upgrade)</li>
 *   <li><b>Condition</b> - WHEN: What circumstances? (sneaking, raining, target type)</li>
 * </ul>
 *
 * @see Condition for controlling WHEN attributes apply
 */
public final class AttributeScope {

    // ========================================================================
    // BUILT-IN SCOPES
    // ========================================================================

    /**
     * Participates in shape+material composition when building parts.
     *
     * <p>Attributes from shape (multipliers) and material (bases) intersect:</p>
     * <ul>
     *   <li>Only attribute types present in BOTH sources produce output</li>
     *   <li>Result = sum(bases) × product(multipliers)</li>
     * </ul>
     *
     * <p>Example: Iron material (durability: +240) + pickaxe_head shape (durability: ×1.0)
     * → Iron pickaxe head (durability: 240)</p>
     */
    public static final OpenIdentifier PART_COMPOSITE =
        new OpenIdentifier("forgero", "scope/part-composite");

    /**
     * Participates in parts→equipment composition.
     *
     * <p>Used when combining multiple parts (head, handle, binding) into equipment.</p>
     */
    public static final OpenIdentifier EQUIPMENT_COMPOSITE =
        new OpenIdentifier("forgero", "scope/equipment-composite");

    /**
     * Only applies when the component is installed as an upgrade.
     *
     * <p>Upgrade-scoped attributes are filtered out when the component is used
     * as a primary material. They only contribute when in an upgrade slot.</p>
     *
     * <p>Example: A gem's "socket bonus" that only applies when installed,
     * not when used as crafting material.</p>
     */
    public static final OpenIdentifier UPGRADE =
        new OpenIdentifier("forgero", "scope/upgrade");

    /**
     * Only applies to the component itself, does not propagate to parent.
     *
     * <p>Local attributes are filtered out during tree traversal and don't
     * contribute to the parent component's final values.</p>
     *
     * <p>Example: A material's internal "crafting modifier" that only matters
     * during its own processing.</p>
     */
    public static final OpenIdentifier LOCAL =
        new OpenIdentifier("forgero", "scope/local");

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Checks if the given scope participates in composition (requires transformation).
     */
    public static boolean isCompositionScope(OpenIdentifier scope) {
        return PART_COMPOSITE.equals(scope) || EQUIPMENT_COMPOSITE.equals(scope);
    }

    /**
     * Checks if the given scope is a filter scope (simple include/exclude).
     */
    public static boolean isFilterScope(OpenIdentifier scope) {
        return LOCAL.equals(scope) || UPGRADE.equals(scope);
    }

    /**
     * Determines if an attribute with the given scope should be included
     * when collecting from an upgrade slot.
     *
     * <p>Rules:</p>
     * <ol>
     *   <li>No scope (null/empty) → always included (default behavior)</li>
     *   <li>UPGRADE scope → always included in upgrade slots</li>
     *   <li>Composition scopes (PART_COMPOSITE, EQUIPMENT_COMPOSITE) → NEVER included
     *       (these should be composed first, producing scopeless output)</li>
     *   <li>Specific scope + matching slot → included</li>
     *   <li>Specific scope + non-matching slot → excluded</li>
     * </ol>
     *
     * @param attributeScope The attribute's scope (may be empty)
     * @param slotScope The slot's scope filter (may be empty)
     * @return true if the attribute should be included for this slot
     */
    public static boolean matchesSlotScope(
            Optional<OpenIdentifier> attributeScope,
            Optional<OpenIdentifier> slotScope) {
        // No scope = default, always included
        if (attributeScope.isEmpty()) {
            return true;
        }

        OpenIdentifier attrScope = attributeScope.get();

        // Composition scopes must be composed first, not passed through raw
        if (isCompositionScope(attrScope)) {
            return false;
        }

        // UPGRADE scope always passes in upgrade slots
        if (UPGRADE.equals(attrScope)) {
            return true;
        }

        // For other scopes, require exact match with slot scope
        return slotScope.map(attrScope::equals).orElse(false);
    }

    private AttributeScope() {} // Utility class
}
```

### Attribute Interface

```java
/**
 * A numerical attribute that can be composed and resolved.
 *
 * <h2>Two Orthogonal Concerns</h2>
 *
 * <table>
 *   <tr><th>Concern</th><th>Method</th><th>Question</th><th>Example</th></tr>
 *   <tr>
 *     <td>Scope</td>
 *     <td>{@link #scope()}</td>
 *     <td>WHERE does this participate?</td>
 *     <td>"Only in part composition"</td>
 *   </tr>
 *   <tr>
 *     <td>Condition</td>
 *     <td>{@link #condition()}</td>
 *     <td>WHEN does this apply?</td>
 *     <td>"Only when sneaking"</td>
 *   </tr>
 * </table>
 *
 * <h2>Lifecycle</h2>
 * <pre>
 * 1. LOAD:  JSON → Attribute (with scope and condition)
 * 2. BAKE:  Filter by scope → Compose → Filter by static conditions → Cache
 * 3. APPLY: Filter by dynamic conditions → Return final value
 * </pre>
 *
 * @see AttributeScope for WHERE this attribute participates
 * @see Condition for WHEN this attribute applies
 */
public sealed interface Attribute permits SimpleAttribute {

    PropertyKey<Attribute> KEY = new PropertyKey<>(Attribute.class, "forgero:attributes");

    /** Optional unique identifier for this attribute instance. */
    Optional<OpenIdentifier> id();

    /** The attribute type (e.g., forgero:durability, forgero:mining_speed). */
    OpenIdentifier type();

    /** The numerical value. */
    float value();

    /** How this value combines with others (add, multiply, etc.). */
    Operator operator();

    /** Ordering group for computation (0 = first, higher = later). */
    int group();

    /**
     * WHERE this attribute participates in the composition pipeline.
     *
     * <p>Empty means default behavior: propagates normally without special handling.</p>
     *
     * @return The composition scope, or empty for default propagation
     * @see AttributeScope for built-in scopes
     */
    Optional<OpenIdentifier> scope();

    /**
     * WHEN this attribute applies at runtime.
     *
     * <p>Conditions are evaluated AFTER composition, during resolution.
     * Static conditions are evaluated during BAKE, dynamic during APPLY.</p>
     *
     * @return The condition, or empty if always applies
     * @see Condition
     */
    Optional<Condition> condition();

    // ========================================================================
    // FLUENT BUILDER
    // ========================================================================

    /**
     * Creates a builder for constructing attributes with clear intent.
     *
     * <pre>
     * // Simple attribute
     * Attribute.of(DURABILITY, 240);
     *
     * // Part composition (shape + material)
     * Attribute.of(DURABILITY, 240)
     *     .forPartComposition()
     *     .build();
     *
     * // Upgrade bonus with condition
     * Attribute.of(ATTACK_DAMAGE, 5)
     *     .forUpgradeSlots()
     *     .when(Condition.isSneaking())
     *     .build();
     * </pre>
     */
    static AttributeBuilder of(OpenIdentifier type, float value) {
        return new AttributeBuilder(type, value);
    }

    /**
     * Creates a simple attribute with default scope and no conditions.
     */
    static Attribute simple(OpenIdentifier type, float value) {
        return new SimpleAttribute(type, value);
    }
}
```

### Fluent Builder

```java
/**
 * Fluent builder for creating attributes with clear, self-documenting code.
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>
 * // Material base value for part composition
 * Attribute ironDurability = Attribute.of(DURABILITY, 240)
 *     .forPartComposition()
 *     .build();
 *
 * // Shape multiplier for part composition
 * Attribute pickaxeMultiplier = Attribute.of(DURABILITY, 1.0f)
 *     .multiply()
 *     .forPartComposition()
 *     .build();
 *
 * // Upgrade-only bonus that applies when sneaking
 * Attribute sneakBonus = Attribute.of(ATTACK_DAMAGE, 5)
 *     .forUpgradeSlots()
 *     .when(Condition.isSneaking())
 *     .build();
 *
 * // Local attribute that doesn't propagate
 * Attribute internalValue = Attribute.of(INTERNAL_MODIFIER, 1.5f)
 *     .localOnly()
 *     .build();
 * </pre>
 */
public final class AttributeBuilder {

    private final OpenIdentifier type;
    private final float value;
    private Optional<OpenIdentifier> id = Optional.empty();
    private Operator operator = AdditionOperator.getInstance();
    private int group = 0;
    private Optional<OpenIdentifier> scope = Optional.empty();
    private Optional<Condition> condition = Optional.empty();

    AttributeBuilder(OpenIdentifier type, float value) {
        this.type = type;
        this.value = value;
    }

    // ========================================================================
    // SCOPE METHODS (WHERE)
    // ========================================================================

    /**
     * This attribute participates in shape+material part composition.
     *
     * <p>Use this for material base values and shape multipliers.</p>
     */
    public AttributeBuilder forPartComposition() {
        this.scope = Optional.of(AttributeScope.PART_COMPOSITE);
        return this;
    }

    /**
     * This attribute participates in parts→equipment composition.
     */
    public AttributeBuilder forEquipmentComposition() {
        this.scope = Optional.of(AttributeScope.EQUIPMENT_COMPOSITE);
        return this;
    }

    /**
     * This attribute only applies when installed in an upgrade slot.
     *
     * <p>Use this for gem bonuses, reinforcement effects, etc.</p>
     */
    public AttributeBuilder forUpgradeSlots() {
        this.scope = Optional.of(AttributeScope.UPGRADE);
        return this;
    }

    /**
     * This attribute only applies to this component, doesn't propagate.
     */
    public AttributeBuilder localOnly() {
        this.scope = Optional.of(AttributeScope.LOCAL);
        return this;
    }

    /**
     * This attribute has custom scope behavior.
     */
    public AttributeBuilder withScope(OpenIdentifier customScope) {
        this.scope = Optional.of(customScope);
        return this;
    }

    // ========================================================================
    // OPERATOR METHODS (HOW)
    // ========================================================================

    /**
     * This value adds to the total. (Default behavior)
     */
    public AttributeBuilder add() {
        this.operator = AdditionOperator.getInstance();
        return this;
    }

    /**
     * This value multiplies the total.
     */
    public AttributeBuilder multiply() {
        this.operator = MultiplicationOperator.getInstance();
        return this;
    }

    /**
     * This value subtracts from the total.
     */
    public AttributeBuilder subtract() {
        this.operator = SubtractionOperator.getInstance();
        return this;
    }

    /**
     * Use a custom operator.
     */
    public AttributeBuilder withOperator(Operator op) {
        this.operator = op;
        return this;
    }

    // ========================================================================
    // CONDITION METHODS (WHEN)
    // ========================================================================

    /**
     * This attribute only applies when the condition is met.
     *
     * @param condition Runtime condition (static or dynamic)
     */
    public AttributeBuilder when(Condition condition) {
        this.condition = Optional.of(condition);
        return this;
    }

    /**
     * This attribute only applies when ALL conditions are met.
     */
    public AttributeBuilder whenAll(Condition... conditions) {
        this.condition = Optional.of(Condition.all(conditions));
        return this;
    }

    /**
     * This attribute only applies when ANY condition is met.
     */
    public AttributeBuilder whenAny(Condition... conditions) {
        this.condition = Optional.of(Condition.any(conditions));
        return this;
    }

    // ========================================================================
    // OTHER CONFIGURATION
    // ========================================================================

    /**
     * Sets the computation group (lower = computed first).
     */
    public AttributeBuilder inGroup(int group) {
        this.group = group;
        return this;
    }

    /**
     * Sets an optional identifier for this attribute instance.
     */
    public AttributeBuilder withId(OpenIdentifier id) {
        this.id = Optional.of(id);
        return this;
    }

    // ========================================================================
    // BUILD
    // ========================================================================

    /**
     * Creates the attribute.
     */
    public Attribute build() {
        return new SimpleAttribute(id, type, value, operator, group, scope, condition);
    }
}
```

### Updated SimpleAttribute

```java
/**
 * Standard implementation of {@link Attribute}.
 */
public record SimpleAttribute(
    Optional<OpenIdentifier> id,
    OpenIdentifier type,
    float value,
    Operator operator,
    int group,
    Optional<OpenIdentifier> scope,      // RENAMED from context
    Optional<Condition> condition
) implements Property, Attribute {

    // Convenience constructors
    public SimpleAttribute(OpenIdentifier type, float value) {
        this(Optional.empty(), type, value, AdditionOperator.getInstance(),
             0, Optional.empty(), Optional.empty());
    }

    // ... other constructors updated to use 'scope' ...

    /**
     * Creates a resolved attribute (after composition).
     * Output attributes have no scope since composition is complete.
     */
    public static SimpleAttribute resolved(OpenIdentifier type, float value) {
        return new SimpleAttribute(type, value);
    }

    /**
     * Creates an attribute with a specific scope.
     */
    public static SimpleAttribute withScope(
            OpenIdentifier type,
            float value,
            Operator operator,
            OpenIdentifier scope) {
        return new SimpleAttribute(
            Optional.empty(), type, value, operator, 0,
            Optional.of(scope), Optional.empty());
    }

    // Mutation methods
    public SimpleAttribute withScope(OpenIdentifier newScope) {
        return new SimpleAttribute(id, type, value, operator, group,
            Optional.ofNullable(newScope), condition);
    }

    public SimpleAttribute withoutScope() {
        return new SimpleAttribute(id, type, value, operator, group,
            Optional.empty(), condition);
    }
}
```

---

## JSON Schema Changes

### Before

```json
{
  "type": "forgero:durability",
  "computation": 240,
  "context": "forgero:part-composite"
}
```

### After

```json
{
  "type": "forgero:durability",
  "computation": 240,
  "scope": "forgero:scope/part-composite"
}
```

### Backwards Compatibility

**Note:** This implementation used a "one-shot replacement" approach with NO backwards compatibility.
All existing code and JSON files were updated simultaneously. The old `"context"` field is no longer supported.

---

## Files to Modify

| File | Changes |
|------|---------|
| `AttributeContext.java` | Rename to `AttributeScope.java` |
| `Attribute.java` | `context()` → `scope()`, add builder |
| `SimpleAttribute.java` | Update parameter names |
| `CompositeAttributeBakingStrategy.java` | Use `scope()` instead of `context()` |
| `PartCompositeContextHandler.java` | Update references |
| `AttributeCodec.java` | Support both "scope" and "context" |
| All tests | Update to use new API |
| All JSON files | Migrate "context" → "scope" |

### Estimated Impact

- **Core module:** ~28 file changes
- **Tests:** ~15 file changes
- **JSON content:** ~50+ file changes (can be scripted)

---

## Consequences

### Positive

1. **Clear terminology** - "Scope" unambiguously means WHERE, "Condition" means WHEN
2. **Self-documenting API** - Builder methods like `forPartComposition()` explain intent
3. **Reduced confusion** - No more "context" overloading
4. **Better discoverability** - IDE autocomplete guides users to correct methods

### Negative

1. **Breaking change** - Existing JSON needs migration
2. **Learning curve** - Users must update mental model
3. **Migration effort** - Non-trivial number of files to update

### Mitigations

1. Backwards-compatible JSON parsing during transition
2. Clear migration guide and tooling
3. Deprecation warnings before removal

---

## Alternatives Considered

### Alternative 1: Keep "Context" but Document Better

**Rejected:** Documentation can't fix API confusion. Users will continue to be confused.

### Alternative 2: Use Enum Instead of OpenIdentifier

```java
public enum AttributeScope {
    NONE, LOCAL, PART_COMPOSITE, EQUIPMENT_COMPOSITE, UPGRADE
}
```

**Rejected:** Loses extensibility for custom scopes. Data-driven design requires open identifiers.

### Alternative 3: Merge Scope into Condition System

```java
// Everything as conditions
Condition.inPartComposition()
Condition.inUpgradeSlot()
Condition.isSneaking()
```

**Rejected:** Conflates fundamentally different concepts. Scope affects composition pipeline, conditions affect runtime evaluation. Merging them would complicate the resolution engine.

---

## Decision Outcome

**Accepted:** Proceed with renaming `AttributeContext` → `AttributeScope` and `context()` → `scope()`, with phased migration maintaining backwards compatibility.

---

## References

- [Original Analysis](./property-attribute-resolution-analysis.md)
- [Attribute Resolution Architecture](./attribute-resolution.md)
- [Creating Content Packs Guide](../guides/creating-content-packs.md)

# Scope vs Condition: Decision Tree

**Quick Reference:** When to use `scope` vs `condition` on attributes.

---

## The Golden Rule

| Use | For | Question Answered |
|-----|-----|-------------------|
| **Scope** | Composition pipeline participation | "WHERE in the pipeline?" |
| **Condition** | Runtime application criteria | "WHEN does it apply?" |

---

## Decision Tree

```
                    ┌─────────────────────────────────────────┐
                    │ I want to control when my attribute...  │
                    └─────────────────────┬───────────────────┘
                                          │
                    ┌─────────────────────┴───────────────────┐
                    │                                         │
                    ▼                                         ▼
    ┌───────────────────────────────┐     ┌───────────────────────────────┐
    │ ...participates in composition │     │ ...is active at runtime       │
    │ (structure-based, static)      │     │ (game-state-based, dynamic)   │
    └───────────────┬───────────────┘     └───────────────┬───────────────┘
                    │                                     │
                    ▼                                     ▼
            ┌───────────────┐                     ┌───────────────┐
            │  Use SCOPE    │                     │ Use CONDITION │
            └───────────────┘                     └───────────────┘
```

---

## Scope: WHERE in the Composition Pipeline

### When to Use Scope

| Goal | Scope to Use | Example |
|------|--------------|---------|
| Material base value that composes with shape | `scope/part-composite` | Iron's 240 durability |
| Shape multiplier that composes with material | `scope/part-composite` | Pickaxe head's ×1.0 durability |
| Bonus that only applies when installed as upgrade | `scope/upgrade` | Gem's +50 durability |
| Value that shouldn't propagate to parent | `scope/local` | Internal crafting modifier |
| Parts combining into equipment | `scope/equipment-composite` | Head + handle composition |

### Scope Examples

```json
// Material base value for part composition
{
  "type": "forgero:durability",
  "computation": 240,
  "scope": "forgero:scope/part-composite"
}

// Shape multiplier for part composition
{
  "type": "forgero:durability",
  "computation": { "multiply": 1.0 },
  "scope": "forgero:scope/part-composite"
}

// Upgrade-only bonus (gem, reinforcement, etc.)
{
  "type": "forgero:durability",
  "computation": { "add": 50 },
  "scope": "forgero:scope/upgrade"
}
```

---

## Condition: WHEN Does It Apply at Runtime

### When to Use Condition

| Goal | Condition Type | Example |
|------|----------------|---------|
| Only when player is sneaking | `forgero:is_sneaking` | Stealth damage bonus |
| Only when it's raining | `forgero:is_raining` | Weather-based bonus |
| Only against undead | `forgero:target_has_tag` | Smite damage |
| Only in specific slot | `forgero:in_slot_type` | Slot-specific bonus |
| Only on root component | `forgero:is_root` | Equipment-level bonus |
| Random chance | `forgero:random_chance` | Critical hit bonus |

### Condition Examples

```json
// Bonus damage when sneaking
{
  "type": "forgero:attack_damage",
  "computation": { "add": 5 },
  "condition": {
    "type": "forgero:is_sneaking"
  }
}

// Smite damage against undead
{
  "type": "forgero:attack_damage",
  "computation": { "add": 10 },
  "condition": {
    "type": "forgero:target_has_tag",
    "tag": "minecraft:undead"
  }
}

// Only applies in gem slots
{
  "type": "forgero:mining_speed",
  "computation": { "add": 2 },
  "condition": {
    "static": [{
      "type": "forgero:in_slot_type",
      "slot_type": "forgero:gem_slot"
    }]
  }
}
```

---

## Combined: Scope AND Condition

You can use BOTH scope and condition on the same attribute:

```json
// Upgrade bonus that only applies when sneaking
{
  "type": "forgero:attack_damage",
  "computation": { "add": 5 },
  "scope": "forgero:scope/upgrade",       // WHERE: Only when installed as upgrade
  "condition": {                           // WHEN: Only when sneaking
    "type": "forgero:is_sneaking"
  }
}
```

**Resolution Order:**
1. Scope filters during BAKE (composition)
2. Static conditions filter during BAKE (after composition)
3. Dynamic conditions filter during APPLY (per query)

---

## Common Mistakes

### Mistake 1: Using Scope When You Mean Condition

```json
// WRONG: "I want +5 damage only when sneaking"
{
  "type": "forgero:attack_damage",
  "computation": 5,
  "scope": "forgero:sneaking"  // ❌ Scope is for pipeline, not game state
}

// CORRECT
{
  "type": "forgero:attack_damage",
  "computation": 5,
  "condition": { "type": "forgero:is_sneaking" }  // ✓
}
```

### Mistake 2: Using Condition for Composition Behavior

```json
// WRONG: "I want this to compose with shape multipliers"
{
  "type": "forgero:durability",
  "computation": 240,
  "condition": { "type": "forgero:in_part_composition" }  // ❌ Not a condition
}

// CORRECT
{
  "type": "forgero:durability",
  "computation": 240,
  "scope": "forgero:scope/part-composite"  // ✓
}
```

### Mistake 3: Forgetting Scope on Material Attributes

```json
// WRONG: Material durability without scope won't compose
{
  "type": "forgero:durability",
  "computation": 240
  // Missing scope - this is a direct value, won't compose with shape
}

// CORRECT
{
  "type": "forgero:durability",
  "computation": 240,
  "scope": "forgero:scope/part-composite"  // ✓ Will compose with shape
}
```

---

## Quick Reference Table

| I want to... | Use | Field | Example Value |
|--------------|-----|-------|---------------|
| Have material compose with shape | Scope | `scope` | `"forgero:scope/part-composite"` |
| Only apply when in upgrade slot | Scope | `scope` | `"forgero:scope/upgrade"` |
| Prevent propagation to parent | Scope | `scope` | `"forgero:scope/local"` |
| Only apply when sneaking | Condition | `condition` | `{ "type": "forgero:is_sneaking" }` |
| Only apply against undead | Condition | `condition` | `{ "type": "forgero:target_has_tag", "tag": "minecraft:undead" }` |
| Only apply in gem slots | Condition | `condition.static` | `{ "type": "forgero:in_slot_type", "slot_type": "forgero:gem_slot" }` |
| Only apply 25% of the time | Condition | `condition` | `{ "type": "forgero:random_chance", "chance": 0.25 }` |

---

## Java API Examples

### Using the Builder (Recommended)

```java
// Material base value
Attribute iron = Attribute.of(DURABILITY, 240)
    .forPartComposition()
    .build();

// Shape multiplier
Attribute pickaxeHead = Attribute.of(DURABILITY, 1.0f)
    .multiply()
    .forPartComposition()
    .build();

// Upgrade bonus with condition
Attribute sneakGem = Attribute.of(ATTACK_DAMAGE, 5)
    .forUpgradeSlots()
    .when(Condition.isSneaking())
    .build();

// Local-only value
Attribute internal = Attribute.of(CRAFTING_MOD, 1.5f)
    .localOnly()
    .build();
```

### Direct Construction

```java
// Using factory methods
SimpleAttribute.withScope(DURABILITY, 240, AdditionOperator.getInstance(),
    AttributeScope.PART_COMPOSITE);

// Using record constructor
new SimpleAttribute(
    Optional.empty(),                           // id
    DURABILITY,                                 // type
    240f,                                       // value
    AdditionOperator.getInstance(),             // operator
    0,                                          // group
    Optional.of(AttributeScope.PART_COMPOSITE), // scope
    Optional.empty()                            // condition
);
```

---

## Summary

| Concept | Controls | Evaluated During | Examples |
|---------|----------|------------------|----------|
| **Scope** | WHERE in pipeline | BAKE (composition) | part-composite, upgrade, local |
| **Condition (static)** | WHEN based on structure | BAKE (after composition) | in_slot_type, is_root, has_tag |
| **Condition (dynamic)** | WHEN based on game state | APPLY (per query) | is_sneaking, target_has_tag, is_raining |

**Remember:**
- Scope = Pipeline participation (structural, fixed)
- Condition = Application criteria (can be runtime-dependent)

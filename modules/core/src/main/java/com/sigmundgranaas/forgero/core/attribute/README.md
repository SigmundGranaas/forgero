# Attribute System

Attributes are numerical properties with computation support. They define stats like attack damage, durability, and mining speed.

## Attribute Interface

```java
public sealed interface Attribute extends Property {
    String type();           // "forgero:attack_damage"
    float value();           // Numerical value
    Operator operator();     // ADD, MULTIPLY, MIN, MAX
    String group();          // Grouping for stacking
    Condition condition();   // When this applies
}
```

## Default Attributes

| Constant | Type ID | Purpose |
|----------|---------|---------|
| `ATTACK_DAMAGE` | `forgero:attack_damage` | Weapon damage |
| `ATTACK_SPEED` | `forgero:attack_speed` | Attack rate |
| `DURABILITY` | `forgero:durability` | Item durability |
| `MINING_SPEED` | `forgero:mining_speed` | Block breaking speed |
| `MINING_LEVEL` | `forgero:mining_level` | Harvestable tier (0-4) |
| `ARMOR` | `forgero:armor` | Protection points |
| `DRAW_POWER` | `forgero:draw_power` | Bow velocity |
| `ACCURACY` | `forgero:accuracy` | Projectile accuracy |

## Operators

| Operator | Formula | Applied |
|----------|---------|---------|
| `ADDITION` | `base + value` | First |
| `MULTIPLICATION` | `base * value` | Second |
| `MIN` / `MAX` | `min/max(base, value)` | Last |

```java
// Example: base=10, +5, ×1.5
// Step 1: 10 + 5 = 15
// Step 2: 15 × 1.5 = 22.5
```

## Creating Attributes

```java
// Using DefaultAttributes
Attribute damage = ATTACK_DAMAGE.create(5.0f);
Attribute bonus = ATTACK_DAMAGE.create(1.5f, Operator.MULTIPLICATION);

// With condition
Attribute conditional = ATTACK_DAMAGE.create(3.0f)
    .withCondition(Condition.tagMatch("forgero:sword"));
```

## Groups

Same-type attributes in the same group combine; different groups stack separately:

```java
Attribute base = ATTACK_DAMAGE.create(5.0f).withGroup("base");
Attribute bonus = ATTACK_DAMAGE.create(2.0f).withGroup("bonus");
// Result: base damage + bonus damage
```

## Resolution

```java
Resolver resolver = services.resolver();
AttributeQueryResult result = resolver.resolveAttribute(
    component,
    DefaultAttributes.ATTACK_DAMAGE,
    context
);
float finalDamage = result.value();
```

## JSON Schema

```json
{
  "type": "forgero:attack_damage",
  "value": 5.0,
  "computation": {
    "operator": "ADDITION",
    "group": "base"
  },
  "condition": {
    "type": "forgero:tag_match",
    "tag": "forgero:sword"
  }
}
```

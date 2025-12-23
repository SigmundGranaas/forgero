# Effect System Design Improvements

## 1. Entity Filter System

**Problem**: Selection and filtering are mixed in selectors. AreaOfEffectSelector has hardcoded filtering.

**Solution**: Separate filters from selectors.

### New Interface

```java
public interface EntityFilter {
    boolean test(Entity source, Entity candidate);
    String type();

    Codec<EntityFilter> CODEC = DispatchCodecUtils.create(
        EntityFilter::getCodec,
        EntityFilter::type
    );
}
```

### Filter Implementations

```java
// Basic filters
public record IsHostileFilter() implements EntityFilter
public record IsAliveFilter() implements EntityFilter
public record IsTeammateFilter(boolean invert) implements EntityFilter
public record HasTagFilter(String tag) implements EntityFilter

// Advanced filters
public record HealthThresholdFilter(float threshold, Comparator comparator) implements EntityFilter
public record DistanceFilter(float min, float max) implements EntityFilter
public record InBiomeFilter(Identifier biome) implements EntityFilter
public record FacingFilter(float angleThreshold) implements EntityFilter

// Composite filters
public record AndFilter(List<EntityFilter> filters) implements EntityFilter
public record OrFilter(List<EntityFilter> filters) implements EntityFilter
public record NotFilter(EntityFilter filter) implements EntityFilter
```

### JSON Example

```json
{
  "selector": {
    "type": "forgero:aoe",
    "radius": 5
  },
  "filters": [
    {"type": "forgero:is_hostile"},
    {"type": "forgero:health_below", "threshold": 0.5},
    {"type": "forgero:distance", "min": 1, "max": 5}
  ],
  "effects": [...]
}
```

### Updated OnHitProperty

```java
public record OnHitProperty(
    EntitySelector selector,
    List<EntityFilter> filters,  // NEW
    List<OnHitEffect> effects,
    @Nullable Condition condition
)
```

**Benefits**:
- Filters are reusable across selectors
- Composable (AND/OR/NOT)
- AreaOfEffectSelector becomes simpler (no hardcoded logic)
- Users can customize filtering per property

---

## 2. Advanced Selectors

### Geometric Selectors

```java
// Cone selector (for sweep attacks, breath weapons)
public record ConeSelector(
    float angle,      // degrees
    float range,
    float arcWidth
) implements EntitySelector

// Line/Ray selector (for piercing effects)
public record RaycastSelector(
    float range,
    boolean piercing,
    int maxHits
) implements EntitySelector

// Ring selector (outward explosion pattern)
public record RingSelector(
    float innerRadius,
    float outerRadius
) implements EntitySelector

// Chain selector (lightning chain)
public record ChainSelector(
    int maxChains,
    float chainRange,
    boolean allowRepeats
) implements EntitySelector
```

### Targeting Strategy Selectors

```java
// Pick N nearest entities
public record NearestNSelector(int count) implements EntitySelector

// Pick N random entities from area
public record RandomSelector(
    int count,
    float searchRadius
) implements EntitySelector

// Prioritize by criteria
public record PrioritizedSelector(
    int count,
    float searchRadius,
    Priority priority  // LOWEST_HEALTH, HIGHEST_HEALTH, NEAREST, FARTHEST
) implements EntitySelector

// Self-targeting
public record SelfSelector() implements EntitySelector
```

### Composite Selectors

```java
public record CompositeSelector(
    List<EntitySelector> selectors,
    boolean removeDuplicates
) implements EntitySelector {
    @Override
    public List<Entity> select(Entity source, Entity initialTarget) {
        List<Entity> result = new ArrayList<>();
        for (EntitySelector selector : selectors) {
            result.addAll(selector.select(source, initialTarget));
        }
        return removeDuplicates
            ? result.stream().distinct().toList()
            : result;
    }
}
```

### JSON Examples

```json
// Cone attack hitting 3 nearest enemies
{
  "selector": {
    "type": "forgero:cone",
    "angle": 45,
    "range": 5,
    "arcWidth": 3
  },
  "filters": [
    {"type": "forgero:is_hostile"}
  ]
}

// Chain lightning
{
  "selector": {
    "type": "forgero:chain",
    "maxChains": 5,
    "chainRange": 3,
    "allowRepeats": false
  },
  "effects": [
    {"type": "forgero:lightning"}
  ]
}

// Self + nearby (aura effect)
{
  "selector": {
    "type": "forgero:composite",
    "selectors": [
      {"type": "forgero:self"},
      {"type": "forgero:aoe", "radius": 3}
    ],
    "removeDuplicates": true
  }
}
```

---

## 3. Effect Probability System

**Problem**: Effects always trigger. No way to make rare/RNG effects.

**Solution**: Add chance/probability to effects.

### Wrapper Pattern

```java
public record ProbabilisticEffect(
    OnHitEffect effect,
    float chance  // 0.0 to 1.0
) implements OnHitEffect {

    @Override
    public String type() {
        return "forgero:probabilistic";
    }
}
```

### JSON Example

```json
{
  "effects": [
    {
      "type": "forgero:probabilistic",
      "chance": 0.1,
      "effect": {
        "type": "forgero:lightning"
      }
    }
  ]
}
```

### Alternative: Direct Integration

Add optional `chance` field to all effects via base interface:

```java
public interface OnHitEffect {
    String type();

    default float chance() {
        return 1.0f;
    }

    default boolean shouldApply() {
        return Math.random() < chance();
    }
}
```

```json
{
  "effects": [
    {
      "type": "forgero:lightning",
      "chance": 0.1
    }
  ]
}
```

---

## 4. Effect Scaling/Modifiers

**Problem**: Effects have fixed values. Can't scale by distance, damage, or other factors.

**Solution**: Add modifier system to effects.

### Modifier Interface

```java
public interface EffectModifier {
    float apply(EffectContext context, float baseValue);
    String type();

    Codec<EffectModifier> CODEC = DispatchCodecUtils.create(
        EffectModifier::getCodec,
        EffectModifier::type
    );
}

public record EffectContext(
    Entity source,
    Entity target,
    float distance,
    float damageDealt,
    ItemStack tool
) {}
```

### Modifier Implementations

```java
// Scale by distance (stronger close, weaker far)
public record DistanceScaleModifier(
    float maxDistance,
    float minMultiplier,
    float maxMultiplier
) implements EffectModifier

// Scale by target's missing health
public record ExecuteModifier(
    float healthThreshold
) implements EffectModifier

// Scale by damage dealt
public record DamageScaleModifier(
    float damagePerPoint
) implements EffectModifier

// Random variance
public record RandomVarianceModifier(
    float minMultiplier,
    float maxMultiplier
) implements EffectModifier
```

### Updated Effect Handlers

```java
public record FireHandler(
    int baseDuration,
    List<EffectModifier> modifiers
) implements EntityEffectHandler {

    @Override
    public void apply(Entity entity) {
        float duration = baseDuration;

        // Apply modifiers
        for (EffectModifier modifier : modifiers) {
            duration = modifier.apply(context, duration);
        }

        entity.setOnFireFor((int) duration);
    }
}
```

### JSON Example

```json
{
  "effects": [
    {
      "type": "forgero:fire",
      "baseDuration": 5,
      "modifiers": [
        {
          "type": "forgero:distance_scale",
          "maxDistance": 10,
          "minMultiplier": 0.5,
          "maxMultiplier": 2.0
        },
        {
          "type": "forgero:execute",
          "healthThreshold": 0.3
        }
      ]
    }
  ]
}
```

---

## 5. Rich Effect Context

**Problem**: Effects only get entities. No damage, location, or tool context.

**Solution**: Pass rich context to effects.

### New Context Object

```java
public record EffectExecutionContext(
    Entity source,
    Entity target,
    @Nullable DamageSource damageSource,
    float damageDealt,
    ItemStack tool,
    Vec3d hitLocation,
    World world
) {
    public float distance() {
        return (float) source.getPos().distanceTo(target.getPos());
    }

    public boolean isInBiome(Identifier biome) {
        return world.getBiome(target.getBlockPos()).getKey()
            .map(key -> key.getValue().equals(biome))
            .orElse(false);
    }
}
```

### Updated Handler Interfaces

```java
public interface EntityEffectHandler extends OnHitEffect {
    void apply(EffectExecutionContext context);
}

public interface ContextualEffectHandler extends OnHitEffect {
    void apply(EffectExecutionContext context);
}
```

### Benefits

Effects can now:
- Scale by damage dealt
- Know exact hit location (for particles)
- Access tool properties
- Check world state (biome, time, weather)
- Use distance without recalculating

---

## 6. Non-Entity Effects

**Problem**: Only entity effects. Can't affect blocks, spawn particles, play sounds, etc.

**Solution**: Expand effect types beyond entities.

### New Effect Categories

```java
// Block effects
public interface BlockEffect extends OnHitEffect {
    void apply(EffectExecutionContext context, BlockPos pos);
}

public record PlaceBlockEffect(Block block, int duration) implements BlockEffect
public record BreakBlockEffect(float radius, Predicate<Block> filter) implements BlockEffect
public record ReplaceBlockEffect(Block from, Block to, int radius) implements BlockEffect

// Visual effects
public interface VisualEffect extends OnHitEffect {
    void apply(EffectExecutionContext context);
}

public record ParticleEffect(
    ParticleType particle,
    int count,
    double spread
) implements VisualEffect

public record SoundEffect(
    SoundEvent sound,
    float volume,
    float pitch
) implements VisualEffect

// Projectile effects
public record ProjectileEffect(
    EntityType<? extends ProjectileEntity> projectileType,
    int count,
    float spread,
    List<OnHitEffect> onProjectileHit
) implements OnHitEffect

// World effects
public record SetWeatherEffect(Weather weather, int duration) implements OnHitEffect
public record SetTimeEffect(int time) implements OnHitEffect
```

### JSON Examples

```json
// Create fire blocks around target
{
  "effects": [
    {
      "type": "forgero:place_block",
      "block": "minecraft:fire",
      "radius": 2,
      "duration": 100
    }
  ]
}

// Spawn particles
{
  "effects": [
    {
      "type": "forgero:particle",
      "particle": "minecraft:flame",
      "count": 20,
      "spread": 1.0
    }
  ]
}

// Launch projectiles from hit entity
{
  "effects": [
    {
      "type": "forgero:projectile",
      "projectileType": "minecraft:arrow",
      "count": 8,
      "spread": 360,
      "onProjectileHit": [
        {"type": "forgero:explosion", "power": 1.0}
      ]
    }
  ]
}
```

---

## 7. Cooldown System

**Problem**: Effects trigger every hit/tick. No way to limit frequency.

**Solution**: Add cooldown tracking.

### Property-Level Cooldowns

```java
public record OnHitProperty(
    EntitySelector selector,
    List<EntityFilter> filters,
    List<OnHitEffect> effects,
    @Nullable CooldownConfig cooldown,
    @Nullable Condition condition
)

public record CooldownConfig(
    int ticks,
    CooldownScope scope  // PER_ENTITY, PER_PLAYER, GLOBAL
)
```

### Cooldown Manager

```java
public class CooldownManager {
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    public static boolean isOnCooldown(UUID entityId, String propertyId) {
        // Check cooldown
    }

    public static void putOnCooldown(UUID entityId, String propertyId, int ticks) {
        // Set cooldown
    }
}
```

### JSON Example

```json
{
  "selector": {"type": "forgero:single_target"},
  "effects": [
    {"type": "forgero:lightning"}
  ],
  "cooldown": {
    "ticks": 100,
    "scope": "per_player"
  }
}
```

---

## 8. Effect Groups & Tags

**Problem**: Can't reference groups of effects. Hard to amplify/modify multiple effects.

**Solution**: Add effect grouping.

### Effect Groups

```json
{
  "effects": [
    {
      "type": "forgero:effect_group",
      "group": "fire_damage",
      "effects": [
        {"type": "forgero:fire", "duration": 5},
        {"type": "forgero:particle", "particle": "minecraft:flame", "count": 10}
      ]
    }
  ]
}
```

### Conditional Effect Groups

```json
{
  "effects": [
    {
      "type": "forgero:conditional_group",
      "condition": {"type": "forgero:target_has_tag", "tag": "minecraft:undead"},
      "effects": [
        {"type": "forgero:fire", "duration": 10},
        {"type": "forgero:status_effect", "effect": "minecraft:weakness", "duration": 100}
      ]
    }
  ]
}
```

---

## 9. Position/Area Selectors

**Problem**: Selectors only return entities. Can't select positions or areas for block effects.

**Solution**: New selector interface for positions.

### Position Selector

```java
public interface PositionSelector {
    List<BlockPos> selectPositions(Entity source, Entity target);
    String type();
}

public record RadialPositionSelector(
    int radius,
    PositionFilter filter
) implements PositionSelector

public record PathSelector(
    int maxLength,
    boolean throughBlocks
) implements PositionSelector
```

### Combined Usage

```json
{
  "positionSelector": {
    "type": "forgero:radial",
    "radius": 3,
    "filter": {"type": "forgero:replaceable_blocks"}
  },
  "blockEffects": [
    {
      "type": "forgero:place_block",
      "block": "minecraft:fire"
    }
  ]
}
```

---

## 10. New Opportunities & Use Cases

### Sweep Attack System

```json
{
  "selector": {
    "type": "forgero:cone",
    "angle": 90,
    "range": 3
  },
  "filters": [
    {"type": "forgero:is_hostile"}
  ],
  "effects": [
    {"type": "forgero:damage", "amount": 0.5, "damageType": "sweep"}
  ]
}
```

### Elemental Reactions

```json
{
  "selector": {"type": "forgero:single_target"},
  "filters": [
    {"type": "forgero:has_status_effect", "effect": "minecraft:poison"}
  ],
  "effects": [
    {"type": "forgero:explosion", "power": 2.0}
  ]
}
```

### Area Denial

```json
{
  "positionSelector": {
    "type": "forgero:radial",
    "radius": 2
  },
  "blockEffects": [
    {
      "type": "forgero:place_block",
      "block": "minecraft:fire",
      "duration": 200
    }
  ]
}
```

### Vampire Aura

```json
{
  "minecraft:on_tick": [
    {
      "selector": {
        "type": "forgero:aoe",
        "radius": 5
      },
      "filters": [
        {"type": "forgero:is_hostile"}
      ],
      "effects": [
        {
          "type": "forgero:life_steal",
          "amount": 0.5,
          "modifiers": [
            {"type": "forgero:distance_scale", "maxDistance": 5, "minMultiplier": 0.2, "maxMultiplier": 1.0}
          ]
        }
      ],
      "interval": 20
    }
  ]
}
```

### Chain Lightning

```json
{
  "selector": {
    "type": "forgero:chain",
    "maxChains": 5,
    "chainRange": 4,
    "allowRepeats": false
  },
  "effects": [
    {
      "type": "forgero:lightning",
      "modifiers": [
        {"type": "forgero:decay_per_bounce", "decayRate": 0.8}
      ]
    }
  ]
}
```

### Execute Mechanics

```json
{
  "selector": {"type": "forgero:single_target"},
  "filters": [
    {"type": "forgero:health_below", "threshold": 0.2}
  ],
  "effects": [
    {
      "type": "forgero:damage",
      "baseDamage": 10,
      "modifiers": [
        {"type": "forgero:execute", "healthThreshold": 0.2}
      ]
    }
  ]
}
```

---

## Summary of Improvements

| Feature | Current | Proposed | Benefit |
|---------|---------|----------|---------|
| **Filtering** | Hardcoded in selectors | Separate composable filters | Reusable, customizable |
| **Selectors** | 2 types | 10+ types (cone, chain, ray, etc.) | Geometric patterns, strategies |
| **Probability** | Always triggers | Per-effect chance | RNG/rare effects |
| **Scaling** | Fixed values | Modifiers (distance, health, damage) | Dynamic, adaptive effects |
| **Context** | Just entities | Rich context (damage, tool, location) | Context-aware effects |
| **Effect Types** | Entity only | Block, particle, sound, projectile | Visual feedback, terrain |
| **Cooldowns** | None | Per-entity/player/global | Rate limiting |
| **Composition** | Single selector | Composite selectors | Complex patterns |
| **Self-targeting** | Not possible | SelfSelector | Buffs, self-damage |
| **Effect groups** | None | Groups & conditional groups | Organization, reusability |

---

## Migration Path

### Phase 1: Non-Breaking Additions
- Add filter system (optional)
- Add new selector types
- Add probability wrapper
- Add modifier system (optional field)

### Phase 2: Enhanced Context
- Add EffectExecutionContext
- Update all handlers to use context
- Maintain backwards compatibility

### Phase 3: New Effect Types
- Add block effects
- Add visual effects
- Add projectile effects

### Phase 4: Advanced Features
- Cooldown system
- Effect groups
- Position selectors

---

## JSON API Simplifications

### Short-hand for Common Patterns

```json
// Instead of verbose single target
{
  "selector": {"type": "forgero:single_target"},
  "effects": [...]
}

// Allow shorthand (selector defaults to single_target)
{
  "effects": [...]
}
```

### Preset Configurations

```json
// Define reusable presets
{
  "presets": {
    "hostile_aoe": {
      "selector": {"type": "forgero:aoe", "radius": 3},
      "filters": [{"type": "forgero:is_hostile"}]
    }
  },
  "properties": {
    "minecraft:on_hit": [
      {
        "preset": "hostile_aoe",
        "effects": [...]
      }
    ]
  }
}
```

### Effect Aliases

```json
// Common effects get short names
{"type": "fire", "duration": 5}  // instead of "forgero:fire"
{"type": "lightning"}             // instead of "forgero:lightning"
```

---

## Testing Strategy

```java
@GameTest(templateName = "cone_selector_test")
public void testConeSelector(TestContext context) {
    // Spawn entities in cone pattern
    // Verify only entities in cone are affected
}

@GameTest(templateName = "effect_probability_test")
public void testProbability(TestContext context) {
    // Run effect 100 times
    // Verify ~10 triggers for 0.1 chance
}

@GameTest(templateName = "cooldown_test")
public void testCooldown(TestContext context) {
    // Trigger effect
    // Verify cooldown prevents re-trigger
    // Wait cooldown duration
    // Verify can trigger again
}
```

---

## Performance Considerations

1. **Filter Caching**: Cache entity lists between filter stages
2. **Selector Optimization**: Use spatial partitioning for AOE
3. **Cooldown Storage**: Prune expired cooldowns periodically
4. **Context Pooling**: Reuse EffectExecutionContext objects
5. **Early Bailout**: Check conditions before expensive operations

---

## Next Steps

1. Implement filter system (highest impact, non-breaking)
2. Add cone and chain selectors (high value use cases)
3. Add probability system (simple, high impact)
4. Prototype modifier system
5. Gather feedback on API ergonomics
6. Iterate on JSON shorthand syntax

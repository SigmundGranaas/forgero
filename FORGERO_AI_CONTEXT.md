# Forgero Practical AI Context

## What is Forgero?

A Minecraft Fabric mod for deep tool/weapon/armor customization. Everything is data-driven through JSON - no code changes needed to add materials, tools, or behaviors.

**Key Concept**: Everything is a `Component` (materials, parts, tools, upgrades). Components compose into hierarchies. Properties attach behaviors. Templates auto-generate permutations.

## Critical File Locations

```
modules/core/           # Platform-agnostic logic (NO Minecraft deps)
modules/mc/loader/      # ForgeroApi, plugin system
modules/mc/properties/  # OnHit, OnTick, BlockBreaking, Loot properties
modules/mc/properties/src/main/java/com/sigmundgranaas/forgero/effects/entity/  # NEW effect handlers
modules/mc/properties/src/main/java/com/sigmundgranaas/forgero/properties/minecraft/entityselector/  # NEW selectors
content/minecraft-vanilla-materials/  # Vanilla materials with properties
```

## ACTUAL JSON PATTERNS (Copy These)

### Material with Auto-Smelt Property

```json
{
  "type": "forgero:material",
  "name": "Blaze rod",
  "tags": ["forgero:upgrade/binding"],
  "host": {
    "identifiers": [{ "type": "item", "id": "minecraft:blaze_rod" }]
  },
  "properties": {
    "minecraft:on_loot_drop": [
      {
        "handler": {
          "type": "forgero:apply_functions",
          "functions": [
            {
              "type": "forgero:auto_smelt",
              "filter": { "type": "forgero:tag", "tag": "c:raw_ores" }
            }
          ]
        }
      }
    ]
  }
}
```

### Material with Attributes and Conditional Application

```json
{
  "type": "forgero:material",
  "name": "Leather",
  "tags": ["forgero:upgrade/binding"],
  "host": {
    "identifiers": [
      { "type": "item", "id": "minecraft:leather" }
    ]
  },
  "attributes": [
    {
      "id": "forgero:leather-binding-durability",
      "type": "forgero:durability",
      "computation": { "value": 100 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:binding_slot"
      }
    },
    {
      "id": "forgero:leather-grip-attack_damage",
      "type": "forgero:attack_damage",
      "computation": { "value": 10 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:handle_grip_slot"
      }
    }
  ]
}
```

### OnHit Property - NEW ARCHITECTURE (forgero-2 branch)

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 3,
          "filters": [
            { "type": "forgero:is_hostile" },
            { "type": "forgero:is_alive" }
          ]
        },
        "effects": [
          {
            "type": "forgero:lightning"
          },
          {
            "type": "forgero:fire",
            "duration": 5
          },
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:slowness",
            "duration": 100,
            "amplifier": 2
          }
        ],
        "condition": {
          "type": "forgero:target_has_tag",
          "tag": "minecraft:undead"
        }
      }
    ]
  }
}
```

**Three-Tier Structure**:
1. **selector** - Who to affect (geometric selection + filtering)
2. **effects** - What happens (array of effect objects)
3. **condition** - When to trigger property (optional, checks component/game state)

**Note**: Filters are now part of the selector, not a separate field. Each selector handles its own filtering.

### OnTick Property - NEW (Periodic Effects)

```json
{
  "properties": {
    "minecraft:on_tick": [
      {
        "selector": {
          "type": "forgero:aoe",
          "radius": 5
        },
        "effects": [
          {
            "type": "forgero:status_effect",
            "effect": "minecraft:slowness",
            "duration": 40,
            "amplifier": 0
          }
        ],
        "interval": 20,
        "condition": {
          "type": "forgero:is_sneaking"
        }
      }
    ]
  }
}
```

**interval**: Ticks between activations (20 = 1 second)

## Available Effect Types

```json
{ "type": "forgero:fire", "duration": 5 }
{ "type": "forgero:lightning" }
{ "type": "forgero:status_effect", "effect": "minecraft:poison", "duration": 100, "amplifier": 1 }
{ "type": "forgero:life_steal", "amount": 2.0 }
{ "type": "forgero:knockback", "force": 2.0, "direction": "push" }
{ "type": "forgero:explosion", "power": 2.0, "create_fire": false, "destruction_type": "none" }
{ "type": "forgero:convert", "convert_to": "minecraft:zombie_villager" }
{ "type": "forgero:disarm" }
```

## Available Selectors

```json
{ "type": "forgero:single_target" }
{ "type": "forgero:aoe", "radius": 3 }
{ "type": "forgero:cone", "angle": 90, "range": 10 }
{ "type": "forgero:chain", "maxChains": 5, "chainRange": 4, "allowRepeats": false }
```

## Available Filters

Filters are applied after selection to narrow down targets. They are composable and reusable.

### Basic Filters
```json
{ "type": "forgero:is_alive" }
{ "type": "forgero:is_hostile" }
{ "type": "forgero:is_teammate", "invert": false }
{ "type": "forgero:has_tag", "tag": "minecraft:undead" }
```

### Advanced Filters
```json
{ "type": "forgero:health_threshold", "threshold": 0.5, "comparator": "less_than" }
{ "type": "forgero:distance", "min": 0, "max": 5 }
```

### Composite Filters
```json
{ "type": "forgero:and", "filters": [...] }
{ "type": "forgero:or", "filters": [...] }
{ "type": "forgero:not", "filter": {...} }
```

### Filter Comparators (for health_threshold)
- `less_than`
- `less_than_or_equal`
- `greater_than`
- `greater_than_or_equal`
- `equal`

## Available Conditions (Static - Component Structure)

```json
{ "type": "forgero:in_slot_type", "slot_type": "forgero:binding_slot" }
{ "type": "forgero:is_root" }
{ "type": "forgero:at_depth", "depth": 2 }
{ "type": "forgero:self_has_tag", "tag": "forgero:sword" }
{ "type": "forgero:root_has_tag", "tag": "forgero:pickaxe" }
{ "type": "forgero:has_sibling", "sibling_tag": "forgero:handle" }
{ "type": "forgero:and", "conditions": [...] }
{ "type": "forgero:or", "conditions": [...] }
{ "type": "forgero:not", "condition": {...} }
```

## Available Dynamic Conditions (Runtime - Game State)

```json
{ "type": "forgero:target_has_tag", "tag": "minecraft:undead" }
{ "type": "forgero:is_sneaking" }
{ "type": "forgero:is_raining" }
```

## CODEC IMPLEMENTATION PATTERNS

### Pattern 1: Singleton Handler (No Parameters)

```java
public record LightningHandler() implements EntityEffectHandler {
    public static final String TYPE = "forgero:lightning";
    public static final LightningHandler INSTANCE = new LightningHandler();
    public static final Codec<LightningHandler> CODEC = Codec.unit(INSTANCE);

    @Override
    public void apply(Entity entity) {
        if (!entity.getWorld().isClient) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(entity.getPos());
                entity.getWorld().spawnEntity(lightning);
            }
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

**Critical**: Use `Codec.unit(INSTANCE)` for handlers with no parameters.

### Pattern 2: Handler with Required Parameters

```java
public record FireHandler(int duration) implements EntityEffectHandler {
    public static final String TYPE = "forgero:fire";

    public static final Codec<FireHandler> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("duration").forGetter(FireHandler::duration)
        ).apply(instance, FireHandler::new)
    );

    @Override
    public void apply(Entity entity) {
        entity.setOnFireFor(duration);
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

### Pattern 3: Handler with Optional Parameters

```java
public record StatusEffectHandler(Identifier effect, int duration, int amplifier)
    implements EntityEffectHandler {

    public static final String TYPE = "forgero:status_effect";

    public static final Codec<StatusEffectHandler> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("effect").forGetter(StatusEffectHandler::effect),
            Codec.INT.fieldOf("duration").forGetter(StatusEffectHandler::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(StatusEffectHandler::amplifier)
        ).apply(instance, StatusEffectHandler::new)
    );

    @Override
    public void apply(Entity entity) {
        if (entity instanceof LivingEntity living) {
            StatusEffect statusEffect = Registries.STATUS_EFFECT.get(effect);
            if (statusEffect != null) {
                living.addStatusEffect(new StatusEffectInstance(statusEffect, duration, amplifier));
            }
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

**Critical**: Use `.optionalFieldOf("field", defaultValue)` for optional fields.

### Pattern 4: Contextual Handler (Needs Source + Target)

```java
public record LifeStealHandler(float amount) implements ContextualEffectHandler {
    public static final String TYPE = "forgero:life_steal";

    public static final Codec<LifeStealHandler> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("amount").forGetter(LifeStealHandler::amount)
        ).apply(instance, LifeStealHandler::new)
    );

    @Override
    public void apply(Entity source, Entity target) {
        if (source instanceof LivingEntity livingSource) {
            livingSource.heal(amount);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

**When to use ContextualEffectHandler**: When you need the attacking entity (source), not just target.

### Pattern 5: Polymorphic Interface

```java
public interface OnHitEffect {
    String type();

    static Codec<? extends OnHitEffect> getCodec(String type) {
        Codec<? extends OnHitEffect> codec = OnHitPropertiesPlugin.getEffectCodec(type);
        if (codec == null) {
            throw new IllegalArgumentException("Unknown OnHitEffect type: " + type);
        }
        return codec;
    }

    Codec<OnHitEffect> CODEC = DispatchCodecUtils.create(
        OnHitEffect::getCodec,  // Get codec from type string
        OnHitEffect::type       // Get type string from instance
    );
}
```

**How it works**:
1. JSON has `"type": "forgero:fire"`
2. `DispatchCodecUtils` calls `getCodec("forgero:fire")`
3. Looks up `FireHandler.CODEC` from registry
4. Decodes rest of JSON with that codec

## PLUGIN REGISTRATION (CRITICAL)

### Step 1: Create Plugin Class

```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit;

public class OnHitPropertiesPlugin implements DataPlugin {

    private static final Map<String, Codec<? extends OnHitEffect>> EFFECTS = new ConcurrentHashMap<>();
    private static final Map<String, Codec<? extends EntitySelector>> SELECTORS = new ConcurrentHashMap<>();

    static {
        // Register all effects
        registerEffect(FireHandler.TYPE, FireHandler.CODEC);
        registerEffect(LightningHandler.TYPE, LightningHandler.CODEC);
        registerEffect(StatusEffectHandler.TYPE, StatusEffectHandler.CODEC);
        registerEffect(LifeStealHandler.TYPE, LifeStealHandler.CODEC);
        registerEffect(KnockbackHandler.TYPE, KnockbackHandler.CODEC);
        registerEffect(ExplosionHandler.TYPE, ExplosionHandler.CODEC);
        registerEffect(ConvertHandler.TYPE, ConvertHandler.CODEC);
        registerEffect(DisarmHandler.TYPE, DisarmHandler.CODEC);

        // Register selectors
        registerSelector(SingleTargetSelector.TYPE, SingleTargetSelector.CODEC);
        registerSelector(AreaOfEffectSelector.TYPE, AreaOfEffectSelector.CODEC);
    }

    public static void registerEffect(String type, Codec<? extends OnHitEffect> codec) {
        EFFECTS.put(type, codec);
    }

    public static Codec<? extends OnHitEffect> getEffectCodec(String type) {
        return EFFECTS.get(type);
    }

    public static void registerSelector(String type, Codec<? extends EntitySelector> codec) {
        SELECTORS.put(type, codec);
    }

    public static Codec<? extends EntitySelector> getSelectorCodec(String type) {
        return SELECTORS.get(type);
    }

    @Override
    public void register(PluginRegistrationContext context) {
        context.registerPropertyCodec(
            OnHitProperty.PROPERTY_KEY,
            conditionCodecSupplier -> ListCodecWrapper.of(
                OnHitProperty.codec(conditionCodecSupplier.get())
            )
        );
    }

    @Override
    public String getId() {
        return "forgero:on-hit-properties";
    }
}
```

### Step 2: Register in fabric.mod.json

```json
{
  "entrypoints": {
    "forgero:data_plugin": [
      "com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin",
      "com.sigmundgranaas.forgero.properties.minecraft.ontick.OnTickPropertiesPlugin"
    ]
  }
}
```

**CRITICAL**: If you forget this, your plugin won't load and JSON will fail silently!

## PROPERTY DEFINITION PATTERN

```java
public record OnHitProperty(
    EntitySelector selector,
    List<OnHitEffect> effects,
    @Nullable Condition condition
) implements ConditionalProperty {

    public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_hit");
    public static final ResolutionKey<List<OnHitProperty>> KEY = new ResolutionKey<>(KEY_ID);
    public static final PropertyKey<OnHitProperty> PROPERTY_KEY =
        new PropertyKey<>(OnHitProperty.class, KEY_ID.toString());

    public static Codec<OnHitProperty> codec(Codec<Condition> conditionCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            EntitySelector.CODEC.fieldOf("selector").forGetter(OnHitProperty::selector),
            Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(OnHitProperty::effects),
            conditionCodec.optionalFieldOf("condition")
                .forGetter(p -> Optional.ofNullable(p.condition()))
        ).apply(instance, (selector, effects, condition) ->
            new OnHitProperty(selector, effects, condition.orElse(null))
        ));
    }

    @Override
    public @Nullable Condition condition() {
        return condition;
    }

    public static class Engine extends AbstractConditionalPropertyEngine<OnHitProperty, List<OnHitProperty>> {
        public Engine() {
            super(KEY, PROPERTY_KEY);
        }

        @Override
        public List<OnHitProperty> apply(OptimizedBakedResult<OnHitProperty> baked, DynamicContext context) {
            return baked.stream(context).collect(Collectors.toList());
        }
    }
}
```

## MIXIN INTEGRATION

### OnHit Mixin

```java
@Mixin(LivingEntity.class)
public abstract class LivingEntityOnHitMixin {
    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void forgero$onHitMixin(DamageSource source, float amount, CallbackInfo ci) {
        Entity attacker = source.getAttacker();
        LivingEntity target = (LivingEntity) (Object) this;

        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack stack = livingAttacker.getMainHandStack();
            if (!stack.isEmpty()) {
                OnHitManager.handleOnHit(stack, livingAttacker, target);
            }
        }
    }
}
```

### OnTick Mixin

```java
@Mixin(LivingEntity.class)
public abstract class LivingEntityTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void forgero$onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        OnTickManager.handle(entity);
    }
}
```

### Register Mixins

```json
{
  "required": true,
  "package": "com.sigmundgranaas.forgero.properties.minecraft.mixin",
  "mixins": [
    "LivingEntityOnHitMixin",
    "LivingEntityTickMixin"
  ]
}
```

Then in fabric.mod.json:
```json
{
  "mixins": [
    "properties.mixin.json"
  ]
}
```

## MANAGER PATTERN (Property Execution)

```java
public class OnHitManager {

    public static void handleOnHit(ItemStack stack, Entity source, Entity target) {
        if (stack.isEmpty()) return;

        ForgeroApi.converter().toComponent(stack).ifPresent(component -> {
            List<OnHitProperty> properties = getActiveProperties(component, target);

            for (OnHitProperty property : properties) {
                // 1. Selector finds targets
                List<Entity> targets = property.selector().select(source, target);

                // 2. Apply each effect to each target
                for (Entity finalTarget : targets) {
                    for (OnHitEffect effect : property.effects()) {
                        if (effect instanceof ContextualEffectHandler contextual) {
                            contextual.apply(source, finalTarget);
                        } else if (effect instanceof EntityEffectHandler simple) {
                            simple.apply(finalTarget);
                        }
                    }
                }
            }
        });
    }

    private static List<OnHitProperty> getActiveProperties(Component component, Entity target) {
        var engine = new OnHitProperty.Engine();
        DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

        // Build context for conditions
        Set<OpenIdentifier> targetTags = Registries.ENTITY_TYPE.getEntry(target.getType())
            .streamTags()
            .map(TagKey::id)
            .map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
            .collect(Collectors.toSet());

        contextBuilder.put(ContextKeys.TARGET_TAGS, targetTags);

        return ForgeroApi.resolver().resolve(component, engine, contextBuilder.build());
    }
}
```

**Flow**:
1. ItemStack → Component (conversion)
2. Resolve properties (with conditions)
3. Selector picks targets
4. Effects execute on each target

## COMMON PITFALLS

### ❌ Properties Not an Array

```json
{
  "properties": {
    "minecraft:on_hit": {  // WRONG
      "selector": {...}
    }
  }
}
```

```json
{
  "properties": {
    "minecraft:on_hit": [  // CORRECT
      {
        "selector": {...}
      }
    ]
  }
}
```

### ❌ Type String Mismatch

```java
public static final String TYPE = "forgero:fire";
```

```json
{ "type": "fire" }  // WRONG - missing namespace
{ "type": "forgero:fire" }  // CORRECT
```

### ❌ Forgot to Register

```java
// Defined handler ✓
public record MyEffect() implements EntityEffectHandler { ... }

// Created codec ✓
public static final Codec<MyEffect> CODEC = Codec.unit(INSTANCE);

// FORGOT THIS ❌
static {
    registerEffect(MyEffect.TYPE, MyEffect.CODEC);  // MUST DO!
}
```

### ❌ Attribute ID Collision

```json
{
  "attributes": [
    { "id": "durability", ... },  // WRONG - not unique
    { "id": "durability", ... }   // CONFLICT
  ]
}
```

```json
{
  "attributes": [
    { "id": "forgero:iron-durability", ... },  // CORRECT - unique
    { "id": "forgero:iron-attack_damage", ... }
  ]
}
```

### ❌ Client/Server Issues

```java
@Override
public void apply(Entity entity) {
    // WRONG - runs on both sides, causes desync
    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
    entity.getWorld().spawnEntity(lightning);
}
```

```java
@Override
public void apply(Entity entity) {
    if (!entity.getWorld().isClient) {  // CORRECT - server only
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(entity.getWorld());
        if (lightning != null) {
            entity.getWorld().spawnEntity(lightning);
        }
    }
}
```

## STEP-BY-STEP: ADD NEW EFFECT

### 1. Define Handler

```java
package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public record FreezeHandler(int duration) implements EntityEffectHandler {
    public static final String TYPE = "forgero:freeze";

    public static final Codec<FreezeHandler> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("duration").forGetter(FreezeHandler::duration)
        ).apply(instance, FreezeHandler::new)
    );

    @Override
    public void apply(Entity entity) {
        if (!entity.getWorld().isClient) {
            entity.setFrozenTicks(duration * 20);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

### 2. Register in Plugin

```java
static {
    // ... existing
    registerEffect(FreezeHandler.TYPE, FreezeHandler.CODEC);
}
```

### 3. Use in JSON

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "selector": { "type": "forgero:single_target" },
        "effects": [
          {
            "type": "forgero:freeze",
            "duration": 5
          }
        ]
      }
    ]
  }
}
```

### 4. Test

```java
@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
public void testFreezeEffect(TestContext context) {
    FreezeHandler effect = new FreezeHandler(5);
    LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));

    effect.apply(target);

    context.assertTrue(target.getFrozenTicks() > 0, "Target should be frozen");
    context.complete();
}
```

## MIGRATION: OLD → NEW SYSTEM

### OLD (1.20 branch)

```java
public interface OnHitHandler {
    void onHit(Entity source, Entity target);
}

public record OnHitProperty(OnHitHandler handler, @Nullable Condition condition) { }
```

```json
{
  "minecraft:on_hit": [
    {
      "handler": {
        "type": "forgero:lightning"
      }
    }
  ]
}
```

### NEW (forgero-2 branch)

```java
public interface EntityEffectHandler extends OnHitEffect {
    void apply(Entity entity);
}

public interface ContextualEffectHandler extends OnHitEffect {
    void apply(Entity source, Entity target);
}

public record OnHitProperty(
    EntitySelector selector,
    List<OnHitEffect> effects,
    @Nullable Condition condition
) { }
```

```json
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [
        { "type": "forgero:lightning" }
      ]
    }
  ]
}
```

**Key Changes**:
1. `handler` → `selector` + `effects`
2. Single handler → List of effects
3. Two effect interfaces for clarity (simple vs contextual)

## PRACTICAL RECIPES

### Lightning on Undead in Rain

```json
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [
        { "type": "forgero:lightning" }
      ],
      "condition": {
        "type": "forgero:and",
        "conditions": [
          { "type": "forgero:target_has_tag", "tag": "minecraft:undead" },
          { "type": "forgero:is_raining" }
        ]
      }
    }
  ]
}
```

### AoE Slow Aura When Sneaking

```json
{
  "minecraft:on_tick": [
    {
      "selector": { "type": "forgero:aoe", "radius": 5 },
      "effects": [
        {
          "type": "forgero:status_effect",
          "effect": "minecraft:slowness",
          "duration": 40,
          "amplifier": 1
        }
      ],
      "interval": 20,
      "condition": { "type": "forgero:is_sneaking" }
    }
  ]
}
```

### Life Steal with Knockback

```json
{
  "minecraft:on_hit": [
    {
      "selector": { "type": "forgero:single_target" },
      "effects": [
        { "type": "forgero:life_steal", "amount": 2.0 },
        { "type": "forgero:knockback", "force": 1.5, "direction": "push" }
      ]
    }
  ]
}
```

### Conditional Durability Bonus

```json
{
  "attributes": [
    {
      "id": "forgero:obsidian-tip-durability",
      "type": "forgero:durability",
      "computation": { "value": 500 },
      "condition": {
        "type": "forgero:in_slot_type",
        "slot_type": "forgero:tip_reinforcement_slot"
      }
    }
  ]
}
```

### NEW: Sweep Attack with Cone Selector

```json
{
  "minecraft:on_hit": [
    {
      "selector": {
        "type": "forgero:cone",
        "angle": 90,
        "range": 5,
        "filters": [
          { "type": "forgero:is_hostile" },
          { "type": "forgero:is_alive" }
        ]
      },
      "effects": [
        { "type": "forgero:knockback", "force": 1.0, "direction": "push" },
        { "type": "forgero:fire", "duration": 3 }
      ]
    }
  ]
}
```

### NEW: Chain Lightning Effect

```json
{
  "minecraft:on_hit": [
    {
      "selector": {
        "type": "forgero:chain",
        "maxChains": 5,
        "chainRange": 4,
        "allowRepeats": false,
        "filters": [
          { "type": "forgero:is_alive" },
          {
            "type": "forgero:not",
            "filter": { "type": "forgero:is_teammate", "invert": false }
          }
        ]
      },
      "effects": [
        { "type": "forgero:lightning" }
      ],
      "condition": {
        "type": "forgero:is_raining"
      }
    }
  ]
}
```

### NEW: Execute Mechanics (Extra Damage to Low Health)

```json
{
  "minecraft:on_hit": [
    {
      "selector": {
        "type": "forgero:single_target",
        "filters": [
          {
            "type": "forgero:health_threshold",
            "threshold": 0.3,
            "comparator": "less_than"
          }
        ]
      },
      "effects": [
        { "type": "forgero:fire", "duration": 10 },
        { "type": "forgero:status_effect", "effect": "minecraft:wither", "duration": 100, "amplifier": 2 }
      ]
    }
  ]
}
```

### NEW: Complex Filter Logic (Hostile AND (Low Health OR Close))

```json
{
  "minecraft:on_hit": [
    {
      "selector": {
        "type": "forgero:aoe",
        "radius": 5,
        "filters": [
          { "type": "forgero:is_hostile" },
          {
            "type": "forgero:or",
            "filters": [
              { "type": "forgero:health_threshold", "threshold": 0.5, "comparator": "less_than" },
              { "type": "forgero:distance", "min": 0, "max": 3 }
            ]
          }
        ]
      },
      "effects": [
        { "type": "forgero:explosion", "power": 1.5, "create_fire": false, "destruction_type": "none" }
      ]
    }
  ]
}
```

### NEW: Hostile AOE Aura (Filters Integrated in Selector)

```json
{
  "minecraft:on_tick": [
    {
      "selector": {
        "type": "forgero:aoe",
        "radius": 8,
        "filters": [
          { "type": "forgero:is_hostile" },
          { "type": "forgero:is_alive" },
          {
            "type": "forgero:not",
            "filter": { "type": "forgero:is_teammate", "invert": false }
          }
        ]
      },
      "effects": [
        {
          "type": "forgero:status_effect",
          "effect": "minecraft:slowness",
          "duration": 40,
          "amplifier": 1
        }
      ],
      "interval": 20,
      "condition": { "type": "forgero:is_sneaking" }
    }
  ]
}
```

## DEBUGGING CHECKLIST

Effect not working? Check:
1. ✓ Plugin in fabric.mod.json `forgero:data_plugin` entrypoint?
2. ✓ Codec registered in plugin static block?
3. ✓ Type string matches exactly (with namespace)?
4. ✓ JSON syntax valid (properties are arrays)?
5. ✓ Mixin registered in mixin.json?
6. ✓ Mixin injecting? (add debug print)
7. ✓ Server-side check `!world.isClient`?

## CODEC QUICK REFERENCE

```java
// Singleton
Codec.unit(INSTANCE)

// Required field
Codec.INT.fieldOf("duration").forGetter(Class::duration)

// Optional field with default
Codec.INT.optionalFieldOf("amplifier", 0).forGetter(Class::amplifier)

// List
Codec.list(OnHitEffect.CODEC).fieldOf("effects")

// Polymorphic dispatch
DispatchCodecUtils.create(Class::getCodec, Class::type)

// Enum
StringIdentifiable.createCodec(Enum::values, Enum::valueOf)
```

## MODULE DEPENDENCY RULES

```
core/ - ZERO Minecraft dependencies (platform-agnostic)
mc/loader/ - Depends on: core, Fabric API
mc/properties/ - Depends on: core, loader, Fabric API
```

**Never** import Minecraft classes in core module!

## CURRENT STATE (forgero-2 branch)

**Completed**:
- ✓ OnHit migrated to selector + effects architecture
- ✓ OnTick system added
- ✓ All effect handlers ported to new interfaces
- ✓ Tests updated
- ✓ Documentation updated

**TODO**:
- Update content pack JSONs to new format
- Migrate remaining old-style handlers

**Active Files**:
- `effects/entity/*Handler.java` - Effect implementations
- `entityselector/` - Target selection
- `ontick/` - Periodic effects
- `OnHitProperty.java` - Property definition

## WHEN TO USE WHAT

**EntityEffectHandler**: Effect only needs target
- Fire, Lightning, StatusEffect, Disarm

**ContextualEffectHandler**: Effect needs source + target
- LifeSteal, Convert, Explosion (with attacker context)

**Static Condition**: Checks component structure
- `in_slot_type`, `is_root`, `has_tag`

**Dynamic Condition**: Checks game state
- `target_has_tag`, `is_raining`, `is_sneaking`

## NAMING CONVENTIONS

**Type strings**: `namespace:snake_case`
- ✓ `"forgero:fire"`
- ✓ `"minecraft:on_hit"`
- ❌ `"Fire"`
- ❌ `"onHit"`

**Attribute IDs**: `namespace:material-attribute_type`
- ✓ `"forgero:iron-durability"`
- ✓ `"forgero:leather-binding-attack_damage"`

**Class names**: `PascalCaseHandler`
- ✓ `FireHandler`
- ✓ `StatusEffectHandler`

**Fields**: `camelCase`
- ✓ `duration`
- ✓ `damagePerSecond`

## KEY APIS

```java
// Get API instance
ForgeroApi api = ForgeroApi.getInstance();

// Convert ItemStack ↔ Component
Optional<Component> comp = api.converter().toComponent(stack);
ItemStack stack = api.converter().toStack(comp);

// Resolve properties
PropertyResolver resolver = api.resolver();
List<Property> props = resolver.resolve(component, engine, context);

// Access registry
ComponentRegistry registry = api.registry();
Optional<Component> comp = registry.find(identifier);
```

## ARCHITECTURE PRINCIPLES

1. **Core is platform-agnostic** - No Minecraft imports in core/
2. **Data-driven** - Prefer JSON over hardcoded logic
3. **Immutable components** - Use `.with*()` for modifications
4. **Plugin-based extensibility** - Use DataPlugin, not mixins
5. **Composition over inheritance** - Components compose hierarchically
6. **Codec-based serialization** - Type-safe bidirectional (de)serialization

This document contains ACTUAL code from Forgero, REAL patterns you'll use, and PRACTICAL solutions to specific problems. Use it as your implementation guide.

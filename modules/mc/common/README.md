# Forgero MC Common Module

Bridge between Forgero core and Minecraft. Contains conversion APIs, tooltip system, and attribute mapping.

## ComponentConverter

Bidirectional conversion between Components and ItemStacks:

```java
ComponentConverter converter = ForgeroApi.converter();

// ItemStack → Component (preserves NBT state)
Optional<Component> component = converter.toComponent(playerStack);

// Component → ItemStack (serializes to NBT)
Optional<ItemStack> stack = converter.toStack(component);

// Type lookup (no NBT)
Optional<Component> defaultComp = converter.toComponent(Items.DIAMOND_PICKAXE);
Optional<Item> item = converter.toItem(component);
```

## TooltipApi

Section-based tooltip rendering:

```java
// Build tooltip
List<Text> tooltip = TooltipApi.builder(component, resolver)
    .withComparison(ComparisonContext.equippedItem(equipped))
    .build(tooltipContext);

// Register custom section (priority determines order)
TooltipSection mySection = TooltipSection.of("mymod:warnings", 150, true);
TooltipApi.registerSection(mySection, ctx -> new SectionWriter() {
    @Override
    public List<Text> write() {
        if (hasWarnings(ctx.component())) {
            return List.of(Text.literal("Warning!").formatted(Formatting.RED));
        }
        return List.of();
    }
});

// Register custom placeholder for templates
TooltipApi.registerPlaceholder("rarity", (component, context) ->
    Optional.of(getRarity(component).name())
);
```

**Default sections** (by priority): subtitle(0), description(100), notes(200), attributes(300), features(400), upgrades(500), slots(600), lore(900)

## AttributeManager

Maps Forgero attributes to Minecraft entity attributes:

```java
// Used internally by mixins
Multimap<EntityAttribute, EntityAttributeModifier> attributes =
    AttributeManager.getAttributes(stack, vanillaMap, EquipmentSlot.MAINHAND);
```

| Forgero | Minecraft |
|---------|-----------|
| `forgero:attack_damage` | `generic.attack_damage` |
| `forgero:attack_speed` | `generic.attack_speed` |
| `forgero:armor` | `generic.armor` |

## UseContext

Immutable context for item use lifecycle:

```java
public record UseContext(
    World world, LivingEntity user, Hand hand, ItemStack stack,
    int chargeTime, int remainingTicks, float pullProgress, @Nullable Entity target
) {
    boolean isServer();
    boolean isClient();
    boolean isFullyCharged();
    Optional<PlayerEntity> asPlayer();
}
```

Factory methods: `UseContext.start()`, `UseContext.tick()`, `UseContext.release()`, `UseContext.finish()`

## Base Item Classes

| Class | Purpose |
|-------|---------|
| `DynamicItem` | Generic item with NBT-driven behavior |
| `DynamicToolItem` | Tool with component-driven mining |
| `DynamicSwordItem` | Sword with component-driven combat |
| `DynamicToolMaterial` | Placeholder material (actual stats from components) |

## NBT Serialization

```java
ComponentNbtConverter nbtConverter = ForgeroApi.nbtConverter();

// Serialize
NbtCompound nbt = nbtConverter.toNbt(component);
stack.setNbt(nbt);

// Deserialize
Optional<Component> loaded = nbtConverter.fromNbt(stack.getNbt());
```

NBT key: `"ForgeroComponent"`

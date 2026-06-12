# Forgero Modding API

How to build on Forgero from Java. Every snippet here is taken from a compile-checked, gametest-proven
example addon (`mods/forgero/src/test/java/.../example/ExampleForgeroAddon.java`); a leak audit keeps
that example to the public API only, so if it's in this guide, it works without touching Forgero's
internals.

## The public surface

A downstream mod uses exactly these namespaces (plus Minecraft and the JDK):

| Namespace | What's there |
|---|---|
| `com.sigmundgranaas.forgero.common.api` | `ForgeroApi` entry point, services, plugin entrypoints, conditions |
| `com.sigmundgranaas.forgero.common.api.item` | `ItemQueryApi`, `ItemMutationApi`, `ItemComparisonApi` |
| `com.sigmundgranaas.forgero.common.identifier.api` | `OpenIdentifier` |
| `com.sigmundgranaas.forgero.effects.api` | `OnHitEffects`, `BlockEffects`, `UseEffects` |

You never need `core.*`, `*.impl.*`, `StatFold`, `Component`, or a hand-written `Codec` for any of
this. (Advanced framework SPI — adding a brand-new property *type* or `Slot` *type* — is the
exception; see the end.)

## Getting the services

`ForgeroApi` is a static accessor, valid **after** Forgero has initialized. Either call it lazily
(inside an event handler, on tick, on use) or register for the ready callback:

```java
ForgeroInitializedCallback.EVENT.register(services -> {
    // services.itemQuery(), .itemMutation(), .converter(), .slotManager() ...
});
```

`ForgeroApi.itemQuery()` / `itemMutation()` / `itemComparison()` are convenience static delegates.

## Reading an item

`ItemQueryApi` is read-only and ItemStack-native; it returns sensible defaults (0, false, empty) for
non-Forgero items.

```java
ItemQueryApi query = ForgeroApi.itemQuery();
if (query.isForgeroItem(stack)) {
    float damage      = query.getAttackDamage(stack);
    int   durability  = query.getMaxDurability(stack);
    float miningSpeed = query.getMiningSpeed(stack);
    int   miningLevel = query.getMiningLevel(stack);
    List<ItemStack>      upgrades = query.getInstalledUpgrades(stack);
    Set<OpenIdentifier>  tags     = query.getTags(stack);
    Optional<OpenIdentifier> material = query.getPrimaryMaterial(stack);
}
```

## Installing & removing upgrades

`ItemMutationApi` is immutable — it returns a **new** stack and never throws (it returns the original
on failure).

```java
ItemMutationApi mutate = ForgeroApi.itemMutation();
if (mutate.canInstallUpgrade(tool, gem)) {
    ItemStack upgraded = mutate.installUpgrade(tool, gem);
}
ItemStack stripped = mutate.removeUpgrade(tool, OpenIdentifier.parse("mymod:ruby_gem"));
ItemStack bare     = mutate.removeAllUpgrades(tool);
```

## Comparing items

```java
ItemComparisonApi compare = ForgeroApi.itemComparison();
compare.isSameType(a, b);  // same tool type, ignoring NBT (durability, upgrades)
compare.areSimilar(a, b);  // same structure parts, ignoring upgrades
```

## Discovering content (no `Component`)

Enumerate loaded Forgero content as ItemStacks — for a JEI-style screen, recipe generator, or compat
layer. `findByTag` resolves through the tag graph, so a parent tag matches its descendants.

```java
List<ItemStack> materials = query.allMaterials();                              // forgero:materials
List<ItemStack> parts     = query.allParts();                                  // forgero:parts
List<ItemStack> metals    = query.findByTag(OpenIdentifier.parse("forgero:materials/types/metal"));
```

## Adding a custom condition

Conditions gate attributes/effects on *structure* ("only at the root", "only in an offensive slot").
Register them from your `ModInitializer` or a Forgero data plugin. No `Codec` is needed for the
common case:

```java
context.registerStaticCondition("mymod:is_top", ConditionContext::isRoot);
context.registerStaticCondition("mymod:in_offensive_slot",
        ctx -> ctx.isInSlotType(OpenIdentifier.parse("forgero:contexts/offensive")));
```

`ConditionContext` exposes `isRoot()`, `depth()`, `slotType()`, `slotTags()`, `isInSlotType(...)` —
no `Component`. For a condition that reads JSON config, supply a `Codec` for your config record
(use `ForgeroCodecs.IDENTIFIER` for identifier fields — never the internal `CodecConstants`):

```java
record InSlot(OpenIdentifier slot) {}
Codec<InSlot> codec = RecordCodecBuilder.create(i -> i.group(
        ForgeroCodecs.IDENTIFIER.fieldOf("slot").forGetter(InSlot::slot)).apply(i, InSlot::new));
context.registerStaticCondition("mymod:in_slot", codec, (cfg, ctx) -> ctx.isInSlotType(cfg.slot()));
```

Use it in content JSON: `{ "type": "mymod:in_slot", "slot": "forgero:contexts/offensive" }`.

## Adding a custom effect

Effects are the "what happens" of a property. Register plain Minecraft logic; the type is then usable
in any content `effects` list. Three channels:

```java
// Entity — on_hit / on_tick / on_kill (they share the entity effect list):
OnHitEffects.registerSingleTarget("mymod:torch", entity -> entity.setOnFireFor(3));
OnHitEffects.registerSourceTarget("mymod:lifesteal", (source, target) -> {
    if (source instanceof LivingEntity attacker) attacker.heal(2.0f);
});

// Block — when a tool hits/breaks a block:
BlockEffects.register("mymod:scorch",
        (world, source, pos) -> world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState()));

// Use — when a player right-clicks with the item:
UseEffects.register("mymod:warm", (user, stack, hand) -> user.setOnFireFor(1));
```

Each facade also has a `Codec<C>`-based overload for effects that read JSON config, e.g.
`OnHitEffects.registerSingleTarget("mymod:burn", codec, (cfg, e) -> e.setOnFireFor(cfg.seconds()))`.

In content: `{ "type": "mymod:torch" }` inside an `on_hit` / `on_tick` `effects` array.

## Most content is data, not Java

The biggest extension surface is **data packs** — materials, parts, schematics, upgrade slots, and
all the built-in property types (`on_hit`, `on_tick`, `on_damage`, `block_breaking`, attributes…) are
authored in JSON with no code. The reference mod `vanilla-upgrades` is an empty `ModInitializer`; it
adds upgrade slots to vanilla tools entirely through data. Reach for Java only for behaviour the data
format can't express. See `docs/guides/creating-content-packs.md`.

## Advanced: new property or slot *types* (framework SPI)

Adding a genuinely new **property type** (a new behaviour channel beyond the built-ins) or a new
**`Slot` type** (beyond the upgrade slot) is deep framework work: a property needs a codec **and** a
compiler pass **and** a runtime manager **and** a mixin to fire it; a slot needs the `Slot`
implementation plus manager/validation/serialization wiring. These go through
`PluginRegistrationContext.registerPropertyCodec(...)` / `registerSlotCodec(...)` and necessarily
touch `core.*` SPI types (`PropertyKey`, `Condition`, `Slot`). They are intended for power users
extending the framework itself — almost every gameplay idea fits an existing property + a custom
effect or condition above. If you find yourself needing a new property type, open an issue: the
right home for these SPI types is a dedicated public `forgero-api` module.

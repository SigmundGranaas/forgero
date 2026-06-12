# Public API Review — can a third-party mod actually build on Forgero?

**Method.** I sat in a downstream author's seat and wrote a real addon (`mods/forgero/src/test/
java/com/sigmundgranaas/forgero/example/ExampleForgeroAddon.java`) that uses **only** what a
consumer would legitimately have as "the Forgero API":

- `com.sigmundgranaas.forgero.common.api.*` (services + plugin entrypoints)
- `com.sigmundgranaas.forgero.common.identifier.api` (`OpenIdentifier`)
- `net.minecraft.*` and `java.*`

Anything outside those namespaces that the compiler *forced* me to import is an **API leak** — the
public surface is pushing an internal abstraction onto the consumer. The compiler is the judge:
the example compiles, and an import audit shows exactly where the boundary breaks.

This is the loop the whole rework opened — *"the API is not really possible to use for other mod
developers."* The internals are now clean; this checks whether the **external** surface followed.

---

## What genuinely works (public-only, no leaks)

The three modular ItemStack APIs are real and usable. These four scenarios compiled with only
`common.api` + `OpenIdentifier`:

1. **Read a tool's stats** for a HUD/compat layer — `ItemQueryApi`: `isForgeroItem`,
   `getAttackDamage`, `getMaxDurability`, `getMiningSpeed`, `getMiningLevel`, `getInstalledUpgrades`,
   `getTags`, `getPrimaryMaterial`. ItemStack-native, null-safe (sane defaults for vanilla items).
2. **Auto-socket a gem** on use — `ItemMutationApi.canInstallUpgrade` + `installUpgrade` (immutable,
   returns a new stack; no exceptions).
3. **Unsocket by id** — `removeUpgrade(stack, OpenIdentifier)`.
4. **Compare / balance** — `ItemComparisonApi.isSameType` + `getAttackDamage`.

This part does **not** "pretend to work" — it works. For the *read/mutate a given stack* use case,
the API is good: Minecraft-native types, no `Component`/`StatFold`/`Optional`-chain exposure.

`ExampleForgeroAddon` is kept in the tree as a compile-checked example **and** a guard: if the
public item API regresses, it stops compiling.

---

## Findings (prioritized)

### F1 — Extension forces internal (and `impl`!) imports — *highest impact*

Registering the single most common custom thing — a condition — required, on top of the public
`DataPlugin`/`PluginRegistrationContext`, **three internal imports** plus raw Mojang codec
machinery (compile-proven in a probe):

- `core.condition.api.StaticCondition` — implement the behavior
- `core.property.compilation.ResolutionContext` — the eval context, undocumented for outsiders
- `data.loading.impl.codec.CodecConstants` — to get the right identifier codec; this is an
  **`impl` package**, i.e. explicitly-internal, yet unavoidable to author a correct codec

The *entrypoints* (`DataPlugin`, `PluginRegistrationContext`) are public, but **everything they ask
you to supply is internal**: `registerStaticConditionCodec` wants a `Codec<? extends
StaticCondition>`, `registerSlotCodec` wants a `Codec<? extends core.component.api.Slot>`,
`registerPropertyCodec` wants a `PropertyKey<?>` and a `Function<Supplier<Codec<Condition>>, …>`.
A downstream mod cannot write any of these without reaching past `common.api`. **This is the core
of the original complaint, still unaddressed for the extension path.**

### F2 — No ItemStack-level discovery; the registry leaks `Component`

There is no public way to enumerate "all loaded materials/parts" as `ItemStack`s. A JEI-style screen
or a recipe generator must call `services.componentRegistry().all()`, which returns
`List<core.component.api.Component>` — straight into the internal type the modular APIs were built to
hide. The item APIs cover *a stack you already hold*, not *discovery*.

### F3 — Docs describe an API that doesn't exist

`CLAUDE.md` (and the in-repo guide examples) show `ForgeroApi.getInstance()`, `api.registry()`,
`api.resolver()`, `api.slotManager().install(component, component)`. None exist. The real surface is
`ForgeroApi.services()` plus static delegates (`itemQuery()`, `itemMutation()`, `slotManager()`, …)
and the `ItemStack`-based APIs. A new author following the docs writes code that won't compile.

### F4 — Small sharp edges in the entrypoints

- `DataPlugin` requires `getId()` in addition to `register(...)` — not obvious; easy to miss.
- The property/condition registration signatures are heavy generics
  (`Function<Supplier<Codec<Condition>>, Codec<? extends List<?>>>`) with no factory helpers.

### F5 — The Java extension API is unexercised by any shipping mod

The only real downstream consumer, `mods/vanilla-upgrades`, is an **empty `ModInitializer`** — the
entire mod is data packs. So the proven, load-bearing extension path is **JSON**, and the Java
extension surface (F1) has no real-world user keeping it honest. That is exactly how a surface comes
to "pretend to work."

---

## The way forward (turn findings into work)

1. **Define and seal the public API boundary (addresses F1/F2).** Decide which packages are the API
   (`common.api.*`, `common.identifier.api`, `common.tags.api`, and a small, *intentionally public*
   set of building blocks). Then give the extension path public building blocks so a mod never
   imports `core.*`/`*.impl.*`:
   - a public `Conditions`/`Codecs` helper exposing the identifier codecs and condition base types
     a plugin needs (re-export, not `data.loading.impl.codec.CodecConstants`);
   - a public, documented `ResolutionContext` view (or a narrower read-only interface) for authoring
     conditions;
   - convenience registration overloads that hide the `Function<Supplier<…>, Codec<…>>` plumbing.

2. **Add ItemStack-level discovery (addresses F2).** e.g. `ItemQueryApi.allMaterials()/allParts()`
   returning `List<ItemStack>` (or ids), so the registry's `Component` type never surfaces.

3. **Fix the docs (addresses F3).** Rewrite the `CLAUDE.md` "Key APIs" + the content-pack guide to
   the real `ForgeroApi.services()` / item-API surface. Cheap, high-signal — current docs actively
   mislead.

4. **Keep the addon example as the contract (addresses F5).** `ExampleForgeroAddon` is now a
   compile-checked guard for the *read/mutate* surface. Add a parallel public-only example for the
   *extension* path once F1 lands — if it can't be written public-only, F1 isn't done.

5. **Guard against `impl` leakage.** A small test/lint asserting that no `common.api` type's
   signatures reference `*.impl.*` or undocumented `core.*` types would keep the boundary from
   eroding again.

### Bottom line

The **consumption** half of the external API (read stats, install/remove upgrades, compare) is good
and now guarded. The **extension** half still hands authors internal — and even `impl` — types, so
for "add new behavior in Java," the original complaint stands. F1 + F3 are the highest-value next
steps: seal the extension boundary, and stop the docs from pointing at an API that isn't there.

---

## Progress

**F1 (conditions) — DONE.** A downstream mod can now author and register a custom condition with
**zero** `core.*`/`*.impl.*` imports, proven by `ExampleDataPlugin` (public-only, compile-guarded)
and the `PublicApiExtensionTest` gametest:

- `ConditionContext` (public) — a read-only structural view (`isRoot`, `depth`, `slotType`,
  `slotTags`, `isInSlotType`) that exposes **no** `Component`.
- `PluginRegistrationContext.registerStaticCondition(type, Predicate<ConditionContext>)` — the
  parameterless/logic case.
- `PluginRegistrationContext.registerStaticCondition(type, Codec<C>, BiPredicate<C, ConditionContext>)`
  — the JSON-parameterized case, with `ForgeroCodecs.IDENTIFIER` providing the path-preserving
  identifier codec so the `data.loading.impl.codec.CodecConstants` leak is gone.

**F1 (on-hit effects) — DONE.** A downstream mod can add a custom on-hit effect with plain
Minecraft logic and no internal types, proven by `ExampleForgeroAddon.registerEffects()`
(public-only) and two `PublicApiExtensionTest` gametests (registered → parsed through the dispatch
codec → applied to a real zombie, no-config and JSON-config paths). New public surface in
`com.sigmundgranaas.forgero.effects.api` (this lives in the `properties` module, not `common`,
because effects are defined above `common` in the module graph):

- `OnHitEffects.registerSingleTarget(type, Consumer<Entity>)` and
  `registerSingleTarget(type, Codec<C>, BiConsumer<C, Entity>)` — single-target effects.
- `OnHitEffects.registerSourceTarget(type, BiConsumer<Entity, Entity>)` and
  `registerSourceTarget(type, Codec<C>, SourceTargetAction<C>)` — attacker+victim effects.
- `FunctionalEntityEffect`/`FunctionalContextualEffect` bridge to the internal handler types and
  round-trip config; the `OnHitEffect` marker, the `type()` boilerplate and `EffectCodecRegistry`
  are hidden.

> The public surface is intentionally split: `common.api.*` (services, items, conditions) +
> `effects.api.*` (effects). This is forced by the module graph (`properties` depends on `common`,
> not the reverse) and is documented so it is not a surprise.

**F3 — DONE.** Fixed the `CLAUDE.md` "Key APIs" block (removed the non-existent
`getInstance()`/`registry()`/`resolver().resolve(...)`; replaced with the real
`ForgeroApi.services()`/static-delegate surface).

**F2 (registry discovery) — DONE.** `ItemQueryApi` gained ItemStack-level discovery so the registry's
`Component` type no longer surfaces:

- `findByTag(OpenIdentifier)` — every loaded component with a tag (tag-graph inheritance, so a parent
  tag matches its descendants), as `ItemStack`s. The flexible primitive.
- `allMaterials()` / `allParts()` — convenience defaults over `findByTag(forgero:materials|parts)`.

Backed by the existing `TaggedRegistry<Component>` + converter (wired into `ItemQueryApiImpl`).
`PublicApiExtensionTest` proves discovery returns real stacks, includes a known member (iron), and
that the convenience tags actually resolve (an empty result would mean a wrong tag — caught).

**Still leaking (next):**

- **Other effect channels** — block effects (`OnHitBlockEffect`), use/interaction handlers and
  on-tick share the same `EffectCodecRegistry` shape; give each the same `OnHitEffects`-style facade.
- **Property codecs** (`registerPropertyCodec`) and **slot codecs** (`registerSlotCodec`) still take
  `PropertyKey<?>` / `core.component.api.Slot` and heavy `Function<Supplier<…>, …>` generics.
- A strict, fully-sealed SPI (so an author never has even the *option* of a `core.*` import) would
  want the condition/property SPI types in a dedicated public `forgero-api` module that both `core`
  and `common` depend on — measured at ~90 import sites, deferred as its own refactor.

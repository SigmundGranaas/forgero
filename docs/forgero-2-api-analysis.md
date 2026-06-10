# Forgero-2 Rework: Architecture & API Post-Mortem

**Scope:** Analysis of the `forgero-2` branch (89 commits, June–October 2025, ~33,400 insertions,
never merged) against the `1.20` baseline, evaluating the statement that led to development being
halted:

> "The rearchitecture just turned into a modern equivalent of the original architecture. [...] The
> new stuff has an API, but it's not really possible to use for other mod developers, due to it
> being so custom and proprietary. We don't really have an API for modularity and expansiveness on
> the MC dependency layer, which is where all other developers operate. [...] The API for
> interacting with the Forgero component model is basically non-existing, proprietary and extremely
> heavy. Simple stuff like 'how much damage does this tool provide?' or 'which upgrades does this
> item have?' are basically impossible. [...] It feels like it was designed in a void."

**Branches examined:** `forgero-2` (head `063d9e02`), `forgero-2-smithing` (smithing minigame
spin-off, diverged Aug 2025), `1.20` (head `1162c74`).

---

## TL;DR Verdict

The core diagnosis is **correct, with one factual overstatement and one missed asset**.

| Claim | Verdict |
|---|---|
| "The rework is a modern equivalent of the original architecture" | **Confirmed.** The new model is a near 1:1 conceptual re-implementation of the old one — same ontology, same service-locator pattern, same convert-then-compute query shape. What improved is implementation quality, not design. |
| "The API is custom and proprietary, unusable for other mod devs" | **Largely confirmed.** A public API exists (`ForgeroApi`) but it exposes Forgero's internal machinery, requires 3+ internal modules on the classpath, and its only documented usage example doesn't compile. |
| "No API for modularity on the MC dependency layer" | **Confirmed for the component model** — but the rework quietly built the *seed* of the right answer: mixins already push damage, durability, and mining speed into vanilla MC surfaces that other mods can read with **zero** Forgero dependency. This is undocumented and was never treated as the API, but it is the most third-party-friendly thing in the codebase. |
| "The data-driven modules are great" | **Confirmed.** The data pipeline, COF format, and property/plugin system are genuinely well designed, documented, and extensible. |
| "Simple queries are basically impossible" | **Overstated.** Attack damage is a 4-line query; upgrades ~10 lines. But every line requires adopting Forgero's proprietary ontology, so the *spirit* of the complaint — "not intuitive, designed in a void" — holds. |

The rework solved the problem the old codebase had *internally* (god interfaces, instanceof chains,
untestable property folds, bolted-on caches) and did not touch the problem it had *externally*
(no consumer-shaped API on Minecraft-native types). Since the stated motivation for stopping was
the external problem, halting was a defensible call — but the branch is **not** a write-off. The
core modules are salvageable; what's missing is a thin facade layer that was never started.

---

## 1. "A modern equivalent of the original architecture" — Confirmed

Every load-bearing abstraction in the old architecture has a direct structural successor in the
new one:

| Old (`1.20`) | New (`forgero-2`) | Same shape? |
|---|---|---|
| `State` (extends `PropertyContainer, Matchable, Identifiable, Comparable, Typed, Visitable, DataSupplier` — a 7-concern god interface) | `Component` (extends `Identifiable, Taggable, PropertyHolder`, plus `getChildren()`) | Yes — slimmer, but still the universal "everything is one of these" root |
| `Composite` / `Construct` (352-line class) | `StructuredComponent` / `CustomizableComponent` + record impls (`StructuredExtensibleEquipment`, …) | Yes — composition tree with slots |
| `SlotContainer` / upgrade slots | `ComponentUpgrades` / `UpgradeSlot` | Yes |
| `PropertyContainer.stream().applyAttribute(KEY)` — fold over sorted attribute list | `Resolver.resolve(component, engine)` → two-phase bake/apply via `DataTypeEngine` | Yes — same "collect properties from tree, fold to a float" pipeline, now with explicit caching |
| `StateService.INSTANCE` (late-initialized mutable singleton) | `ForgeroApi` (static `CONTEXT`, `initialize()`, throws if accessed early) | Yes — identical service-locator pattern |
| `CachedStackConverter` (Guava cache, `ItemStack → State`) | `ComponentConverter` / `StatefulConverter` (+ Caffeine cache in `ResolverEngine`) | Yes |
| `StateParser` / `StateEncoder` (ad-hoc NBT) | COF (`ComponentCofCodec`, versioned, two-way) | Yes — but materially better |
| `ResourcePipeline` / `PipelineBuilder` | `ForgeroDataInitializer` 6-stage pipeline | Yes — but materially better |
| `ReloadableStateRegistry` | `TaggedRegistry<Component>` / `ComponentRegistry` | Yes |
| `ForgeroInitializedEntryPoint` | `DataPlugin` / `PostLoadPlugin` | Yes — richer |

The clearest proof is the third-party query for attack damage, old vs. new:

```java
// 1.20
StateService.INSTANCE.convert(stack)
    .map(state -> state.stream().applyAttribute(AttackDamage.KEY));

// forgero-2
ForgeroApi.converter().toComponent(stack)
    .map(c -> ForgeroApi.resolver().resolve(c, new AttributeEngine())
                        .getValue(DefaultAttributes.ATTACK_DAMAGE));
```

These are isomorphic. Same number of proprietary concepts (singleton entry point, domain object,
computation machinery, attribute key); the new version is arguably *heavier* at the call site
because the consumer must construct an internal engine object (`new AttributeEngine()`) just to
read a number. From an external developer's seat, **nothing changed between 2022 and 2025.**

### The fair counterpoint

"Just baking in the old stuff" undersells what did improve, and it's worth being precise, because
it determines what's salvageable:

- **Sealed interfaces and records** replaced 141+ `instanceof` chains and the 352-line `Construct`.
- **Two-phase bake/apply resolution** (`ResolverEngine`, Caffeine-cached) replaced re-streaming
  the whole property fold on every query with hidden 10-second Guava TTL caches.
- **COF is a versioned, documented, two-way serialization contract** (`cof_version`,
  pristine-vs-mutated optimization) replacing the ad-hoc `StateParser`/`StateEncoder` NBT.
- **The data pipeline is staged, testable, and documented** (`modules/core/.../data/readme.md`),
  replacing the tangled `ResourcePipeline`.
- **Real module boundaries** (`modules/core` has no MC dependency; `mc/*` modules have explicit
  dependency direction) and an actual test suite (PRs #1169, #1170).

So: a real engineering step forward, and **zero product/API step forward**. The rework re-derived
its requirements from the old architecture instead of from external consumers — which is exactly
the "designed in a void" feeling. The ontology (Component trees, property resolution engines,
tagged registries) was rebuilt because it existed, not because anyone outside Forgero asked for it.

---

## 2. The API claims — confirmed in substance, overstated in degree

### What actually exists

`modules/mc/loader/.../loader/api/ForgeroApi.java` is explicitly labelled *"The official public
API for Forgero"* and exposes five static accessors: `converter()`, `tagGraph()`, `resolver()`,
`components()`, `defaultComponents()`. So "basically non-existing" is factually wrong — an API
exists, was deliberately added (commit `7b407f0a`, *"Added a better API for accessing Forgero
resources"*), and has javadoc.

### Why the claim is still right in substance

1. **The only documented example doesn't compile.** The `ForgeroApi` class javadoc demonstrates
   `ForgeroApi.component(player.getMainHandStack())` — a method that does not exist on the class.
   The real path is `ForgeroApi.converter().toComponent(stack)`. The flagship API's one usage
   example was never tested against the API. No stronger evidence of "designed in a void" exists
   in the codebase.

2. **It's a service locator over internals, not an API.** Every accessor returns internal
   machinery (`Resolver`, `TaggedRegistry<Component>`, `ComponentConverter`). To answer "how much
   damage?", the consumer must know to instantiate `AttributeEngine` — an `impl`-package class —
   and pass it to a generic resolution engine. The API answers "how do I operate Forgero's
   property system?", not "what is this item's damage?". Those are the implementer's questions,
   not the consumer's.

3. **Dependency weight.** The minimal damage query drags in `modules:mc:loader`,
   `modules:mc:common`, and `modules:core` (plus transitive Caffeine/Datafixer), touching 4
   proprietary types. There is no thin `forgero-api` artifact; depending on the API means
   depending on the implementation modules, with no semantic-versioning promise on any of them.

4. **No discoverability.** No root-level developer documentation, no `FOR_DEVELOPERS.md`, no
   example consumer mod. The best reference implementation for the query API is the internal debug
   screen (`modules/mc/development/.../ComponentInspector.java`) — i.e., you learn the API by
   reading Forgero's own internal tooling.

5. **Asymmetric extensibility.** Adding a new *property type* (on-hit handlers etc.) is genuinely
   well supported via `DataPlugin` + codec registration and is documented in
   `modules/mc/properties/.../readme.md`. But adding a custom *attribute* from another mod is
   effectively unsupported (`SimpleAttribute` construction and `ComponentMutater` are internal),
   and there are no crafting/lifecycle event hooks. The system is extensible along the axes
   Forgero's own content needed — and only those.

### The two example questions, answered honestly

**"How much damage does this tool provide?"** — possible, 4 lines, but requires `ForgeroApi` +
`Component` + `AttributeEngine` + `DefaultAttributes` and three gradle modules. Verdict: not
impossible — *proprietary*.

**"Which upgrades does this item have?"** — possible (~10 lines): convert to `Component`,
`instanceof CustomizableComponent`, iterate `upgrades().slots()`, unwrap `Optional<Component>`
per slot, read `OpenIdentifier`s. The result is Forgero identifiers, not ItemStacks or MC
identifiers — the consumer ends up holding objects from Forgero's ontology with no bridge back to
the layer they operate in. Verdict: not impossible — *unintuitive and ontology-locked*, exactly as
stated.

---

## 3. The missed asset: vanilla interop already half-exists

The statement says there's no API on the MC dependency layer. For the *component model* that's
true. But the rework's mixins already route computed values into **vanilla-readable surfaces**:

| Value | Vanilla surface | Mixin |
|---|---|---|
| Attack damage / attack speed / armor | `ItemStack.getAttributeModifiers(slot)` → `EntityAttributeModifier` | `ItemStackAttributeMixin` → `AttributeManager` |
| Durability | `ItemStack.getMaxDamage()` (and item bar) | `ItemStackDurabilityMixin` |
| Mining speed | `Item.getMiningSpeedMultiplier()` | `ItemMiningMixin` |

A combat-overhaul mod can therefore read a Forgero tool's effective damage **without Forgero on
the classpath at all** — through the exact vanilla APIs it already uses for every other item. This
is the answer to "where all other developers operate," and it already works. It's just:

- **undocumented** (no mod author could know this is reliable),
- **untreated as a contract** (no test pins it, nothing stops a refactor from breaking it),
- **incomplete** (parts/upgrades/custom properties have no MC-layer representation; the COF NBT
  blob is written to the stack but its schema — despite being versioned and documented internally —
  is not published as a stable contract).

The strategic irony: the most valuable third-party API in the rework is the one part nobody
designed as an API.

---

## 4. Root cause: inside-out API design

The pattern across both generations is consistent: the API surface is the *implementation's
vocabulary* (states/components, resolvers/streams, registries, engines) rather than the
*consumer's questions* (is this yours? what are its stats? what's attached? notify me when X).
Successful mod-ecosystem APIs (Fabric API lookups, Trinkets, EMI) invert this: tiny interfaces,
MC-native types in every signature, events over registration plumbing, and the host mod's domain
model kept entirely behind the curtain.

Forgero-2 had all the raw material for this — the resolution engine even computes everything a
facade would need — but the facade layer was never built, because requirements were sourced from
the existing system rather than from external use cases. Hence: technically excellent modules, and
an API that answers questions nobody outside the project asks.

---

## 5. What is salvageable (most of it)

**Keep as-is (good, finished, documented):**
- The data loading pipeline (`modules/core/.../data/`) — staged, testable, documented.
- COF (`modules/core/.../cof/`) — versioned two-way serialization with the pristine/mutated
  optimization; this is *already* the right foundation for a public NBT contract.
- The property system + plugin architecture (`modules/mc/properties`, `DataPlugin`/
  `PostLoadPlugin`) — the "data-driven features" correctly identified as great.
- The vanilla mixin interop (`modules/mc/loader/.../mixin/`) — promote from accident to contract.

**Keep but demote to internal:**
- `Component`/`Resolver`/`AttributeEngine`/registries — these are good internals. They should
  simply never appear in a third-party method signature.

**Build (the actual missing piece, and it's small):**

1. **A deliberately tiny `forgero-api` artifact** — one module, no implementation dependencies
   leaked, semver-promised — shaped by consumer questions, MC-native types only:
   ```java
   ForgeroItems.isForgero(ItemStack)            -> boolean
   ForgeroItems.stats(ItemStack)                -> ItemStats (damage, durability, miningSpeed, …)
   ForgeroItems.parts(ItemStack)                -> List<PartView>    // Identifier + display name + ItemStack form
   ForgeroItems.upgrades(ItemStack)             -> List<UpgradeView> // slot id, filled?, content as ItemStack
   ForgeroItems.withUpgrade(ItemStack, ItemStack) -> Optional<ItemStack>
   ForgeroEvents.TOOL_CRAFTED / UPGRADE_APPLIED   // Fabric-style callbacks
   ```
   Everything above is a thin wrapper over machinery that already exists; this is weeks of work,
   not a rewrite.

2. **Publish the NBT/COF schema and the vanilla-attribute behavior as documented contracts**, with
   tests that pin them. That makes "integrate with Forgero without depending on Forgero" the
   default path — the strongest possible answer to the MC-dependency-layer complaint.

3. **A consumer-driven acceptance test**: a tiny in-repo example mod (`mods/example-integration`)
   that depends *only* on the API artifact and implements the canonical questions. If the example
   mod can't do something pleasantly, the API isn't done. This is the structural fix for
   "designed in a void" — it puts a consumer in the room.

---

## 6. Conclusion

The halt decision was based on an accurate read: forgero-2 is a higher-quality re-statement of the
same design, and re-stating the design was never going to fix external usability, because the
design's ontology *is* the usability problem for outsiders. But the conclusion "I lost faith"
overshoots the evidence. The branch contains roughly 80% of a good system: the data layer, the
serialization contract, the property/plugin system, and (accidentally) the vanilla interop are all
keepers. What it's missing — a consumer-shaped facade and the discipline of treating the MC layer
as the primary API — is the *cheapest* part of the whole effort, and the only part that was never
attempted in either generation.

The fix is not forgero-3. It's finishing forgero-2 from the outside in.

# Runtime-in-the-Tree: The Carrier Inventory and the Build-Time Inversion

**Question under analysis:** which parts of the system exist *only* to make runtime behaviour
resolvable inside the component tree — and what does each become when the rule is inverted:
**components resolve everything at build; results live at the outermost layer (the compiled
item); the MC interface handles runtime from there; runtime state never enters a component
tree.**

Grounded in the June 2026 head (`8da586ed5`), every claim verified in source.

---

## 1. The canonical specimen: one hit, end to end

`OnHitManager.handleOnHit` (modules/mc/properties/.../onhit/OnHitManager.java) is the violation
in its purest form. Annotated:

```java
public static void handleOnHit(ItemStack stack, Entity source, Entity target) {
    // [1] A vanilla event fired. We are in the MC domain holding live MC state.

    // [2] MC state is TRANSLATED INTO CORE VOCABULARY — entity tags re-encoded
    //     into a core-defined bag (ContextKeys.TARGET_TAGS lives in modules/core!).
    DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
    Set<OpenIdentifier> targetTags = Registries.ENTITY_TYPE.getEntry(target.getType())
            .streamTags().map(...).collect(...);
    contextBuilder.put(ContextKeys.TARGET_TAGS, targetTags);

    // [3] THE ROUND TRIP: stack NBT → full component tree parse (per hit, uncached)
    //     → runtime context descends the tree → bake/apply inside core → list returns.
    List<OnHitProperty> properties =
        ForgeroApi.itemProperty().resolve(stack, OnHitProperty.Engine::new, contextBuilder.build());

    // [4] Back in the MC domain: select targets, run effects — pure MC execution.
    for (OnHitProperty property : properties) { ... effect.apply(source, finalTarget); ... }
}
```

Steps [2] and [3] are the entire cost. The question the tree is asked — "which on-hit effects
does this item have, and do their conditions pass?" — decomposes exactly:

- *which effects the item has* — a pure function of the tree → **build-time**;
- *do the conditions pass against this target* — a question about live MC state → **MC-domain
  evaluation**, currently smuggled into core inside the context bag.

Neither half needs runtime-in-the-tree. The round trip exists because the architecture stores
the answer in the tree and the question in the world, then builds a courier to carry the world
to the tree on every event. **There are fourteen copies of this courier** (see §2E).

## 2. The complete carrier inventory

Everything below exists solely to make step [2]/[3] possible. Categorized by role.

### A. The smuggling vehicle
| Artifact | Location | Role |
|---|---|---|
| `DynamicContext`, `Key<T>` | **core**/property/context | the typed bag runtime state travels in |
| `ContextKeys` (`TARGET_TAGS`…) | **core**/property/context | core declaring *runtime entity* vocabulary — its javadoc states the pattern: "the game-specific module (populating the context) and the core module (reading from the context)" |
| `MinecraftContextKeys` | mc/predicate | the MC-side key set |
| `DynamicContextFactory` | mc/predicate | manufactures the bag from `Entity`/`World` |

### B. APIs whose *signatures* institutionalize the violation
| Signature | Why it's the disease |
|---|---|
| `DataTypeEngine.apply(B, DynamicContext)` / `resolve(component, context)` | every engine is *defined* as runtime-parameterized |
| `Resolver.resolve(component, engine, context)` + the `DynamicContext.empty()` default overload | the empty-default is the smoking gun: the parameter exists, and is unused at almost every call site |
| **`EquipmentComponent.getAttribute(type, DynamicContext)`** | the *domain model itself* takes runtime state — the violation is in the component API, not just the machinery around it |
| `ItemPropertyApi.resolve(stack, engineSupplier, context)` | the public API teaching every consumer the round trip |
| `AttributeEngine.resolveAttributes(component, context)` / `PrecomputedAttribute.compute(context)` | runtime-conditional attribute evaluation inside core |

### C. The conditional machinery inside core
`DynamicCondition` (core), `Condition.dynamicConditions` + the dynamic halves of
`And/Or/NotCondition`, `OrDynamicCondition`, `ConditionalProperty.test(context)`,
`OptimizedBakedResult.stream(context)` — the static/dynamic partition is *correct analysis*
implemented in the *wrong place*: the dynamic half is filtered inside core resolution instead of
at the MC boundary.

### D. The registration plumbing
`PluginRegistrationContext.registerDynamicConditionCodec(...)` and the dynamic half of
`ConditionCodec` — the mechanism by which data files attach runtime predicates *to tree nodes*,
which is what obligates the tree to be runtime-evaluable in the first place.

### E. Fourteen couriers
Every property manager repeats the §1 round trip verbatim: `OnHit`, `OnTick`, `OnKill`,
`OnDamageReceived`, `OnHitBlock`, `OnBlockPlace`, `OnSneakToggle`, `SwingHand`, `Loot`,
`BlockBreaking`, `BlockUse`, `EntityUse`, `UseInteraction` (+ `OnTickProjectileManager` in bows).
Fourteen independent event→context→tree→execute paths — fourteen reasons the tree must stay
runtime-resolvable, and fourteen per-event NBT parses.

### F. The consequences (cost paid *everywhere* because dynamics are *possible* anywhere)
This is the load-bearing analytical point: because resolution is parameterized on
`DynamicContext`, a resolved result is **not a function of the tree alone** — so it cannot be
persisted on the stack. The *possibility* of runtime context is what forbids compile-and-store.
Hence: the per-query NBT parse on every mixin and every event, `BakedAttributes` only able to
cache the *static partition* per component instance, `AttributeQueryResult` as a closure instead
of data, and tooltips (`TooltipBuilder`, `DifferenceCalculator`, `TooltipValueResolver`…)
threading a context parameter they almost never fill. The tax is not collected where dynamics are
used; it is collected at every call site because the type system says dynamics *might* be.

## 3. The inversion: what each piece becomes

The target shape — one artifact, two domains:

```
BUILD (mutation/craft/reload)                     RUNTIME (MC interface)
─────────────────────────────                     ─────────────────────────
Component tree                                    vanilla event fires
   │ compile (static conditions                      │
   │ resolved away, scopes folded)                   ▼
   ▼                                              read CompiledItem from stack   (one NBT read)
CompiledItem  ── persisted on stack ──►           descriptors for this trigger
  • stats  → vanilla AttributeModifiers              │ evaluate predicate SPECS against
  • effect descriptors:                              │ the live event — in MC types,
      (trigger, selector spec,                       │ at the boundary, no bag, no tree
       predicate spec, payload)                      ▼
  • tags / identity / gen-stamp                   run registered handlers (already MC code)
```

| Today's carrier | Becomes |
|---|---|
| The 14 managers | **one dispatcher**: `event → CompiledItem.effects(trigger) → predicate check → handler` |
| `DynamicContext`/`Key`/`ContextKeys` (core) | **deleted from core.** Handlers and predicates receive MC types (`Entity`, `World`, `BlockState`) directly — no translation bag |
| `DynamicContextFactory` | deleted — there is no context to fabricate; the event *is* the context |
| `DataTypeEngine.apply(...)` | deleted; engines become **compiler passes** (the bake half survives as "compile to descriptors/stats") |
| `EquipmentComponent.getAttribute(type, ctx)` | `getAttribute(type)` — the domain model becomes pure |
| `DynamicCondition` in core | moves wholesale to mc: a **predicate spec** — *data* compiled into the descriptor, evaluated by the dispatcher. The existing mc/predicate codecs (`EntityPredicate`, `BlockPredicate`, `WeatherPredicate`, `DamagePredicate`, `RandomPredicate`) keep their JSON format verbatim; only the evaluation site moves |
| `registerDynamicConditionCodec` | survives as the registration point for predicate specs in the descriptor format — same extension point, new home |
| `ItemPropertyApi.resolve(stack, engine, ctx)` | `CompiledItem.effects(trigger)` / an `ItemEffectApi` — a read, not a resolution |
| Runtime-conditional attributes ("+5 at night") | an effect descriptor toggling a vanilla modifier — vanilla's own model (potions, Smite) |
| Tooltips / `DifferenceCalculator` | read the artifact; comparisons compile both candidate trees and **diff two flat artifacts** — no context parameter anywhere in the tooltip stack |

**What survives unchanged** — and this is why the inversion is cheaper than it looks:
static conditions + `ResolutionContext` (already pure, already build-time), the scope/collector
composition logic (becomes compiler passes), the effect *handlers* (`FireHandler` etc. — already
MC code taking MC types), the selectors, and **the authored JSON format almost entirely**: a
property with an entity predicate keeps the same bytes on disk. What changes is *when* the
property list is computed (build, once) and *where* its predicate runs (MC dispatch, per event).

**The pristine trick transfers:** COF already serializes unmodified components as a bare id.
The compiled artifact does the same — pristine items reference their compiled definition in the
registry (near-zero NBT); only mutated items carry their full descriptor set. Stamped with the
data-generation fingerprint for reload staleness, as designed for stats.

## 4. The enforcement: make the violation uncompilable

The rule "runtime never enters a component tree" must not be a guideline — it should be a
**compile error**. The inversion achieves that structurally: `DynamicContext`,
`DynamicCondition`, and `ContextKeys` are *deleted from modules/core*, so core has no type with
which to receive runtime state. `modules/core` already has zero MC dependencies; after this it
also has zero runtime *vocabulary*. The only thing core exports toward the game is the compiled
artifact and the compiler that produces it. A future PR that wants to "just pass the target into
resolution" cannot — the parameter type does not exist on that side of the boundary.

One law, enforced by the module graph:

> **Runtime reads the artifact, never the tree. The tree is consumed at build and does not exist
> at play-time.**

## 5. The ledger

| Dimension | Today | After inversion |
|---|---|---|
| Runtime-carrier types in core | 8+ (`DynamicContext`, `Key`, `ContextKeys`, `DynamicCondition`, dynamic halves of 3 logical conditions, `OptimizedBakedResult`'s context path, `PrecomputedAttribute.compute(ctx)`) | **0** |
| Runtime-parameterized API signatures | 5 families (engine, resolver, component, property API, attribute engine) | **0** |
| Event→tree couriers | 14 managers | 1 dispatcher + handler/predicate registries |
| NBT tree parses on hot paths | every mixin call + every event of 14 kinds | 0 (one flat read; resolve-fallback only for stale stacks) |
| Concepts to predict one damage number | ~25 (traced in the accumulation analysis) | tree-build concepts at *craft time* only; at runtime: artifact → number |
| Authored JSON format | — | ~unchanged (predicate/effect specs identical; evaluation site moves) |

## 6. Honest risks, named

1. **The descriptor format becomes a persisted contract.** It needs a version field and the
   generation stamp from day one — same discipline COF already has (`cof_version`).
2. **The dispatcher is new central code.** It must stay a dumb loop (read, filter, dispatch).
   The regrowth failure mode is a handler calling `converter.toComponent(stack)` to ask a
   structural question at runtime — that one call would re-justify the entire deleted machinery.
   Structural questions must be answered at compile time by putting their answers *into* the
   descriptor (e.g., compile "head material is netherite" into a boolean/payload field, not a
   runtime tree query).
3. **Third-party custom engines** (the `ItemPropertyApi.resolve(stack, customEngine, ctx)` users)
   migrate to "register an effect type: a compile function (tree → descriptor) + a runtime
   handler (event + descriptor → action)". Strictly clearer than implementing a
   `DataTypeEngine`, but it is a breaking change to that extension point.
4. **Sequencing:** this lands after (and subsumes) the stat-persistence step — stats are simply
   the first compiled output; effect descriptors are the second. One parity gate each: stats
   parity against `AttributeEngine` (harness already proven), and descriptor parity asserting
   that for every item and every trigger, the dispatcher's selected effect set equals today's
   manager-resolved set across the test content.

## 7. Conclusion

Strip the fourteen couriers, the context bag, and the apply-phase, and what remains *is* the
architecture you were trying to build both times: a pure compiler over a component tree (core),
a flat compiled item at the boundary (the outermost layer), and Minecraft handling Minecraft.
Every piece inventoried in §2 exists for exactly one reason — the decision, inherited from the
original architecture, that the tree must be able to answer questions about the live world.
Reverse that single decision and the carriers don't need to be refactored; they need to be
deleted, because the question they exist to transport is no longer asked.

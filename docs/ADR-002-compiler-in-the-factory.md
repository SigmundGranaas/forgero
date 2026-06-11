# ADR-002: The Compiler in the Factory

**Status:** Proposed (founding statement for the runtime-inversion work)
**Context:** Follows from `forgero-2-runtime-in-tree-analysis.md` — runtime state must never
enter the component tree; components resolve everything at build; the MC layer owns runtime.

---

## Decision

> **The Resolver is rebranded and re-scoped as the Compiler. The Compiler is not a public API —
> it is part of the component factory.** It traverses the tree once, at construction, and
> constructs all attributes and properties into a compiled map stored on the outermost
> component. That map exposes the only query API. The MC layer reads it and applies dynamic
> behaviour at its own level — at event time, in Minecraft types.

Three sentences carry the whole architecture:

1. **Compile-on-construct is a constructor invariant.** A component is never observable in an
   uncompiled state. The factory (data-load builder, COF deserialization, every
   `withStructure`/`withUpgrades` mutation) invokes the compiler; everything downstream of the
   factory sees only compiled data. Immutability makes this airtight — tree and compiled state
   cannot drift because neither can change after construction.

2. **The compiled map is the outermost layer.** The root component carries
   `CompiledProperties`: flat stats plus effect descriptors per trigger, with all *static*
   conditions already resolved away and *dynamic* predicates carried as data (specs), not
   evaluated. No method on it takes a context parameter.

3. **The MC layer applies dynamics when querying.** Mixins read stats from the map. A single
   event dispatcher reads `effects(trigger)` from the map, evaluates predicate specs against the
   live event in raw MC types (`Entity`, `World`, `BlockState` — no context bag), and runs
   registered handlers. Runtime evaluation exists only at the MC boundary.

## Why the rename matters

A *resolver* answers questions on demand: the name implies a runtime service, a service invites
callers, callers demand context parameters — that chain is exactly how `DynamicContext` ended up
threaded through `DataTypeEngine.apply`, `Resolver.resolve`, `ItemPropertyApi.resolve`, and
`EquipmentComponent.getAttribute`. A *compiler* transforms representations during a phase: it has
no runtime callers and accepts no runtime context **by definition of what it is**. The
apply-phase is not refactored away; it becomes a category error that the vocabulary itself
rejects.

## The shape

```
FACTORY (the only place the compiler runs)            PUBLIC SURFACE
──────────────────────────────────────────            ────────────────────────────
data load ──┐                                         CompiledProperties (on root component)
COF parse ──┼─► build tree ─► Compiler passes ─►        float   stat(OpenIdentifier)
mutation  ──┘    (attributes pass,                      List<EffectDescriptor> effects(trigger)
                  effects pass,                         <T> List<T> get(PropertyKey<T>)
                  tooltip pass, …)                       — no context parameter anywhere —
                          │
                          ▼                            MC LAYER
              root component stores                    mixins      → stat() reads
              CompiledProperties                       dispatcher  → effects(trigger)
                                                          → evaluate predicate specs vs live event
                                                          → run handlers (plain MC code)
```

## Where this lands in the current code

| Today | Becomes |
|---|---|
| `AttributeBaker.bake()` in `StaticEquipment`'s constructor | the precedent — generalized into the factory's compile step for **all** property types |
| `Resolver` / `ResolverEngine` / `DataTypeEngine<B,R>` | `Compiler` + `CompilerPass` (the bake halves survive as passes); **package-private to the factory** — `ForgeroApi.resolver()` is removed |
| `BakedAttributes` (attributes only, partial) | `CompiledProperties` (all property types, total) |
| `EquipmentComponent.getAttribute(type, DynamicContext)` | `compiled().stat(type)` — the domain model goes pure |
| `ItemPropertyApi.resolve(stack, engine, ctx)` | deleted; consumers call `effects(trigger)` (via `ItemQueryApi`/an `ItemEffectApi` facade) |
| 14 property managers | one dispatcher over `effects(trigger)` |
| `DynamicContext`/`ContextKeys`/`DynamicCondition` in core | deleted from core; dynamic predicates are descriptor *data*, evaluated by the dispatcher in MC types |
| `ItemQueryApi` (good, keep) | unchanged signatures, now backed by map reads instead of per-query resolution |
| Plugin extension (`registerPropertyCodec`, engines) | register per property type: **a compile pass** (tree → compiled entries) + **an MC handler** (event + descriptor → action) |

## The gap the decision statement leaves open — closed here

Compile-in-factory alone does **not** fix the hot path: stacks are converted to components per
query, and construction-per-parse would just move the cost. The completion is:

> **The compiled map is what gets serialized to the stack.** Queries deserialize
> `CompiledProperties` directly — the tree is *not reconstructed for reads*. The tree is only
> deserialized (COF) when the item is **mutated**, at which point the factory recompiles and
> writes both back.

With the existing pristine/mutated trick: unmodified items store a registry reference (near-zero
NBT, compiled map looked up from the data bundle); mutated items carry their map inline, stamped
with the data-generation fingerprint for reload staleness. Entity attributes within the map are
additionally projected to vanilla `AttributeModifiers` NBT so non-Forgero mods read and extend
them through standard Minecraft APIs.

End state: **the tree exists at build time; the artifact exists at play time.** Queries never
see a tree; the compiler never sees the world.

## Enforcement

- `modules/core` loses `DynamicContext`, `DynamicCondition`, `ContextKeys` entirely — runtime
  state into the tree becomes *uncompilable*, not discouraged.
- The compiler types are not exported from the factory package — "resolve it yourself" is not
  expressible by external code.
- Regrowth tripwire (from the carrier analysis): no runtime handler may call
  `converter.toComponent(stack)`. Structural answers are compiled *into* descriptors, never
  queried at event time.

## Migration gates

1. Stats parity: compiled map vs `AttributeEngine.resolveAttributes` (empty context) across the
   full component registry — harness pattern already proven on this branch.
2. Descriptor parity: for every test item and every trigger, the dispatcher's selected effect
   set equals today's manager-resolved set.
3. Only after both gates: delete the resolver surface, the context machinery, and the managers.

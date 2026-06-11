# Ripple Refactor Plan: Consequences of the Compile-at-Construction Inversion

**Context:** Two changes landed — (1) runtime state removed from `modules/core` (the compile/run
boundary is now type-enforced), and (2) terminals compile their full property artifact at
construction (`CompiledProperties`), with reads going through it. This document inventories what
those changes make *redundant, over-general, or newly possible*, ordered by the maintainer's
stated goal: **reduce concept count / restore conceptual integrity**, not raw LOC.

Two read-only surveys (core internals, MC modules) fed this; I've re-weighted their verdicts
against concept-count rather than "is it low-cost to keep."

---

## The single thread tying every finding together

Every item below is one of two shapes:

1. **A concept that existed to serve runtime resolution** — now that resolution is a build step,
   it's either dead or over-general (the `Resolver`/two-phase engine, the `instanceof terminal
   else resolve` fallbacks, the per-query NBT parse).
2. **Duplication that the uniform compiled artifact now lets us collapse** — the 14 managers, the
   engine-vs-key call shape, the context-bag boilerplate.

Fixing them is not cleanup for its own sake; it's deleting the concepts that the inversion
*orphaned*. That is exactly the accumulation cure.

---

## Tier 1 — Concept deletions (highest impact on holdability)

### 1.1 Collapse the `DataTypeEngine<B,R>` two-phase framework → one-phase `CompilerPass`
The core survey verified: **no `Resolver` exists**, baking happens **once at construction**, and
`apply()` is now trivial (`AttributeEngine.apply` wraps a map; conditional engines call
`baked.all()`). The two-phase bake/apply split existed solely to let a `Resolver` cache `B` and
re-`apply` per runtime context — a use case the demolition deleted. Keeping it is keeping the
*shape* of the old runtime engine, which is precisely the "modern equivalent of the original
architecture" smell.

- **Change:** `interface CompilerPass<R> { ResolutionKey<R> key(); R compile(Stream<Component>); }`.
  `AbstractConditionalPropertyEngine.bake`+`apply` fold into one `compile`. `ComponentCompiler`
  and `ItemPropertyApiImpl` call `compile`/read; the `resolve(Component)` convenience stays as the
  non-terminal fallback (one default method).
- **Blast radius:** `AttributeEngine`, `AbstractConditionalPropertyEngine` (→ all 14 property
  Engines inherit), test stubs, `DataTypeEngine.java`. Mechanical.
- **Why over the survey's "keep":** the survey kept it for "conceptual clarity / future
  extensibility." But the future it's ready for (context-dependent finalization) is the exact
  thing the architecture now forbids by design. The generality is not low-cost — it is the
  central concept the maintainer wants gone. **Do it.**

### 1.2 Collapse 14 property Managers → 1 dispatcher
Verified ~900 LOC of near-identical structure: build context → `RuntimeConditions.filter(resolve(
stack, KEY), ctx)` → iterate effects with the same `ContextualEffectHandler`/`EntityEffectHandler`
instanceof dispatch. The compiled artifact made the bodies identical (no per-manager engine run,
no tree walk).

- **Change:** one `PropertyDispatcher.dispatch(stack, KEY, ctx, effectRunner)` (and a variant for
  the few that iterate a target set). Each manager shrinks to a thin adapter that builds its
  context and supplies its effect-runner — or disappears into the mixin call.
- **Blast radius:** 14 managers, ~16 mixin entry points (call sites stay or thin out). Medium.
- **Risk:** behavior is gametest-covered, not unit-covered headless — needs care. Stage it: build
  the dispatcher, migrate **one** manager (OnHit) end-to-end, confirm compile + any covering
  gametest, then fan out. **Do it, staged.**

### 1.3 Delete the `instanceof EquipmentComponent … else resolve()` fallbacks where non-terminals never reach
This pattern now appears in 4+ places (`AttributeEngine.getAttribute`/`resolveAttributes`,
`ItemPropertyApiImpl.read`, `TooltipBuilder`). Per the role model, **only terminals are ever
queried** — a bare part is never handed to a read path. If that invariant holds (needs
confirming: does any caller pass a non-terminal `Component` to these?), the `else` branches are
dead, and deleting them removes the last places core "resolves at query time."

- **Change:** confirm the invariant by auditing callers; if it holds, the read paths take/assume a
  terminal and the fallback (and `resolve(Component)` convenience) can go. If a legitimate
  non-terminal caller exists, keep a single explicit path, not four copies.
- **Why over the survey's "keep as safety net":** an unexercised "safety net" that lets a tree be
  resolved at runtime is exactly the regrowth vector ADR-002 warns about. Verify, then delete or
  centralize. **Do it after the audit.**

---

## Tier 2 — Latent correctness issues the inversion exposed

### 2.1 Dynamic-conditional **attributes** are now silently dropped
`AttributeEngine.buildBakedAttributes` partitions attributes into unconditional (→ `value()`) and
conditional (→ `PrecomputedAttribute.conditionalAttributes`, **never read**). Pre-demolition,
`compute(ctx)` applied them; now nothing does. So content declaring "+5 armor when sneaking" as an
*attribute* compiles to a value that silently omits it — no error, no log.

- This is **architecturally intended** (dynamic stats should be effects, per ADR-002) but
  currently **unannounced**, which is a content-migration trap. The survey called it "acceptable
  debt"; I'd call it a **named hazard**.
- **Change (now):** at compile, if an attribute carries dynamic conditions, log a one-time warning
  ("dynamic-conditional attribute X will not contribute; model it as an effect"). Drop the dead
  `conditionalAttributes` storage from `PrecomputedAttribute` (it's written, never read).
- **Change (later):** survey content JSON for dynamic-conditional attributes; migrate any to the
  event layer. Gate with a check.

### 2.2 The vanilla attribute-modifier merge still discards other mods' modifiers
`ItemQueryApiImpl.getAttributeModifiers` (lines ~363-368) still drops vanilla/other-mod modifiers
key-wise. This is the long-standing interop break, and the compiled artifact is the enabler for
the real fix (write vanilla `AttributeModifiers` NBT at compile time, merge by UUID). **Belongs to
the persistence step (Tier 3), not a standalone patch** — do it there so it's done once, correctly.

---

## Tier 3 — The persistence step (separate, larger; unblocks the rest)

### 3.1 Serialize `CompiledProperties` to the stack; stop parsing the tree per query
The surviving hot-path cost is `converter.toComponent(stack)` (full COF parse) on every mixin call
(`getMaxDamage`, `getMiningSpeedMultiplier`, `getAttributeModifiers`) and every projectile tick
(`OnTickProjectileManager` line 81, uncached). Compile-at-construction removed the *re-resolve*;
it did **not** remove the *re-parse*, because reads still rebuild the tree to reach the terminal.

- **Change:** persist the compiled artifact to NBT at mutation; reads deserialize
  `CompiledProperties` directly (one tag read), and only mutation parses the tree (COF). Pristine
  items reference their registry-compiled artifact (near-zero NBT) with a generation stamp.
  Project entity attributes into vanilla `AttributeModifiers` NBT — which fixes 2.2 by writing our
  own UUID-keyed entries and merging the rest.
- This is the next *increment*, not part of this ripple pass — it's the ADR-002 step the inversion
  was the prerequisite for. Flagged here so the Tier-1/2 work doesn't redundantly touch the same
  read paths.

---

## Tier 4 — Low-risk tidy (do alongside Tier 1, near-zero concept cost)

| Item | Change | Files |
|---|---|---|
| `ItemPropertyApi` takes `ResolutionKey`, not `Supplier<Engine>` | call sites pass `OnHitProperty.KEY`; impl reads `equipment.properties(key)` directly, no engine constructed just for `.key()` | `ItemPropertyApi`, impl, 14 call sites (folds into 1.2) |
| `ComponentTraversal` duplication | `ComponentCompiler` has its own `traverse`; reuse the shared one | `ComponentCompiler` |
| `bakedAttributes()` vestigial alias | delete; callers use `compiled().attributes()` (2 prod + 1 test) | `EquipmentComponent` |
| `property/context` package misnamed | only the *static* `ResolutionContext` remains there; rename pkg to `property/compilation` (or move beside `ResolutionKey`) | ~12 imports |
| Empty `DynamicContext.Builder` sites | 7 managers build empty bags → `DynamicContext.empty()` (folds into 1.2) | 7 managers |
| Dead `Engine` classes | `NameReplacementProperty.Engine` / `BetterCombatIdentifierProperty.Engine` — zero call sites; delete inner Engine, keep property + codec | 2 files |
| `DynamicContextFactory` | test-only now; move to test utils or delete | 1 file |

---

## Recommended execution order

1. **Tier 4 micro-deletions** that don't depend on the engine collapse (`ComponentTraversal`,
   `bakedAttributes()` alias, dead Engines, `property/context` rename) — pure subtractions, green
   in minutes, shrink the surface the bigger refactors touch.
2. **Tier 2.1 warning + dead-storage removal** — cheap, closes a silent hazard.
3. **1.3 audit** of read-path callers (decides whether fallbacks are dead).
4. **1.1 `CompilerPass` collapse** — the single biggest concept deletion; everything else gets
   simpler once engines are one-phase.
5. **1.2 dispatcher**, staged (OnHit first), absorbing Tier-4's `ResolutionKey` API and empty-bag
   cleanups.
6. **Tier 3 persistence** as its own subsequent increment (also lands 2.2).

Each step is independently compilable + green and is a *deletion of a concept the inversion
orphaned*, not new machinery. That ordering keeps the tree green and moves concept-count
monotonically down — which is the actual objective.

## What NOT to do (guard against over-reaching)

- Don't add a component-parse cache (survey's 2a) as a band-aid — Tier 3 persistence makes the
  parse disappear; a cache would be a bolted-on concept the architecture is meant to remove.
- Don't keep the two-phase engine "for future context-dependent finalization" — that future is
  the deleted runtime path; readiness for it is the smell.
- Don't collapse the `ContextualEffectHandler`/`EntityEffectHandler` 2-way dispatch into a visitor
  — it's already minimal; touching it adds concepts.

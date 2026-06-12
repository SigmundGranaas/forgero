# The Forgero-2 Attribute System: What's Actually Going On, and How to Collapse the Dual Layer

> **Process doc (historical).** This is the analysis/proposal that led to the realized design.
> The outcome shipped as `docs/ADR-002-compiler-in-the-factory.md` (compile-at-construction) and
> `docs/ADR-003-stat-contribution-kernel.md` (the `StatFold` kernel) — read those for the current
> state; the machinery analysed below has been deleted.

**Scope:** Deep-dive into attribute handling on the `forgero-2` branch — the full value path from
JSON to Minecraft — followed by a redesign proposal that improves the external API surface and the
internal design at the same time, by removing the parallel attribute layer rather than wrapping it.

Companion to `forgero-2-api-analysis.md`.

---

## 1. What the dual layer actually is

Forgero-2 runs a complete, parallel stat system next to Minecraft's, and bridges them with mixins
at four vanilla call sites. The same number (say, a pickaxe's attack damage) exists in **three
places simultaneously**:

```
                    JSON (materials, parts, upgrades)
                              │  data pipeline
                              ▼
              Component tree (COF, in stack NBT)
                              │
        ┌─────────────────────┼──────────────────────────┐
        │ (1) at item         │ (2) at EVERY vanilla     │ (3) vanilla's own
        │     registration    │     query, per call      │     layer
        ▼                     ▼                          ▼
  ForgeroToolMaterial   AttributeManager           PickaxeItem super-ctor
  resolve() once  ───►  stack → NBT parse →        called with (material, 0, 0)
  feeds vanilla         Component → bake →         "damage/speed will be
  ToolMaterial          ComputationChain →         provided by attributes"
  (durability,          EntityAttributeModifiers   i.e. the vanilla layer is
  mining speed/level)   that REPLACE vanilla's     deliberately zeroed out
```

The four runtime bridges (all in `modules/mc/loader/.../mixin/`):

| Mixin | Vanilla call site | Called how often |
|---|---|---|
| `ItemStackAttributeMixin` | `ItemStack.getAttributeModifiers(slot)` | Tooltip render, equipment change checks |
| `ItemStackDurabilityMixin` | `ItemStack.getMaxDamage()` + `getItemBarStep()` | Constantly — durability bar every frame, stack comparisons |
| `ItemMiningMixin` | `Item.getMiningSpeedMultiplier(stack, state)` | Every tick while breaking a block |
| `ItemTooltipMixin` | `Item.appendTooltip` | Every tooltip frame |

Each of these calls `AttributeManager.getResolvedAttributes(stack)`, which executes:

1. `StatefulConverter.toComponent(stack)` — a **full recursive COF deserialization from NBT,
   with no caching** (`ComponentNbtConverter.fromNbt` has none; the old 1.20 architecture's
   Guava `ItemStack → State` cache was dropped in the rework and never replaced).
2. `resolver.resolve(component, new AttributeEngine())` — a fresh engine allocation per call;
   the bake phase is Caffeine-cached, but the cache key is `record CacheKey(Component, OpenIdentifier)`,
   so every lookup pays **deep `equals`/`hashCode` over the entire component tree**.
3. `AttributeQueryResult.getValue(type)` — per attribute type queried: filter the baked list,
   evaluate dynamic conditions, construct a `ComputationChain` (which sorts), fold.

So the per-frame cost of showing a durability bar is: NBT tree parse + tree hash + sort + fold.
This is the dual-layer tax in its most concrete form: **vanilla already has a place to store the
answer, and Forgero recomputes the question instead.**

## 2. The replacement semantics actively break vanilla interop

`AttributeManager.getAttributes` (modules/mc/common/.../attribute/AttributeManager.java:90-107)
merges Forgero's map with vanilla's like this:

```java
finalMap.putAll(forgeroAttributes);
vanillaMap.entries().stream()
    .filter(entry -> !finalMap.containsKey(entry.getKey()))   // ← drops the whole key
    .forEach(entry -> finalMap.put(entry.getKey(), entry.getValue()));
```

For any attribute Forgero provides (attack damage, attack speed, armor, toughness), **every
vanilla-side modifier under that key is discarded** — including modifiers another mod added via
its own mixin earlier in the chain, and including the stack's own `AttributeModifiers` NBT that
map makers, datapacks, and `/attribute`-style commands use. The one MC-native extension point
other developers already know how to use is the one Forgero overwrites.

This is the strongest concrete form of the original complaint: the system doesn't just fail to
*offer* an MC-layer API — it disables the MC-layer API that already existed.

Smaller boundary smells in the same class: attack speed is translated with a hardcoded
`val - 4.0f` (player base speed), and the four vanilla armor-slot UUIDs are copy-pasted as magic
constants.

## 3. The legitimate core vs. the accidental duplication

To redesign this, separate what Forgero's attribute layer does that vanilla genuinely cannot,
from what it re-implements for no structural reason.

**Genuinely Forgero (keep):**

- **Compositional computation**: a tool's stats are a function of a part tree — head contributes
  base damage, material multiplies durability, gem adds mining speed. Vanilla has no concept of
  "this item's stats derive from its sub-items." This is the product.
- **Structural conditions**: `is_root`, `at_depth`, `in_slot_type`, `has_sibling`,
  `slot_contains`, `tag_match` — "this binding gives +2 armor only when socketed in a chestplate."
  All evaluate against the *component tree*, nothing else.
- **Custom stat types**: `mining_level`, future stats vanilla doesn't model.

**Accidental duplication (the dual layer — candidates for deletion):**

- A parallel runtime *representation* (`AttributeQueryResult` vs. `EntityAttributeModifier` maps).
- A parallel *operator algebra* (6 `Operator` classes + `group` ints + `compositeKey` sub-formulas
  vs. vanilla's add / multiply_base / multiply_total).
- A parallel *application* path (mixins re-resolving per call vs. vanilla reading stored modifiers).
- A parallel *display* path (`ForgeroTooltipRenderer` vs. vanilla's automatic attribute lines).

## 4. The pivotal observation: attributes are craft-time data pretending to be runtime data

Every single vanilla-facing resolution in the codebase uses the **empty** dynamic context:

- `AttributeManager.getResolvedAttributes` → `resolver.resolve(component, new AttributeEngine())`
  → `DynamicContext.empty()` (the convenience overload in `Resolver.java`).
- `ForgeroToolMaterial`, `ItemMiningMixin`, `ItemStackDurabilityMixin` — same.
- A repo-wide search finds **no attribute resolution anywhere that passes a real
  `DynamicContext`**. The only `DynamicCondition` implementations live in the predicate module
  (`BlockPredicate`, `EntityPredicate`) and are exercised by the *on-hit property system*, not by
  attributes.

This makes sense, because vanilla's query sites can't supply context anyway —
`getAttributeModifiers(slot)` has no target entity, `getMaxDamage()` has no world. Dynamic,
context-dependent stat bonuses ("+ damage vs. undead") are *event-shaped* (vanilla models them as
enchantments resolved during the damage event), and forgero-2 already has exactly the right home
for them: the on-hit/conditional property system in `modules/mc/properties` — the part of the
rework that is genuinely good.

Meanwhile, the inputs to attribute computation — the component tree — change **only** when the
item is crafted, a part is swapped, or an upgrade is socketed. All of which happen at Forgero's
own stations, in Forgero's own code.

**Conclusion: for everything the vanilla layer can observe, attribute values are immutable
between mutations.** The entire runtime half of the attribute system — the per-call resolution,
the bake cache, the engine framework participation, three of the four mixins — recomputes a
constant.

## 5. Complexity inventory (what the current design costs internally)

Found while tracing the code; each is an independent signal the abstraction is overbuilt:

1. **Bug — conditions silently dropped:** `SimpleAttribute`'s 5-arg constructor takes a
   `localCondition` parameter and passes `Condition.ALWAYS_TRUE` to the canonical constructor
   instead (modules/core/.../attribute/api/SimpleAttribute.java:46-54). Any caller using that
   overload gets unconditional attributes without warning.
2. **Doc/code contradiction on composite scope:** `CompositeAttribute`'s javadoc says components
   "must all originate from the same parent component"; `CompositeAttributeBakingStrategy`
   explicitly implements *global* combination across all parts ("This is the key change: we now
   consider all components together"). Two design generations are both still in the tree.
3. **Silent stat loss by design:** a composite group with fewer than two *distinct operators* is
   discarded entirely — the components "do NOT fall back to being treated as simple attributes."
   A data author who writes two ADD composite components gets nothing, with no log line.
4. **Dead configuration:** `AttributeEngine.ignoreComponents` is set by both constructors and
   never read; `ComputationChain` has its own independent `ignoreComponent` flag.
5. **Recomputation inside computation:** `CompositeAttribute.value()` constructs and sorts a new
   `ComputationChain` on every call — and `value()` is called from inside the outer chain's fold,
   on every query.
6. **Three attribute kinds where one would do:** the sealed triad (`SimpleAttribute`,
   `CompositeAttributeComponent`, `CompositeAttribute`) + two baking strategies + the
   `AttributeComponent` marker for filtering exist to express "named sub-formula." Vanilla's
   entire modded ecosystem runs on three operations and no sub-formulas.
7. **Hot-path waste:** uncached NBT parse, deep-equality cache keys, per-call engine allocation
   (§1) — a performance architecture (two-phase bake/apply + Caffeine) that still ends up slower
   on the common path than a single cached float would be.

None of these is fatal alone. Together they say: the attribute system was given the full generic
property-engine treatment (two phases, strategies, sealed hierarchies, generic engines) for a
computation that is, observably, a craft-time fold over a tree.

## 6. The redesign: resolve at mutation, persist vanilla-first

### Principle

> Forgero's attribute system is a **compiler**, not a **runtime**. It compiles a component tree
> into stats once, when the tree changes. Minecraft's own systems are the runtime.

### Mechanics

**1. One pure function replaces the engine for attributes.**

```java
// modules/core — no MC types, trivially unit-testable
Map<OpenIdentifier, Float> computeStats(Component root)
```

Internally: traverse tree → collect attribute entries → evaluate *structural* conditions (all of
`IsRoot`, `AtDepth`, `InSlotType`, `HasSibling`, `SlotContains`, `TagMatch` are pure functions of
the tree) → fold per stat type. No `DynamicContext`, no bake/apply split, no `DataTypeEngine`
participation, no result-object lambda. The generic `Resolver`/`DataTypeEngine` framework remains
for the property types that genuinely are dynamic (on-hit, mining patterns) — attributes simply
exit it.

**2. Run it only at mutation points, write results into the stack.**

Mutation happens exclusively in Forgero code: assembly, part swap, upgrade socket/removal, plus
first conversion of a mapped vanilla item. At those points:

- **Attack damage, attack speed, armor, toughness →** written as standard slot-scoped
  `AttributeModifiers` NBT on the stack — the vanilla 1.20.1 per-stack mechanism. Consequences:
  - `ItemStackAttributeMixin` and `AttributeManager`'s replacement logic are **deleted**.
  - Vanilla renders the green/blue attribute tooltip lines itself.
  - Other mods read damage via `stack.getAttributeModifiers(...)` — the API they already use —
    and can **add their own modifiers** without Forgero wiping them (fixes §2).
  - The `val - 4.0f` and armor-UUID magic happens once, at write time, in one place.
- **Durability, mining speed, mining level (no vanilla stack representation in 1.20.1) →**
  written as a flat cache tag: `forgero:stats: {durability: 561, mining_speed: 7.2, mining_level: 2}`.
  The durability and mining mixins shrink to a single `getNbt().getFloat(...)` — no parse, no
  resolve, no cache machinery. The tags are also a **documented, readable contract** for other
  mods and commands.

**3. Staleness handled with a generation stamp.**

Resolved values can drift from datapack changes. Stamp each resolved stack with the data-bundle
generation hash (`forgero:stats_gen`). Any code touching a Forgero stack (the converter is the
natural choke point) compares stamps and lazily re-resolves once on mismatch. This also covers
hand-written `/give` COF NBT: no stamp → resolve on first touch. Cost: one string compare on the
paths that today do a full NBT parse.

**4. Dynamic stat bonuses move to where dynamics already live.**

"+2 damage vs. undead"-style effects become conditional *on-hit properties* (the system in
`modules/mc/properties` that already has handlers, dynamic conditions, JSON codecs, and a README).
This is also how vanilla models them (Smite is an enchantment applied in the damage event, not an
attribute). The attribute JSON schema then needs only *static* conditions, and `DynamicCondition`
disappears from the attribute path entirely — matching how it is actually used today (§4).

**5. Simplify the attribute algebra to match the problem.**

Replace `{6 Operator classes + group ints + compositeKey + sealed triad + 2 baking strategies}`
with the model the entire MC ecosystem already understands:

```json
{ "type": "forgero:attack_damage", "operation": "add",            "value": 3.0 }
{ "type": "forgero:durability",    "operation": "multiply_base",  "value": 0.2 }
{ "type": "forgero:mining_speed",  "operation": "multiply_total", "value": 1.1 }
```

One record, three operations, fixed fold order (`(base + Σadd) × (1 + Σmultiply_base) × Π(1 + multiply_total)`),
conditions allowed. This expresses "head provides base, material multiplies it" — the use case
composite attributes were built for — without sub-formula identity, operator-count validation, or
silent group discarding. Survey the actual content JSON before committing: if some recipe really
needs an exotic formula (min/max clamps appear in the operator set), add an explicit `min`/`max`
field on the stat rather than a general operator algebra — clamps are the only non-fold operators
present.

### What gets deleted (the complexity ledger)

| Deleted | Replaced by |
|---|---|
| `ItemStackAttributeMixin`, `AttributeManager` merge/replace logic | vanilla `AttributeModifiers` NBT written at craft time |
| Per-call NBT parse + Component deep-equality cache in the stat path | `getFloat` on a flat NBT tag + generation stamp |
| `AttributeEngine`, `AttributeBakingStrategy` × 2, `AttributeQueryResult`, attribute participation in `DataTypeEngine`/`ResolverEngine` | `computeStats(Component)` pure function |
| `CompositeAttribute`, `CompositeAttributeComponent`, `AttributeComponent`, `compositeKey`, group ints, 6 `Operator` classes | one stat record, 3 operations (+ optional clamp) |
| `DynamicCondition` on attributes, `DynamicContext` in the stat path | conditional on-hit/event properties (existing module) |
| `ForgeroTooltipRenderer`'s stat lines (partially) | vanilla attribute tooltip rendering |
| Known bugs: `SimpleAttribute` condition-drop, dead `ignoreComponents`, composite silent-discard | cease to exist |

The bake/apply engine framework, the condition system (static side), the COF format, the data
pipeline, and the property/plugin system are all untouched — this removes attributes' *misuse* of
the generic machinery, not the machinery.

### What it does to the API surface

The two motivating questions from the original statement get ecosystem-native answers:

- **"How much damage does this tool provide?"** → `stack.getAttributeModifiers(MAINHAND)` —
  vanilla API, zero Forgero classes, and now *honest* (other mods' modifiers survive).
- **"Which upgrades does this item have?"** → still Forgero's question to answer; with stats out
  of the way, the Forgero-specific API shrinks to composition queries (parts, upgrades, slots)
  plus craft/upgrade events — a small, explainable surface instead of a resolution framework.
- The flat `forgero:stats` tag plus documented COF schema gives datapack/command/KubeJS users a
  read path with no Java at all.

### Forward compatibility

This design gets *stronger* on modern Minecraft: 1.20.5+ data components provide first-class
per-stack `minecraft:attribute_modifiers`, `minecraft:max_damage`, and `minecraft:tool` (per-block
mining rules). On a 1.21 port, the two remaining thin mixins (durability, mining speed) are
replaced by writing those components at the same mutation points — the runtime footprint of the
attribute system reaches zero. "Resolve at mutation, persist as components" is the direction
vanilla itself moved; building the parallel runtime layer now means rebuilding against the grain
of every future version.

### Risks and honest tradeoffs

- **NBT growth:** ~5 floats + a stamp per stack, marginal next to the COF tree already stored.
- **Stale display in creative/cheated items:** mitigated by the generation stamp + lazy
  re-resolve; worst case equals the current behavior.
- **Loss of "live" datapack tweaking:** today a datapack reload changes existing items' stats
  instantly (because everything recomputes constantly); under this design they update on first
  touch after reload. Acceptable, and the stamp makes it deterministic.
- **Truly dynamic stats** (e.g., damage scaling with durability) cannot be precomputed — but they
  also can't be expressed in today's vanilla-facing layer (empty context, §4). If ever needed,
  they belong in the event layer, same as conditional damage.
- **Migration:** existing forgero-2 test suites (PRs #1169/#1170) target the engine path; the
  pure `computeStats` function is strictly easier to test, but the suites need porting.

---

## 7. Recommended sequence

1. **Survey content JSON** for attribute usage: count operator/group/composite usage to confirm
   the 3-operation model covers real data (expect: it does; clamps via min/max fields).
2. **Extract `computeStats(Component)`** in modules/core beside the existing engine; port the
   property test suite to it; verify value-parity against `AttributeEngine` for all loaded
   components (a one-off parity test over the generated component registry).
3. **Write-at-mutation:** emit vanilla `AttributeModifiers` + `forgero:stats` + stamp in the
   converter and crafting/upgrade paths.
4. **Thin the mixins** to NBT reads; delete `ItemStackAttributeMixin`; delete `AttributeManager`.
5. **Delete the composite attribute machinery** and migrate any JSON that used it.
6. **Move dynamic stat cases** (if any exist in content) to conditional properties.
7. Only then design the small public composition API on top — per the companion report — now that
   "what are its stats" is no longer the API's job.

The point of the sequence: steps 1–2 are cheap and prove the thesis with a parity test before
anything is deleted. If parity holds with empty context — and §4 says it must — the dual layer is
formally redundant and the rest is demolition.

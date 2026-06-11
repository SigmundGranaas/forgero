# ADR-003: The Stat Contribution Kernel

**Status:** Proposed (design for the attribute composition/scope subsystem replacement)
**Context:** Follows ADR-002 (compiler in the factory) and the ripple refactor. The remaining
most complex subsystem is attribute composition: ~10 classes / ~1,100 LOC (`AttributeScope` +
`ScopeMatcher`/`ScopeMatchDecisionTable`/`ScopeMatchRule` ≈ 390 LOC of matching machinery, three
collectors with a dual intersection/additive algebra and double-count guards, two strategies
selected by `instanceof`, four scope handlers, a sorted `ComputationChain`, seven operator
classes). It is the one kernel the architecture work surrounded but never replaced.

---

## 1. Evidence: what the machinery serves vs. what content uses

Authored scope usage across all non-legacy content and module JSON:

| Scope | Uses | Reality |
|---|---|---|
| `forgero:scope/part-composite` | **674** | the actual feature: material values × shape multipliers compose inside a part |
| `forgero:scope/upgrade` | **17** | "applies when this component sits in an upgrade slot" |
| `forgero:scope/equipment-composite` | **0** | unused |
| `forgero:scope/local` | **0** | unused |
| `forgero:contexts/offensive·utility·defensive` | **52** | **stale forgero-1 Category strings** — match no handler, silently inert |

So: a four-scope ontology with a decision-table matching engine exists to serve **two** live
concepts, while 52 data entries reference vocabulary from the previous architecture without any
error. The complexity is not proportional to the functionality; and the silence around
`contexts/*` shows the current design cannot even tell us when data is wrong.

## 2. The design in one sentence

> **A stat is built from typed contributions folded bottom-up through the component tree: each
> node sums its additions, applies the multipliers that found their base, seals, and exports —
> structure composes, upgrades modify, and *where* a contribution applies is derived from where
> it sits, never from an authored scope label.**

## 3. The building blocks (exactly four)

### 3.1 `StatContribution` — the atom (authoring unit)

```java
record StatContribution(
    OpenIdentifier type,        // forgero:durability
    Operation operation,        // ADD | MULTIPLY      (closed)
    float value,
    boolean local,              // self-display only; never exported (replaces LOCAL scope)
    Optional<Condition> condition   // STATIC conditions (in_slot_type, is_root, tags, …)
) {}
```

- **Operations are a closed two-element algebra.** `ADD` and `MULTIPLY` cover all 674+17 live
  uses. Migration maps `subtraction → ADD(-v)`, `division → MULTIPLY(1/v)`. `min`/`max` (no
  observed data usage) become optional `clamp` fields on the contribution if content ever needs
  them — clamps are bounds, not fold steps, and must not participate in ordering.
- **No `scope`, no `group`, no `operator.order()`.** Sequence is math (adds before multiplies);
  placement is positional (§3.3). The `group` int is unused by data and is dropped.
- **Conditions stay** as the one gate for role-differentiated values ("+2 armor only
  `in_slot_type: chest`") and set-bonus-style counting — the existing static-condition registry
  is the open extension axis, unchanged.

### 3.2 The fold — one closed kernel function (~40 LOC)

Per node, bottom-up, with a fixed two-layer order derived from relationship kind:

```
Export seal(node):
    pool  = node.ownContributions(!local, static-conditions pass)
          ∪ child.export  for each STRUCTURE slot child          // compose layer
    for each stat type t:
        base[t] = Σ ADD(pool, t)
        val[t]  = base[t] × Π MULTIPLY(pool, t)   if base exists for t
        carry   = MULTIPLY contributions whose t has no base here   // rise, see 3.3

    upool = child.export  for each filled UPGRADE slot child,
            filtered by the slot (3.4)                            // modify layer
    val[t] = ( val[t] + Σ ADD(upool, t) ) × Π MULTIPLY(upool, t)

    node.compiledStats = val ∪ ownContributions(local)            // what the node shows
    return export = { ADD(t, val[t]) } ∪ carry                    // what flows upward
```

This **is** `ComponentCompiler`'s attribute pass; it replaces both baking strategies, all three
collectors, all four scope handlers, and the `ComputationChain` sort. The structure-then-upgrades
order is not configuration — it falls out of what structure and slots *are* (identity vs.
addition), per the earlier structure≠slots conclusion.

### 3.3 Sealing + the rise rule — placement derived, not authored

- **Sealing:** a node's structure folds to values before flowing up. Parents receive results,
  never internals — a parent-level multiplier can never retroactively scale one part's material,
  and diamond-as-head never leaks into diamond-as-gem.
- **Exports are a small contribution set, not a bare number** (the Case-C correction from the
  design gauntlet): the sealed `ADD` values plus any *risen* multipliers.
- **The rise rule replaces both composite scopes with one structural law:**

  > A `MULTIPLY` applies at the nearest ancestor fold where its stat type has a base; until
  > then it rises with the export. At the terminal, an unmet multiplier is inert (reported by
  > validation, §6).

  - `part-composite` (674 uses): shape's `×0.7 durability` meets the material's base **at the
    part node** — consumed there. Identical behavior, zero labels.
  - `equipment-composite` (0 uses, concept preserved): a handle's `× weight` with no weight base
    at the handle rises and applies at the tool fold, where the head's base lives. The concept
    survives as an emergent property of the rule rather than a scope string.
  - The **intersection-vs-additive dual algebra disappears**: "compose only types present in ≥2
    sources, else fall back to summing, else discard scoped attributes" was an approximation of
    exactly this — adds always sum; multipliers act iff a base exists. One algebra, no fallback,
    no `composedTypes` double-count guard (recursion means each contribution has exactly one
    path).

### 3.4 The slot — the control point (receiving side)

Control lives where composition happens, not in coordinated labels:

- **Layer:** which fold layer a child feeds is the slot's *kind* — structure slot ⇒ compose,
  upgrade slot ⇒ modify. Never declared per contribution. (`scope/upgrade`, 17 uses, becomes
  derivable: those contributions apply when the component sits in an upgrade slot — which is
  simply *the only way they reach a modify layer*. Where authors used it to mean "only as an
  upgrade, not as a part", that intent maps to an `in_slot_type` condition during migration.)
- **Filtering:** a slot may restrict what it accepts from its occupant — the existing
  `Slot.filterProperties` hook stays as the single occupant-filtering mechanism (this is what
  `slot.scope()` was reaching for).
- **Extension:** new slot types and new conditions are the open vocabulary; the fold itself is
  closed.

## 4. Worked example — the dagger blade, end to end

Authored (migrated form; today's `scope` lines simply deleted):

```json5
// material (iron), in the blade's material slot:        { "type":"forgero:durability", "op":"add", "value":250 }
// dagger_blade template (own contributions):            { "type":"forgero:durability", "op":"multiply", "value":0.7 },
//                                                       { "type":"forgero:attack_damage", "op":"add", "value":1 }
// reinforcement (upgrade slot on the blade):            { "type":"forgero:durability", "op":"add", "value":50 }
// sneak bonus (dynamic condition — untouched by this ADR; carried as data, applied at the event)
```

Fold at the blade node: compose `(250) × 0.7 = 175` → seal → modify `175 + 50 = 225` → export
`ADD(durability,225), ADD(attack_damage, 1+material…)`. The sword node receives sealed part
exports as plain adds and runs the same fold with its own upgrades. No scopes, no strategies, no
collectors — the same ~40 lines at every node.

## 5. Deletion ledger

| Deleted (≈1,100 LOC, ~10 concepts) | Replaced by |
|---|---|
| `AttributeScope` (4 scopes + ~15 predicates) | position in the tree + `local` flag |
| `ScopeMatcher` (6 variants), `ScopeMatchDecisionTable` (209 LOC), `ScopeMatchRule` | nothing — no label matching exists |
| `DefaultAttributeCollector`, `PartCompositeAttributeCollector`, `UpgradeAttributeCollector` (238 LOC, dual algebra + fallback + double-count guard) | the fold + recursion |
| `AttributeBakingStrategy` ×2 + `instanceof StructuredComponent…` selection | the fold (asks `structure()`/`upgrades()`, branches on nothing) |
| 4 `AttributeScopeHandler`s | compose/modify layers of the fold |
| `ComputationChain` (group/order sort) + 7 `Operator` classes | `Operation` enum {ADD, MULTIPLY}, fixed math order |
| `Attribute` sealed-ish surface (`scope()`, `group()`, `operator()`) | `StatContribution` |

Kept, unchanged: static conditions + `ResolutionContext`, `Slot.filterProperties`,
`BakedAttributes`/`PrecomputedAttribute` as the compiled output shape (conditional/dynamic
attributes continue to ride as data for `DynamicAttributes` — the dagger sneak bonus path is
orthogonal to this ADR), `CompilerPass`/`ComponentCompiler`, the dispatcher, the MC layer.

## 6. Migration and the parity gate

1. **Codec shim, content untouched first:** the attribute codec maps existing JSON onto
   `StatContribution` — `computation.{value,operation}` → op+value; `scope/part-composite` and
   `scope/equipment-composite` → *dropped* (the rise rule reproduces placement);
   `scope/upgrade` → dropped or `in_slot_type` condition where the restrictive intent is real;
   `scope/local` → `local:true`; `contexts/*` → **load-time warning** (stale forgero-1
   vocabulary), treated as no scope.
2. **Parity test, registry-wide:** new fold vs. current `AttributeEngine.compile` for every
   component the data pipeline generates, every stat type — the same harness pattern that
   already caught two design errors this session. Expected legitimate diffs are enumerable in
   advance: (a) lone multipliers — currently silently *discarded*, now inert-but-reported;
   (b) `contexts/*` entries — currently silently inert, now warned. The stance is
   **semantics-first**: where parity flags a divergence, we fix the *content* (it is ours) or
   consciously accept the new semantics — never re-grow bug-for-bug machinery.
3. **Validation instead of silence:** unmet multipliers at the terminal and unknown scope strings
   become loader diagnostics. The current system's worst property — silently discarding authored
   stats — becomes impossible to hit quietly.
4. Only after the gate: delete the old kernel (the ledger above) in one commit.

## 7. Phase 2 — the component lattice (coupled, staged separately)

The `instanceof`-strategy selection dies with the kernel, which removes the last *behavioral*
dependence on the concrete type lattice. That unlocks collapsing the seven near-identical
records (`StaticEquipment`, `StructuredEquipment`, `StructuredExtensibleEquipment`,
`ExtensibleEquipment`, `StructuredPart`, …) into **one** `ComponentNode(id, tags, properties,
structure, upgrades, compiled)` with empty-default facets — the capability interfaces
(`StructuredComponent`, `CustomizableComponent`, `EquipmentComponent`) remaining as views over
facet presence, and terminal-ness as a role at the MC boundary rather than a subtype. The fold
is written against `structure()`/`upgrades()` accessors, so it is lattice-agnostic by
construction: phase 2 can land after, independently, without touching the kernel.

## 8. What this buys, in the maintainer's terms

- **Holdable:** the entire stat system is one record, one enum, one ~40-line recursive fold, and
  the conditions you already know. It fits on one page; the answer to "why is this value X" is a
  walk of the tree, not a trace through collectors, handlers, strategies, and a decision table.
- **Composable:** new stat types, conditions, and slot types are free; nodes compose by
  recursion; sealing makes contributions local-by-construction.
- **Controlled:** the slot is the single control point (what it accepts, which layer it feeds);
  validation reports what the old system swallowed.
- **Honest:** placement is derived from structure that must exist anyway — the last hand-authored
  coordination vocabulary (scopes) goes the way of `compositeKey`, `group`, and `DynamicContext`
  before it, and for the same reason: anything authored in parallel to the tree eventually lies
  about it.

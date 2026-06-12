# Gametest Coverage Analysis (and a regression it surfaced)

## The corpus vs. what was validated

Forgero has **978 `@GameTest` methods across 14 suites**, each run by its own module/mod
`runGameTest` task:

| Suite | Registered classes | Notes |
|---|---|---|
| `mods/forgero` | 25 (~270 methods) | integration: assembly, upgrades, attributes, recipes, bows, effects |
| `modules/mc/properties` | 19 (~242 methods) | conditions, effects, selectors, filters, on-hit/on-tick/block/use properties |
| `modules/mc/predicate` | 11 | predicates |
| `modules/mc/bows` | 9 | bows, arrows |
| `mods/vanilla-upgrades` | 2 (~65) | vanilla-tool upgrades gameplay |
| `mods/repair-kit` | 4 | repair kit |
| `mods/drp` | 3 | data/resource packs |
| `modules/mc/{tools,blocks,armor,loader,test-common}` | 1–3 each | per-module behaviour |

**During the whole forgero-2 rework session, validation ran only the `mods/forgero` suite (270).**
The other ~700 gametests — including the `modules/mc/properties` suite that covers the exact
machinery the rework touched most (properties, conditions, effects, attributes) — were never run.

## Regression found

`modules/mc/properties:runGameTest`:

- **pre-session baseline (`8da586ed5`): 242/242 pass.**
- **current branch: 23 failures.**

The failures are all property-driven behaviours not firing: on-hit effects (fire/poison/slowness/
lifesteal/lightning/freeze/disarm/convert/explosion/AoE), on-tick auras, 3×3 block breaking, instant
block-hardness, loot, teleport. No load-time errors — the properties are simply never **applied**.

### Root cause

The session's commit `6cae2faed` ("Compile all properties at construction; **terminals serve them
by key**") inverted property serving: a terminal (`EquipmentComponent`) pre-compiles its properties
and `ItemPropertyApi` reads them by key. Its message states *"the engine.resolve path remains only as
a fallback for non-terminal components"* — but in the final state that fallback is **missing**:

```java
// ItemPropertyApiImpl.get
return converter.toComponent(stack)
        .filter(component -> component instanceof EquipmentComponent)   // terminals only
        .map(component -> ((EquipmentComponent) component).properties(key))
        .orElse(Collections.emptyList());                              // non-terminals → nothing
```

So **non-terminal components serve no properties**. Real tools are terminals (built by the factory),
so real gameplay and the `mods/forgero` suite are unaffected — which is why this went unnoticed. But
the properties gametests build synthetic tools via `ComponentTester` (a `StaticComponent`, i.e.
`ContributingComponent`, not `EquipmentComponent`), and so do legitimate runtime cases (a *part*
shown in a tooltip/JEI is a `ContributingComponent`). They all silently lose their properties.

This is a **real product regression**, not just a test artifact: any non-terminal component
(parts, synthetic items) stopped serving on-hit/on-tick/block-breaking/loot/etc. properties.

## Fix direction

Restore the on-demand fallback the inversion promised: when `toComponent(stack)` yields a
non-terminal, compile/resolve its property list for `key` on demand (run the registered
`CompilerPass` for that key over the component tree) instead of returning empty. This mirrors what
`AttributeEngine.resolveAttributes` already does for non-terminal *attributes*
(`StatFold.fold(component)`), so attributes survived but properties did not.

## Process gap to close

1. The remaining suites (`bows`, `tools`, `armor`, `blocks`, `predicate`, `vanilla-upgrades`,
   `repair-kit`, `drp`) are **also unvalidated this session** and may hide further regressions for
   the same reason (non-terminal property serving, slot/identifier changes, condition changes).
2. CI / the validation loop should run **every** module's `runGameTest`, not just `mods/forgero`.
   A single aggregating task (or the loop running each `:module:runGameTest`) would have caught this
   immediately.

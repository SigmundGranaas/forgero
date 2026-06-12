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

## Full-suite sweep (this branch vs. pre-session baseline)

| Suite | This branch | Verdict |
|---|---|---|
| `mods/forgero` | 270 ✅ | green |
| `modules/mc/properties` | **23 fail** / 242 | **REGRESSION #1** (baseline 242/242) |
| `modules/mc/predicate` | 39 ✅ | green |
| `modules/mc/bows` | 93 ✅ | green |
| `modules/mc/loader` | 12 ✅ | green |
| `modules/mc/test-common` | 25 ✅ | green |
| `mods/repair-kit` | 42 ✅ | green |
| `mods/drp` | 24 ✅ | green |
| `mods/recipe-generator` | 11 ✅ | green |
| `modules/mc/tools` | **cannot boot** | **REGRESSION #2** (booted at baseline) |
| `modules/mc/armor` | **cannot boot** | **REGRESSION #2** |
| `mods/vanilla-upgrades` | **cannot boot** | **REGRESSION #2** |
| `modules/mc/blocks` | 1 fail / 11 | pre-existing (fails at baseline too) |

### Regression #1 — non-terminal property serving (described above)

`modules/mc/properties`: 23 failures, all non-terminal components serving no properties.

### Regression #2 — properties→predicate runtime coupling breaks gametest boot

`tools` / `armor` / `vanilla-upgrades` fail to boot:
`ClassNotFoundException: com.sigmundgranaas.forgero.predicate.minecraft.DynamicContextFactory`.
The session's dynamic-attrs commit (`76c2b8be4`) added
`properties/.../mixin/LivingEntityDynamicAttributeMixin`, whose injected method calls predicate's
`DynamicContextFactory.fromEntities(...)`. Any gametest runtime that loads `properties` but not
`predicate` now fails to apply that mixin and the server never starts. `mods/forgero` is fine
(it has predicate); these three module/mod suites don't. At baseline the mixin didn't exist, so they
booted. (`tools` also had pre-existing *test* failures separate from this boot break.)

### Not regressions

- `modules/mc/blocks` `assemblystationgametest.damagedforgeroitem_cannotbedisassembled` — fails at
  baseline too (pre-existing).

## Resolution

Both regressions fixed; full sweep re-run:

- **#1 (non-terminal property serving)** — restored the on-demand fallback in `ItemPropertyApiImpl`
  (run the registered `CompilerPass` for the key when the component isn't a terminal). `properties`
  suite 23 → **242/242**.
- **#2 (properties→predicate runtime coupling)** — moved `DynamicContextFactory` and
  `MinecraftContextKeys` from `predicate.minecraft` to `common.runtime` (they only depend on
  `common.runtime` + Minecraft; `common.runtime` already owns `DynamicContext`/`Key`/`ContextKeys`).
  The properties mixin no longer needs predicate at runtime, so the affected suites boot again:
  `armor` 7/7, `vanilla-upgrades` 65/65, `tools` boots.

After the fixes: `mods/forgero` 270/270, `properties` 242/242, `predicate` 39/39, `bows` 93/93,
`loader` 12/12, `test-common` 25/25, `repair-kit` 42/42, `drp` 24/24, `recipe-generator` 11/11,
`armor` 7/7, `vanilla-upgrades` 65/65.

Remaining (pre-existing, **not** session regressions — present at baseline `8da586ed5`):
- `tools` `ToolBehaviorIntegrationGametest`: `OpenIdentifier.of("forgero:diamond-sword")` — the test
  uses `of()` (rejects `:`) where it should use `parse()`.
- `blocks` `assemblystationgametest.damagedforgeroitem_cannotbedisassembled`.
- `properties` `testOnHitMultipleEffectsIntegration` is **flaky** (a freeze-tick + multi-effect
  timing race); passes on re-run.

## Process gap to close

1. The remaining suites (`bows`, `tools`, `armor`, `blocks`, `predicate`, `vanilla-upgrades`,
   `repair-kit`, `drp`) are **also unvalidated this session** and may hide further regressions for
   the same reason (non-terminal property serving, slot/identifier changes, condition changes).
2. CI / the validation loop should run **every** module's `runGameTest`, not just `mods/forgero`.
   A single aggregating task (or the loop running each `:module:runGameTest`) would have caught this
   immediately.

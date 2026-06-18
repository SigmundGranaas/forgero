# Vanilla Upgrades — Completion Plan

Status: proposal / roadmap. Goal: take `mods/vanilla-upgrades` from "adds slots to vanilla items"
to a shippable, self-contained mod a player can actually use end-to-end.

## What it is today

A deliberately data-driven mod: one empty `ModInitializer` (`VanillaUpgradesMod`) plus the
`content/vanilla-upgrades-base` pack of **54 static parts** that attach Forgero upgrade slots to all
vanilla tools (5 types × 6 tiers) and armor (6 tiers × 4 pieces). It bundles core/loader/common/
properties/render/predicate/tools + `forgero-base` + `forgero-materials` + `drp` + `recipe-generator`.
It has **65 GameTests** (attributes, slot counts, mining levels) — all green.

## The core problem

**You can add slots, but you cannot fill them.** Two hard blockers make the mod non-functional for
a survival player:

### Blocker A — No upgrade-install mechanism is shipped
The only runtime way to install/remove an upgrade is the **Upgrade Station block**
(`modules/mc/blocks` → `UpgradeStationScreenHandler.installUpgrade(...)`). `mods/forgero` bundles
`modules:mc:blocks`; **`mods/vanilla-upgrades` does not.** There is **no** smithing-table or anvil
upgrade handler anywhere in the repo (no `SmithingScreenHandler`/`AnvilScreenHandler` mixin;
`recipe-generator` has empty entrypoints). So the README's "Use a Smithing Table to combine your
tool with an upgrade material" is **not implemented**. Net result: slots exist but nothing installs
into them.

### Blocker B — Slots have no (or too few) fillers, and tags have drifted
Slot supply (from the only bundled material pack, `forgero-materials`) vs demand
(vanilla-upgrades slots):

| Slot type | Demanded by vanilla-upgrades | Supplied by bundled packs | Verdict |
|---|---|---|---|
| `upgrades/types/gem` | 22 | **0** (gems live in unbundled `forgero-gems`, 35 files) | **Unfillable** |
| `materials/types/binding` | 31 | 3 (+ 1 under the *new* `upgrades/types/binding` tag) | Under-supplied + **tag drift** |
| `armor_lining_material` | 24 | 1 | **Severely under-supplied** |
| `upgrades/types/reinforcement` | 18 | 36 | OK |
| `upgrades/types/tip_reinforcement` | 4 | 14 | OK |

Tag drift: `forgero-materials` has migrated bindings toward the canonical `upgrades/types/binding`
(the recent "Normalize drifted upgrade slot tags" commits), but `vanilla-upgrades-base` parts still
demand the old `materials/types/binding`. The new **slot-fillability validator is now a build error**,
so every shipped slot must resolve to ≥1 filler.

## Secondary gaps

- **Item coverage:** no bow, crossbow, trident, shield, elytra, fishing rod, shears,
  flint & steel, carrot-on-a-stick, brush, spyglass. (A `modules/mc/bows` module already exists.)
- **No disassembly/repair story shipped:** removal is part of the Upgrade Station; `repair-kit`
  already integrates optionally but isn't bundled.
- **Docs drift:** README slot table is stale (says diamond/netherite = 2 slots; content has more,
  incl. sword guards), and promises smithing-table install that doesn't exist. The referenced
  `docs/status/vanilla-upgrades.md` does not exist; `docs/README.md` cites a stale ~67%.
- **No install-flow tests:** the 65 GameTests validate attributes/slots but never install an
  upgrade (because there's no mechanism to test).

---

## Phase 1 — Make upgrades installable (unblock the mod) [CRITICAL]

Decision: ship the existing **Upgrade Station** (reuse, fast) rather than build a new smithing
handler. It already does install + remove + swap with a real screen.

- Bundle `:modules:mc:blocks` into `mods/vanilla-upgrades/build.gradle` (`include` + `implementation`),
  matching how `mods/forgero` wires it.
- Ensure the station's block + block-item registration, screen handler, model/texture, and crafting
  recipe ship and work standalone (the blocks module + its content). Verify client screen opens.
- Update the README install section to the Upgrade Station (or, if a smithing-table flow is
  preferred as a product choice, that becomes its own feature phase — larger, new mixin + dynamic
  result handling).

**Tests:** GameTest that places/open the station, installs a binding/reinforcement into a vanilla
sword via the screen handler, and asserts the resulting stack gained the upgrade
(`ItemQueryApi.getFilledSlotCount` / attribute delta); a remove test.

## Phase 2 — Make every slot fillable + fix tag drift [CRITICAL]

- Reconcile vanilla-upgrades slot tags with the canonical `upgrades/types/*` scheme (finish the
  migration for `materials/types/binding` → the binding tag `forgero-materials` actually supplies).
- For each slot type, guarantee ≥1 shipped filler:
  - **gems:** bundle `:content:forgero-gems` (and its loot) **or** remove gem slots from vanilla
    parts. Recommended: bundle gems — they're the most interesting upgrades.
  - **linings:** add/borrow a set of `armor_lining_material` materials (leather/wool/phantom
    membrane, per the README's own promise) — currently only 1 exists.
  - **bindings:** ensure several `binding` materials ship (leather/string/vines as README states).
- Add a **slot-fillability GameTest** that, for every vanilla part, asserts each slot has at least
  one installable shipped material (turns the build-time validator into a runtime guarantee).

**Tests:** parametrised fillability test across all 54 parts; install-one-of-each-slot-type test.

## Phase 3 — Reconcile content & docs, then settle the slot model [HIGH]

- Define and document the **canonical slot-per-tier matrix** (the source of recent churn). Decide:
  do wood/stone get 1 slot, netherite up to 5, swords a guard slot, etc.? Encode it consistently
  across all 54 parts and write it once in the README + a new `docs/status/vanilla-upgrades.md`.
- Fix the README (install mechanism, slot table) and the stale completion figure.

**Tests:** a content-consistency test (every part of a tier has the documented slot set).

## Phase 4 — Expand item coverage [MEDIUM]

Add static parts for the uncovered vanilla equipment, each with appropriate slots:
- **Shield** (binding + reinforcement) — high value, simple.
- **Bow / Crossbow** — reuse `modules/mc/bows` patterns (bows module already exists).
- **Trident**, **Elytra** (lining), **Fishing rod**, **Shears**, **Flint & steel**,
  **Carrot on a stick**, **Brush**, **Spyglass** — slots where they make sense.

**Tests:** extend the gameplay/slot GameTests to the new items (recognition, slot counts,
attribute parity with vanilla).

## Phase 5 — Disassembly, repair & polish [MEDIUM]

- Confirm upgrade **removal** via the station works for vanilla items; add a removal GameTest.
- Bundle/verify **repair-kit** integration (it already checks `isModLoaded("vanilla-upgrades")`),
  or document the repair path.
- Client polish: ensure tooltips (render module) show slots/installed upgrades on vanilla items;
  add `en_us.json` for any vanilla-upgrades-specific strings; verify the station has lang/model.

**Tests:** removal round-trip; tooltip smoke test; `runClient` manual check.

---

## Sequencing & rationale

```
Phase 1 (install mechanism)  ──┐  both are release-blockers; do first, in parallel
Phase 2 (fillable slots)     ──┘
        │
        ▼
Phase 3 (slot model + docs)  ── stabilise the churny part once install+fillers are real
        │
        ▼
Phase 4 (item coverage)  ──  Phase 5 (disassembly/repair/polish)   (independent, parallelisable)
```

Phases 1–2 convert the mod from "non-functional in survival" to "playable". Phase 3 stops the
recurring slot-tag churn by making the model explicit. Phases 4–5 are breadth/polish.

## Effort (relative)

| Phase | Effort | Risk |
|---|---|---|
| 1 Install mechanism (bundle blocks) | M | med — verify standalone block/screen/recipe + assets |
| 2 Fillable slots + tag migration | M | med — interacts with the slot-fillability validator |
| 3 Slot model + docs | S–M | low |
| 4 Item coverage | M–L | low–med (bows/trident need care) |
| 5 Disassembly/repair/polish | M | low |

## Open decisions to confirm

1. **Install UX:** ship the existing **Upgrade Station** (recommended, reuses working code) or build
   the README's **Smithing Table** flow (new mixin + dynamic-result handling, larger)?
2. **Gem slots:** bundle `forgero-gems` (richer upgrades, bigger jar) or drop gem slots from vanilla
   items (simpler, smaller scope)?
3. **Scope of new items:** which of shield/bow/crossbow/trident/elytra/etc. are in scope for v1?
4. **Slot-per-tier matrix:** confirm the canonical progression so Phase 3 can lock it down.

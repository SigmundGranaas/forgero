# Vanilla Upgrades — Status

The `vanilla-upgrades` mod adds Forgero upgrade slots to vanilla Minecraft equipment without
changing vanilla stats or recipes. It is a thin, data-driven mod: an empty `ModInitializer` plus
the `content/vanilla-upgrades-base` part pack, bundling the Forgero loader/core modules, the
station-blocks module, and material/gem content.

## What works

- **Coverage:** all vanilla tools (pickaxe/axe/shovel/hoe/sword × 6 tiers) and armor (6 tiers ×
  4 pieces + turtle shell), plus shield, trident, elytra, fishing rod, shears, flint & steel,
  brush, carrot/warped-fungus on a stick, and ranged weapons (bow, crossbow via the
  `vanilla-upgrades-ranged` pack). Every durable vanilla item is covered.
- **Installing upgrades:** the **Upgrade Station** block (from `modules/mc/blocks`, now bundled)
  installs and removes upgrades on vanilla items.
- **Fillable slots:** binding, gem, reinforcement, tip-reinforcement, and lining slots all have
  shipped fillers (`forgero-materials` + bundled `forgero-gems`); the build-time
  slot-fillability validator passes.
- **Tests:** `runGameTest` green — vanilla attribute parity, slot queries, the install/remove
  flow, and new-item coverage.

## Slot model

- **Binding** — every tool and weapon has a binding slot (the handle). Armor has none; it uses a
  **lining** slot instead.
- **Tip reinforcement** — every head/blade tool (pickaxe, axe, shovel, hoe, sword, all tiers) has a
  tip slot. Non-head tools (fishing rod, shears, shield, etc.) and armor do not.
- **Gem / reinforcement** — present on mid/high tiers, scaling with tier.

## Upgrade effects on attributes (important)

Installing an upgrade changes an item's **attributes only when the material's attribute uses an
upgrade-applicable scope** (no scope = propagates normally, or conditioned on the slot's
`in_slot_type`). Materials whose attributes use `forgero:scope/part-composite` (which exists for
shape+material *part construction*, using intersection logic) are **stat-neutral when slotted as an
upgrade** — they fill the slot and can still carry event properties, but they do not change stats.

Verified on vanilla items (`UpgradeAttributeChangeTest`): `forgero:ender_pearl` (durability +105,
no scope) raises a vanilla pickaxe/sword/trident's resolved durability by 105 through the gem slot.
So the resolution path works on vanilla static parts — the determinant is the material's scope.

Current stat-filler status per slot type (with the bundled packs):

| Slot | Stat-applying filler available? |
|------|-------------------------------|
| gem | Yes — `ender_pearl` (and other secondary-materials gems) |
| reinforcement | No — bundled reinforcement materials (e.g. calcite) are part-composite, stat-neutral as upgrades |
| binding | No — bundled binding materials (e.g. leather) are part-composite/binding-conditioned, stat-neutral here |
| lining (armor) | No — only `leather` has the lining role and it adds no stats in a lining slot |

Events fire regardless of scope (verified: `on_hit` via blaze rod, `on_crit` via example aspect).

`forgero-secondary-materials` is now bundled to provide at least one working stat-upgrade path
(gem slot). Giving reinforcement/binding/lining slots real stat upgrades needs materials authored
with upgrade-applicable scopes for those slots — a content decision (see follow-ups).

## Known follow-ups

- **Slot-per-tier matrix:** gem/reinforcement counts per tier still vary and are not locked to a
  single documented model (the README table is indicative). Binding and tip coverage are now
  uniform; gem/reinforcement scaling is worth pinning down with a content-consistency test.
- **Binding tag migration:** vanilla parts still use `materials/types/binding` while
  `forgero-materials` is migrating toward `upgrades/types/binding`. Fillable today (≥1 material),
  but should be reconciled when the upstream migration settles.
- **Lining variety:** only a small number of `armor_lining_material` materials ship; more variety
  would improve armor builds.
- **Smithing-table install:** the original README promised a smithing-table flow; the shipped
  mechanism is the Upgrade Station. A smithing path could be added later as a separate feature.
- **Ranged weapons (bow / crossbow):** covered via the dedicated `content/vanilla-upgrades-ranged`
  pack, which is bundled only by the vanilla-upgrades mod (never by the main Forgero mod, whose bows
  module owns `minecraft:bow`).

## Layout

- `mods/vanilla-upgrades/` — entrypoint + build (bundles loader, core, common, properties, render,
  predicate, tools, **blocks**, drp, recipe-generator, and the content packs).
- `content/vanilla-upgrades-base/` — `static_part` definitions (`data/forgero/parts/**`).

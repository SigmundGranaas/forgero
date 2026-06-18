# Vanilla Upgrades — Status

The `vanilla-upgrades` mod adds Forgero upgrade slots to vanilla Minecraft equipment without
changing vanilla stats or recipes. It is a thin, data-driven mod: an empty `ModInitializer` plus
the `content/vanilla-upgrades-base` part pack, bundling the Forgero loader/core modules, the
station-blocks module, and material/gem content.

## What works

- **Coverage:** all vanilla tools (pickaxe/axe/shovel/hoe/sword × 6 tiers) and armor (6 tiers ×
  4 pieces), plus shield, trident, elytra, fishing rod, shears, flint & steel, and ranged weapons
  (bow, crossbow via the `vanilla-upgrades-ranged` pack).
- **Installing upgrades:** the **Upgrade Station** block (from `modules/mc/blocks`, now bundled)
  installs and removes upgrades on vanilla items.
- **Fillable slots:** binding, gem, reinforcement, tip-reinforcement, and lining slots all have
  shipped fillers (`forgero-materials` + bundled `forgero-gems`); the build-time
  slot-fillability validator passes.
- **Tests:** `runGameTest` green — vanilla attribute parity, slot queries, the install/remove
  flow, and new-item coverage.

## Known follow-ups

- **Slot-per-tier matrix:** slot counts per tier have drifted over time and are not yet locked to
  a single documented model (the README table is indicative, not exact). Worth pinning down with a
  content-consistency test.
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

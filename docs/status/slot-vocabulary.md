# Slot vocabulary: convention, what was normalized, what remains

A slot definition (in a part template's `upgrades[]` or an equipment `slots[]`) carries two
identifiers that do different jobs — keeping them straight is the key to a consistent vocabulary:

| field | role | matched by |
|---|---|---|
| `type` | **validator** — what content the slot accepts (`requireTag(type)` unless `valid_tags` is set) | install-time validation |
| `tags` | **identity** — what the slot *is* | `in_slot_type` conditions (its `slot_type`) |

`in_slot_type` matches a slot when its `slot_type` equals the slot's `type` **or** any of its `tags`,
resolved through the tag graph. So the **identity** layer (`tags`) is what content gates on, and it is
the layer that must be consistent.

## Canonical convention
- **Upgrade-slot identity:** `forgero:upgrades/types/<x>` (e.g. `upgrades/types/gem`,
  `upgrades/types/reinforcement`, `upgrades/types/cosmetic`, `upgrades/types/grip`,
  `upgrades/types/binding`). This is the form `in_slot_type` conditions should reference, and the form
  every upgrade slot should carry as an identity tag.
- **Material-role identity:** `forgero:materials/roles/<x>` (`upgrade_material`, `armor_material`) —
  the generic validator types, left as-is.
- **Context identity:** `forgero:contexts/<x>` (`offensive`/`defensive`/`utility`) — slot context.

The build-time `SlotReferenceValidator` already fails the build if an `in_slot_type` references a
`slot_type` no slot provides, so a *dangling* reference can never silently rot. This doc covers the
remaining *consistency* (not correctness) work.

## Normalized (safe, done)
Inert, non-canonical identity tags on the `minecraft-tools` equipment binding/grip/pommel/tip slots
were referenced by **zero** conditions, so replacing them was purely additive in effect (the slots
keep their `type`/validator, so existing matches are untouched, and they gain a canonical identity):

- `forgero:parts/binding_type` → `forgero:upgrades/types/binding`
- `forgero:parts/grip_type` → `forgero:upgrades/types/grip`
- `forgero:parts/pommel_type` → `forgero:upgrades/types/pommel`
- `forgero:parts/tip_type` → `forgero:upgrades/types/tip`

`validateContentPacks` (incl. slot-reference checking) and the full gametest corpus stay green.

## Remaining — needs design intent, deliberately NOT auto-normalized
These are entangled with the **validator** (`type`) field, so changing them changes *what installs
where* — a semantics decision, not a rename. Left for a maintainer pass with install tests:

1. **The binding slot is typed two ways.** Some equipment files type it `forgero:binding_slot`,
   others `forgero:binding`; the part-template binding slot uses `forgero:parts/binding`. So a
   material gating on `in_slot_type: forgero:binding_slot` (e.g. `feather`) matches *some* binding
   slots but not others. Decide one canonical binding slot type + identity and align the conditions.
   (Same shape for grip: `binding_slot`-style `handle_grip_slot` vs type `forgero:grip` vs
   `materials/types/soft`.)
2. **Is binding/grip/pommel/tip an "upgrade" or a "structural part"?** They currently mix
   `upgrades/types/*`, `parts/*`, and bare names. The right canonical prefix depends on whether these
   are removable upgrades or fixed structural parts — a taxonomy decision.
3. **`materials/types/gem` (2) vs `upgrades/types/gem` (1) vs the generic
   `materials/roles/upgrade_material` + `upgrades/types/gem` tag (26)** — three ways a gem slot is
   expressed. Consolidate once (1) and (2) are decided.

These were investigated fully (every slot type, its identity tags, and which conditions reference it
are mapped) — the blocker is a design decision, not missing analysis. A blind rename here would
silently change install validation, which is exactly the class of bug the slot-reference validator
exists to prevent.

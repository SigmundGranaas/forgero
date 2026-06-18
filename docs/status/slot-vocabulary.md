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

## Binding/grip identity unified (done) — the decouple-identity-from-validator pattern
The binding slot was spelled three ways — `forgero:binding_slot` (only `diamond-pickaxe`),
`forgero:binding` + tag `upgrades/types/binding` (iron/golden/netherite), and `forgero:parts/binding`
(all 5 base tools + 2 extended weapons) — while **22** `in_slot_type` conditions (feather, leather, and
the rest of the secondary materials) gated on `binding_slot`, a classifier present on **one** of the
~8 binding slots. The build stayed green because `SlotReferenceValidator` checks *global existence* of
a classifier, not whether the slots a material actually occupies carry it — so the bonus was a silent
near-no-op (same class as the dead gem conditional fixed earlier).

Resolved by **decoupling identity from validation** rather than picking a winner for the overloaded
`type` field (which is *also* the `requireTag(type)` install validator):

- Added the canonical identity tag `forgero:upgrades/types/binding` to the `tags` of **every** binding
  slot (base tools, extended weapons, diamond-pickaxe) — purely additive, each slot's `type`/validator
  untouched, so install rules don't move. The sword "guard" slot (`type: parts/binding`, holds a
  binding) got it too, so the condition fires on swords as well.
- Created the graph node `tags/upgrades/types/binding.json` (mirroring `grip.json`/`gem.json`).
- Migrated all 22 conditions `slot_type: binding_slot` → `upgrades/types/binding`, and the 1
  `handle_grip_slot` → `upgrades/types/grip` (grip slots already carried that identity). Only the
  condition field `slot_type` was rewritten; slot-def `type` validators were never touched.

Net effect: an "am I in a binding?" condition now matches uniformly across all binding slots instead of
just `diamond-pickaxe` — strictly *widening* matches, never regressing one. `validateContentPacks` +
full corpus green.

> Pattern to reuse: when a `type` field is doing double duty as install-validator and match-identity,
> put the canonical identity in `tags` and gate conditions on that, leaving `type` as the validator.

## Remaining — needs design intent, deliberately NOT auto-normalized
These are entangled with the **validator** (`type`) field, so changing them changes *what installs
where* — a semantics decision, not a rename. Left for a maintainer pass with install tests:

1. **The binding slot is still *validated* three ways.** Identity is now unified (above), but the
   `type`/validator still differs: `parts/binding` (base/extended), `binding` (iron/golden/netherite),
   `binding_slot` (diamond-pickaxe). These now-orphan validator names (no condition references them)
   still decide what installs. Consolidating them is an install-semantics decision: pick one canonical
   binding validator and confirm with install tests that the same parts remain installable.
2. **Is binding/grip/pommel/tip an "upgrade" or a "structural part"?** They currently mix
   `upgrades/types/*`, `parts/*`, and bare names. The right canonical prefix depends on whether these
   are removable upgrades or fixed structural parts — a taxonomy decision.
3. **`materials/types/gem` (2) vs `upgrades/types/gem` (1) vs the generic
   `materials/roles/upgrade_material` + `upgrades/types/gem` tag (26)** — three ways a gem slot is
   expressed. Consolidate once (1) and (2) are decided.
4. **Harden `SlotReferenceValidator`** from "classifier exists *somewhere*" to "every slot a material
   role can occupy carries the identity it's gated on" — that check would have caught the binding
   drift at build time, and is what stops the next one.

These were investigated fully (every slot type, its identity tags, and which conditions reference it
are mapped) — the blocker is a design decision, not missing analysis. A blind rename of a `type`
field would silently change install validation, which is exactly the class of bug the slot-reference
validator exists to prevent.

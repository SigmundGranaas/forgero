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

## SlotFillabilityValidator (added) — found 9 unfillable slot types, now migrated

A build-time guard (`modules/validation/.../SlotFillabilityValidator`) cross-checks every upgrade
slot's **validator** tag against the tags real content carries — the install-side complement to
`SlotReferenceValidator` (which guards `in_slot_type` *conditions*). It mirrors runtime exactly: slot
validation is `SlotValidator.test` → `component.getTags().contains(requiredTag)`, a **literal**
membership test with no tag-graph walk (`SlotFactoryRegistry` builds `requireTag(slotType)` / 
`requireAllTags(validTags)`; no `TagResolver`). So a slot whose required tag is carried by no content
is silently **unfillable**. The guard is conservative — it only reports single-required-tag validators
(accept-all / multi-tag / custom are never flagged), so no false positives.

It found **9 distinct unfillable slot-validator tags**: the slot `type` vocabulary
(`trinket`/`dye`/`binding`/`grip`/`tip`/`pommel`, plus diamond's `*_slot` names) predated the content,
which moved to `upgrades/types/*` / `materials/roles/*` / `materials/types/*` and never updated the
slots. (Gems still installed — they carry `materials/roles/upgrade_material`, and those slots worked —
so the dedicated `trinket` "gem slots" were dead *redundant* slots, not a broken feature. That is why
it was silent.) **All 9 are now migrated** (62 slot defs across 51 files), each validator repointed at
the tag its intended content actually carries, decided by the slot's own identity tag and by working
analogues:

| Dead validator `type` | Migrated to | Basis |
|---|---|---|
| `forgero:trinket` (gem-slot) | `forgero:upgrades/types/gem` | gems carry it (slot identity = gem) |
| `forgero:trinket` (pommel-slot) | `forgero:upgrades/types/pommel` | pommel schematics carry it |
| `forgero:trinket` (grip-slot) | `forgero:upgrades/types/grip` | grips take soft materials (see note) |
| `forgero:trinket` (generic upgrade-slot) | `forgero:materials/roles/upgrade_material` | slot identity is the generic `upgrades` |
| `forgero:dye` (cosmetic-slot) | `forgero:upgrades/types/cosmetic` | dyes carry it |
| `forgero:binding` / `forgero:binding_slot` | `forgero:upgrades/types/binding` | binding materials (see note) |
| `forgero:tip` / `forgero:tip_reinforcement_slot` | `forgero:upgrades/types/tip_reinforcement` | tip materials carry it (`tip` was a name near-miss) |
| `forgero:grip` / `forgero:handle_grip_slot` | `forgero:upgrades/types/grip` | grips take soft materials (see note) |
| `forgero:pommel` | `forgero:upgrades/types/pommel` | pommel schematics carry it |

**Taxonomy decision (maintainer): binding / grip / pommel / tip are upgrades.** So every per-type
upgrade slot validates `forgero:upgrades/types/<x>` uniformly (the generic catch-all slot stays
`materials/roles/upgrade_material`). Two families had no content under that identity, so — applying the
decouple-identity-from-validator pattern to the content side — the identity tag was added to exactly the
content installable today, preserving the install sets while relabeling the vocabulary:

- **binding:** the 24 binding materials carried only `materials/types/binding`; added
  `upgrades/types/binding` to them and pointed the 4 vanilla binding slots at it.
- **grip:** the only grip-capable content is soft materials (per the bow-limb grip slot, which validates
  the soft type); added `upgrades/types/grip` to the 14 `materials/properties/soft` materials and pointed
  the 4 grip slots at it. (`materials/types/binding` / `materials/properties/soft` remain on the content
  as their material-nature tags; the new tags are purely additive upgrade identities.)

Judgment calls made (per maintainer request to migrate all 9): the `trinket` gem-slots now accept gems
(rather than reserving a future "trinket" item); grip slots accept soft materials. After migration the
guard reports **zero** unfillable slots, so it is now an **error** (build-failing): any new dead slot is
a regression.

`validateContentPacks` and the full gametest corpus stay green.

## Remaining — needs design intent, deliberately NOT auto-normalized
1. **Two binding install models coexist by design:** native tools take a crafted binding **part**
   (`parts/binding`); vanilla `static_part` tools take a raw binding **material** (now
   `upgrades/types/binding`). Documented as intentional; revisit only if unifying the crafting flow —
   that would mean teaching crafted binding parts to carry the `upgrades/types/binding` identity too.
2. **Harden `SlotReferenceValidator`** from "classifier exists *somewhere*" to "every slot a material
   role can occupy carries the identity it's gated on" — the condition-side analogue of the fillability
   guard, to catch identity drift (not just validator drift) at build time.

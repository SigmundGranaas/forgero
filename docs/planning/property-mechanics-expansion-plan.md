# Property System Expansion — Implementation & Testing Plan

Status: **implemented** (all phases 0–5). Scope: add stateful, compositional, skill-rewarding
mechanics to the existing three-tier property system (selector → effect → condition) without
reinventing the engine.

## Implementation status

All phases are built, compile under Java 17, and pass GameTests
(`:modules:mc:properties:runGameTest` = 261, `:modules:mc:predicate:runGameTest` = 42).

| Phase | What shipped | Tests |
|---|---|---|
| 0 Mark store | `LivingEntityMarkDataMixin`, `MarkStore` (NBT, lazy expiry) | `MarkGametest` |
| 1 Marks & detonation | `forgero:mark`, `forgero:clear_mark`, `forgero:has_mark` filter, `forgero:target_has_mark` condition | `MarkGametest` |
| 2 Positional | `forgero:backstab`, `forgero:crowd_count` conditions; `forgero:is_airborne`/`facing_away`/`line_of_sight` filters; `forgero:on_crit`, `forgero:on_block` events | `PositionalCombatGametest` (+ mixins validated by boot under `defaultRequire:1`) |
| 3 Zones | `forgero:create_zone` + `ZoneManager` (ServerTickEvents), `owner_mode` enum | `ZoneGametest` |
| 4 Identity | `forgero:time_of_day`, `forgero:moon_phase` conditions | `IdentityConditionGametest` |
| 5 Set bonuses | `forgero:on_equip`/`on_unequip` events + `EquipmentChangeManager`, `forgero:wearing_set` condition | `WearingSetGametest` |

**Also fixed:** `OnHitManager` now puts `SOURCE_ENTITY`/`TARGET_ENTITY`/`WORLD` into the dynamic
context, so entity-based conditions (`minecraft:entity`, `forgero:backstab`,
`forgero:target_has_mark`) actually evaluate in `on_hit` (previously only `TARGET_TAGS` was present).

**Coverage layers now in place per mechanic:** (1) behavioural — handlers/filters/conditions/
selection logic exercised directly (`MarkGametest`, `PositionalCombatGametest`, `ZoneGametest`,
`IdentityConditionGametest`, `WearingSetGametest`); (2) JSON authorability — new effects/filters
parse through the registered dispatch codecs (`NewMechanicsCodecGametest`); (3) structural — the
crit/shield mixins inject and the plugins register (server boots under `defaultRequire:1`).

**Event firing — CLOSED.** A real example upgrade material (`forgero:example_event_aspect`, hosted
on `minecraft:heart_of_the_sea`) carries all four new event properties. `EventPropertyFiringTest`
(in `mods/forgero`, where content loads) installs it on an iron sword and asserts: each property
resolves onto the real item, and triggering the managers applies the effect (on_crit/on_block
ignite the victim/attacker, on_equip grants speed, on_unequip applies glowing). `on_equip` /
`on_unequip` were broadened to cover **all** equipment slots (hands + armor), so they fire on
wielding as well as armoring. The content is intentionally a labelled demo — tune naming/balance
as desired.

**Other deferrals:** `create_zone` ships the entity (`ContextualEffectHandler`) variant only — a
block (`OnHitBlockEffect`) variant remains future work. `wearing_set` matches against item-registry
tags.

## Guiding principles

1. **Reuse the engine.** New mechanics should compose existing effects/selectors/filters.
   We do **not** add more one-shot instantaneous effects (that surface is saturated).
2. **Fail fast.** Validate config at construction/data-load with clear messages; never
   silently clamp or fall back (consistent with the rest of the codebase and project policy).
3. **Match existing patterns exactly.** Every new piece slots into an established registration
   path; no new frameworks. Concrete templates are cited per phase.
4. **Test at two levels.** Pure JUnit for logic (codecs, math, state bookkeeping); GameTests
   for in-world behaviour. GameTests share one world — assert *invariants*, not exact counts
   (see Testing Strategy).

## Integration points (verified against the codebase)

| Concern | Extension API | Template to copy | Registration |
|---|---|---|---|
| Entity effect | `EntityEffectHandler.apply(Entity)` / `ContextualEffectHandler.apply(source,target)` (both extend `OnHitEffect`) | `FireHandler`, `KnockbackHandler`, `SpawnEntityHandler` | `OnHitPropertiesPlugin.registerEffect(TYPE, CODEC)` |
| Block effect | `OnHitBlockEffect.apply(World,Entity,BlockPos)` | `PlaceBlockEffect`, `IgniteBlockEffect` | `OnHitBlockPropertiesPlugin.registerEffect` |
| Entity filter | `EntityFilter.test(source, candidate)` | `HealthThresholdFilter`, `EnvironmentFilter` | `OnHitPropertiesPlugin.registerFilter(TYPE, CODEC)` |
| Dynamic condition | `EvaluableCondition.test(DynamicContext)` | `WeatherPredicate`, `RelationalPredicate`, `LocationPredicate` | `MinecraftPredicatePlugin` → `registerDynamicConditionCodec` |
| New event | record `implements ConditionalProperty` + `KEY_ID/KEY` + manager + mixin | `OnHitProperty`+`OnHitManager`+`LivingEntityOnHitMixin` | `CompilerPasses.register(KEY, …)` + `context.registerPropertyCodec(…)`; add mixin to `properties.mixin.json` |
| Effect dispatch loop | `EntityEffects.apply(selector, effects, source, target)` | — | — |
| Property read at runtime | `PropertyDispatcher.active(stack, KEY, DynamicContext)` | — | — |
| Runtime context keys | `MinecraftContextKeys.{STACK, WORLD, SOURCE_ENTITY, TARGET_ENTITY, BLOCK_POS}` | — | — |

Mixin package: `com.sigmundgranaas.forgero.properties.minecraft.mixin`; config:
`modules/mc/properties/src/main/resources/properties.mixin.json`. Tick-based managers attach to
the existing `LivingEntityTickMixin` (`LivingEntity.tick()` @HEAD), as `OnTickManager` and
`OnSneakToggleManager` already do.

---

## Phase 0 — Foundation: NBT-tracked entity mark store (PREREQUISITE)

Marks are **entity-scoped** transient state. Decision: store them in **per-entity persistent
NBT** with **lazy expiry**, not in an in-memory map and not in a world `PersistentState`.

Rationale:
- Marks belong to the entity; Minecraft persists/loads entity NBT automatically (mobs with the
  chunk, players with player data) — survives save/reload.
- NBT travels with the entity **across dimensions**; a world store is per-world and would drop
  marks on a portal transition.
- The data dies with the entity → **no orphaned-UUID cleanup** and no disconnect listener.
- **Performant by lazy expiry**: store absolute expiry `world.getTime() + duration`; evaluate it
  *on read*. No per-tick scan of any entity; serialization happens only on chunk save.

Mechanism: a mixin on `LivingEntity` adds a `forgero$markData` `NbtCompound`, serialized via
`writeCustomDataToNbt` / `readCustomDataFromNbt`, exposed through a small accessor (duck-typed
mixin interface). All access goes through one service so callers never touch NBT directly.

### Deliverables
- `mixin/LivingEntityMarkDataMixin.java` (+ `MarkDataHolder` accessor interface) — adds and
  (de)serializes the `forgero$markData` compound; add to `properties.mixin.json`.
- `effects/mark/MarkStore.java` — `mark(LivingEntity, Identifier markId, int durationTicks)`,
  `hasMark(LivingEntity, Identifier)` (lazy-expiry check + opportunistic prune of expired keys),
  `clearMark(LivingEntity, Identifier)`. Stores `markId -> expiryGameTime` in the compound.
  Validates `durationTicks > 0` (fail fast).

### Tests
- **JUnit** (no server): expiry comparison logic, prune-on-read, fail-fast on non-positive
  duration, NBT round-trip via a real `NbtCompound`.
- **GameTest**: `mark` an entity → `hasMark` true → advance past expiry (`world.getTime()`
  moves) → false; `clearMark` removes immediately; a re-`mark` refreshes expiry.

---

## Phase 1 — Marks & detonation (highest leverage)

Turns isolated effects into combo systems by reusing `chain`/`explosion`/`lightning`/`aoe`.

### Deliverables
- Effect `forgero:mark` — `MarkHandler implements ContextualEffectHandler` → calls
  `MarkStore.mark(target, markId, duration)` (target must be a `LivingEntity`). Fields: `mark`
  (Identifier), `duration` (int, >0 fail-fast).
- Effect `forgero:clear_mark` — `MarkStore.clearMark(target, markId)`; record-codec field `mark`.
- Filter `forgero:has_mark` — `EntityFilter.test` → `MarkStore.hasMark(candidate, mark)`.
  Lets selectors target only marked entities.
- Condition `forgero:target_has_mark` — `EvaluableCondition` reading `TARGET_ENTITY` →
  `MarkStore.hasMark(...)` (mirrors existing `forgero:target_has_tag`).

### JSON
```json
"minecraft:on_hit": [
  { "selector": {"type":"forgero:single_target"},
    "effects": [{"type":"forgero:mark","mark":"forgero:hexed","duration":100}] },
  { "selector": {"type":"forgero:aoe","radius":6,
      "filters":[{"type":"forgero:has_mark","mark":"forgero:hexed"}]},
    "effects": [{"type":"forgero:explosion","power":2.0},
                {"type":"forgero:clear_mark","mark":"forgero:hexed"}] }
]
```

### Tests
- **GameTest**: hit A → A marked; AOE detonate only marks A not unmarked B; `clear_mark`
  removes it; mark expires after `duration`.
- **JUnit**: codec round-trip for all three; filter logic against a stubbed service.

---

## Phase 2 — Positional / skill combat (conditions, filters, two events)

Adds the geometry/skill checks the system lacks today (all current filters are state checks).

### Deliverables — conditions (predicate module)
- `forgero:backstab` — reads `SOURCE_ENTITY`+`TARGET_ENTITY`; angle between target facing and
  source→target vector ≤ `max_angle` (default 60). Template: `RelationalPredicate`.
- `forgero:crowd_count` — reads an entity + `WORLD`; counts living entities in `radius`,
  passes if `min` ≤ count (≤ optional `max`). Template: `LocationPredicate` for world access.

### Deliverables — filters (properties module)
- `forgero:line_of_sight` — `EntityFilter.test` via `world.raycast` source↔candidate; keeps only
  targets with clear LOS (AOE/chain that won't hit through walls).
- `forgero:is_airborne` — `!candidate.isOnGround()`.
- `forgero:facing_away` — candidate facing away from source (backstab targeting in selectors).

### Deliverables — events
- `forgero:on_crit` — `PlayerEntityAttackMixin` capturing vanilla crit flag in
  `PlayerEntity.attack(...)`, firing `OnCritManager` (record/manager mirror `OnHitProperty`).
- `forgero:on_block` — `LivingEntityDamageMixin` (existing) extended to detect
  `blockedByShield(source)` → `OnBlockManager` (parry-triggered effects).

### JSON
```json
"minecraft:on_hit": [{
  "condition": {"dynamic":[{"type":"forgero:backstab","max_angle":60}]},
  "selector": {"type":"forgero:single_target"},
  "effects": [{"type":"forgero:status_effect","effect":"minecraft:weakness","duration":100}]}]
```

### Tests
- **JUnit**: backstab angle math (behind / front / flank); crowd count thresholds.
- **GameTest**: position attacker behind target → backstab fires, in front → does not; LOS filter
  drops a target behind a wall; `on_crit` fires only on a critical hit; `on_block` fires when a
  shield blocks.

> Risk: `on_crit`/`on_block` need new injection points. `on_crit` reads the crit boolean in
> `PlayerEntity.attack`; `on_block` keys off `blockedByShield`. Both are isolated mixins; build and
> test them last in this phase.

---

## Phase 3 — Persistent zones (lingering AOE fields)

Today AOE is instantaneous. A zone re-applies a selector+effects to entities inside an area on an
interval — unlocking fire fields, healing totems, frost ground, gravity wells.

### Deliverables
- Effect `forgero:create_zone` (entity + block variants: `ContextualEffectHandler` and
  `OnHitBlockEffect`). Fields: `radius`, `duration`, `interval`, `effects` (List<OnHitEffect>),
  `filters` (List<EntityFilter>), `owner_mode` (enum, see below), all validated fail-fast.
- `effects/zone/ZoneManager.java` — in-memory list of active zones per world, ticked via
  `ServerTickEvents.END_WORLD_TICK`. Each tick where `age % interval == 0`: query
  `world.getOtherEntities(owner, box)`, apply filters (reusing `AreaOfEffectSelector`'s spherical
  distance test for the radius), then apply effects. Remove on expiry.
- Reuse `EntityEffects` invocation semantics for applying the effect list.

### Configurable owner semantics
`owner_mode` controls behaviour when a `ContextualEffectHandler` runs but the zone owner is gone
(logged out / dead / unloaded):
- `keep_owner` (default) — use the original owner as `source`; if the owner is absent, skip
  source-dependent (contextual) effects but still run target-only effects.
- `target_as_source` — pass the affected entity as its own `source` (self-inflicted semantics);
  always runs all effects.
- `owner_required` — the whole zone tick is skipped while the owner is absent (fail-closed).

### JSON
```json
"minecraft:on_hit_block": [{ "effects": [{
  "type":"forgero:create_zone","radius":3,"duration":200,"interval":10,"owner_mode":"target_as_source",
  "filters":[{"type":"forgero:is_hostile"}],
  "effects":[{"type":"forgero:status_effect","effect":"minecraft:slowness","duration":40}]}]}]
```

### Tests
- **JUnit**: zone lifecycle (expiry, interval gating) with a fake clock; `owner_mode` routing for
  each enum value with a stubbed effect.
- **GameTest**: spawn zone → entity inside gets the effect each interval → entity outside does not
  → zone stops after `duration`. Owner-absent: assert each `owner_mode` behaves as specified.

> Risk: highest effort. Persistence of in-flight zones across restart is out of scope for MVP
> (document it); zones are seconds-scale so this is acceptable.

---

## Phase 4 — Identity gear (cheap, atmospheric)

### Deliverables (predicate module, template `WeatherPredicate`)
- `forgero:time_of_day` — reads `WORLD`; `world.getTimeOfDay() % 24000` against
  `min`/`max` (or a `phase`: `day`|`night`).
- `forgero:moon_phase` — `world.getMoonPhase()` against allowed phases.

### JSON
```json
"condition": {"dynamic":[{"type":"forgero:time_of_day","phase":"night"}]}
```

### Tests
- **JUnit**: time/phase boundary math.
- **GameTest**: set world time day/night → condition gates an effect accordingly.

---

## Phase 5 — Set bonuses (armor synergy)

The armor module has no equip hook today; equipment is only read per-trigger. Add change
detection by diffing equipment per tick (same pattern as `OnSneakToggleManager` for sneak state).

### Deliverables
- Events `forgero:on_equip` / `forgero:on_unequip` — `EquipmentChangeManager.handleTick(entity)`
  compares previous vs current per-slot equipment (stored per UUID), firing the relevant property
  off the changed stack. Hook into `LivingEntityTickMixin`.
- Condition `forgero:wearing_set` — counts equipped items sharing a tag (`tag`, `min_count`);
  reads `SOURCE_ENTITY` + iterates `getArmorItems()`.

### JSON
```json
"forgero:on_equip": [{ "condition": {"dynamic":[{"type":"forgero:wearing_set","tag":"forgero:dragon_set","min_count":4}]},
  "effects": [{"type":"forgero:status_effect","effect":"minecraft:fire_resistance","duration":1000000}]}]
```

### Tests
- **GameTest**: equip 4th set piece → bonus applied; remove one → `on_unequip` fires, bonus gone.
- **JUnit**: `wearing_set` counting logic.

---

## Cross-cutting testing strategy

- **Two tiers.** Pure JUnit (logic: codecs, math, state) in `modules/core`/`modules/mc`
  test sources; GameTests for in-world behaviour using `ForgeroGameTest` / `forgero(context)`,
  `PlayerFactory`, `ItemStackAssertions`, `EffectAssertions`, `context.spawnEntity`,
  `@GameTest(templateName = EMPTY_STRUCTURE)`.
- **GameTests share one world.** Assert invariants (uniqueness, bounds, specific-reference
  inclusion/exclusion, nearest-anchor), **not** exact global counts — neighbouring tests' entities
  fall within generous ranges. (This bit the selector tests; see `EntitySelectorGametest`.)
- **Every codec gets a round-trip test** (encode→decode→equals) and a **fail-fast test**
  (bad config throws at parse).
- **Run**: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :modules:mc:properties:runGameTest`
  for in-world; `:modules:core:test` / `:modules:mc:properties:test` for JUnit.

## Sequencing & dependencies

```
Phase 0 (mark store)  ──> Phase 1 (marks & detonation)
Phase 2 (positional/skill)  independent
Phase 3 (zones)             independent (reuses effects+selectors)
Phase 4 (identity)          independent, smallest
Phase 5 (set bonuses)       independent (needs equip-diff manager)
```

Recommended order by leverage/effort: **0 → 1 → 2 → 4 → 5 → 3**
(foundation first; marks for biggest payoff; positional for "feel"; identity is nearly free;
set-bonuses next; zones last as the largest lift).

## Rough effort (relative)

| Phase | Effort | Risk |
|---|---|---|
| 0 Mark store (NBT mixin) | M | low |
| 1 Marks & detonation | M | low |
| 2 Positional (cond/filter) | M | low |
| 2 on_crit / on_block events | M | med (new injection points) |
| 3 Zones | L | med (lifecycle, configurable owner semantics) |
| 4 Identity conditions | S | low |
| 5 Set bonuses | M | med (equip diffing) |

## Resolved decisions

1. **Mark persistence** — NBT-tracked on the entity with lazy expiry (server-side; survives
   save/reload; follows across dimensions). Not a world store, not an in-memory map.
2. **Combo meters** — dropped from scope (and with them the `scope`/stack concepts).
3. **Zone owner-gone behaviour** — configurable via `owner_mode`
   (`keep_owner` | `target_as_source` | `owner_required`).

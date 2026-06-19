# Mergeable resources (independent content packs)

Two content packs can each fully define the **same resource id** and have them **merged** when both
are loaded — no shared "base"/shell pack required, and each pack is independently usable on its own.
This lets you split a resource's concerns across packs, e.g. a material's stats in one pack and its
upgrade/binding role in another.

## How it works

At load, all definitions are grouped by id. A group with more than one definition is merged in
ascending **priority** order (ties fall back to load order); the higher-priority definition is
applied last, so it wins on id-keyed overrides.

Field merge rules:

| Field | Rule |
|---|---|
| `tags` / `local_tags` | union |
| `host.identifiers` | union (both packs may claim the same host) |
| `attributes` / `local_attributes` | keyed by attribute `id` — a matching id **overrides**; no id / new id **appends** |
| `upgrades` (slots) | keyed by slot `id` — matching id overrides, else appends |
| `properties` | deep merge (objects recurse, arrays concatenate, scalars: higher priority wins) |
| `include` | union |
| `type` / `name` | must agree (see clashes) |

Set an optional top-level `"priority"` (default `0`) to control override precedence.

## Example: split a material from its binding role

`pack-a/.../materials/iron.json` (stats + host):
```json
{ "type": "forgero:material", "name": "iron",
  "tags": ["forgero:metal"],
  "host": { "identifiers": [{ "type": "item", "id": "minecraft:iron_ingot" }] },
  "attributes": [{ "id": "forgero:iron-durability", "type": "forgero:durability", "computation": 250 }] }
```

`pack-b/.../materials/iron.json` (upgrade/binding role — independently usable, higher priority):
```json
{ "type": "forgero:material", "name": "iron", "priority": 10,
  "tags": ["forgero:materials/roles/upgrade_material", "forgero:upgrades/types/binding"],
  "attributes": [{ "id": "forgero:iron-durability", "type": "forgero:durability", "computation": 300 }] }
```

Loaded together, `forgero:iron` ends up with both tag sets, a single `forgero:iron-durability`
attribute of **300** (pack B overrides by id at higher priority), and the host from pack A. Loaded
alone, either pack yields a complete `forgero:iron`.

## Claiming the same host

Different resources **may** claim the same host item (e.g. `minecraft:quartz` backs both a quartz
gem and a quartz material) — that is allowed. Merging only happens between definitions that share an
**id**.

## Clashes (fail fast)

Definitions sharing an id but declaring different `type`s (e.g. one `forgero:material`, one
`forgero:static_part`) are an identity clash and fail the load with a clear error. Give them distinct
ids, or align their type.

## Relationship to extensions

`forgero:extension` resources (under `data/forgero/extensions/`) layer onto an existing **target**
id and use the same field-merge rules. Use an extension when you want a *fragment* that only applies
if its target exists; use same-id definitions (this feature) when each pack should be a *complete*,
standalone resource that merges when co-present.

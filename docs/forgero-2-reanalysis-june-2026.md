# Forgero-2 Re-Analysis: June 2026 Head (`8da586ed5`)

**Scope:** Full re-run of the October-2025 analysis against the current `forgero-2` head — 67 new
commits spanning the Christmas 2025 rework through June 11 2026 (~200k insertions, 6,297 files).
Supersedes the verdicts in `forgero-2-api-analysis.md` and updates
`forgero-2-attribute-redesign.md` where noted. Verified by three independent code surveys plus
direct reads of the load-bearing functions; core test suite confirmed green on the new head.

---

## 1. Headline: the halt statement is now mostly obsolete — by your own hand

The October analysis confirmed your reasons for losing faith: no usable API, a proprietary heavy
component model, broken tests, no docs. The Christmas-through-June work addressed the majority of
it directly:

| October 2025 finding | June 2026 state |
|---|---|
| API: 4 proprietary types + 3 modules to read damage; javadoc example didn't compile | **`ItemQueryApi`**: `query.getAttackDamage(stack)` — one line, **zero proprietary types**, MC-native signatures throughout (`isForgeroItem`, `getParts`, `getInstalledUpgrades`, `getMiningSpeed(stack, blockState)`, plus `ItemMutationApi.installUpgrade/canInstallUpgrade`, `ItemComparisonApi`, `ForgeroInitializedCallback`) |
| No documentation | `docs/` with 7 architecture docs incl. **ADR-001**, guides, reference, a scope-vs-condition decision tree; JSON schemas in `schemas/`; comprehensive javadoc on the API |
| Core tests didn't compile | **Green build**; 189+ tests incl. 27 mod-level gametests (`VanillaToolParityTest`, assembly/upgrade-station gametests), 82 behavioral model tests, fixtures in `test-common` modules |
| Composite machinery: string `compositeKey`, ≥2-operator silent discard, doc/code contradiction | **`CompositeAttribute`/`CompositeAttributeComponent` deleted.** Replaced by `AttributeScope` (LOCAL / PART_COMPOSITE / EQUIPMENT_COMPOSITE / UPGRADE) + three collectors; material×shape semantics preserved, mechanism rebuilt |
| `AttributeManager` god-bridge | **Deleted** (June 11); mixins are thin delegations through `MixinServiceAccessor` → `ItemQueryApi` |
| Extension points missing (custom conditions impossible) | `PluginRegistrationContext`: register static/dynamic condition codecs, property codecs, slot codecs, item creators |
| Content half-migrated, mod not runnable | 14 content modules, 41+ materials, bows/blocks/stations, legacy packs quarantined read-only — **runnable** |

The third-party simulation that scored the October code 2/10 on its core tasks now scores 8–10/10
on API surface, queries, composition listing, and vanilla read-interop. The strategic complaint —
"no API for other developers on the MC layer" — has been substantially answered.

## 2. The convergence worth noting

The January–June work and this session's design dialogue arrived at the same architecture from
opposite directions, in some places almost verbatim:

- **ADR-001's scope-vs-condition split** ("WHERE in composition" vs "WHEN at runtime") is the
  orthogonality this dialogue derived as position-scoping + conditions.
- **`AttributeScope` replacing `compositeKey`** is "scope is structural, not a coordinated
  string" — the named-scope mechanism instead of hand-matched keys.
- **The three collectors** (`Default`, `PartComposite`, `Upgrade`) implement the
  structure-composes / upgrades-modify layering, and `UpgradeAttributeCollector.composeStructuredUpgrade`
  — composing a structured upgrade internally before merging, explicitly avoiding double-count —
  is the **sealing** concept.
- **`DynamicContextFactory`** builds real context exactly and only at event-shaped call sites
  (arrows, throw handlers — places that hold an `Entity`), which is the read-scope law: dynamics
  live where the world is in hand.

Independent convergence is strong evidence the shared conclusions are load-bearing rather than
taste.

## 3. What did NOT get fixed (verified directly, not from survey summaries)

Three of the October findings survived the rework intact, and they are precisely the remaining
distance to the finish line:

**3.1 The vanilla-modifier discard is still there — moved, not fixed.**
`ItemQueryApiImpl.getAttributeModifiers` (modules/mc/common/.../item/impl/ItemQueryApiImpl.java:363-370):

```java
finalMap.putAll(forgeroAttributes);
vanillaMap.entries().stream()
    .filter(entry -> !finalMap.containsKey(entry.getKey()))   // same key-wise wipe as AttributeManager
    .forEach(entry -> finalMap.put(entry.getKey(), entry.getValue()));
```

Any modifier another mod, a map maker, or a command attaches under an attribute key Forgero
provides is silently destroyed. Reading Forgero stats without a dependency now works (the mixin
merges into the vanilla call); *writing* through the vanilla mechanism is still broken. The
attack-speed `-4.0f` translation and armor-UUID constants also persist in `createAttributeMap`.

**3.2 The hot path still re-parses NBT per query.** All three vanilla-facing mixins
(`ItemStackDurabilityMixin`, `ItemMiningMixin`, `ItemStackAttributeMixin`) resolve fresh on every
call: `converter.toComponent(stack)` (full COF deserialization, uncached) → resolve. The new
`BakedAttributes` (baked once per *component instance* at construction, O(1) lookup,
`PrecomputedAttribute` fast path) removes the per-query *bake* for registry-pristine components —
a real improvement — but mutated stacks (the point of the mod) reconstruct and re-bake per parse,
and the parse itself is never cached or persisted. The durability bar still costs a tree
deserialization per frame.

**3.3 The craft-time thesis still holds for the vanilla layer — now with sharper evidence.**
Call-site classification at the new head: every mixin/tooltip-default path resolves with an empty
`DynamicContext`; real context is constructed only in projectile/throw code. So the system now
demonstrates *both halves in the same codebase*: dynamics where context exists (correct, new),
statics recomputed per query where context never exists (still wasteful). The conclusion is
unchanged: for everything `getAttributeModifiers`/`getMaxDamage`/`getMiningSpeedMultiplier` can
observe, values are constant between mutations and should be **compiled at mutation time and
persisted on the stack** — vanilla `AttributeModifiers` NBT for entity attributes (which also
fixes 3.1, since the writer can replace only its own UUID-keyed entries), a flat stats tag +
generation stamp for durability/mining. That is exactly the `StatCompiler`/`StatPersistence`
design proven on this branch's predecessor; it now slots in cleanly *behind* `ItemQueryApi` as its
fast path, invisible to consumers.

## 4. Status of this branch's prior work

- The two analysis reports' *verdicts* are superseded as described above (this document is the
  update); their method — parity-gated demolition — was independently validated again.
- The June 10 implementation (`StatCompiler`, `ComputedStats`, `StatPersistence`,
  `ComputedStatsNbt`, `VanillaAttributeWriter`, `StatGeneration`) targets the *old* engine
  (pre-scope, pre-collectors) and **must be rebuilt against the new one, not rebased** — the
  parity harness pattern transfers directly (compile vs `AttributeEngine.resolveAttributes` with
  empty context across the component registry), the persistence layer transfers almost verbatim,
  and the single-choke-point wiring now has an even better home: inside `ItemMutationApi` /
  converter, under the API facade.
- The API-facade recommendation from the first report is **done** (better than proposed, in
  places) by `ItemQueryApi`/`ItemMutationApi`. What that report called Tier 0 — vanilla-native
  persistence as the primary contract — is the part still open.

## 5. Remaining work, in priority order

1. **Fix the modifier merge** (3.1): replace key-wise discard with UUID-owned replacement —
   small, self-contained, removes the last active interop break.
2. **Bake-and-persist behind `ItemQueryApi`** (3.2/3.3): compile stats at mutation points
   (`ItemMutationApi.installUpgrade`, crafting, conversion), write vanilla `AttributeModifiers` +
   stats tag + generation stamp; mixins read one NBT value with resolve-fallback for stale/legacy
   stacks. Gate with a registry-wide parity test against the new engine. This deletes the
   per-frame parse and makes vanilla NBT the durable contract (1.20.5+/1.21 data components later
   reduce the remaining mixins to zero).
3. **Third-party onboarding docs**: the API is now good and documented in javadoc nobody will
   find — a quick-start ("depend on Forgero, query an item, register a condition") plus an
   in-repo example consumer mod as the acceptance test.
4. **Publishing/versioning statement** for the API artifact (semver promise on
   `common/api`), so other developers can actually depend on what was built.

## 6. Bottom line

October's verdict was "halting was defensible, but the branch is ~80% salvageable." June's verdict
is stronger: **the salvage happened.** The rework now has the consumer-shaped API, the docs, the
tests, the content, and an attribute model that independently converged on the scope/condition,
layered-composition, and context-at-event-sites design this dialogue derived. What remains is one
deliberate finishing move — make the vanilla layer the *persisted* contract instead of a per-query
projection (fixing the modifier wipe in the same stroke) — plus onboarding docs. The original
complaint is no longer "designed in a void"; it's two verified code-level gaps with known,
parity-testable fixes.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Forgero is a Minecraft Fabric mod for deep tool/weapon/armor customization. The mod is data-driven through JSON - materials, tools, and behaviors can be added without code changes. Everything is a `Component` that composes into hierarchies, with properties attaching behaviors and templates auto-generating permutations.

**Target**: Minecraft 1.20.1 (Fabric)
**Language**: Java 17

## Build Commands

### Running the Mod
```bash
# Run Minecraft client with the mod
./gradlew :mods:runClient


# Run vanilla-upgrades mod specifically
./gradlew :mods:vanilla-upgrades:runClient
```

### Building
```bash
# Build the entire project
./gradlew build

# Build only the mods module
./gradlew :mods:build

# Build vanilla-upgrades specifically
./gradlew :mods:vanilla-upgrades:build

# Clean build artifacts
./gradlew clean
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :modules:core:test
./gradlew :modules:mc:properties:test
./gradlew :modules:mc:loader:test

# Run tests with detailed output
./gradlew test --info
```

## Extracting Sources for Claude Analysis

Claude Code can analyze decompiled Minecraft and mod sources when extracted to the local filesystem.

### Extract Minecraft Sources

```bash
./gradlew extractMinecraftSources
```

Extracts ~4,000 Minecraft source files to `.minecraft/sources/minecraft/`. Claude can then read:
- `.minecraft/sources/minecraft/net/minecraft/item/ItemStack.java`
- `.minecraft/sources/minecraft/net/minecraft/block/Block.java`
- `.minecraft/sources/minecraft/entity/player/PlayerEntity.java`

This allows Claude to analyze Minecraft's internals to understand how systems work, find the right methods to call, and ensure compatibility with Minecraft's code.

### Extract Mod Sources

```bash
# Fabric API
./gradlew extractJarSources -Pjars=net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1

# Multiple mods (comma-separated)
./gradlew extractJarSources -Pjars="com.jamieswhiteshirt:reach-entity-attributes:2.4.0,net.fabricmc.fabric-api:fabric-api:0.92.2+1.20.1"

# Custom JAR file
./gradlew extractJarSources -PjarPath=/path/to/my-mod-sources.jar
```

Extracted mod sources are placed in `.minecraft/sources/mods/{artifact}-{version}/`.

### Clean Extracted Sources

```bash
./gradlew cleanExtractedSources
# Or manually: rm -rf .minecraft/
```

**Note:** The `.minecraft/` directory is git-ignored and safe to delete anytime. Regenerate sources as needed using the tasks above.

## Module Architecture

Forgero uses a multi-module architecture with strict dependency rules:

### Core Modules (Platform-Agnostic)
- **`modules/core/`** - Platform-agnostic core logic with ZERO Minecraft dependencies
  - Component system, tags, identifiers, data pipeline
  - Pure Java - can run on any JVM
  - **CRITICAL**: Never import Minecraft classes here

### Minecraft Modules
- **`modules/mc/loader/`** - ForgeroApi and plugin system
  - Plugin registry, data loading orchestration
  - Depends on: `modules/core`, Fabric API

- **`modules/mc/properties/`** - OnHit, OnTick, BlockBreaking, Loot properties
  - Effect handlers (`effects/entity/`), selectors (`entityselector/`)
  - Depends on: `modules/core`, `modules/mc/loader`, Fabric API

- **`modules/mc/common/`** - Common Minecraft utilities
- **`modules/mc/tools/`** - Tool-specific implementations
- **`modules/mc/armor/`** - Armor-specific implementations
- **`modules/mc/bows/`** - Bow and arrow implementations
- **`modules/mc/render/`** - Rendering utilities
- **`modules/mc/predicate/`** - Predicates and conditions
- **`modules/mc/development/`** - Development tools

### Mod Implementations
- **`mods/vanilla-upgrades/`** - Vanilla item upgrade system mod
  - Uses modules/core, modules/mc/* as dependencies
  - Provides runtime implementation of Forgero for vanilla items

### Content Modules (JSON Data Packs)

**Shipped Content Packs (Modern Format)** — bundled into the runtime mod (`mods/forgero`):
- **`content/forgero-base/`** - Core tags, templates, and upgrade slots
- **`content/forgero-materials/`** - 41 primary materials (metals, woods, stones, etc.)
- **`content/minecraft-tools/`** - Vanilla tool integration (static parts)
- **`content/forgero-conditions/`** - Random item-modifier conditions (sharp, durable, swift, etc.; StatusModifier system)
- **`content/forgero-armor/`** - Armor shapes/texture templates
- **`content/forgero-armor-content/`** - Armor part templates & models
- **`content/forgero-mastercrafted/`** - Quality schematics (refined, mastercrafted)
- **`content/forgero-extended-schematics/`** - Extended schematics (weapon blades, tool heads, guards, bindings)
- **`content/forgero-secondary-materials/`** - Secondary materials (soft, hard, hybrid, dyes)
- **`content/forgero-extended-weapons/`** - Extended weapon & mining parts/equipment (katana, rapier, hammer, spade, etc.)
- **`content/forgero-gems/`** - Tiered gem upgrades
- **`content/forgero-bows/`** - Bow & arrow parts and equipment
- **`content/vanilla-upgrades-base/`** - Static parts for vanilla items (bundled via the `vanilla-upgrades` mod)

**Unshipped Content Packs**
- **`content/forgero-structures/`** - Structure templates (world generation); built but not bundled into the runtime mod

> Legacy/deprecated content packs (`forgero-*-legacy-read-only`, `forgero-deprecated`) were removed from
> the build and repository during the forgero-2 rework. The old-format packs remain available in git history.

### Dependency Rules
```
modules/core/              → ZERO Minecraft dependencies
modules/mc/loader/         → core, Fabric API
modules/mc/properties/     → core, loader, Fabric API
mods/vanilla-upgrades/     → core, mc/*, content/*
content/*                  → Pure JSON data packs
```

## Key Architectural Concepts

### Component System
Everything in Forgero is a `Component` - materials, parts, tools, and upgrades all implement the Component interface. Components:
- Are immutable - use `.with*()` methods for modifications
- Compose into hierarchies (e.g., a sword contains a blade, handle, and binding)
- Carry properties that define behaviors
- Are registered in `ComponentRegistry`

**Key APIs** (static accessor; available after Forgero is initialized — see
`ForgeroInitializedCallback` for the injected form):
```java
// Convert ItemStack ↔ Component
Optional<Component> comp = ForgeroApi.converter().toComponent(stack);
Optional<ItemStack> back = ForgeroApi.converter().toStack(comp.get());

// Access the component registry (by id)
ComponentRegistry registry = ForgeroApi.componentRegistry();
Optional<Component> found = registry.get(identifier);

// Slot operations
SlotManager slots = ForgeroApi.slotManager();
```

> Properties are no longer "resolved" at runtime — a terminal compiles its full property
> artifact once at construction and the game layer reads it by key. There is no
> `api.resolver()`/`resolver.resolve(...)`. For stats off an `ItemStack`, prefer the
> **modular ItemStack APIs** below; they hide `Component` entirely.

### Modular ItemStack APIs

For developers more familiar with Minecraft's ItemStack API, Forgero provides three modular, capability-based APIs that abstract away internal Component details. These APIs work directly with `ItemStack` and return sensible defaults (0, false, empty collections) for non-Forgero items.

#### ItemQueryApi - Read-Only Queries

Provides read-only access to item properties without exposing internal Component abstractions.

```java
ItemQueryApi query = ForgeroApi.itemQuery();

// Check item properties
if (query.isForgeroItem(stack)) {
    boolean customizable = query.isCustomizable(stack);
    boolean hasSlots = query.hasEmptySlots(stack);
    int partCount = query.getPartCount(stack);
}

// Query attributes
float damage = query.getAttackDamage(stack);
int durability = query.getMaxDurability(stack);
float miningSpeed = query.getMiningSpeed(stack);
int miningLevel = query.getMiningLevel(stack);

// Get composition
List<ItemStack> parts = query.getParts(stack);
List<ItemStack> upgrades = query.getInstalledUpgrades(stack);
Optional<OpenIdentifier> material = query.getPrimaryMaterial(stack);

// Check slots
int totalSlots = query.getUpgradeSlotCount(stack);
int emptySlots = query.getEmptySlotCount(stack);
int filledSlots = query.getFilledSlotCount(stack);

// Query tags
boolean hasFire = query.hasTag(stack, OpenIdentifier.of("forgero:fire"));
Set<OpenIdentifier> allTags = query.getTags(stack);
```

**Returns sensible defaults** for vanilla items:
- Numeric queries: `0` or `0.0f`
- Boolean queries: `false`
- Collection queries: Empty collections
- Optional queries: `Optional.empty()`

#### ItemMutationApi - Upgrade Operations

Provides immutable upgrade operations that return new ItemStacks.

```java
ItemMutationApi mutate = ForgeroApi.itemMutation();

// Check compatibility before installing
if (mutate.canInstallUpgrade(tool, gem)) {
    // Install upgrade (returns new ItemStack, original unchanged)
    ItemStack upgraded = mutate.installUpgrade(tool, gem);

    // Remove specific upgrade by ID
    ItemStack removed = mutate.removeUpgrade(upgraded, OpenIdentifier.of("forgero:diamond_gem"));

    // Clear all upgrades
    ItemStack cleared = mutate.removeAllUpgrades(upgraded);
}
```

**Immutability guarantee**: All mutation methods return new ItemStacks, original stacks are never modified.

**Failure behavior**: Returns original stack on failure (incompatible items, null inputs, etc.)

#### ItemComparisonApi - Type and Similarity Checking

Provides stateless comparison operations for ItemStacks.

```java
ItemComparisonApi compare = ForgeroApi.itemComparison();

// Compare by type (ignoring NBT state like durability, upgrades)
if (compare.isSameType(stack1, stack2)) {
    // Same tool type (e.g., both iron pickaxes)
}

// Compare structure (same base parts, ignoring upgrades)
if (compare.areSimilar(stack1, stack2)) {
    // Same structure parts, but may have different upgrades
}
```

**Comparison semantics**:
- `isSameType()`: Compares component IDs (stateless), falls back to vanilla item comparison
- `areSimilar()`: Compares structure parts, ignores upgrades (only works for Forgero items)

#### Benefits of Modular APIs

**Before** (Component-based approach — internal types, more boilerplate):
```java
// Getting attack damage meant dropping into the internal Component + attribute layer
Optional<Component> comp = ForgeroApi.converter().toComponent(stack);
if (comp.isPresent()) {
    AttributeQueryResult result = AttributeEngine.resolveAttributes(comp.get());
    float damage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);
}

// Installing upgrade required Component round-tripping
Optional<Component> toolComp = ForgeroApi.converter().toComponent(tool);
Optional<Component> gemComp = ForgeroApi.converter().toComponent(gem);
if (toolComp.isPresent() && gemComp.isPresent()) {
    InstallationResult result = ForgeroApi.slotManager().install(toolComp.get(), gemComp.get());
    if (result.success() && result.component().isPresent()) {
        tool = ForgeroApi.converter().toStack(result.component().get()).orElse(tool);
    }
}
```

**After** (Modular APIs):
```java
// Getting attack damage is one line
float damage = ForgeroApi.itemQuery().getAttackDamage(stack);

// Installing upgrade is one line
tool = ForgeroApi.itemMutation().installUpgrade(tool, gem);
```

**Advantages**:
- **Simpler**: No Component exposure, no Optional chains
- **Modular**: Use only the APIs you need (query-only code doesn't need mutation API)
- **Safer**: Null-safe, returns sensible defaults
- **Cleaner**: Less boilerplate, more readable code
- **Familiar**: Works with ItemStack like vanilla Minecraft

### Plugin System
Forgero uses a plugin-based architecture for extensibility. Plugins are registered via `fabric.mod.json`:

```json
{
  "entrypoints": {
    "forgero:data_plugin": [
      "com.example.MyDataPlugin"
    ],
    "forgero:item_registration_plugin": [
      "com.example.MyItemPlugin"
    ],
    "forgero:post_load_plugin": [
      "com.example.MyPostLoadPlugin"
    ]
  }
}
```

**Plugin Types**:
- `DataPlugin` - Register property codecs, add custom data processing
- `ItemRegistrationPlugin` - Register custom items
- `PostLoadPlugin` - Post-initialization tasks

### Codec-Based Serialization
Forgero uses Mojang's Codec system for type-safe JSON (de)serialization:

- **Singleton codecs**: `Codec.unit(INSTANCE)` for parameterless handlers
- **Record codecs**: `RecordCodecBuilder` for handlers with fields
- **Optional fields**: `.optionalFieldOf("field", defaultValue)`
- **Polymorphic dispatch**: `DispatchCodecUtils.create()` for type-based routing

See `docs/guides/creating-content-packs.md` for detailed codec patterns and JSON API reference.

### Property System
Properties attach behaviors to components. They are resolved dynamically based on:
- **Static Conditions**: Component structure (`in_slot_type`, `is_root`, `has_tag`)
- **Dynamic Conditions**: Game state (`is_sneaking`, `is_raining`, `target_has_tag`)

**Property Types**:
- `minecraft:on_hit` - Triggers when entity is hit
- `minecraft:on_tick` - Periodic effects while held/worn
- `minecraft:on_loot_drop` - Modifies loot drops
- `minecraft:block_breaking` - Block breaking behaviors
- Attributes (`forgero:durability`, `forgero:attack_damage`, etc.)

**Three-Tier Property Structure** (OnHit/OnTick):
1. **Selector** - Who to affect (single target, AOE, cone, chain)
2. **Effects** - What happens (fire, lightning, status effects, etc.)
3. **Condition** - When to trigger (optional)

## JSON Data Structure

Forgero's data-driven design means most content is defined in JSON files located in `content/*/src/main/resources/data/forgero/`.

### Directory Structure
```
data/forgero/
├── materials/       - Material definitions (iron, diamond, etc.)
├── schematics/      - Tool part schematics (blade, handle, etc.)
├── tags/           - Tag definitions for grouping
└── properties/     - Custom property definitions
```

### Common JSON Patterns

See `docs/guides/creating-content-packs.md` for comprehensive JSON examples including:
- Material definitions with properties and attributes
- OnHit effects with selectors and conditions
- OnTick periodic effects
- Conditional attribute application
- All available effect types, selectors, filters, and conditions

## Testing

Tests use JUnit 5:

```bash
# Run unit tests
./gradlew test

# Run tests for specific module
./gradlew :modules:core:test
./gradlew :modules:mc:properties:test

# Run specific test class
./gradlew test --tests "com.example.MyTest"

# Run with verbose output
./gradlew test --info
```

**Test Locations**:
- Unit tests: `src/test/java/` in each module
- Test resources: `src/test/resources/`

### Test Utilities

Forgero provides two test utility modules to simplify writing tests:

#### modules/core/test-common

Platform-agnostic test utilities with zero Minecraft dependencies. Ideal for pure Java unit tests of core components.

**Add to dependencies**:
```gradle
dependencies {
    testImplementation(project(":modules:core:test-common"))
}
```

**Key Features**:
- **PropertyFixtures**: Factory methods for test attributes (`attackDamage(5.0f)`, `durability(1000)`)
- **MaterialFixtures**: Pre-configured materials (`iron()`, `diamond()`, `oak()`)
- **ComponentBuilder**: Fluent API for building test components
- **ComponentAssertions**: Chainable assertions for validation

**Example**:
```java
import static com.sigmundgranaas.forgero.testcommon.fixtures.MaterialFixtures.*;
import static com.sigmundgranaas.forgero.testcommon.fixtures.PropertyFixtures.*;
import static com.sigmundgranaas.forgero.testcommon.assertions.ComponentAssertions.*;

@Test
void test_component_structure() {
    // Build a component with fluent API
    Component pickaxe = ComponentBuilder.create()
        .id("test-pickaxe")
        .type(Type.PICKAXE)
        .part(ComponentBuilder.simplePickaxeHead(iron()))
        .part(ComponentBuilder.simpleHandle(oak()))
        .upgradeSlot(Type.GEM, 2)
        .build();

    // Fluent assertions
    assertThat(pickaxe)
        .hasType(Type.PICKAXE)
        .isCustomizable()
        .asCustomizable()
        .hasPartCount(2)
        .hasSlotCount(2)
        .hasSlotOfType(Type.GEM);
}
```

#### modules/mc/test-common

Minecraft-specific test utilities for GameTests and ItemStack testing.

**Add to dependencies**:
```gradle
dependencies {
    testImplementation(project(path: ":modules:mc:test-common", configuration: 'namedElements'))
}
```

**Key Features**:
- **ForgeroGameTest**: Base interface with easy access to ForgeroServices
- **ForgeroTestContext**: Enhanced TestContext with Component/ItemStack conversion
- **PlayerFactory**: Fluent builder for creating test players
- **TestPos/TestPosCollection**: Position utilities for GameTest coordinates
- **ItemStackAssertions**: Fluent assertions for ItemStacks

**Example**:
```java
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import static com.sigmundgranaas.forgero.mc.testcommon.assertions.ItemStackAssertions.*;

public class MyTest implements ForgeroGameTest {

    @GameTest(templateName = EMPTY_STRUCTURE)
    public void test_component_conversion(TestContext context) {
        // Enhanced context with Forgero utilities
        var ctx = forgero(context);

        // Look up and convert components
        var component = ctx.component("forgero:iron-pickaxe_head").orElseThrow();
        var stack = ctx.toStack(component).orElseThrow();

        // Fluent ItemStack assertions
        assertThat(stack)
            .isNotEmpty()
            .hasItem(Items.IRON_PICKAXE)
            .isDamageable()
            .convertsToComponent();

        // Create test player with fluent API
        ServerPlayerEntity player = PlayerFactory.create(context)
            .at(1, 64, 1)
            .holding(stack)
            .survival()
            .build();

        ctx.complete();
    }
}
```

**Documentation**:
- See `modules/core/test-common/README.md` for core utilities
- See `modules/mc/test-common/README.md` for MC utilities

## Mixins

Mixins are used to inject into Minecraft code. Mixin configuration files (`.mixin.json`) are located in `src/main/resources/` and referenced in `fabric.mod.json`.

**Example**: `modules/mc/properties/src/main/resources/properties.mixin.json`

**Important**:
- Always check `!world.isClient` for server-only logic
- Use injection points carefully to avoid conflicts
- Test both client and server sides

## Common Development Patterns

### Adding a New Effect Handler

1. **Create the handler** in `modules/mc/properties/src/main/java/com/sigmundgranaas/forgero/effects/entity/`
2. **Implement** `EntityEffectHandler` or `ContextualEffectHandler`
3. **Define the codec** (see `docs/guides/creating-content-packs.md` for patterns)
4. **Register in plugin** static block
5. **Add plugin to** `fabric.mod.json` if new
6. **Use in JSON** with `"type": "forgero:your_effect"`

### Adding a New Material

1. Create JSON file in `content/minecraft-vanilla-materials/src/main/resources/data/forgero/materials/`
2. Define type, name, tags, host identifiers
3. Add properties and attributes as needed
4. Use conditional application for slot-specific behaviors

### Working with Components

Components are immutable - always create new instances:
```java
// Wrong
component.setName("new name");

// Correct
Component updated = component.with(builder -> builder.name("new name"));
```

## Important Context Documents

Documentation is organized in `docs/` by purpose. See **[docs/README.md](docs/README.md)** for the full index.

### Guides (How-To)
- **`docs/guides/creating-content-packs.md`** - Comprehensive JSON API guide with examples
- **`docs/guides/best-practices.md`** - Common patterns and pitfalls

### Reference
- **`docs/reference/quick-reference.md`** - Quick lookup tables for JSON APIs
- **`docs/reference/json-api/`** - Detailed JSON type documentation

### Architecture
- **`docs/architecture/attribute-resolution.md`** - Attribute computation pipeline
- **`docs/architecture/tag-system.md`** - Graph-based tag system (DAG)
- **`docs/architecture/data-pipeline.md`** - How content packs are loaded

### Root-Level
- **`README.md`** - Project overview

## Version Information

- **Minecraft Version**: 1.20.1
- **Java Version**: 17
- **Fabric Loader**: See module-specific `gradle.properties`
- **Fabric API**: 0.92.2+1.20.1
- **Yarn Mappings**: 1.20.1+build.10
- **Fabric Loom**: 1.9-1.10-SNAPSHOT (varies by module)

## Git Workflow

- **Main Branch**: `1.20` (use this for PRs)
- **Current Branch**: `feature/overhauled-selection-filter-and-interfaces`
- The repository uses semantic versioning
- Version is derived from git tags via `build.gradle`

## Access Wideners

Forgero modules may use access wideners to access Minecraft internals when necessary. These are configured in the `loom` block of module-specific `build.gradle` files.

## Additional Resources

- **Wiki**: https://github.com/sigmundgranaas/forgero/wiki
- **Discord**: https://discord.gg/3vK7ZwEDex
- **Issues**: https://github.com/sigmundgranaas/forgero/issues
- **CurseForge**: https://www.curseforge.com/minecraft/mc-mods/forgero

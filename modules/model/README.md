# Forgero Model Module

Platform-agnostic model and texture system for Forgero. Handles JSON-based model definitions, layer composition, and palette-based texture generation.

## Overview

**Purpose**: Separate presentation logic (models, textures) from data logic (components, properties).

**Key Features**:
- JSON-driven model definitions
- Layer-based texture composition
- Template generation with placeholder resolution
- Palette-based texture generation

**Dependencies**: `modules/core`, DataFixerUpper (Mojang Codec)

## Model Types

### TextureModel - Simple Static Texture

```json
{
  "type": "forgero:texture_model",
  "texture": "forgero:item/diamond-gem"
}
```

### CompositeModel - Layered Composition

```json
{
  "type": "forgero:composite_model",
  "layers": [
    {
      "order": 10,
      "textures": {
        "default": "forgero:item/oak-handle"
      }
    }
  ]
}
```

### CompositeModel with Slots

```json
{
  "type": "forgero:composite_model",
  "slots": [
    {
      "id": "head",
      "order": 20,
      "renderer": { "type": "forgero:component" }
    },
    {
      "id": "handle",
      "order": 10,
      "renderer": { "type": "forgero:component" }
    }
  ]
}
```

## JSON Schema

### ModelDTO

```typescript
{
  "id"?: string,              // Optional model identifier
  "type": string,             // Required: model type
  "parent"?: string,          // Parent model inheritance
  "target"?: string,          // Target component
  "context"?: string,         // Contextual model context
  "texture"?: string,         // Single texture (texture_model)
  "layers"?: LayerDTO[],      // Texture layers
  "slots"?: SlotDTO[],        // Child component slots
  "mountPoints"?: MountPointDTO[],
  "display"?: object          // Minecraft display transforms
}
```

### LayerDTO

```typescript
{
  "order": number,            // Render order (lower = bottom)
  "textures": TexturesDTO,    // Texture configuration
  "offset"?: [number, number] // Pixel offset [x, y]
}
```

### TexturesDTO

```typescript
{
  "default": string,          // Default texture
  "variants"?: VariantDTO[]   // Conditional variants
}
```

### VariantDTO

```typescript
{
  "predicate": PredicateDTO[], // Conditions (AND logic)
  "texture"?: string,          // Texture (mutually exclusive with model)
  "model"?: string,            // Model (mutually exclusive with texture)
  "offset"?: [number, number]
}
```

### PredicateDTO

```typescript
{
  "type": string,  // "forgero:root_tag" | "forgero:child_tag" | "forgero:bow_pulling" | "forgero:bow_pull"
  "tag"?: string,  // For tag predicates
  "pull"?: number, // For bow_pull (0.0-1.0)
  "pulling"?: boolean // For bow_pulling
}
```

### SlotDTO

```typescript
{
  "id": string,               // Slot identifier (matches component part ID)
  "order": number,            // Render order
  "renderer": RendererDTO,
  "mount"?: string,           // Parent mount point
  "child_mount"?: string      // Child mount point
}
```

### RendererDTO

```typescript
{
  "type": string,    // "forgero:component" | "forgero:default"
  "context"?: string // Context for contextual models
}
```

### MountPointDTO

```typescript
{
  "name": string,
  "position"?: [number, number] // Default: [0, 0]
}
```

## Texture Variants

### Conditional Textures

```json
{
  "textures": {
    "default": "forgero:item/oak-handle",
    "variants": [
      {
        "predicate": [
          { "type": "forgero:root_tag", "tag": "forgero:pickaxe" }
        ],
        "texture": "forgero:item/oak-pickaxe_handle"
      }
    ]
  }
}
```

### Predicate Types

**root_tag** - Root component has tag:
```json
{ "type": "forgero:root_tag", "tag": "forgero:pickaxe" }
```

**child_tag** - Child component has tag:
```json
{ "type": "forgero:child_tag", "tag": "forgero:reinforced" }
```

**bow_pulling** - Bow is being pulled:
```json
{ "type": "forgero:bow_pulling", "pulling": true }
```

**bow_pull** - Bow pull amount:
```json
{ "type": "forgero:bow_pull", "pull": 0.65 }
```

## Template Generation

### Part Model Template

Generates models for all materials matching a tag.

```json
{
  "type": "forgero:part_model_template",
  "target": {
    "tag": "forgero:wood"
  },
  "model": {
    "id": "forgero:parts/{target.name}-handle",
    "type": "forgero:composite_model",
    "layers": [
      {
        "order": 20,
        "template": "forgero:texture_templates/parts/handle",
        "palette": "forgero:palettes/{target.name}",
        "output": "forgero:item/{target.name}-handle"
      }
    ]
  }
}
```

**Generates**: `oak-handle`, `birch-handle`, `spruce-handle`, etc.

### Placeholders

Format: `{component.property.nested}`

**Available placeholders**:
- `{target}` → Target component name
- `{target.name}` → Target name (explicit)
- `{material}` → Material component name
- `{head.material.name}` → Nested traversal

**Special handling**:
- `_shape` suffix automatically stripped: `iron_shape` → `iron`
- Unknown placeholders kept as-is: `{unknown}` → `{unknown}`

### Texture Generation

**Template** (grayscale PNG) + **Palette** (color strip PNG) → **Output** (colored texture)

```json
{
  "template": "forgero:texture_templates/parts/handle",
  "palette": "forgero:palettes/{target.name}",
  "output": "forgero:item/{target.name}-handle"
}
```

**Process**:
1. Load grayscale template
2. Load palette color strip
3. Map template grayscale values to palette colors
4. Write output texture

## Model Extensions

Extensions add visual elements to existing models without modifying the original files.

### ModelExtensionDTO

```typescript
{
  "type": "forgero:model_extension",  // Required
  "target": string,                    // Model ID to extend (required)
  "priority"?: number,                 // Merge priority (default: 0)
  "layers"?: LayerDTO[],               // Layers to append
  "slots"?: SlotDTO[],                 // Slots to add/override
  "mount_points"?: MountPointDTO[]     // Mount points to add/override
}
```

### Merge Semantics

Extensions are applied in priority order (lowest first).

| Field | Strategy |
|-------|----------|
| `layers` | Appended after target layers |
| `slots` | By ID: extension wins (warning logged for duplicates) |
| `mount_points` | By name: extension wins (warning logged for duplicates) |

### Extension Examples

**Adding an overlay layer**:
```json
{
  "type": "forgero:model_extension",
  "target": "forgero:parts/iron-pickaxe_head",
  "priority": 100,
  "layers": [
    {
      "order": 50,
      "textures": {
        "default": "forgero:item/overlays/dye_overlay",
        "variants": [
          {
            "predicate": [{ "type": "forgero:root_tag", "tag": "forgero:dyed" }],
            "texture": "forgero:item/overlays/dye_overlay_active"
          }
        ]
      }
    }
  ]
}
```

**Adding a slot to equipment**:
```json
{
  "type": "forgero:model_extension",
  "target": "forgero:equipment/iron-pickaxe",
  "slots": [
    {
      "id": "dye_slot",
      "order": 5,
      "renderer": { "type": "forgero:component" }
    }
  ]
}
```

**Adding a mount point**:
```json
{
  "type": "forgero:model_extension",
  "target": "forgero:parts/handle",
  "mount_points": [
    {
      "name": "charm_mount",
      "position": [8, 2]
    }
  ]
}
```

### Multiple Extensions

When multiple extensions target the same model, they are merged in priority order:

```json
// Extension A (priority: 0) - applied first
{ "type": "forgero:model_extension", "target": "forgero:parts/iron-head", "priority": 0,
  "layers": [{ "order": 10, "textures": { "default": "mod_a:overlay" } }] }

// Extension B (priority: 100) - applied second
{ "type": "forgero:model_extension", "target": "forgero:parts/iron-head", "priority": 100,
  "layers": [{ "order": 20, "textures": { "default": "mod_b:overlay" } }] }

// Result: iron-head has both layers appended (mod_a first, then mod_b)
```

## File Locations

```
assets/forgero/
├── forgero_models/          # Concrete model definitions
│   ├── parts/              # Part models
│   ├── equipment/          # Tool/weapon models
│   └── upgrades/           # Upgrade models
├── model_templates/         # Generation templates
│   ├── parts/
│   └── equipment/
├── texture_templates/       # Grayscale PNG templates
│   └── parts/
├── palettes/               # Color palette PNGs
│   └── {material}.png
└── textures/               # Generated/static textures
    └── item/
```

## Examples

### Multi-Layer Composite

```json
{
  "type": "forgero:composite_model",
  "layers": [
    {
      "order": 10,
      "textures": { "default": "forgero:item/base" }
    },
    {
      "order": 20,
      "textures": { "default": "forgero:item/detail" },
      "offset": [0, 2]
    }
  ]
}
```

### Contextual Model

**Base**: `iron-pickaxe_head.json`
```json
{
  "type": "forgero:composite_model",
  "layers": [
    { "order": 20, "textures": { "default": "forgero:item/iron-pickaxe_head" } }
  ]
}
```

**Reinforced**: `iron-pickaxe_head-reinforced.json`
```json
{
  "type": "forgero:contextual_model",
  "target": "forgero:iron-pickaxe_head",
  "context": "reinforced",
  "layers": [
    { "order": 20, "textures": { "default": "forgero:item/iron-pickaxe_head" } },
    { "order": 25, "textures": { "default": "forgero:item/reinforcement_overlay" } }
  ]
}
```

### Bow with Pull States

```json
{
  "type": "forgero:composite_model",
  "layers": [
    {
      "order": 20,
      "textures": {
        "default": "forgero:item/oak-bow",
        "variants": [
          {
            "predicate": [
              { "type": "forgero:bow_pulling", "pulling": true },
              { "type": "forgero:bow_pull", "pull": 0.65 }
            ],
            "texture": "forgero:item/oak-bow_pulling_0"
          },
          {
            "predicate": [
              { "type": "forgero:bow_pull", "pull": 0.9 }
            ],
            "texture": "forgero:item/oak-bow_pulling_1"
          }
        ]
      }
    }
  ]
}
```

### Equipment Template

```json
{
  "type": "forgero:equipment_model_template",
  "target": { "tag": "forgero:pickaxe" },
  "model": {
    "id": "forgero:{target.name}",
    "type": "forgero:composite_model",
    "slots": [
      { "id": "head", "order": 20, "renderer": { "type": "forgero:component" } },
      { "id": "handle", "order": 10, "renderer": { "type": "forgero:component" } },
      { "id": "binding", "order": 30, "renderer": { "type": "forgero:component" } }
    ]
  }
}
```

## API Usage

```java
// Load model
ModelProvider provider = /* injected */;
Optional<Model> model = provider.provide(
    OpenIdentifier.of("forgero", "iron-pickaxe")
);

// Generate models from templates
ModelGenerator generator = /* injected */;
ModelGenerationResult result = generator.generate();
Map<OpenIdentifier, ModelDTO> models = result.generatedModels();
List<TextureGenerationTask> textureTasks = result.textureGenerationTasks();
```

## Testing

```bash
# Run all tests
./gradlew :modules:model:test

# Run specific test
./gradlew :modules:model:test --tests "ModelCodecsTest"
```

See [TEST_PLAN.md](./TEST_PLAN.md) and [TEST_COVERAGE_SUMMARY.md](./TEST_COVERAGE_SUMMARY.md) for test documentation.

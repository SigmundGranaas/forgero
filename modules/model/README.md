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

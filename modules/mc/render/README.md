# Forgero MC Render Module

Client-side rendering: dynamic model resolution, texture generation, and armor rendering.

## ForgeroClient

Holds all client services in a volatile record for atomic hot-reload:

```java
public class ForgeroClient {
    public static volatile ClientServices services;

    public record ClientServices(
        ItemModelRegistry modelRegistry,
        ArmorModelRegistry armorModelRegistry,
        Function<ItemStack, Optional<Component>> itemToComponent,
        ComponentRegistry componentRegistry,
        ForgeroArmorTextureManager armorTextureManager,
        ForgeroArmorModelManager armorModelManager
    ) { }
}
```

Access after initialization:
```java
ForgeroClient.ClientServices services = ForgeroClient.services;
if (services != null) {
    Optional<Model> model = services.modelRegistry().find(componentId);
}
```

## Two-Phase Initialization

1. **Synchronous pre-load** (startup): Loads models before ModelLoader creates
2. **Hot-reload** (F3+T): Rebuilds all models via `ForgeroModelResourceListener`

## Item Model Pipeline

```
ForgeroModelProvider (ModelResolver)
    ↓ resolveModel()
UnbakedForgeroModel
    ↓ bake()
BakedForgeroModel
    ↓ getOverrides()
ForgeroItemModelOverrides → selects model per ItemStack
```

### ForgeroModelProvider

Intercepts model loading for Forgero items:

```java
public class ForgeroModelProvider implements ModelResolver {
    @Override
    public UnbakedModel resolveModel(Context context) {
        // Convert minecraft:item/iron-sword → forgero:iron-sword
        // Look up in modelRegistry
        // Return UnbakedForgeroModel or null for vanilla handling
    }
}
```

### ForgeroItemModelOverrides

Per-stack model selection (handles modified components):

```java
@Override
public BakedModel apply(BakedModel model, ItemStack stack, ...) {
    Optional<Component> component = itemToComponent.apply(stack);
    // Cache and return component-specific baked model
}
```

## Model Transform Priority

1. `display` block in model JSON (highest)
2. `parent` model transforms
3. Fallback to `minecraft:item/generated`

## Armor Rendering

### ForgeroArmorModelManager

Loads and caches armor models:

```java
public <T> Optional<EntityModel<T>> getModel(Identifier modelId, EquipmentSlot slot, ...) {
    // Uses PLAYER_INNER_ARMOR for legs, PLAYER_OUTER_ARMOR for others
}
```

### ForgeroArmorTextureManager

Maps components to texture identifiers:

```java
public Optional<Identifier> getTexture(Component component, EquipmentSlot slot) {
    // Looks up texture from model registry
}
```

## Texture Generation

`RuntimeTextureWriter` writes generated textures to the Dynamic Resource Pack:

```java
public class RuntimeTextureWriter implements TextureWriter {
    @Override
    public void write(String textureId, BufferedImage image) {
        resourcePack.addTexture(id, image);
    }
}
```

## Hot Reload

Press **F3+T** to reload resources:
1. `ForgeroModelResourceListener` rebuilds models
2. `ForgeroClient.services` atomically swapped
3. Models re-baked on next render

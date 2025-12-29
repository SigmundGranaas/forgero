# Forgero Loader Module

Central orchestrator for Minecraft integration. Provides the plugin system, data loading, and primary API access.

## Accessing Services

```java
// After initialization
ForgeroServices services = ForgeroApi.services();
ComponentConverter converter = services.converter();
ComponentRegistry registry = services.componentRegistry();
Resolver resolver = services.resolver();

// Safe initialization callback (replays if already initialized)
ForgeroInitializedCallback.registerAndReplay(services -> {
    // Safe to use services here
});
```

## ForgeroServices

```java
public interface ForgeroServices {
    ComponentConverter converter();         // ItemStack ↔ Component
    ComponentRegistry componentRegistry();  // Look up by ID
    TaggedRegistry<Component> taggedComponents();  // Query by tags
    Resolver resolver();                    // Resolve properties
    TagResolver tagResolver();              // Tag inheritance
    ComponentNbtConverter nbtConverter();   // NBT serialization
    SlotManager slotManager();              // Upgrade slots
}
```

## Plugin System

Register plugins via `fabric.mod.json` entrypoints:

```json
{
  "entrypoints": {
    "forgero:data_plugin": ["com.example.MyDataPlugin"],
    "forgero:item_registration_plugin": ["com.example.MyItemPlugin"],
    "forgero:post_load_plugin": ["com.example.MyPostPlugin"]
  }
}
```

### DataPlugin

Register codecs and item creators during initialization:

```java
public class MyPlugin implements DataPlugin {
    @Override
    public void register(PluginRegistrationContext context) {
        // Property codec
        context.registerPropertyCodec(MyProperty.KEY, MyProperty.CODEC);

        // Condition codec
        context.registerDynamicConditionCodec("mymod:custom", MyCondition.CODEC);

        // Item creator
        context.registerItemCreator("mymod:custom_item", component ->
            new MyCustomItem(component)
        );
    }

    @Override
    public String getId() { return "mymod:my-plugin"; }
}
```

### PostLoadPlugin

Execute code after Forgero is fully initialized:

```java
public class MyPostPlugin implements PostLoadPlugin {
    @Override
    public void onLoad(DataLoadingContext context) {
        ComponentRegistry registry = context.componentRegistry();
        // Post-initialization logic
    }

    @Override
    public String getId() { return "mymod:post-plugin"; }
}
```

## Initialization Pipeline

11-phase pipeline:
1. Register dynamic item classes
2. Discover plugins via entrypoints
3. Load tags
4. Register plugin codecs/conditions
5. Register slot codecs
6. Create data config
7. Load data bundle (components, models)
8. Initialize core services
9. Setup item registration callbacks
10. Register items to Minecraft
11. Fire `ForgeroInitializedCallback`

## Built-in Item Creators

| Type ID | Item Class |
|---------|------------|
| `forgero:pickaxe_item` | ForgeroPickaxeItem |
| `forgero:armor_item` | ForgeroArmorItem |
| `forgero:bow_item` | ForgeroBowItem |
| `forgero:part_item` | ForgeroPartItem |

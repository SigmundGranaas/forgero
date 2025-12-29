# Forgero MC Bows Module

Bow implementation with data-driven projectile handlers.

## BowPlugin

Registers bow item creator and projectile handlers:

```java
public class BowPlugin implements DataPlugin {
    static {
        UseInteractionPropertiesPlugin.registerHandler(MountProjectileHandler.TYPE, ...);
        UseInteractionPropertiesPlugin.registerHandler(LaunchProjectileHandler.TYPE, ...);
        UseInteractionPropertiesPlugin.registerHandler(ConsumeProjectileHandler.TYPE, ...);
    }

    @Override
    public void register(PluginRegistrationContext context) {
        context.registerItemCreator("forgero:bow_item", this::createBowItem);
    }
}
```

## ForgeroBowItem

```java
public class ForgeroBowItem extends BowItem implements ForgeroHostItem {
    private final Component component;

    @Override
    public Component getForgeroComponent() { return component; }
}
```

**Key design**: No direct use handling - all behavior is data-driven via `UseInteractionProperty`.

## Projectile Handlers

### MountProjectileHandler

Initiates bow use (checks for arrows, sets current hand):

```json
{ "type": "forgero:mount_projectile" }
```

### LaunchProjectileHandler

Fires projectile on release with attribute-driven power/accuracy:

```json
{
  "type": "forgero:launch_projectile",
  "base_power": 3.0,
  "base_divergence": 1.0
}
```

Resolves `forgero:draw_power` and `forgero:accuracy` from component. Respects Power, Punch, Flame, Infinity enchantments.

### ConsumeProjectileHandler

Consumes arrow from inventory (respects Infinity, creative mode):

```json
{ "type": "forgero:consume_projectile" }
```

## Complete Bow JSON

```json
{
  "id": "forgero:oak-bow",
  "properties": {
    "forgero:attributes": [
      {"type": "forgero:durability", "value": 384},
      {"type": "forgero:draw_power", "value": 3.5},
      {"type": "forgero:accuracy", "value": 75}
    ],
    "forgero:use_interaction": {
      "max_use_time": 72000,
      "on_start": [{"type": "forgero:mount_projectile"}],
      "on_release": [{"type": "forgero:launch_projectile", "base_power": 3.0}]
    }
  },
  "create": { "type": "forgero:bow_item" }
}
```

## Attribute Effects

| Attribute | Effect |
|-----------|--------|
| `forgero:draw_power` | Projectile velocity multiplier |
| `forgero:accuracy` | 0-100, where 100 = perfect accuracy |

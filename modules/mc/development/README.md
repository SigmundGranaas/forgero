# Forgero MC Development Module

Development tools for testing and debugging Forgero components in-game.

## Features

- **Component Upgrade Screen** (U key): Visual drag-and-drop upgrade management
- **Component Inspector**: Detailed text report of component structure
- **Right-Click Slotting**: In-game upgrade installation via off-hand

## Keybinding

| Key | Action |
|-----|--------|
| `U` | Opens ComponentUpgradeScreen for main hand item |

## DevClientPlugin

Registers the U keybinding:

```java
public class DevClientPlugin implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        viewUpgradesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.forgero.view_upgrades", GLFW.GLFW_KEY_U, "category.forgero.dev"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (viewUpgradesKey.wasPressed()) {
                converter.toComponent(client.player.getMainHandStack())
                    .filter(CustomizableComponent.class::isInstance)
                    .map(CustomizableComponent.class::cast)
                    .filter(c -> !c.getUpgradeSlots().isEmpty())
                    .ifPresent(c -> client.setScreen(new ComponentUpgradeScreen(c, converter, Hand.MAIN_HAND)));
            }
        });
    }
}
```

## DevPlugin

Registers server-side handlers:

```java
public class DevPlugin implements PostLoadPlugin {
    @Override
    public void onDataLoaded(DataLoadingContext context) {
        // Right-click slotting
        UseItemCallback.EVENT.register(new ComponentSlottingHandler(context.converter())::handle);

        // Client→server component sync
        ServerPlayNetworking.registerGlobalReceiver(UpgradeComponentPacket.ID, ...);
    }
}
```

## ComponentUpgradeScreen

- 3D rotating item display at center
- Circular slot layout around center
- Drag-and-drop from inventory bar
- Left-click to pick up/place, right-click to quick-remove
- Changes synced to server on close via `UpgradeComponentPacket`

## ComponentInspector

Generates text reports:

```
============================================
  COMPONENT REPORT: forgero:iron-sword
============================================

-- FINAL CALCULATED STATS --
- Attack Damage: 6.0
- Durability: 250

-- COMPOSITION --
forgero:iron-sword
├─ Part: forgero:iron-sword_blade
│  └─ Slot: gem [Empty]
└─ Part: forgero:oak-handle

-- ATTRIBUTE BREAKDOWN --
- Attack Damage: 6.0 = (+4.0 [blade] +2.0 [handle])
```

## ComponentSlottingHandler

In-game upgrade installation:

1. Hold Forgero tool in main hand
2. Hold compatible upgrade in off-hand
3. Right-click to install
4. Upgrade consumed (survival mode)

## Enabling

Add to `fabric.mod.json`:

```json
{
  "entrypoints": {
    "client": ["com.sigmundgranaas.forgero.tools.DevClientPlugin"],
    "forgero:post_load_plugin": ["com.sigmundgranaas.forgero.tools.DevPlugin"]
  }
}
```

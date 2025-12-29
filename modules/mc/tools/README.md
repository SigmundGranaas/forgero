# Forgero MC Tools Module

Tool and part item implementations bridging Forgero components with Minecraft tool mechanics.

## ToolPlugin

Registers item creators:

```java
public class ToolPlugin implements DataPlugin {
    @Override
    public void register(PluginRegistrationContext context) {
        context.registerItemCreator("forgero:pickaxe_item", this::createPickaxeItem);
        context.registerItemCreator("forgero:part_item", this::createPartItem);
    }
}
```

| Creator ID | Item Class |
|------------|------------|
| `forgero:pickaxe_item` | ForgeroPickaxeItem |
| `forgero:part_item` | ForgeroPartItem |

## ForgeroPickaxeItem

Extends `PickaxeItem` with Forgero component:

```java
public class ForgeroPickaxeItem extends PickaxeItem implements ForgeroHostItem {
    private final Component component;

    public ForgeroPickaxeItem(ToolMaterial material, Settings settings, Component component) {
        super(material, 0, 0, settings);  // Base stats are 0
        this.component = component;
    }

    @Override
    public Component getForgeroComponent() { return component; }
}
```

**Key design**: Base attack damage/speed are 0. Actual stats are injected via `AttributeManager` mixins.

## ForgeroToolMaterial

Resolves stats from component attributes:

```java
public class ForgeroToolMaterial implements ToolMaterial {
    @Override
    public int getDurability() {
        return (int) attributes.getValue(DefaultAttributes.DURABILITY);
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return attributes.getValue(DefaultAttributes.MINING_SPEED);
    }
}
```

| Method | Forgero Attribute |
|--------|-------------------|
| `getDurability()` | `forgero:durability` |
| `getMiningSpeedMultiplier()` | `forgero:mining_speed` |
| `getAttackDamage()` | `forgero:attack_damage` |
| `getMiningLevel()` | `forgero:mining_level` |

## ForgeroPartItem

Generic item for tool parts (heads, handles, blades):

```java
public class ForgeroPartItem extends Item implements ForgeroHostItem {
    private final Component component;

    @Override
    public Component getForgeroComponent() { return component; }
}
```

## JSON Usage

```json
{
  "id": "forgero:iron-pickaxe",
  "create": { "type": "forgero:pickaxe_item" }
}

{
  "id": "forgero:iron-pickaxe_head",
  "create": { "type": "forgero:part_item" }
}
```

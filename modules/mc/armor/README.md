# Forgero MC Armor Module

Armor item implementation bridging Forgero components with Minecraft's armor system.

## ArmorPlugin

Registers armor item creator with tag-based type detection:

```java
public class ArmorPlugin implements DataPlugin {
    @Override
    public void register(PluginRegistrationContext context) {
        context.registerItemCreator("forgero:armor_item", this::createArmorItem);
    }

    private Item createArmorItem(Component component, CreateData data, Resolver resolver) {
        ArmorItem.Type armorType = determineArmorType(component);  // From tags
        ForgeroArmorMaterial material = new ForgeroArmorMaterial(component, resolver);
        return new ForgeroArmorItem(material, armorType, new Item.Settings(), component);
    }
}
```

## Tag-Based Type Detection

Armor slot is determined from component tags:

| Tag | ArmorItem.Type |
|-----|----------------|
| `helmet` | HELMET |
| `chestplate` | CHESTPLATE |
| `leggings` | LEGGINGS |
| `boots` | BOOTS |

## ForgeroArmorItem

```java
public class ForgeroArmorItem extends ArmorItem implements ForgeroHostItem {
    private final Component component;

    @Override
    public Component getForgeroComponent() { return component; }
}
```

## ForgeroArmorMaterial

Resolves stats from component attributes:

```java
public class ForgeroArmorMaterial implements ArmorMaterial {
    @Override
    public int getDurability(ArmorItem.Type type) {
        return (int) attributes.getValue(DefaultAttributes.DURABILITY);
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return (int) attributes.getValue(DefaultAttributes.ARMOR);
    }

    @Override
    public float getToughness() {
        return attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS);
    }
}
```

| Method | Forgero Attribute |
|--------|-------------------|
| `getDurability()` | `forgero:durability` |
| `getProtection()` | `forgero:armor` |
| `getToughness()` | `forgero:armor_toughness` |

## JSON Usage

```json
{
  "id": "forgero:diamond-helmet",
  "tags": ["helmet", "armor", "diamond"],
  "properties": {
    "forgero:attributes": [
      {"type": "forgero:durability", "value": 363},
      {"type": "forgero:armor", "value": 3},
      {"type": "forgero:armor_toughness", "value": 2.0}
    ]
  },
  "create": { "type": "forgero:armor_item" }
}
```

**Required**: One of `helmet`, `chestplate`, `leggings`, or `boots` tag must be present.

package com.sigmundgranaas.forgeroforge.item;

import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items {
    public static Item EMPTY_REPAIR_KIT;

    static {
        EMPTY_REPAIR_KIT = Registry.register(Registry.ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new Item.Settings().group(ItemGroup.MISC)));
    }
}

package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.Forgero;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items {
    public static Item EMPTY_REPAIR_KIT;

    static {
        EMPTY_REPAIR_KIT = Registry.register(Registry.ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new FabricItemSettings().group(ItemGroup.TOOLS)));
    }
}

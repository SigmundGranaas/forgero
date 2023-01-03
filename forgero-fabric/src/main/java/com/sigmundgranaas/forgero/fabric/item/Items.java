package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.Forgero;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;


public class Items {
    public static Item EMPTY_REPAIR_KIT;

    static {
        EMPTY_REPAIR_KIT = Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, "empty_repair_kit"), new Item(new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(EMPTY_REPAIR_KIT));
    }
}

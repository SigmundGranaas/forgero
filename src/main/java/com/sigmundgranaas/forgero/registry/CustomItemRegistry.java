package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CustomItemRegistry {
    public void register() {
        Registry.register(Registry.ITEM, new Identifier(ForgeroInitializer.MOD_NAMESPACE, "magnetico"), new Item(new Item.Settings().group(ItemGroup.MISC)));
    }
}

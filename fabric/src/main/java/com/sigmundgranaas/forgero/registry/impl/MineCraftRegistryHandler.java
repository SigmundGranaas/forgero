package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.registry.RegistryHandler;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MineCraftRegistryHandler implements RegistryHandler {
    @Override
    public void register(Item item, Identifier id) {
        Registry.register(Registry.ITEM, id, item);
    }
}

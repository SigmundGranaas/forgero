package com.sigmundgranaas.forgero.registry;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public interface RegistryHandler {
    void register(Item item, Identifier id);
}

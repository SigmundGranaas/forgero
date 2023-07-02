package com.sigmundgranaas.forgero.minecraft.common.registry.registrar;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public interface Registrar {
	default void registerItem(Registry<Item> registry) {
	}

	default void register() {
	}
}

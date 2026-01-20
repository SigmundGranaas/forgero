package com.sigmundgranaas.forgero.minecraft.common.registry.registrar;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;

public interface Registrar {
	default void registerItem(Registry<Item> registry) {
	}

	default void register() {
	}
}

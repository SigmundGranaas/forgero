package com.sigmundgranaas.forgero.minecraft.common.registry;

@FunctionalInterface
public interface Registerable<T> {
	void register(GenericRegistry<T> registry);
}

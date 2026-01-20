package com.sigmundgranaas.forgero.core.registry;

@FunctionalInterface
public interface Registerable<T> {
	void register(GenericRegistry<T> registry);
}

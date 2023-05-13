package com.sigmundgranaas.forgero.fabric.initialization;

import net.minecraft.util.registry.Registry;

public interface Registrar<T> {
	void register(Registry<T> registry);
}

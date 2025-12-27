package com.sigmundgranaas.forgero.fabric.resources.dynamic;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;

@FunctionalInterface
public interface DynamicResourceGenerator {
	void generate(DynamicResourcePack pack);

	default boolean enabled() {
		return true;
	}
}

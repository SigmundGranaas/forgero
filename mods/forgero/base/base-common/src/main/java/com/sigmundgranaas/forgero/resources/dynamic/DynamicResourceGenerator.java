package com.sigmundgranaas.forgero.resources.dynamic;

import com.sigmundgranaas.forgero.dynamicresourcepack.resource.DynamicResourcePackImpl;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DynamicResourceGenerator {
	void generate(@NotNull DynamicResourcePackImpl pack);

	default boolean enabled() {
		return true;
	}
}

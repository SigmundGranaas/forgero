package com.sigmundgranaas.forgero.core.customdata;

@FunctionalInterface
public interface DataSupplier {
	DataContainer customData();

	default boolean hasData() {
		return !(customData() instanceof EmptyContainer);
	}
}

package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

/**
 * A container for custom data.
 * Will be used to store custom data for resources.
 * <p>
 * Containers will be merged when resources are inherited.
 *
 * @see DataVisitor for how to extract data from a container.
 * @see DataSupplier for how to get a container from a resource.
 */
public interface DataContainer {
	static DataContainer empty() {
		return new EmptyContainer();
	}

	static DataContainer transitiveMerge(DataContainer a, DataContainer b) {
		return a.merge(b, Context.TRANSITIVE);
	}

	Optional<Integer> getInteger(String key);

	Optional<Float> getFloat(String key);

	Optional<String> getString(String key);

	Optional<Boolean> getBoolean(String key);

	default <T> Optional<T> accept(DataVisitor<T> visitor) {
		return visitor.visit(this);
	}

	DataContainer merge(DataContainer other);

	DataContainer merge(DataContainer other, Context context);
}
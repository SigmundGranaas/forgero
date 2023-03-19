package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

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

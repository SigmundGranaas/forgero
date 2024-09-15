package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.Target;

/**
 * An empty data container. Used as a default value for custom data containers.
 */
public class EmptyContainer implements DataContainer {
	public static EmptyContainer EMPTY = new EmptyContainer();

	private EmptyContainer() {
	}

	@Override
	public Optional<Integer> getInteger(String key) {
		return Optional.empty();
	}

	@Override
	public Optional<Float> getFloat(String key) {
		return Optional.empty();
	}

	@Override
	public Optional<String> getString(String key) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return Optional.empty();
	}

	@Override
	public DataContainer merge(DataContainer other) {
		return other;
	}

	@Override
	public DataContainer merge(DataContainer other, Context context, Target target) {
		return other;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof EmptyContainer && o == EMPTY;
	}

	@Override
	public int hashCode() {
		return 1;
	}
}

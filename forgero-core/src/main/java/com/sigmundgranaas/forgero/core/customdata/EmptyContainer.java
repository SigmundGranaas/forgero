package com.sigmundgranaas.forgero.core.customdata;

import java.util.Optional;

public class EmptyContainer implements DataContainer {
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
	public DataContainer merge(DataContainer other, Context context) {
		return other;
	}
}

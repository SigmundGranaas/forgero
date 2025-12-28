package com.sigmundgranaas.forgero.recipegen.impl.variable;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverterRegistry;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of {@link VariableConverterRegistry}.
 */
public class VariableConverterRegistryImpl implements VariableConverterRegistry {

	private final Map<String, VariableConverter<?>> converters = new ConcurrentHashMap<>();

	@Override
	public <T> ConverterReference<T> register(String id, VariableConverter<T> converter) {
		converters.put(id, converter);
		return new ConverterReferenceImpl<>(id, converter);
	}

	@Override
	public Optional<VariableConverter<?>> get(String id) {
		return Optional.ofNullable(converters.get(id));
	}

	@Override
	public Collection<?> convert(JsonElement element) {
		return converters.values().stream()
				.filter(c -> c.matches(element))
				.max(Comparator.comparingInt(VariableConverter::priority))
				.map(c -> c.convert(element))
				.orElseThrow(() -> new IllegalArgumentException(
						"No converter found for JSON element: " + element));
	}

	@Override
	public Collection<?> convert(String id, JsonElement element) {
		VariableConverter<?> converter = converters.get(id);
		if (converter == null) {
			throw new IllegalArgumentException("No converter registered with id: " + id);
		}
		return converter.convert(element);
	}

	private record ConverterReferenceImpl<T>(String id, VariableConverter<T> converter)
			implements ConverterReference<T> {
	}
}

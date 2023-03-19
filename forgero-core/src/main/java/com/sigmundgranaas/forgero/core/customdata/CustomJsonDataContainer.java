package com.sigmundgranaas.forgero.core.customdata;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class CustomJsonDataContainer implements DataContainer {
	private final Map<String, JsonElement> customData;

	public CustomJsonDataContainer(Map<String, JsonElement> customData) {
		this.customData = customData;
	}

	public static DataContainer of(Map<String, JsonElement> customData) {
		return new CustomJsonDataContainer(customData);
	}

	@Override
	public Optional<Integer> getInteger(String key) {
		return simpleValues(key, Integer.class);
	}

	@Override
	public Optional<Float> getFloat(String key) {
		return simpleValues(key, Float.class);
	}

	@Override
	public Optional<String> getString(String key) {
		return simpleValues(key, String.class);
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return simpleValues(key, Boolean.class);
	}

	private <T> Optional<T> simpleValues(String key, Class<T> type) {
		Optional<T> element = getObject(key, type);
		if (element.isPresent()) {
			return element;
		} else {
			return contextValue(key, type).map(ContextAwareData::value);
		}
	}

	private <T> Optional<ContextAwareData<T>> contextValue(String key, Class<T> type) {
		var dataOptional = getObject(key, ContextAwareData.class);
		if (dataOptional.isPresent()) {
			var data = dataOptional.get();
			return convertFromJsonElement(new Gson().toJsonTree(data.value()), type).map(value -> new ContextAwareData<>(data.context(), value));
		}
		return Optional.empty();
	}

	public <T> Optional<T> getObject(String key, Class<T> type) {
		Optional<JsonElement> element = Optional.ofNullable(customData.get(key));
		if (element.isEmpty()) {
			return Optional.empty();
		}
		return convertFromJsonElement(element.get(), type);
	}

	private <T> Optional<T> convertFromJsonElement(JsonElement element, Class<T> type) {
		try {
			return Optional.of(new Gson().fromJson(element, type));
		} catch (JsonSyntaxException e) {
			return Optional.empty();
		}
	}

	@Override
	public CustomJsonDataContainer merge(DataContainer other) {
		if (other instanceof CustomJsonDataContainer customJsonDataContainer) {
			Map<String, JsonElement> combinedMap = ImmutableMap.<String, JsonElement>builder()
					.putAll(customData)
					.putAll(customJsonDataContainer.getCustomData())
					.build();
			return new CustomJsonDataContainer(combinedMap);
		}
		return this;
	}

	@Override
	public DataContainer merge(DataContainer other, Context context) {
		var filteredValues = merge(other).customData.entrySet().stream()
				.filter(entry -> isRightContext(entry, context))
				.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
		return new CustomJsonDataContainer(filteredValues);
	}

	private boolean isRightContext(Map.Entry<String, JsonElement> entry, Context context) {
		var convertedValue = contextValue(entry.getKey(), Object.class);
		return convertedValue.map(objectContextAwareData -> objectContextAwareData.context() == context).orElse(true);
	}

	protected Map<String, JsonElement> getCustomData() {
		return customData;
	}
}

package com.sigmundgranaas.forgero.core.customdata;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom data container that uses a {@link Map} to store data.
 * Will store data as JsonData.
 * <p>
 * Can be used to store custom data in a {@link DataSupplier}.
 * Will handle contextual merges, as well as mapping to and from Lists when running into duplicated keys.
 */
public class CustomJsonDataContainer implements DataContainer {
	@NotNull
	private final Map<String, JsonElement> customData;

	public CustomJsonDataContainer(@NotNull Map<String, JsonElement> customData) {
		this.customData = customData;
	}

	public static DataContainer of(@Nullable Map<String, JsonElement> customData) {
		if (customData == null || customData.isEmpty()) {
			return DataContainer.empty();
		}
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

	/**
	 * Tries to parse a type of data with context
	 *
	 * @param key  the key to look for
	 * @param type the type to parse
	 * @param <T>  the type of data to parse
	 * @return an optional containing the contextual data, or empty if not found
	 */
	private <T> Optional<ContextAwareData<T>> contextValue(String key, Class<T> type) {
		var dataOptional = getObject(key, ContextAwareData.class);
		if (dataOptional.isPresent()) {
			var data = dataOptional.get();
			return convertFromJsonElement(new Gson().toJsonTree(data.value()), type).map(value -> new ContextAwareData<>(data.context(), value));
		}
		return Optional.empty();
	}

	/**
	 * Tries to parse a type of data from a JsonElement
	 *
	 * @param key  the key to look for
	 * @param type the type to parse
	 * @param <T>  the type of data to parse
	 * @return an optional containing the data, or empty if not found
	 */
	public <T> Optional<T> getObject(String key, Class<T> type) {
		Optional<JsonElement> element = Optional.ofNullable(getCustomData().get(key));
		//noinspection OptionalIsPresent
		if (element.isEmpty()) {
			return Optional.empty();
		}
		return convertFromJsonElement(element.get(), type).or(() -> getObjectList(key, type).stream().findFirst());
	}

	/**
	 * Tries to parse a type of data from a JsonElement as a List
	 *
	 * @param key  the key to look for
	 * @param type the type to parse
	 * @param <T>  the type of data to parse
	 * @return A list of the available data, or an empty list if not found
	 */
	public <T> List<T> getObjectList(String key, Class<T> type) {
		Type token = TypeToken.getParameterized(List.class, type).getType();
		JsonElement element = getCustomData().get(key);
		if (element == null || element.isJsonNull()) {
			return Collections.emptyList();
		}
		Optional<List<T>> typedList = getTypedList(element, type, token);
		return typedList.orElse(Collections.emptyList());
	}


	private <T> Optional<List<T>> getTypedList(JsonElement element, Class<T> type, Type token) {
		Optional<Object> obj = convertFromJsonElement(element, token);
		if (obj.isPresent() && obj.get() instanceof List<?> list) {
			if (list.stream().allMatch(type::isInstance)) {
				@SuppressWarnings("unchecked")
				List<T> typedList = (List<T>) list;
				return Optional.of(typedList);
			}
		}
		return Optional.empty();
	}

	private <T> Optional<T> convertFromJsonElement(JsonElement element, Class<T> type) {
		try {
			return Optional.ofNullable(new Gson().fromJson(element, type));
		} catch (JsonSyntaxException e) {
			return Optional.empty();
		}
	}

	private <T> Optional<T> convertFromJsonElement(JsonElement element, Type type) {
		try {
			return Optional.ofNullable(new Gson().fromJson(element, type));
		} catch (JsonSyntaxException e) {
			return Optional.empty();
		}
	}

	/**
	 * Merges two data containers, with the other container taking precedence.
	 * Will convert the data to a list if there are duplicate keys. No filtering of contexts is done.
	 * <p>
	 *
	 * @param other the other container to merge with
	 * @return a new data container with the merged data
	 */
	@Override
	public CustomJsonDataContainer merge(DataContainer other) {
		if (other instanceof CustomJsonDataContainer customJsonDataContainer) {
			Map<String, JsonElement> combinedMap = new HashMap<>(getCustomData());
			for (Map.Entry<String, JsonElement> entry : customJsonDataContainer.getCustomData().entrySet()) {
				combinedMap.computeIfPresent(entry.getKey(), (key, value) -> mergeAsList(value, entry.getValue()));
			}
			return new CustomJsonDataContainer(combinedMap);
		}
		return this;
	}

	/**
	 * Merges two data containers, with the other container taking precedence.
	 * <p>
	 * Takes filtering into account, and will convert the data to a list if there are duplicate keys.
	 *
	 * @param other   the other container to merge with
	 * @param context the context to filter by
	 * @return a new data container with the merged data
	 */
	@Override
	public DataContainer merge(DataContainer other, Context context) {
		var filteredValues = merge(other).getCustomData().entrySet().stream()
				.filter(entry -> isRightContext(entry, context))
				.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
		return new CustomJsonDataContainer(filteredValues);
	}

	private JsonElement mergeAsList(JsonElement a, JsonElement b) {
		if (a.isJsonArray() && b.isJsonArray()) {
			JsonArray aArray = a.getAsJsonArray();
			JsonArray bArray = b.getAsJsonArray();
			aArray.addAll(bArray);
			return aArray;
		} else if (a.isJsonArray()) {
			JsonArray aArray = a.getAsJsonArray();
			aArray.add(b);
			return aArray;
		} else if (b.isJsonArray()) {
			JsonArray bArray = b.getAsJsonArray();
			bArray.add(a);
			return bArray;
		} else {
			JsonArray newArray = new JsonArray();
			newArray.add(a);
			newArray.add(b);
			return newArray;
		}
	}

	private boolean isRightContext(Map.Entry<String, JsonElement> entry, Context context) {
		var convertedValue = contextValue(entry.getKey(), Object.class);
		return convertedValue.map(objectContextAwareData -> objectContextAwareData.context() == context).orElse(true);
	}

	@NotNull
	protected Map<String, JsonElement> getCustomData() {
		return customData;
	}
}

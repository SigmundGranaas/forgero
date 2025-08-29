package com.sigmundgranaas.forgero.core.property.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A dispatch codec that handles serialization/deserialization of properties
 * based on their registered property keys and associated codecs.
 * <p />
 * This codec only processes properties with registered codecs, ignoring
 * any unknown property types.
 */
public class KeyMapDispatchCodec extends MapCodec<Map<String, List<?>>> {
	private final Map<PropertyKey<?>, Codec<? extends List<?>>> dispatchCodecMap;
	private final Map<String, PropertyKey<?>> keyLookup;

	public KeyMapDispatchCodec(Map<PropertyKey<?>, Codec<? extends List<?>>> dispatchCodecMap) {
		this.dispatchCodecMap = dispatchCodecMap;
		this.keyLookup = createKeyLookup(dispatchCodecMap);
	}

	private Map<String, PropertyKey<?>> createKeyLookup(Map<PropertyKey<?>, Codec<? extends List<?>>> dispatchCodecMap) {
		Map<String, PropertyKey<?>> lookup = new HashMap<>();
		for (PropertyKey<?> key : dispatchCodecMap.keySet()) {
			lookup.put(key.key(), key);
		}
		return lookup;
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return keyLookup.keySet().stream().map(ops::createString);
	}

	@Override
	public <T> DataResult<Map<String, List<?>>> decode(DynamicOps<T> ops, MapLike<T> input) {
		Map<String, List<?>> result = new HashMap<>();
		Map<String, DataResult<List<?>>> errors = new HashMap<>();

		// Only decode entries with registered property keys
		input.entries().forEach(entry -> {
			String key = ops.getStringValue(entry.getFirst())
					.result()
					.orElse(null);

			if (key != null) {
				PropertyKey<?> propertyKey = keyLookup.get(key);

				if (propertyKey != null) {
					// Found a registered property key, use its codec
					// CHANGE 4: Add unchecked cast when retrieving from the map
					@SuppressWarnings("unchecked") // Safe because we only put Codec<List<ActualType>> into the map
					Codec<List<?>> codec = (Codec<List<?>>) dispatchCodecMap.get(propertyKey);

					DataResult<List<?>> decodeResult = codec.decode(ops, entry.getSecond())
							.map(Pair::getFirst);

					decodeResult.result().ifPresent(value -> result.put(key, value));
					decodeResult.error().ifPresent(error -> errors.put(key, DataResult.error(error::message)));
				}
				// Skip unknown property keys - only handle registered properties
			}
		});

		// If there were any errors, return partial result with errors
		if (!errors.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder("Failed to decode properties: ");
			errors.forEach((key, error) ->
					error.error().ifPresent(e -> errorMessage.append("\n  ").append(key).append(": ").append(e.message())));
			return DataResult.error(errorMessage::toString, result);
		}

		return DataResult.success(result);
	}

	@Override
	public <T> RecordBuilder<T> encode(Map<String, List<?>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		RecordBuilder<T> builder = prefix;

		for (Map.Entry<String, List<?>> entry : input.entrySet()) {
			String key = entry.getKey();
			List<?> value = entry.getValue();
			PropertyKey<?> propertyKey = keyLookup.get(key);

			if (propertyKey != null) {
				// Use the registered codec for encoding
				// CHANGE 5: Add unchecked cast when retrieving from the map
				@SuppressWarnings("unchecked") // Safe because we only put Codec<List<ActualType>> into the map
				Codec<List<?>> codec = (Codec<List<?>>) dispatchCodecMap.get(propertyKey);

				DataResult<T> encoded = codec.encodeStart(ops, value);

				var result = encoded.result();
				if(result.isPresent()) {
					builder = builder.add(key, result.get());
				}else{
					builder = builder.withErrorsFrom(encoded);

				}
			}
			// Skip unknown properties - only encode registered property types
		}

		return builder;
	}

	/**
	 * Creates a Codec from this MapCodec for easier usage
	 */
	public Codec<Map<String, List<?>>> codec() {
		return new MapCodecCodec<>(this);
	}
}

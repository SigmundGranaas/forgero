package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public class AbstractMapCodec extends MapCodec<Map.Entry<String, Object>> {
	private final Map<String, Codec<Object>> map = new HashMap<>();

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return null;
	}

	@Override
	public <T> RecordBuilder<T> encode(Map.Entry<String, Object> entry, DynamicOps<T> ops, RecordBuilder<T> recordBuilder) {
		String key = entry.getKey();
		Object value = entry.getValue();

		// Find the appropriate codec for the value based on the key
		Codec<Object> valueCodec = map.get(key);
		if (valueCodec == null) {
			// If no codec is registered for this key, add an error
			recordBuilder.add(ops.createString(key), DataResult.error(() -> "No codec registered for key: " + key));
		} else {
			// Encode the value using the found codec and add it to the record builder
			DataResult<T> encodedValue = valueCodec.encodeStart(ops, value);
			encodedValue.resultOrPartial(System.err::println).ifPresent(encoded -> recordBuilder.add(ops.createString(key), encoded));
		}

		return recordBuilder;
	}


	@Override
	public <T> DataResult<Map.Entry<String, Object>> decode(DynamicOps<T> ops, MapLike<T> input) {
		if (input.entries().toList().size() != 1) {
			return DataResult.error(() -> "Expected a single entry for map codec");
		}

		T key = input.entries().findFirst().get().getFirst();
		if (key == null) {
			return DataResult.error(() -> "Missing key for map entry");
		}

		DataResult<String> keyResult = ops.getStringValue(key);
		if (keyResult.error().isPresent()) {
			return DataResult.error(() -> "Key is not a string: " + keyResult.error().get().message());
		}

		String keyString = keyResult.result().orElse(null);
		Codec<Object> valueCodec = map.get(keyString);
		if (valueCodec == null) {
			return DataResult.error(() -> "No codec registered for key: " + keyString);
		}

		T value = input.get(key);
		return valueCodec.parse(ops, value).map(v -> Map.entry(keyString, v));
	}
}

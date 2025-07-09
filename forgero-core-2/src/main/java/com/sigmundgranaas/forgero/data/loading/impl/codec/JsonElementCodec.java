package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

/**
 * A custom codec for serializing and deserializing {@link JsonElement} objects.
 * This is necessary because the serialization library does not provide a public,
 * reusable codec for raw JSON elements.
 */
public class JsonElementCodec implements Codec<JsonElement> {
	public static final JsonElementCodec INSTANCE = new JsonElementCodec();

	private JsonElementCodec() {}

	@Override
	public <T> DataResult<Pair<JsonElement, T>> decode(DynamicOps<T> ops, T input) {
		// Convert the input from the given DynamicOps format into a JsonElement
		// by first converting it to the library's internal Dynamic type, and then to JSON.
		JsonElement jsonElement = new Dynamic<>(ops, input).convert(JsonOps.INSTANCE).getValue();
		// The result is the JsonElement itself, and the remaining input is empty.
		return DataResult.success(Pair.of(jsonElement, ops.empty()));
	}

	@Override
	public <T> DataResult<T> encode(JsonElement input, DynamicOps<T> ops, T prefix) {
		// Convert the JsonElement into the target DynamicOps format.
		// This is done by first converting the JsonElement to a Dynamic<JsonElement>
		// and then converting it to the target ops.
		return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, input).convert(ops).getValue());
	}
}

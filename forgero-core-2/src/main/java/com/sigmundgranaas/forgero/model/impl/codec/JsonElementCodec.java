package com.sigmundgranaas.forgero.model.impl.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

/**
 * A custom codec for serializing and deserializing a {@link com.google.gson.JsonElement}.
 * This is useful for handling arbitrary, unstructured JSON data within a larger structured model,
 * essentially acting as a pass-through for sections like "display" or "overrides".
 */
public enum JsonElementCodec implements Codec<JsonElement> {
	INSTANCE;

	@Override
	public <T> DataResult<T> encode(final JsonElement input, final DynamicOps<T> ops, final T prefix) {
		if (ops instanceof JsonOps) {
			return DataResult.success(ops.mergeToPrimitive(prefix, (T) input).getOrThrow(false, s -> {}));
		}
		throw new JsonSyntaxException("Cannot encode " + input);
	}

	@Override
	public <T> DataResult<com.mojang.datafixers.util.Pair<JsonElement, T>> decode(DynamicOps<T> ops, T input) {
		// Convert the input from the dynamic operations system into a JsonElement.
		try {
			JsonElement element = ops.convertTo(JsonOps.INSTANCE, input);
			return DataResult.success(com.mojang.datafixers.util.Pair.of(element, ops.empty()));
		} catch (IllegalArgumentException | JsonSyntaxException e) {
			return DataResult.error(() -> "Failed to parse input as JsonElement: " + e.getMessage());
		}
	}
}

package com.sigmundgranaas.forgero.core.property.api.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Collections;
import java.util.List;

/**
 * A utility wrapper codec that converts single property codecs to list-compatible codecs.
 * This allows PropertyDispatchCodec to work with codecs for individual property types
 * while automatically handling both single entries and list entries in JSON.
 * <p>
 * When deserializing:
 * - Single values are converted to single-element lists
 * - Array values are deserialized as lists
 * <p>
 * When serializing:
 * - Single-element lists are serialized as single values
 * - Multi-element lists are serialized as arrays
 *
 * @param <T> The type of the individual property
 */
public record ListCodecWrapper<T>(Codec<T> elementCodec) implements Codec<List<T>> {
	@Override
	public <D> DataResult<Pair<List<T>, D>> decode(DynamicOps<D> ops, D input) {
		// First try to decode as a list
		DataResult<Pair<List<T>, D>> listResult = Codec.list(elementCodec).decode(ops, input);

		if (listResult.result().isPresent()) {
			return listResult;
		}

		// If list decoding failed, try to decode as a single element
		DataResult<Pair<T, D>> singleResult = elementCodec.decode(ops, input);

		return singleResult.map(pair ->
				Pair.of(Collections.singletonList(pair.getFirst()), pair.getSecond())
		);
	}

	@Override
	public <D> DataResult<D> encode(List<T> input, DynamicOps<D> ops, D prefix) {
		if (input.isEmpty()) {
			return Codec.list(elementCodec).encode(input, ops, prefix);
		}

		// If we have exactly one element, serialize as a single element for cleaner JSON
		if (input.size() == 1) {
			return elementCodec.encode(input.get(0), ops, prefix);
		}

		// Otherwise, serialize as a list
		return Codec.list(elementCodec).encode(input, ops, prefix);
	}

	/**
	 * Creates a wrapper codec for the given element codec.
	 * Single-element lists will be serialized as single values.
	 *
	 * @param elementCodec The codec for individual elements
	 * @param <T> The element type
	 * @return A list codec that can handle both single values and arrays
	 */
	public static <T> Codec<List<T>> of(Codec<T> elementCodec) {
		return new ListCodecWrapper<>(elementCodec);
	}
}

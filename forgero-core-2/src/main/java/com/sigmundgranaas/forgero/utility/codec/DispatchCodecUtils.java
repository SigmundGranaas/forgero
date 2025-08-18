package com.sigmundgranaas.forgero.utility.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import java.util.function.Function;

/**
 * A utility for creating polymorphic "dispatch" codecs.
 * <p>
 * This utility abstracts the common pattern of serializing an interface where the concrete
 * implementation is determined by a "type" field in the serialized data.
 *
 * @param <I> The interface type to be serialized/deserialized.
 */
public final class DispatchCodecUtils<I> {

	/**
	 * Creates a polymorphic codec for an interface.
	 *
	 * @param codecRegistry A function that takes a type string and returns the corresponding Codec for a specific implementation.
	 *                      This is typically a static `getCodec(String type)` method on the interface.
	 * @param typeExtractor A function that takes an instance of the interface and returns its type string.
	 *                      This is typically an instance method like `type()` on the interface.
	 * @param <I>           The type of the interface.
	 * @return A new Codec that can encode and decode any implementation of the interface.
	 */
	public static <I> Codec<I> create(Function<String, Codec<? extends I>> codecRegistry, Function<I, String> typeExtractor) {
		return Codec.of(encoder(codecRegistry, typeExtractor), decoder(codecRegistry));
	}

	private static <I> Encoder<I> encoder(Function<String, Codec<? extends I>> codecRegistry, Function<I, String> typeExtractor) {
		return new Encoder<I>() {
			@Override
			public <T> DataResult<T> encode(I input, DynamicOps<T> ops, T prefix) {
				final String type = typeExtractor.apply(input);
				final Codec<? extends I> codec = codecRegistry.apply(type);

				// This cast is safe because we know the 'input' object is of the exact
				// type the specific codec handles, based on the 'type' string.
				@SuppressWarnings("unchecked")
				final Codec<I> castedCodec = (Codec<I>) codec;

				// The 'T' here is now correctly resolved as the generic parameter of this method.
				final DataResult<T> encoded = castedCodec.encode(input, ops, ops.empty());
				if (encoded.error().isPresent()) {
					return encoded;
				}

				return ops.mergeToMap(encoded.result().get(), ops.createString("type"), ops.createString(type));
			}
		};
	}

	private static <I> Decoder<I> decoder(Function<String, Codec<? extends I>> codecRegistry) {
		return new Decoder<I>() {
			@Override
			public <T> DataResult<Pair<I, T>> decode(DynamicOps<T> ops, T input) {
				final DataResult<String> typeResult = ops.get(input, "type").flatMap(ops::getStringValue);

				return typeResult.flatMap(type -> {
					final Codec<? extends I> codec = codecRegistry.apply(type);
					return codec.decode(ops, input)
							.map(pair -> pair.mapFirst(i -> i));
				});
			}
		};
	}
}

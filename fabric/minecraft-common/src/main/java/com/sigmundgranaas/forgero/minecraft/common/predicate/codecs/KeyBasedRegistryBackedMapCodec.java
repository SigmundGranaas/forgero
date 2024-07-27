package com.sigmundgranaas.forgero.minecraft.common.predicate.codecs;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.predicate.error.MissingFieldErrorHandler;
import com.sigmundgranaas.forgero.minecraft.common.predicate.error.PredicateErrorHandler;
import com.sigmundgranaas.forgero.minecraft.common.predicate.error.TypeFieldErrorHandler;
import org.apache.logging.log4j.Logger;

public class KeyBasedRegistryBackedMapCodec<T> extends MapCodec<GroupEntry<KeyPair<Predicate<T>>>> {
	private static final Logger LOGGER = Forgero.LOGGER;
	private final List<PredicateErrorHandler> errorHandlerRegistry;
	private final SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs;
	private final String key;

	public KeyBasedRegistryBackedMapCodec(SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs, String key) {
		this.errorHandlerRegistry = List.of(new TypeFieldErrorHandler(), new MissingFieldErrorHandler());
		this.codecs = codecs;
		this.key = key;
	}

	@Override
	public <R> Stream<R> keys(DynamicOps<R> ops) {
		return codecs.keySet().stream().map(ops::createString);
	}

	@Override
	public <R> RecordBuilder<R> encode(GroupEntry<KeyPair<Predicate<T>>> input, DynamicOps<R> ops, RecordBuilder<R> prefix) {
		for (KeyPair<Predicate<T>> element : input.entries()) {
			var codec = codecs.apply(element.key()).map(KeyPair::value);
			if (codec.isPresent()) {
				var res = codec.get().encodeStart(ops, element).result();
				if (res.isPresent()) {
					prefix.add(ops.createString(element.key()), res.get());
				}
			}
		}
		return prefix;
	}

	@Override
	public <R> DataResult<GroupEntry<KeyPair<Predicate<T>>>> decode(DynamicOps<R> ops, MapLike<R> input) {
		List<KeyPair<Predicate<T>>> successfulElements = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		for (Pair<R, R> entry : input.entries().toList()) {
			DataResult<String> keyResult = getEntryKey(ops, entry.getFirst());

			keyResult.result().ifPresent(key -> {
				DataResult<KeyPair<Predicate<T>>> entryResult = decodeEntry(key, ops, entry.getSecond(), input);
				entryResult.result().ifPresent(successfulElements::add);
				entryResult.error().map(DataResult.PartialResult::message).ifPresent(warnings::add);
			});

			keyResult.error().ifPresent(error -> warnings.add("Failed to decode key: " + error));
		}

		if (successfulElements.isEmpty() && !warnings.isEmpty()) {
			String warningMessage = "Warnings occurred during decoding: " + String.join(", ", warnings);
			LOGGER.warn(warningMessage);
			return DataResult.error(() -> warningMessage);
		} else if (!warnings.isEmpty()) {
			warnings.forEach(LOGGER::warn);
		}

		return DataResult.success(new GroupEntry<>(key, successfulElements));
	}

	private <R> DataResult<KeyPair<Predicate<T>>> decodeEntry(String entryKey, DynamicOps<R> ops, R value, MapLike<R> fullInput) {
		return codecs.apply(entryKey)
				.map(KeyPair::value)
				.map(codec -> codec.parse(ops, value))
				.orElseGet(() -> handleError(entryKey, ops, value, fullInput));
	}

	private <R> DataResult<KeyPair<Predicate<T>>> handleError(String entryKey, DynamicOps<R> ops, R value, MapLike<R> fullInput) {
		for (PredicateErrorHandler errorHandler : errorHandlerRegistry) {
			if (errorHandler.canHandle(entryKey, value, codecs)) {
				String errorMessage = errorHandler.createWarningMessage(ops, fullInput, entryKey, value, codecs);
				LOGGER.warn(errorMessage);
				return DataResult.error(() -> errorMessage);
			}
		}
		return DataResult.error(() -> "No suitable error handler found for key: " + entryKey);
	}

	private <R> DataResult<String> getEntryKey(DynamicOps<R> ops, R keyObj) {
		return ops.getStringValue(keyObj)
				.mapError(error -> "Failed to get entry key: " + error);
	}
}

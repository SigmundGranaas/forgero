package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.sigmundgranaas.forgero.core.Forgero;

public class SpecificationBackedPredicateCodec<T> extends MapCodec<GroupEntry<KeyPair<Predicate<T>>>> {
	private final String key;
	private final SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs;

	public SpecificationBackedPredicateCodec(String key, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codec) {
		this.key = key;
		this.codecs = codec;
	}

	@Override
	public <R> Stream<R> keys(DynamicOps<R> ops) {
		return codecs.keySet().stream().map(ops::createString);
	}

	@Override
	public <R> RecordBuilder<R> encode(GroupEntry<KeyPair<Predicate<T>>> input, DynamicOps<R> ops, RecordBuilder<R> prefix) {
		var temp = prefix;
		for (var element : input.entries()) {
			var codec = codecs.apply(element.key()).map(KeyPair::value);
			if (codec.isPresent()) {
				var res = codec.get().encodeStart(ops, element).result();
				if (res.isPresent()) {
					temp = temp.add(ops.createString(element.key()), res.get());
				}
			}
		}
		temp = prefix.add("type", ops.createString(key));
		return temp;
	}

	private <R> DataResult<KeyPair<Predicate<T>>> decodeEntry(String entryKey, DynamicOps<R> ops, R value) {
		return codecs.apply(entryKey)
				.map(KeyPair::value)
				.map(codec -> codec.parse(ops, value))
				.orElseGet(() -> {
					String closestMatch = findClosestMatch(entryKey);
					String errorMessage = "No codec found for key: " + entryKey;
					if (!closestMatch.isEmpty() && !closestMatch.equals(entryKey)) {
						errorMessage += ". Did you mean '" + closestMatch + "'?";
					}
					errorMessage += " Available keys: " + String.join(", ", codecs.keySet());
					Forgero.LOGGER.error(errorMessage);
					String finalErrorMessage = errorMessage;
					return DataResult.error(() -> finalErrorMessage);
				});
	}

	@Override
	public <R> DataResult<GroupEntry<KeyPair<Predicate<T>>>> decode(DynamicOps<R> ops, MapLike<R> input) {
		return getType(ops, input)
				.flatMap(type -> {
					if (!type.equals(this.key)) {
						return DataResult.error(() -> "Incorrect type: expected " + this.key + ", got " + type);
					}

					List<DataResult<KeyPair<Predicate<T>>>> elementResults = input.entries()
							.filter(entry -> !entry.getFirst().equals(ops.createString("type")))
							.map(entry ->
									getEntryKey(ops, entry.getFirst())
											.flatMap(entryKey -> decodeEntry(entryKey, ops, entry.getSecond()))
							)
							.toList();

					List<KeyPair<Predicate<T>>> successfulElements = new ArrayList<>();
					List<String> errors = new ArrayList<>();

					for (DataResult<KeyPair<Predicate<T>>> result : elementResults) {
						result.result().ifPresent(successfulElements::add);
						result.error().ifPresent(e -> errors.add(e.message()));
					}

					if (!errors.isEmpty()) {
						String errorMessage = "Errors occurred during decoding: " + String.join(", ", errors);
						Forgero.LOGGER.error(errorMessage);
						return DataResult.error(() -> errorMessage);
					}

					return DataResult.success(new GroupEntry<>(key, successfulElements));
				});
	}

	private <R> DataResult<String> getType(DynamicOps<R> ops, MapLike<R> input) {
		return ops.getStringValue(input.get("type"))
				.mapError(error -> "Failed to get type: " + error);
	}

	private <R> DataResult<String> getEntryKey(DynamicOps<R> ops, R keyObj) {
		return ops.getStringValue(keyObj)
				.mapError(error -> "Failed to get entry key: " + error);
	}

	private int calculateSimilarity(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = Math.min(
							dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
							Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
					);
				}
			}
		}

		return dp[s1.length()][s2.length()];
	}

	private String findClosestMatch(String inputKey) {
		return codecs.keySet().stream()
				.min(Comparator.comparingInt(registryKey -> calculateSimilarity(inputKey, registryKey)))
				.orElse("");
	}
}


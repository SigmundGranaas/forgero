package com.sigmundgranaas.forgero.minecraft.common.predicate.error;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.sigmundgranaas.forgero.core.util.StringSimilarity;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.SpecificationRegistry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.util.AnsiColors;
import com.sigmundgranaas.forgero.minecraft.common.predicate.util.JsonUtils;

public class MissingFieldErrorHandler implements PredicateErrorHandler {
	@Override
	public <T> boolean canHandle(String key, Object value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs) {
		return !codecs.keySet().contains(key);
	}

	@Override
	public <R, T> String createWarningMessage(DynamicOps<R> ops, MapLike<R> input, String key, R value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs) {
		StringBuilder warningMessage = WarningMessageBuilder.createBaseWarningMessage(ops, input);

		String closestMatch = StringSimilarity.findClosestMatch(key, codecs.keySet());
		warningMessage.append("No codec found for key: ").append(AnsiColors.RED).append(AnsiColors.BOLD).append(key).append(AnsiColors.RESET).append("\n");
		if (!closestMatch.isEmpty() && !closestMatch.equals(key)) {
			warningMessage.append("Did you mean '").append(closestMatch).append("'?\n");
		}
		warningMessage.append("Available keys: ").append(String.join(", ", codecs.keySet())).append("\n");

		warningMessage.append("Wrong structure:\n");
		warningMessage.append(JsonUtils.prettyPrintJsonWithHighlight(ops, input, key)).append("\n");
		warningMessage.append("Suggested structure:\n");
		warningMessage.append(createSuggestedStructure(ops, input, key, closestMatch));

		return warningMessage.toString();
	}

	private <R> String createSuggestedStructure(DynamicOps<R> ops, MapLike<R> input, String wrongKey, String suggestedKey) {
		Map<R, R> suggestedMap = new HashMap<>();
		input.entries().forEach(entry -> {
			R key = entry.getFirst();
			R value = entry.getSecond();
			if (ops.getStringValue(key).result().orElse("").equals(wrongKey)) {
				key = ops.createString(suggestedKey);
			}
			suggestedMap.put(key, value);
		});
		return JsonUtils.prettyPrintJson(ops, new MapLike<>() {
			@Override
			public R get(R key) {
				return suggestedMap.get(key);
			}

			@Override
			public R get(String key) {
				return suggestedMap.get(ops.createString(key));
			}

			@Override
			public Stream<Pair<R, R>> entries() {
				return suggestedMap.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
			}
		});
	}
}

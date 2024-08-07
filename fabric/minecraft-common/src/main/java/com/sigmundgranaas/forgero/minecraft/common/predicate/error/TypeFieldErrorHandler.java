package com.sigmundgranaas.forgero.minecraft.common.predicate.error;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.SpecificationRegistry;
import com.sigmundgranaas.forgero.minecraft.common.predicate.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

public class TypeFieldErrorHandler implements PredicateErrorHandler {
	@Override
	public <T> boolean canHandle(String key, Object value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs) {
		return key.equals("type");
	}

	@Override
	public <R, T> String createWarningMessage(DynamicOps<R> ops, MapLike<R> input, String key, R value, SpecificationRegistry<Codec<KeyPair<Predicate<T>>>> codecs) {
		StringBuilder warningMessage = WarningMessageBuilder.createBaseWarningMessage(ops, input);

		warningMessage.append("Ignoring 'type' field in nested map codecs. This field is only needed at the root level. The codec already knows what type it is. \n");
		warningMessage.append("Wrong structure:\n");
		warningMessage.append(JsonUtils.prettyPrintJsonWithHighlight(ops, input, "type")).append("\n");
		warningMessage.append("Correct structure:\n");

		Map<R, R> correctedMap = input.entries()
				.filter(pair -> !pair.getFirst().equals(ops.createString("type")))
				.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));

		warningMessage.append(JsonUtils.prettyPrintJson(ops, new MapLike<>() {
			@Nullable
			@Override
			public R get(R key) {
				return correctedMap.get(key);
			}

			@Nullable
			@Override
			public R get(String key) {
				return correctedMap.get(ops.createString(key));
			}

			@Override
			public Stream<Pair<R, R>> entries() {
				return correctedMap.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
			}
		}));

		return warningMessage.toString();
	}
}

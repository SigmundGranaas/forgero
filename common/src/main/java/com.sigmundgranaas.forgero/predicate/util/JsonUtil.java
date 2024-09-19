package com.sigmundgranaas.forgero.predicate.util;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import com.sigmundgranaas.forgero.core.Forgero;
import org.apache.commons.lang3.StringUtils;

public class JsonUtil extends com.sigmundgranaas.forgero.core.util.json.JsonUtil {
	public static <R> String prettyPrintJson(DynamicOps<R> ops, MapLike<R> mapLike) {
		Map<R, R> map = mapLike.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
		String jsonString = ops.convertTo(JsonOps.INSTANCE, ops.createMap(map)).toString();
		return prettyPrintJson(jsonString);
	}

	public static <R> String prettyPrintJsonWithHighlight(DynamicOps<R> ops, MapLike<R> mapLike, String highlightKey) {
		String jsonString = prettyPrintJson(ops, mapLike);
		String[] lines = jsonString.split("\n");
		StringBuilder highlighted = new StringBuilder();

		for (String line : lines) {
			if (line.contains("\"" + highlightKey + "\"")) {
				String indentation = StringUtils.substringBefore(line, "\"" + highlightKey + "\"");
				highlighted.append(indentation)
				           .append(AnsiColors.RED)
				           .append(line.substring(indentation.length()))
				           .append(AnsiColors.RESET)
				           .append("\n");
			} else {
				highlighted.append(line).append("\n");
			}
		}

		return highlighted.toString();
	}

	public static <T, R> String encodeToJson(Codec<R> codec, R input, DynamicOps<T> ops) {
		DataResult<T> result = codec.encodeStart(ops, input);
		return result.resultOrPartial(error -> Forgero.LOGGER.error("Error encoding example: {}", error))
		             .map(encoded -> ops.convertTo(JsonOps.INSTANCE, encoded))
		             .map(JsonElement::toString)
		             .orElse("Failed to encode example");
	}
}



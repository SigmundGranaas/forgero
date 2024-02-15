package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import static com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SameBlockFilter.Key;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.BlockPredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.BlockPredicateMatcher;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockFilter {
	ClassKey<BlockFilter> KEY = new ClassKey<>("forgero:filter", BlockFilter.class);

	BlockFilter DEFAULT = (source, current, root) -> true;
	BlockFilter DEFAULT_FALSE = (source, current, root) -> false;


	static BlockFilter fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
			String type = json.getAsJsonPrimitive().getAsString();
			return fromString(type, json.getAsJsonPrimitive());
		}
		if (json.isJsonArray()) {
			List<BlockFilter> filters = new ArrayList<>();
			json.getAsJsonArray().forEach(element -> filters.add(fromJson(element)));
			return new FilterWrapper(filters);
		}

		if (json.isJsonObject()) {
			var object = json.getAsJsonObject();
			if (object.has("type")) {
				var type = object.get("type");
				return fromString(type.getAsString(), object);
			}
		}
		return BlockFilter.DEFAULT;
	}

	static BlockFilter fromString(String type, JsonElement json) {
		if (type.equals(Key)) {
			return SameBlockFilter.DEFAULT;
		} else if (type.equals(SimilarBlockFilter.Key)) {
			return SimilarBlockFilter.DEFAULT;
		} else if (type.equals(BlockPredicateMatcher.ID)) {
			return new BlockPredicateMatcher(BlockPredicate.fromJson(json));
		} else if (type.equals(CanMineFilter.Key)) {
			return CanMineFilter.DEFAULT;
		}
		Forgero.LOGGER.warn("Unknown filter type: {}", type);
		return BlockFilter.DEFAULT_FALSE;
	}

	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);
}

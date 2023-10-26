package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import static com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SameBlockFilter.Key;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockFilter {
	ClassKey<BlockFilter> KEY = new ClassKey<>("forgero:filter", BlockFilter.class);

	BlockFilter DEFAULT = (source, current, root) -> true;


	static BlockFilter fromJson(JsonElement json) {
		if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
			String type = json.getAsJsonPrimitive().getAsString();
			return fromString(type);
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
				return fromJson(type);
			}
		}
		return BlockFilter.DEFAULT;
	}

	static BlockFilter fromString(String type) {
		if (type.equals(Key)) {
			return new SameBlockFilter();
		}
		return BlockFilter.DEFAULT;
	}

	boolean filter(Entity entity, BlockPos currentPos, BlockPos root);
}

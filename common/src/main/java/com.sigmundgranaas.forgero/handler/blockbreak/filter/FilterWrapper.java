package com.sigmundgranaas.forgero.handler.blockbreak.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.util.TypeToken;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class FilterWrapper implements BlockFilter {
	public static String TYPE = "forgero:filter_list";
	public static ClassKey<FilterWrapper> KEY = new ClassKey<>(TYPE, FilterWrapper.class);

	public static JsonBuilder<FilterWrapper> BUILDER = new JsonBuilder<>() {
		@Override
		public Optional<FilterWrapper> build(JsonElement json) {
			if (json.isJsonArray()) {
				List<BlockFilter> filters = new ArrayList<>();
				json.getAsJsonArray().forEach(element -> HandlerBuilder.DEFAULT.build(BlockFilter.KEY, element).ifPresent(filters::add));
				return Optional.of(new FilterWrapper(filters));
			}

			return Optional.empty();
		}

		@Override
		public TypeToken<FilterWrapper> getTargetClass() {
			return KEY.clazz();
		}
	};

	private final List<BlockFilter> filterList;
	
	public FilterWrapper(List<BlockFilter> filterList) {
		this.filterList = filterList;
	}

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return filterList.stream().allMatch(filter -> filter.filter(entity, currentPos, root));
	}


}

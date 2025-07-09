package com.sigmundgranaas.forgero.core.property.api.custom;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.OperatorMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A generic PropertyBuilder for properties represented by a JSON array of objects.
 *
 * @param <D> The raw data record type (implementing CustomPropertyData).
 * @param <P> The final Property type.
 */
public abstract class GenericListPropertyBuilder<D extends CustomPropertyData, P extends Property> implements PropertyBuilder {
	private static final Logger LOGGER = LogManager.getLogger(GenericListPropertyBuilder.class);
	private final String propertyType;
	private final Codec<List<D>> dataListCodec;
	private final BiFunction<D, Condition, P> propertyFactory;

	protected GenericListPropertyBuilder(String propertyType, Codec<List<D>> dataListCodec, BiFunction<D, Condition, P> propertyFactory) {
		this.propertyType = propertyType;
		this.dataListCodec = dataListCodec;
		this.propertyFactory = propertyFactory;
	}

	@Override
	public String getPropertyType() {
		return propertyType;
	}

	@Override
	public List<Property> build(Map<String, JsonElement> properties, ConditionMapper conditionMapper, OperatorMapper operatorMapper) {
		JsonElement customPropJson = properties.get(getPropertyType());
		if (customPropJson == null) {
			return Collections.emptyList();
		}

		List<D> dataList = dataListCodec.parse(JsonOps.INSTANCE, customPropJson)
				.resultOrPartial(error -> LOGGER.error("Error parsing list of '{}' data: {}", getPropertyType(), error))
				.orElse(Collections.emptyList());

		return dataList.stream()
				.map(data -> (Property) propertyFactory.apply(data, conditionMapper.apply(data.condition())))
				.collect(Collectors.toList());
	}
}

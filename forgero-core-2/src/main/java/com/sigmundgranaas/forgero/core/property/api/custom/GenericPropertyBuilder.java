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
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A generic PropertyBuilder for properties represented by a single JSON object.
 *
 * @param <D> The raw data record type (implementing CustomPropertyData).
 * @param <P> The final Property type.
 */
public abstract class GenericPropertyBuilder<D extends CustomPropertyData, P extends Property> implements PropertyBuilder {
	private static final Logger LOGGER = LogManager.getLogger(GenericPropertyBuilder.class);

	private final String propertyType;
	private final Codec<D> dataCodec;
	private final BiFunction<D, Condition, P> propertyFactory;

	protected GenericPropertyBuilder(String propertyType, Codec<D> dataCodec, BiFunction<D, Condition, P> propertyFactory) {
		this.propertyType = propertyType;
		this.dataCodec = dataCodec;
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

		Optional<D> data = dataCodec.parse(JsonOps.INSTANCE, customPropJson)
				.resultOrPartial(error -> LOGGER.error("Error parsing '{}' data: {}", getPropertyType(), error));

		return data.map(d -> (Property) propertyFactory.apply(d, conditionMapper.apply(d.condition())))
				.map(Collections::singletonList)
				.orElse(Collections.emptyList());
	}
}

package com.sigmundgranaas.forgero.data.mapper.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.OperatorMapper;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttributePropertyBuilder implements PropertyBuilder {
	private static final Logger LOGGER = LogManager.getLogger(AttributePropertyBuilder.class);

	@Override
	public String getPropertyType() {
		return "forgero:attributes";
	}

	@Override
	public List<Property> build(Map<String, JsonElement> properties, ConditionMapper conditionMapper, OperatorMapper operatorMapper) {
		JsonElement attributesJson = properties.get(getPropertyType());
		if (attributesJson == null) {
			return Collections.emptyList();
		}

		List<AttributeData> attributeDataList = AttributeCodecs.ATTRIBUTE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, attributesJson)
				.resultOrPartial(error -> LOGGER.error("Error parsing attributes from properties map: {}", error))
				.orElse(Collections.emptyList());

		return attributeDataList.stream()
				.map(attrData -> mapAttribute(attrData, conditionMapper, operatorMapper))
				.collect(Collectors.toList());
	}

	private Property mapAttribute(AttributeData data, ConditionMapper conditionMapper, OperatorMapper operatorMapper) {
		var computation = data.computation();
		var operator = operatorMapper.apply(computation.operator());
		int group = operatorMapper.leveledOrder(computation.order());
		Condition condition = conditionMapper.apply(data.condition());

		if (data.composite() != null) {
			return new CompositeAttributeComponent(data.type(),  computation.value(), operator, group, data.composite());
		} else {
			return new SimpleAttribute(data.type(), computation.value(), operator, group, condition);
		}
	}
}

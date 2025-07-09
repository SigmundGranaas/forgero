package com.sigmundgranaas.forgero.data.mapper.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.OperatorMapper;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeaturePropertyBuilder implements PropertyBuilder {
	private static final Logger LOGGER = LogManager.getLogger(FeaturePropertyBuilder.class);


	@Override
	public String getPropertyType() {
		return "forgero:features";
	}

	@Override
	public List<Property> build(Map<String, JsonElement> properties, ConditionMapper conditionMapper, OperatorMapper operatorMapper) {
		JsonElement featuresJson = properties.get(getPropertyType());
		if (featuresJson == null) {
			return Collections.emptyList();
		}

		List<FeatureData> featureDataList = FeatureCodecs.FEATURE_DATA_LIST_CODEC.parse(JsonOps.INSTANCE, featuresJson)
				.resultOrPartial(error -> LOGGER.error("Error parsing features from properties map: {}", error))
				.orElse(Collections.emptyList());

		return featureDataList.stream()
				.map(featureData -> new Feature(featureData.type(), 1, conditionMapper.apply(featureData.condition())))
				.collect(Collectors.toList());
	}
}

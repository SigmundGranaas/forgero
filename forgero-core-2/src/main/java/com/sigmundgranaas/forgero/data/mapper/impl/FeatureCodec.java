package com.sigmundgranaas.forgero.data.mapper.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class FeatureCodec implements PropertyCodec<FeatureData> {

	private final ConditionMapper conditionMapper;

	public FeatureCodec(ConditionMapper conditionMapper) {
		this.conditionMapper = conditionMapper;
	}

	@Override
	public String getPropertyType() {
		return "forgero:features";
	}

	@Override
	public Class<FeatureData> getPropertyDataType() {
		return FeatureData.class;
	}

	@Override
	public List<Property> build(List<FeatureData> dataList) {
		return dataList.stream()
				.map(data -> new Feature(data.type(), 1, conditionMapper.apply(data.condition())))
				.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public FeatureData toData(Property property) {
		if (property instanceof Feature feature) {
			// A full implementation requires reverse mapping for custom feature data fields.
			// For a basic Feature, this is straightforward.
			return new FeatureData() { // Anonymous implementation for basic features
				@Override
				public com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier type() {
					return feature.type();
				}

				@Override
				public com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData condition() {
					return conditionMapper.toData(feature.condition());
				}
			};
		}
		return null;
	}

	@Override
	public Codec<List<FeatureData>> getCodec() {
		return FeatureCodecs.FEATURE_DATA_LIST_CODEC;
	}
}

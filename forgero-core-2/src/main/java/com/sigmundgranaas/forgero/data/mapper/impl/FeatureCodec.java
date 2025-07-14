package com.sigmundgranaas.forgero.data.mapper.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.feature.api.Feature;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.FeatureData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureCodec implements PropertyCodec<FeatureData> {
	private Codec<List<FeatureData>> codec;

	public FeatureCodec() {
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
		if (dataList == null) {
			return Collections.emptyList();
		}
		return dataList.stream()
				.map(data -> new Feature(data.type(), 1, data.condition() != null ? data.condition() : Condition.ALWAYS_TRUE))
				.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public FeatureData toData(Property property) {
		if (property instanceof Feature feature) {
			return new FeatureData() {
				@Override
				public com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier type() {
					return feature.type();
				}

				@Override
				public com.sigmundgranaas.forgero.core.property.condition.Condition condition() {
					return feature.condition();
				}
			};
		}
		return null;
	}

	@Override
	public Codec<List<FeatureData>> getCodec() {
		if (this.codec == null) {
			throw new IllegalStateException("Codec has not been initialized. Call setCodecs(...) first.");
		}
		return this.codec;
	}

	public void setCodecs() {
		this.codec = FeatureCodecs.createFeatureDataListCodec();
	}
}

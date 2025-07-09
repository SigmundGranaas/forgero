package com.sigmundgranaas.forgero.property.namereplacement;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NameReplacementCodec implements PropertyCodec<NameReplacementData> {
	private final ConditionMapper conditionMapper;

	public NameReplacementCodec(ConditionMapper conditionMapper) {
		this.conditionMapper = conditionMapper;
	}

	@Override
	public String getPropertyType() {
		return DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString();
	}

	@Override
	public Class<NameReplacementData> getPropertyDataType() {
		return NameReplacementData.class;
	}

	@Override
	public List<Property> build(List<NameReplacementData> dataList) {
		return dataList.stream()
				.map(data -> (Property) new NameReplacementProperty(data.from(), data.to(), conditionMapper.apply(data.condition())))
				.toList();
	}

	@Override
	@Nullable
	public NameReplacementData toData(Property property) {
		if (property instanceof NameReplacementProperty nrp) {
			return new NameReplacementData(nrp.from(), nrp.to(), conditionMapper.toData(nrp.condition()));
		}
		return null;
	}

	@Override
	public Codec<List<NameReplacementData>> getCodec() {
		return NameReplacementData.CODEC.listOf();
	}
}

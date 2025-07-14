package com.sigmundgranaas.forgero.property.namereplacement;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class NameReplacementCodec implements PropertyCodec<NameReplacementData> {
	private Codec<List<NameReplacementData>> codec;

	public NameReplacementCodec() {
	}

	public void initialize(Codec<Condition> conditionCodec) {
		this.codec = NameReplacementData.createCodec(conditionCodec).listOf();
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
				.map(data -> (Property) new NameReplacementProperty(data.from(), data.to(), data.condition()))
				.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public NameReplacementData toData(Property property) {
		if (property instanceof NameReplacementProperty nrp) {
			return new NameReplacementData(nrp.from(), nrp.to(), nrp.condition());
		}
		return null;
	}

	@Override
	public Codec<List<NameReplacementData>> getCodec() {
		if (codec == null) {
			throw new IllegalStateException("NameReplacementCodec has not been initialized. Call initialize() first.");
		}
		return codec;
	}
}

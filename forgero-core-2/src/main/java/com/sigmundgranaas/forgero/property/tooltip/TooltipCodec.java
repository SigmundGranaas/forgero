package com.sigmundgranaas.forgero.property.tooltip;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class TooltipCodec implements PropertyCodec<TooltipData> {
	private Codec<List<TooltipData>> codec;

	public TooltipCodec() {
	}

	public void initialize(Codec<Condition> conditionCodec) {
		this.codec = TooltipData.createCodec(conditionCodec).listOf();
	}

	@Override
	public String getPropertyType() {
		return DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString();
	}

	@Override
	public Class<TooltipData> getPropertyDataType() {
		return TooltipData.class;
	}

	@Override
	public List<Property> build(List<TooltipData> dataList) {
		return dataList.stream()
				.map(data -> new TooltipProperty(data.key(), data.value(), data.format(), data.condition()))
				.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public TooltipData toData(Property property) {
		if (property instanceof TooltipProperty tp) {
			return new TooltipData(tp.key(), tp.value(), tp.format(), tp.condition());
		}
		return null;
	}

	@Override
	public Codec<List<TooltipData>> getCodec() {
		if (codec == null) {
			throw new IllegalStateException("TooltipCodec has not been initialized. Call initialize() first.");
		}
		return codec;
	}
}

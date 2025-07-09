package com.sigmundgranaas.forgero.property.tooltip;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipCodec implements PropertyCodec<TooltipData> {
	private final ConditionMapper conditionMapper;

	public TooltipCodec(ConditionMapper conditionMapper) {
		this.conditionMapper = conditionMapper;
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
				.map(data -> (Property) new TooltipProperty(data.key(), data.value(), data.format(), conditionMapper.apply(data.condition())))
				.toList();
	}

	@Override
	@Nullable
	public TooltipData toData(Property property) {
		if (property instanceof TooltipProperty tp) {
			return new TooltipData(tp.key(), tp.value(), tp.format(), conditionMapper.toData(tp.condition()));
		}
		return null;
	}

	@Override
	public Codec<List<TooltipData>> getCodec() {
		return TooltipData.CODEC.listOf();
	}
}

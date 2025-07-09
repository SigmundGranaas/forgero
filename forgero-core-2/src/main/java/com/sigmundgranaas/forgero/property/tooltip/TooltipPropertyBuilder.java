package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.core.property.api.custom.GenericListPropertyBuilder;


public class TooltipPropertyBuilder extends GenericListPropertyBuilder<TooltipData, TooltipProperty> {

	public TooltipPropertyBuilder() {
		super(
				DefaultTooltipKeys.TOOLTIP_SECTION_IDENTIFIER.toString(),
				TooltipData.CODEC.listOf(),
				(data, condition) -> new TooltipProperty(data.key(), data.value(), data.format(), condition)
		);
	}
}

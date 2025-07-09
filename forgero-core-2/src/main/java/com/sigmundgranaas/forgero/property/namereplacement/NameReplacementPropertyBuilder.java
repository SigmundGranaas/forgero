package com.sigmundgranaas.forgero.property.namereplacement;

import com.sigmundgranaas.forgero.core.property.api.custom.GenericPropertyBuilder;


public class NameReplacementPropertyBuilder extends GenericPropertyBuilder<NameReplacementData, NameReplacementProperty> {

	public NameReplacementPropertyBuilder() {
		super(
				DefaultNameReplacementKeys.NAME_REPLACEMENT_IDENTIFIER.toString(),
				NameReplacementData.CODEC,
				(data, condition) -> new NameReplacementProperty(data.from(), data.to(), condition)
		);
	}
}

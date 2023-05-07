package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

public class PassivePropertyBuilder {
	public static PassiveProperty createPassivePropertyFromPojo(PropertyPojo.Passive propertyPOJO) {
		return new LeveledProperty(LeveledPassiveType.MAGNETIC);
	}
}

package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;

public class AttackSpeed {
	public static String KEY = "ATTACK_SPEED";

	public static AttributeModification clampMinimumAttackSpeed() {
		return (attribute, state) ->
				Attribute.of(Math.max(ForgeroConfigurationLoader.configuration.minimumAttackSpeed - 4f, attribute.asFloat()), attribute.key());
	}
}

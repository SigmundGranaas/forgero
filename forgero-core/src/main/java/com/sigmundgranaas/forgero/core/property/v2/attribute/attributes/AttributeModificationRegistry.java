package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;

public class AttributeModificationRegistry {
	public static final AttributeModificationRegistry INSTANCE = new AttributeModificationRegistry();
	private final Map<String, List<AttributeModification>> modifications;

	private AttributeModificationRegistry() {
		modifications = new HashMap<>();
		defaults();
	}

	public void defaults() {
		register(AttackDamage.KEY, new BrokenToolAttributeModification(0f));
		register(MiningSpeed.KEY, new BrokenToolAttributeModification(1f));
		register(MiningLevel.KEY, new BrokenToolAttributeModification(0f));
		register(Armor.KEY, new BrokenToolAttributeModification(0f));
		register(AttackSpeed.KEY, Weight.reduceAttackSpeedByWeight());
		register(AttackSpeed.KEY, AttackSpeed.clampMinimumAttackSpeed());
	}

	public final void register(Attribute attribute, AttributeModification mod) {
		register(attribute.key(), mod);
	}

	public final void register(String attribute, AttributeModification mod) {
		if (modifications.containsKey(attribute)) {
			modifications.get(attribute).add(mod);

		} else {
			modifications.put(attribute, new ArrayList<>(List.of(mod)));
		}
	}

	public final List<AttributeModification> get(String attribute) {
		return Optional.ofNullable(modifications.get(attribute)).orElseGet(Collections::emptyList);
	}
}

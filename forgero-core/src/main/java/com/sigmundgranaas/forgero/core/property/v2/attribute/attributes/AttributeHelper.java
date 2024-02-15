package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

public class AttributeHelper {
	private final PropertyContainer container;
	private final Map<String, BiFunction<ContainerTargetPair, String, ComputedAttribute>> map;

	public AttributeHelper(PropertyContainer container) {
		this.container = container;
		this.map = attributeStringMap();
	}

	public static Map<String, BiFunction<ContainerTargetPair, String, ComputedAttribute>> attributeStringMap() {
		Map<String, BiFunction<ContainerTargetPair, String, ComputedAttribute>> map = new HashMap<>();
		map.put(AttackDamage.KEY, ComputedAttribute::of);
		map.put(Durability.KEY, ComputedAttribute::of);
		map.put(AttackSpeed.KEY, ComputedAttribute::of);
		map.put(MiningSpeed.KEY, ComputedAttribute::of);
		map.put(MiningLevel.KEY, ComputedAttribute::of);
		map.put(Weight.KEY, ComputedAttribute::of);
		return map;
	}

	private static BiFunction<ContainerTargetPair, String, ComputedAttribute> defaultAttributeFn(String type) {
		return (container, target) -> ComputedAttribute.of(container.container().stream(container.target(), MatchContext.of()).applyAttribute(type), type);
	}

	public ComputedAttribute apply(String type) {
		return map.getOrDefault(type, defaultAttributeFn(type))
				.apply(ContainerTargetPair.of(container), type);
	}

	public ComputedAttribute apply(ComputedAttribute type) {
		return apply(type.key());
	}
}

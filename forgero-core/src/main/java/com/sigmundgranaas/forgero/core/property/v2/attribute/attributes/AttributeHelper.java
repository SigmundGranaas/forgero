package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;

public class AttributeHelper {
	private final PropertyContainer container;
	private final Map<String, BiFunction<ContainerTargetPair, String, Attribute>> map;

	public AttributeHelper(PropertyContainer container) {
		this.container = container;
		this.map = attributeStringMap();
	}

	public static Map<String, BiFunction<ContainerTargetPair, String, Attribute>> attributeStringMap() {
		Map<String, BiFunction<ContainerTargetPair, String, Attribute>> map = new HashMap<>();
		map.put(AttackDamage.KEY, Attribute::of);
		map.put(Durability.KEY, Attribute::of);
		map.put(AttackSpeed.KEY, Attribute::of);
		map.put(MiningSpeed.KEY, Attribute::of);
		map.put(MiningLevel.KEY, Attribute::of);
		map.put(Weight.KEY, Attribute::of);
		return map;
	}

	private static BiFunction<ContainerTargetPair, String, Attribute> defaultAttributeFn(String type) {
		return (container, target) -> Attribute.of(container.container().stream(container.target(), MatchContext.of()).applyAttribute(type), type);
	}

	public Attribute apply(String type) {
		return map.getOrDefault(type, defaultAttributeFn(type))
				.apply(ContainerTargetPair.of(container), type);
	}

	public Attribute apply(Attribute type) {
		return apply(type.key());
	}
}

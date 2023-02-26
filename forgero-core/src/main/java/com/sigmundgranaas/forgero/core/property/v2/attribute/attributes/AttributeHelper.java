package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AttributeHelper {
    private final PropertyContainer container;
    private final Map<String, BiFunction<PropertyContainer, Target, Attribute>> map;

    public AttributeHelper(PropertyContainer container) {
        this.container = container;
        this.map = attributeStringMap();
    }

    public static Map<String, BiFunction<PropertyContainer, Target, Attribute>> attributeStringMap() {
        Map<String, BiFunction<PropertyContainer, Target, Attribute>> map = new HashMap<>();
        map.put(AttackDamage.KEY, AttackDamage::of);
        map.put(Durability.KEY, Durability::of);
        map.put(AttackSpeed.KEY, AttackSpeed::of);
        map.put(MiningSpeed.KEY, MiningSpeed::of);
        map.put(MiningLevel.KEY, MiningLevel::of);
        return map;
    }

    public Attribute apply(String type) {
        return map.getOrDefault(type, (container, target) -> Attribute.of(container.stream().applyAttribute(target, type), type))
                .apply(container, Target.EMPTY);
    }
}

package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.cache.AttributeCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;

public class MiningLevel implements Attribute {

    public static final String KEY = "MINING_LEVEL";
    private final Integer value;

    public MiningLevel(ContainerTargetPair pair) {
        this.value = (int) pair.container().stream().applyAttribute(pair.target(), AttributeType.MINING_LEVEL);
    }

    public static Attribute of(PropertyContainer container) {
        var pair = ContainerTargetPair.of(container);
        return AttributeCache.computeIfAbsent(pair, () -> new MiningLevel(pair), KEY);
    }

    public static Integer apply(PropertyContainer container) {
        return of(container).asInt();
    }

    public static Integer apply(PropertyContainer container, Target target) {
        return of(container, target).asInt();
    }

    public static Attribute of(PropertyContainer container, Target target) {
        var pair = new ContainerTargetPair(container, target);
        return AttributeCache.computeIfAbsent(pair, () -> new MiningLevel(pair), KEY);
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public Float asFloat() {
        return value.floatValue();
    }

    @Override
    public Integer asInt() {
        return value;
    }
}

package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.attribute.AttributeCache;

public class AttackDamage implements Attribute {

    public static final String KEY = "ATTACK_DAMAGE";
    private final float value;

    public AttackDamage(ContainerTargetPair pair) {
        this.value = pair.container().stream().applyAttribute(pair.target(), AttributeType.ATTACK_DAMAGE);
    }

    public static Attribute of(PropertyContainer container) {
        var pair = ContainerTargetPair.of(container);
        return AttributeCache.computeIfAbsent(pair, () -> new AttackDamage(pair), KEY);
    }

    public static Float apply(PropertyContainer container) {
        return of(container).asFloat();
    }

    public static Float apply(PropertyContainer container, Target target) {
        return of(container, target).asFloat();
    }

    public static Attribute of(PropertyContainer container, Target target) {
        var pair = new ContainerTargetPair(container, target);
        return AttributeCache.computeIfAbsent(pair, () -> new AttackDamage(pair), KEY);
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public Float asFloat() {
        return value;
    }
}

package com.sigmundgranaas.forgero.core.property.v2;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;

public record ContainerTargetPair(PropertyContainer container, Target target) {
    public static ContainerTargetPair of(PropertyContainer container) {
        return new ContainerTargetPair(container, Target.EMPTY);
    }

    @Override
    public int hashCode() {
        return container.hashCode() + target.hashCode();
    }
}

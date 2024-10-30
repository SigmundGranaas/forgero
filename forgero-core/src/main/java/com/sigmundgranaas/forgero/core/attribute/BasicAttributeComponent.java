package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.core.attribute.computation.Computation;

public record BasicAttributeComponent(Computation computation, String type) implements AttributeComponent {
}

package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.type.Type;

public record IdentifiableContainer(String name, String nameSpace, Type type) implements Identifiable, Typed {
}

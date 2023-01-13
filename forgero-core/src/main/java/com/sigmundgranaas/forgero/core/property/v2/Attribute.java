package com.sigmundgranaas.forgero.core.property.v2;

public interface Attribute {

    String key();

    default Integer asInt() {
        return asFloat().intValue();
    }

    Float asFloat();

    default Double asDouble() {
        return asFloat().doubleValue();
    }

}

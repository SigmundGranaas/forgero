package com.sigmundgranaas.forgero.core.property.v2;

public interface Attribute {

    static Attribute of(float value, String key) {
        return new Attribute() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public Float asFloat() {
                return value;
            }
        };
    }

    String key();

    default Integer asInt() {
        return asFloat().intValue();
    }

    Float asFloat();

    default Double asDouble() {
        return asFloat().doubleValue();
    }

}

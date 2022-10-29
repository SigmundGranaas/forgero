package com.sigmundgranaas.forgero.property.attribute;

public class AttributeResult {
    private final float value;

    public AttributeResult(float value) {
        this.value = value;
    }

    public static AttributeResult of(float value) {
        return new AttributeResult(value);
    }

    public float asFloat() {
        return value;
    }

    public int asInt() {
        return (int) value;
    }
}

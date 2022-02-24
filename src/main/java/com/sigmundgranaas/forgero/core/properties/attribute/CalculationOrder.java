package com.sigmundgranaas.forgero.core.properties.attribute;

public enum CalculationOrder {
    BASE(0),
    BASE_MULTIPLICATION(1),
    MIDDLE(5),
    END(10);

    private final int value;

    CalculationOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

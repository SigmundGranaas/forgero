package com.sigmundgranaas.forgerocore.property;

/**
 * Enum for describing in which order calculations should occur.
 */
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

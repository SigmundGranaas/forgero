package com.sigmundgranaas.forgero.smithing.attribute;

import com.sigmundgranaas.forgero.core.property.Property;

/**
 * Represents smithing temperature requirements for a material.
 * Can be attached as a property to a material for smithing logic.
 */
public class SmithingRange implements Property {
    public static final String KEY = "SMITHING_RANGE";

    private final int minimum;
    private final int perfectMin;
    private final int perfectMax;
    private final int maximum;

    public SmithingRange(int minimum, int perfectMin, int perfectMax, int maximum) {
        this.minimum = minimum;
        this.perfectMin = perfectMin;
        this.perfectMax = perfectMax;
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getPerfectMin() {
        return perfectMin;
    }

    public int getPerfectMax() {
        return perfectMax;
    }

    public int getMaximum() {
        return maximum;
    }

    public boolean isSmithable(int value) {
        return value >= minimum && value <= maximum;
    }

    public boolean isPerfect(int value) {
        return value >= perfectMin && value <= perfectMax;
    }

    @Override
    public String type() {
        return KEY;
    }
}

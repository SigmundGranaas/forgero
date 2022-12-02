package com.sigmundgranaas.forgero.property.attribute;

import java.util.EnumSet;

public enum Category {
    OFFENSIVE,
    UTILITY,
    DEFENSIVE,
    ALL,
    COMPOSITE,
    LOCAL,
    PASS,
    UNDEFINED;

    public static final EnumSet<Category> UPGRADE_CATEGORIES = EnumSet.of(OFFENSIVE, UTILITY, DEFENSIVE, ALL);
}

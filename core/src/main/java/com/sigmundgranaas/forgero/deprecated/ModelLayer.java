package com.sigmundgranaas.forgero.deprecated;

import java.util.Locale;

public enum ModelLayer {
    PRIMARY,
    SECONDARY,
    GEM,
    MISC;

    public String getFileName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}

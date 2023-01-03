package com.sigmundgranaas.forgero.core.property;

import java.util.List;

public interface PropertyAdjuster {
    List<Property> raw();

    List<Property> adjusted();
}

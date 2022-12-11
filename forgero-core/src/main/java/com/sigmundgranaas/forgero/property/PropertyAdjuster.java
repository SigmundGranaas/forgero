package com.sigmundgranaas.forgero.property;

import java.util.List;

public interface PropertyAdjuster {
    List<Property> raw();

    List<Property> adjusted();
}

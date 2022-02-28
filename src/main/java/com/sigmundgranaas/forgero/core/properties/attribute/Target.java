package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

public interface Target {
    TargetTypes getType();

    TargetTagSet getTag();
}

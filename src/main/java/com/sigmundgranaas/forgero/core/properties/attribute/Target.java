package com.sigmundgranaas.forgero.core.properties.attribute;

import com.sigmundgranaas.forgero.core.properties.TargetTypes;

import java.util.Set;

public interface Target {
    Set<TargetTypes> getTypes();

    TargetTagSet getTag();
}

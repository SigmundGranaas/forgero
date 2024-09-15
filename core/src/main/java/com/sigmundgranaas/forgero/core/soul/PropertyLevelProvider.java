package com.sigmundgranaas.forgero.core.soul;

import com.sigmundgranaas.forgero.core.property.Property;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface PropertyLevelProvider extends Function<Integer, List<Property>> {

}

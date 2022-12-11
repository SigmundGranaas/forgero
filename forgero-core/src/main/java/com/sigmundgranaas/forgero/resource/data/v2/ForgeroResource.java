package com.sigmundgranaas.forgero.resource.data.v2;

import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.resource.data.v2.data.TypeData;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface ForgeroResource {
    default Collection<String> dependencies() {
        return Collections.emptyList();
    }

    default Optional<ForgeroResource> parent() {
        return Optional.empty();
    }

    default Optional<ConstructData> construct() {
        return Optional.empty();
    }

    default Optional<PropertyContainer> propertyContainer() {
        return Optional.empty();
    }

    default Optional<TypeData> type() {
        return Optional.empty();
    }
}
package com.sigmundgranaas.forgero.core.data.v2;

import com.sigmundgranaas.forgero.core.data.v2.data.*;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface ForgeroResource {
    String name();

    String id();

    String type();

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

    default Optional<ModelData> model() {
        return Optional.empty();
    }

    default Optional<HostData> host() {
        return Optional.empty();
    }

    default Optional<ContainerData> container() {
        return Optional.empty();
    }

    default Optional<PaletteData> palette() {
        return Optional.empty();
    }

    default Optional<ConditionData> condition() {
        return Optional.empty();
    }
}

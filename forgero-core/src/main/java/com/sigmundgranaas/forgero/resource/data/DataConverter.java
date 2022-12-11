package com.sigmundgranaas.forgero.resource.data;

import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.util.Optional;

@FunctionalInterface
public interface DataConverter<T> {
    Optional<T> convert(DataResource resource);
}

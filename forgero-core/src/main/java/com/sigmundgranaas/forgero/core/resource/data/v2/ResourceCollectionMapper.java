package com.sigmundgranaas.forgero.core.resource.data.v2;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;


public interface ResourceCollectionMapper extends Function<List<DataResource>, List<DataResource>> {
    ResourceCollectionMapper DEFAULT = (list) -> list;

    default ResourceCollectionMapper andThen(ResourceCollectionMapper after) {
        Objects.requireNonNull(after);
        return (List<DataResource> t) -> after.apply(apply(t));
    }
}

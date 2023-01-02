package com.sigmundgranaas.forgero.core.resource.data;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.util.List;

@FunctionalInterface
public interface ResourceLoader {
    List<DataResource> load();
}

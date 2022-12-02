package com.sigmundgranaas.forgero.resource.data;

import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.util.List;

@FunctionalInterface
public interface ResourceLoader {
    List<DataResource> load();
}

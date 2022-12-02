package com.sigmundgranaas.forgero.resource.data.v2;

import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.util.List;

public record DefaultPackage(List<String> dependencies, int priority,
                             List<DataResource> data,
                             String nameSpace, String name) implements DataPackage {
}

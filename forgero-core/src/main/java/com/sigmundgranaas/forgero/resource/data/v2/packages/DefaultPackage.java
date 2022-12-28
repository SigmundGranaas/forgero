package com.sigmundgranaas.forgero.resource.data.v2.packages;

import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.util.List;
import java.util.function.Supplier;

public record DefaultPackage(List<String> dependencies, int priority,
                             Supplier<List<DataResource>> data,
                             String nameSpace, String name) implements DataPackage {
}

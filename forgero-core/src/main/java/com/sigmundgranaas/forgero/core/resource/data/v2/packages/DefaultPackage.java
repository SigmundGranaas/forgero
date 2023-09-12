package com.sigmundgranaas.forgero.core.resource.data.v2.packages;

import java.util.List;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DependencyData;

public record DefaultPackage(DependencyData dependencies, int priority,
                             Supplier<List<DataResource>> data,
                             String nameSpace, String name) implements DataPackage {
	@Override
	public List<DataResource> loadData() {
		return data.get();
	}
}

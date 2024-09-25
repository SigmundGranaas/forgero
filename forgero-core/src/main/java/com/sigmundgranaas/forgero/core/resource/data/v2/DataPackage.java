package com.sigmundgranaas.forgero.core.resource.data.v2;

import java.util.List;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DependencyData;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.DefaultPackage;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.util.Identifiers;

public interface DataPackage extends Identifiable {
	static DataPackage of(List<DataResource> data) {
		return new DefaultPackage(DependencyData.empty(), 6, () -> data, Identifiers.EMPTY_IDENTIFIER, Identifiers.EMPTY_IDENTIFIER);
	}

	static DataPackage of(DataResource data) {
		return new DefaultPackage(data.dependencies(), 6, () -> List.of(data), data.nameSpace(), data.name());
	}

	int priority();

	DependencyData dependencies();

	List<DataResource> loadData();
}
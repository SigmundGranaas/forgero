package com.sigmundgranaas.forgero.core.resource.data.v2;


import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface DataOverrideSupplier extends Supplier<List<DataResource>>{
	static DataOverrideSupplier of(List<DataResource> res){
		return () -> res;
	}
}

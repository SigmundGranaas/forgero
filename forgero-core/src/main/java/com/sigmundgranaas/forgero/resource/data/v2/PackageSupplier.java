package com.sigmundgranaas.forgero.resource.data.v2;


import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;

import java.util.List;

@FunctionalInterface
public interface PackageSupplier {
    List<DataPackage> supply();
}

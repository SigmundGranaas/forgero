package com.sigmundgranaas.forgero.resource.data.v2;


import java.util.List;

@FunctionalInterface
public interface PackageSupplier {
    List<DataPackage> supply();
}

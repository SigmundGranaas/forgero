package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.data.DataPool;
import com.sigmundgranaas.forgero.resource.data.StateConverter;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.state.State;

import java.util.ArrayList;
import java.util.List;

public class CoreResourceManager {
    private static final List<DataPackage> packages = new ArrayList<>();

    public static void register(DataPackage dataPackage) {
        Forgero.LOGGER.info("Registered {}", dataPackage.name());
        packages.add(dataPackage);
    }

    public static void register(PackageSupplier supplier) {
        var packs = supplier.supply();
        packs.forEach(pack -> Forgero.LOGGER.info("Registered {}", pack.name()));
        packages.addAll(packs);
    }

    public static ResourceRegistry<State> init() {
        Forgero.LOGGER.info("Initialising {} packages", packages.size());
        var data = new DataPool(packages).assemble();
        StateConverter converter = new StateConverter(data.tree());
        data.resources().forEach(converter::convert);

        return ResourceRegistry.of(converter.states(), data.tree());
    }
}

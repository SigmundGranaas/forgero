package com.sigmundgranaas.forgeroforge.test.util;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.resource.data.v2.packages.FilePackageLoader;

import java.util.List;

import static com.sigmundgranaas.forgero.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.resource.ForgeroVanilla.VANILLA_SUPPLIER;
import static com.sigmundgranaas.forgero.resource.data.Constant.*;

public class ForgeroPipeLineSetup {

    public static PackageSupplier EXTENDED_SUPPLIER = () -> List.of(new FilePackageLoader(EXTENDED_PACKAGE).get(), new FilePackageLoader(TRINKETS_PACKAGE).get(), new FilePackageLoader(SWORD_ADDITIONS_PACKAGE).get(), new FilePackageLoader(MINECRAFT_EXTENDED_PACKAGE).get());

    public static void setup() {
        if (ForgeroStateRegistry.COMPOSITES == null) {
            PipelineBuilder
                    .builder()
                    .register(VANILLA_SUPPLIER)
                    .register(EXTENDED_SUPPLIER)
                    .state(stateListener())
                    .state(compositeListener())
                    .inflated(containerListener())
                    .build()
                    .execute();
        }
    }
}

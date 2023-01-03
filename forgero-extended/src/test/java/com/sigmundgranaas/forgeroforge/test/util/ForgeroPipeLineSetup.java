package com.sigmundgranaas.forgeroforge.test.util;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.BuildableConfiguration;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.util.List;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.*;
import static com.sigmundgranaas.forgero.vanilla.ForgeroVanilla.VANILLA_SUPPLIER;

public class ForgeroPipeLineSetup {

    public static PackageSupplier EXTENDED_SUPPLIER = () -> List.of(new FilePackageLoader(EXTENDED_PACKAGE).get(), new FilePackageLoader(TRINKETS_PACKAGE).get(), new FilePackageLoader(SWORD_ADDITIONS_PACKAGE).get(), new FilePackageLoader(MINECRAFT_EXTENDED_PACKAGE).get());

    public static void setup() {
        if (ForgeroStateRegistry.COMPOSITES == null) {
            var config = BuildableConfiguration.builder()
                    .availableDependencies(ImmutableSet.of("minecraft", "forgero", "forgero-vanilla", "forgero-extended", "minecraft-vanilla")).build();

            PipelineBuilder
                    .builder()
                    .register(() -> config)
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

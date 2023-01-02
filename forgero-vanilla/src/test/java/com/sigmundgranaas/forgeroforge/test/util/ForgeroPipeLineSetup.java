package com.sigmundgranaas.forgeroforge.test.util;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.BuildableConfiguration;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.util.List;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.MINECRAFT_PACKAGE;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.VANILLA_PACKAGE;

public class ForgeroPipeLineSetup {

    public static void setup() {
        if (ForgeroStateRegistry.COMPOSITES == null) {
            var config = BuildableConfiguration.builder()
                    .availableDependencies(ImmutableSet.of("minecraft", "forgero", "forgero-vanilla"))
                    .build();

            PipelineBuilder
                    .builder()
                    .register(() -> config)
                    .register(() -> List.of(new FilePackageLoader(MINECRAFT_PACKAGE).get(), new FilePackageLoader(VANILLA_PACKAGE).get()))
                    .state(stateListener())
                    .state(compositeListener())
                    .inflated(containerListener())
                    .build()
                    .execute();
        }
    }
}

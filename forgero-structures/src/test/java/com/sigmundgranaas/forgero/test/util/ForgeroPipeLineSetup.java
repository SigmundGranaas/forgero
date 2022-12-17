package com.sigmundgranaas.forgero.test.util;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;

import java.util.List;

import static com.sigmundgranaas.forgero.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.resource.data.Constant.MINECRAFT_PACKAGE;
import static com.sigmundgranaas.forgero.resource.data.Constant.VANILLA_PACKAGE;

public class ForgeroPipeLineSetup {

    public static void setup() {
        if (ForgeroStateRegistry.COMPOSITES == null) {
            PipelineBuilder
                    .builder()
                    .register(() -> List.of(new FilePackageLoader(MINECRAFT_PACKAGE).get(), new FilePackageLoader(VANILLA_PACKAGE).get()))
                    .state(stateListener())
                    .state(compositeListener())
                    .inflated(containerListener())
                    .build()
                    .execute();
        }
    }
}

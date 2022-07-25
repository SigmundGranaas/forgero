package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.resourceloader.TestResourceLoader;

public class Util {
    public static Runnable INIT_TEST_REGISTRY = () -> ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(new TestResourceLoader());
}

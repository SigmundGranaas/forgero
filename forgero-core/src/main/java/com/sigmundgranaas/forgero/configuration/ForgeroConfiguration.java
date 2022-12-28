package com.sigmundgranaas.forgero.configuration;

import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;

public interface ForgeroConfiguration {
    InputStreamLoader streamLoader();

    ResourceLocator locator();

    PathFinder pathFinder();
}

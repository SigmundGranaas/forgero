package com.sigmundgranaas.forgero.configuration;

import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;
import lombok.Builder;

@Builder
public class BuildableConfiguration implements ForgeroConfiguration {
    private InputStreamLoader loader;

    private ResourceLocator locator;

    private PathFinder finder;

    @Override
    public InputStreamLoader streamLoader() {
        return null;
    }

    @Override
    public ResourceLocator locator() {
        return null;
    }

    @Override
    public PathFinder pathFinder() {
        return null;
    }
}

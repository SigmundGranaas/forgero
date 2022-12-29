package com.sigmundgranaas.forgero.configuration;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;
import lombok.Builder;

@Builder
public class BuildableConfiguration implements ForgeroConfiguration {
    private InputStreamLoader loader;

    private ResourceLocator locator;

    private PathFinder finder;

    private ImmutableSet<String> availableDependencies;

    private ForgeroSettings settings;

    @Override
    public InputStreamLoader streamLoader() {
        return loader;
    }

    @Override
    public ResourceLocator locator() {
        return locator;
    }

    @Override
    public PathFinder pathFinder() {
        return finder;
    }

    @Override
    public ImmutableSet<String> availableDependencies() {
        return availableDependencies;
    }

    @Override
    public ForgeroSettings settings() {
        return settings;
    }


}

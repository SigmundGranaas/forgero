package com.sigmundgranaas.forgero.core.configuration;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;
import lombok.Builder;

@Builder
public class BuildableConfiguration implements ForgeroConfiguration {
    @Builder.Default
    private InputStreamLoader loader = ForgeroConfiguration.DEFAULT.streamLoader();

    @Builder.Default
    private ResourceLocator locator = ForgeroConfiguration.DEFAULT.locator();
    @Builder.Default
    private PathFinder finder = ForgeroConfiguration.DEFAULT.pathFinder();
    @Builder.Default
    private ImmutableSet<String> availableDependencies = ForgeroConfiguration.DEFAULT.availableDependencies();
    @Builder.Default
    private ForgeroSettings settings = ForgeroConfiguration.DEFAULT.settings();

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

package com.sigmundgranaas.forgero.configuration;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;

public interface ForgeroConfiguration {
    InputStreamLoader streamLoader();

    ResourceLocator locator();

    PathFinder pathFinder();

    ForgeroSettings settings();

    ImmutableSet<String> availableDependencies();
}

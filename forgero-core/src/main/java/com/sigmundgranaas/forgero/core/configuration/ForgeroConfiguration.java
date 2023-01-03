package com.sigmundgranaas.forgero.core.configuration;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.core.util.loader.ClassLoader;
import com.sigmundgranaas.forgero.core.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;

public interface ForgeroConfiguration {
    ForgeroConfiguration DEFAULT = new DefaultConfiguration();

    default InputStreamLoader streamLoader() {
        return new ClassLoader();
    }

    default ResourceLocator locator() {
        return PathWalker.builder()
                .pathFinder(pathFinder())
                .pathFinder(pathFinder())
                .depth(10)
                .build();
    }

    default PathFinder pathFinder() {
        return PathFinder::ClassFinder;
    }

    default ForgeroSettings settings() {
        return ForgeroSettings.SETTINGS;
    }

    default ImmutableSet<String> availableDependencies() {
        return ImmutableSet.<String>builder().add("forgero", "minecraft").build();
    }
}

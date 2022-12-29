package com.sigmundgranaas.forgero.configuration;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.settings.ForgeroSettings;
import com.sigmundgranaas.forgero.util.loader.ClassLoader;
import com.sigmundgranaas.forgero.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.util.loader.PathFinder;

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

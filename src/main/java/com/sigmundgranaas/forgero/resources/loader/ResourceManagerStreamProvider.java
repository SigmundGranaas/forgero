package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.InputStreamProvider;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class ResourceManagerStreamProvider implements InputStreamProvider {
    private final ResourceManager manager;

    public ResourceManagerStreamProvider(ResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public Collection<InputStream> getStreams(ForgeroResourceType type) {
        return manager.findResources(getStartingPath(type), path -> true)
                .values()
                .stream()
                .map(this::getInputStream)
                .flatMap(Optional::stream)
                .toList();

    }

    private Optional<InputStream> getInputStream(Resource res) {
        try {
            return Optional.ofNullable(res.getInputStream());
        } catch (IOException e) {
            ForgeroInitializer.LOGGER.error("Error occurred while loading resource json ", e);
            return Optional.empty();
        }
    }

    private String getStartingPath(ForgeroResourceType type) {
        return switch (type) {
            case MATERIAL -> "materials";
            case GEM -> "gems";
            case SCHEMATIC -> "schematic";
            case TOOL -> "tools";
            case TOOL_PART -> "tool_parts";
        };
    }
}

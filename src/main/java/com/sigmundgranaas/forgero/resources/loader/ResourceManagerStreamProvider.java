package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.InputStreamProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

public class ResourceManagerStreamProvider implements InputStreamProvider {
    private final ResourceManager manager;

    public ResourceManagerStreamProvider(ResourceManager manager) {
        this.manager = manager;
    }

    @Override
    public Collection<InputStream> getStreams(ForgeroResourceType type) {
        return manager.findResources(getStartingPath(type), path -> true)
                .stream()
                .map(this::getInputStream)
                .filter(Objects::nonNull)
                .toList();

    }

    @Nullable
    private InputStream getInputStream(Identifier id) {
        try {
            var resource = manager.getResource(id);
            return resource.getInputStream();
        } catch (IOException e) {
            ForgeroInitializer.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
            return null;
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

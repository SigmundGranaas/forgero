package com.sigmundgranaas.forgero.core.resource.data.v2.loading;

import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceCollectionMapper;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ContextData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DefaultResourcePair;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DefaultMapper implements ResourceCollectionMapper {
    private List<DataResource> defaults;

    public DefaultMapper() {
        this.defaults = new ArrayList<>();
    }

    @Override
    public List<DataResource> apply(List<DataResource> resources) {
        defaults = extractDefaults(resources);

        return resources.stream()
                .filter(this::notDefault)
                .map(this::applyDefaults)
                .toList();
    }

    private List<DataResource> extractDefaults(List<DataResource> resources) {
        return resources.stream()
                .filter(this::defaultResource)
                .toList();
    }

    private DataResource applyDefaults(DataResource resource) {
        var resourcePath = resource.context().map(ContextData::path);
        if (resourcePath.isPresent()) {
            var defaultResources = defaults.stream()
                    .filter(defaultResource -> hasOverlappingPath(resourcePath.get(), defaultResource))
                    .toList();
            Optional<DataResource> linkedDefault = linkDefaults(defaultResources);
            return linkedDefault.map(linked -> DefaultResourcePair.applyDefaults(resource, linked)).orElse(resource);
        }

        return resource;
    }

    public Optional<DataResource> linkDefaults(List<DataResource> defaults) {
        return defaults.stream()
                .filter(res -> res.context().isPresent())
                .sorted(Comparator.comparingInt(aDefault -> aDefault.context().get().path().split("\\" + File.separator).length))
                .reduce(DefaultResourcePair::applyDefaults);
    }

    private boolean hasOverlappingPath(String resourcePath, DataResource defaultResource) {
        return defaultResource.context()
                .map(ContextData::path)
                .filter(resourcePath::contains)
                .isPresent();
    }

    private boolean defaultResource(DataResource resource) {
        return resource.context().map(context -> context.fileName().equals("default.json")).orElse(false);
    }

    private boolean notDefault(DataResource resource) {
        return !defaultResource(resource);
    }

}

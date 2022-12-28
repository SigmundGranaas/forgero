package com.sigmundgranaas.forgero.resource.data.v2.packages;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.resource.data.v2.loading.DefaultMapper;
import com.sigmundgranaas.forgero.resource.data.v2.loading.FileResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.util.Identifiers;
import com.sigmundgranaas.forgero.util.loader.PathFinder;

import java.util.List;
import java.util.function.Supplier;

public class FilePackageLoader implements Supplier<DataPackage> {
    private final ResourceLoader loader;

    public FilePackageLoader(String folderPath) {
        ResourceLocator walker = PathWalker.builder()
                .contentFilter(new JsonContentFilter())
                .pathFinder(PathFinder::ClassFinder)
                .build();
        this.loader = FileResourceLoader.of(folderPath, walker, List.of(new DefaultMapper()));
    }

    @Override
    public DataPackage get() {
        List<DataResource> resources = loader.load();
        var packageInfo = resources.stream().filter(resource -> resource.resourceType() == ResourceType.PACKAGE).findAny();
        var dependencies = packageInfo.map(DataResource::dependencies).orElse(ImmutableList.<String>builder().build());
        var priority = packageInfo.map(DataResource::priority).orElse(5);
        var name = packageInfo.map(DataResource::name).orElse(Identifiers.EMPTY_IDENTIFIER);
        var nameSpace = packageInfo.map(DataResource::nameSpace).orElse(Identifiers.EMPTY_IDENTIFIER);
        return new DefaultPackage(dependencies, priority, () -> resources, nameSpace, name);
    }
}

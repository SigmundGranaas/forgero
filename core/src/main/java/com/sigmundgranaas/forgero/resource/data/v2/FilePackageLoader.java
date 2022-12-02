package com.sigmundgranaas.forgero.resource.data.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.ResourceLoader;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;

import java.util.List;
import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.resource.data.v2.data.ResourceType.PACKAGE;
import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

public class FilePackageLoader implements Supplier<DataPackage> {
    private final ResourceLoader loader;

    public FilePackageLoader(String folderPath) {
        this.loader = new JsonResourceLoader(folderPath);
    }

    @Override
    public DataPackage get() {
        List<DataResource> resources = loader.load();
        var packageInfo = resources.stream().filter(resource -> resource.resourceType() == PACKAGE).findAny();
        var dependencies = packageInfo.map(DataResource::dependencies).orElse(ImmutableList.<String>builder().build());
        var priority = packageInfo.map(DataResource::priority).orElse(5);
        var name = packageInfo.map(DataResource::name).orElse(EMPTY_IDENTIFIER);
        var nameSpace = packageInfo.map(DataResource::nameSpace).orElse(EMPTY_IDENTIFIER);
        return new DefaultPackage(dependencies, priority, resources, nameSpace, name);
    }
}

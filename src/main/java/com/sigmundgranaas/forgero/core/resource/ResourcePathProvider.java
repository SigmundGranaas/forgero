package com.sigmundgranaas.forgero.core.resource;

import java.nio.file.Path;
import java.util.List;

public interface ResourcePathProvider {
    List<Path> getPaths(ForgeroResourceType type);
}

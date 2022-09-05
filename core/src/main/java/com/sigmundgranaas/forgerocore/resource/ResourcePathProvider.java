package com.sigmundgranaas.forgerocore.resource;

import java.nio.file.Path;
import java.util.List;

public interface ResourcePathProvider {
    List<Path> getPaths(ForgeroResourceType type);
}

package com.sigmundgranaas.forgero.core.resource.data.v2;

import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface ResourceLocator {
	List<Path> locate(String path);
}

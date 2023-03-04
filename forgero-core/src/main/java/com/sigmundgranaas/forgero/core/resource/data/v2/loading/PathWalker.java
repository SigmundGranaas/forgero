package com.sigmundgranaas.forgero.core.resource.data.v2.loading;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Builder(toBuilder = true)
public class PathWalker implements ResourceLocator {
	@Builder.Default
	private PathFinder pathFinder = (path) -> Optional.empty();
	@Builder.Default
	private Predicate<Path> contentFilter = (path) -> true;
	@Builder.Default
	private int depth = 5;

	@Override
	public List<Path> locate(String location) {
		Optional<Path> rootPath = pathFinder.find(location);
		if (rootPath.isPresent()) {
			Path path = rootPath.get();
			try (var filesStream = Files.walk(path, depth)) {
				return filesStream.filter(contentFilter).toList();
			} catch (IOException e) {
				Forgero.LOGGER.error("Unable to list files from {}", path.toString());
				Forgero.LOGGER.error(e);
			}
		}
		Forgero.LOGGER.error("Unable to locate resources in {}, as the path could not be found", location);
		return Collections.emptyList();
	}
}

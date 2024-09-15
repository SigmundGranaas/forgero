package com.sigmundgranaas.forgero.core.resource.data.v2.loading;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class JsonContentFilter implements Predicate<Path> {
	@Override
	public boolean test(Path path) {
		if (!Files.isRegularFile(path)) {
			return false;
		}

		if (!path.getFileName().toString().endsWith(".json")) {
			return false;
		}
		return true;
	}
}

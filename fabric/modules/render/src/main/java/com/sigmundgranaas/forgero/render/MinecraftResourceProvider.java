// FILE: fabric/modules/render/src/main/java/com/sigmundgranaas/forgero/render/MinecraftResourceProvider.java
package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MinecraftResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftResourceProvider.class);
	private final ResourceManager resourceManager;

	public MinecraftResourceProvider(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	public Set<String> getNamespaces() {
		return resourceManager.getAllNamespaces();
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return resourceManager.findResources(path.path(), id -> id.getPath().endsWith(".json"))
				.keySet()
				.stream()
				.filter(id -> id.getNamespace().equals(path.namespace()))
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()));
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		Identifier mcIdentifier = new Identifier(identifier.namespace(), identifier.path());

		return resourceManager.getResource(mcIdentifier).flatMap(resource -> {
			try {
				return Optional.of(resource.getInputStream());
			} catch (IOException e) {
				LOGGER.warn("Failed to read resource stream for identifier: {}", mcIdentifier, e);
				return Optional.empty();
			}
		});
	}
}

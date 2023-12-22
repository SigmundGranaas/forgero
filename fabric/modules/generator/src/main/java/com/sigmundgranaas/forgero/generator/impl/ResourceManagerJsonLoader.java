package com.sigmundgranaas.forgero.generator.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;

public class ResourceManagerJsonLoader {

	private final ResourceManager resourceManager;

	public ResourceManagerJsonLoader(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	private Collection<Resource> loadJsonData(String path) {
		return resourceManager.findResources(path, p -> p.getPath().endsWith(".json")).values();
	}

	private Stream<JsonElement> streamElements(String path) {
		return loadJsonData(path)
				.stream()
				.map(this::tryParse)
				.flatMap(Optional::stream);
	}

	public Collection<JsonObject> load(String path) {
		return streamElements(path)
				.filter(JsonElement::isJsonObject)
				.map(JsonElement::getAsJsonObject)
				.collect(Collectors.toList());
	}

	private Optional<JsonElement> tryParse(Resource resource) {
		try {
			JsonElement element = new Gson().fromJson(new String(resource.getInputStream().readAllBytes()), JsonElement.class);
			return Optional.of(element);
		} catch (Exception e) {
			Forgero.LOGGER.error(e);
			return Optional.empty();
		}
	}
}

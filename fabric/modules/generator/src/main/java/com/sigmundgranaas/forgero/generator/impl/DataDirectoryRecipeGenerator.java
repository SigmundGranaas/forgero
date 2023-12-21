package com.sigmundgranaas.forgero.generator.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.JsonContentFilter;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;

import net.minecraft.util.Identifier;

public class DataDirectoryRecipeGenerator {
	private final StringReplacer replacer;
	private final VariableToMapTransformer transformer;
	private final String directory;

	public DataDirectoryRecipeGenerator(StringReplacer replacer, VariableToMapTransformer transformer, String directory) {
		this.replacer = replacer;
		this.transformer = transformer;
		this.directory = directory;
	}

	public Collection<IdentifiedJson> generate() {
		return locatePathsInDirectory(directory)
				.stream()
				.map(this::loadJsonFromPath)
				.flatMap(Optional::stream)
				.flatMap(this::convertToIdentifiedJson)
				.collect(Collectors.toList());
	}

	private List<Path> locatePathsInDirectory(String directory) {
		ResourceLocator walker = PathWalker.builder()
				.contentFilter(new JsonContentFilter())
				.pathFinder(PathFinder::ClassLoaderFinder)
				.build();

		return walker.locate(directory);
	}

	private Stream<IdentifiedJson> convertToIdentifiedJson(JsonObject object) {
		return transformer.transformStateMap(object.getAsJsonObject("variables"))
				.stream()
				.map(variables -> createRecipe(copy(object), variables));
	}

	private Optional<JsonObject> loadJsonFromPath(Path path) {
		try {
			String jsonContent = Files.readString(path);
			return Optional.of(new Gson().fromJson(jsonContent, JsonObject.class));
		} catch (IOException e) {
			Forgero.LOGGER.error("Error reading file: " + path, e);
			return Optional.empty();
		}
	}

	private IdentifiedJson createRecipe(JsonObject template, Map<String, Object> variableMap) {
		Identifier id = new Identifier(replacer.applyReplacements(template.get("identifier").getAsString(), variableMap));
		template.remove("identifier");
		template.remove("generator_type");
		template.remove("variables");


		template = new Gson().fromJson(replacer.applyReplacements(template.toString(), variableMap), JsonObject.class);
		return new IdentifiedJson(id, template);
	}

	private JsonObject copy(JsonObject object) {
		return new Gson().fromJson(object.toString(), JsonObject.class);
	}
}

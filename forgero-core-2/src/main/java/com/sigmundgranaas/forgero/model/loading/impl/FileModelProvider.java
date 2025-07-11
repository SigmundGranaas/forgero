package com.sigmundgranaas.forgero.model.loading.impl;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Model;
import com.sigmundgranaas.forgero.model.loading.api.ModelProvider;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class FileModelProvider implements ModelProvider, ResourceConverter<Model> {
	private final ResourceProvider resourceProvider;
	private final ModelTranslator translator;

	public FileModelProvider(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.translator = new ModelTranslator();
	}

	@Override
	public Optional<Model> convert(InputStream stream, OpenIdentifier resourceId) { // resourceId is the raw resource path, e.g., "forgero:models/tools/iron-pickaxe.json"
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			JsonElement modelJson = JsonParser.parseReader(reader);

			if (modelJson == null || modelJson.isJsonNull()) {
				System.err.println("Failed to parse model " + resourceId + ": File is empty or contains only 'null'.");
				return Optional.empty();
			}

			DataResult<ModelDTO> result = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, modelJson);
			if (result.error().isPresent()) {
				System.err.println("Failed to parse model " + resourceId + " due to codec error: " + result.error().get().message());
				return Optional.empty();
			}

			// Normalize the ID before passing it to the translator and creating the Model instance
			String idPath = resourceId.path();
			String normalizedPath = idPath.substring(idPath.indexOf("models/") + "models/".length());
			normalizedPath = normalizedPath.replace(".json", "");
			OpenIdentifier normalizedModelId = new OpenIdentifier(resourceId.namespace(), normalizedPath);

			return result.result().map(dto -> translator.toDomain(normalizedModelId, dto)); // Pass the normalized ID
		} catch (JsonSyntaxException e) {
			System.err.println("Failed to parse model " + resourceId + " due to a JSON syntax error: " + e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			System.err.println("An unexpected error occurred while parsing model " + resourceId + ": " + e.getMessage());
			e.printStackTrace();
			return Optional.empty();
		}
	}

	// This implements ModelProvider, allowing direct fetching of a model by ID (uses ResourceProvider and then converts)
	@Override
	public Optional<Model> get(OpenIdentifier id) { // id here is already normalized, e.g., "forgero:tools/iron-pickaxe"
		// We need to reconstruct the file path from the normalized ID
		OpenIdentifier modelFilePath = new OpenIdentifier(id.namespace(), "models/" + id.path() + ".json");
		return resourceProvider.read(modelFilePath).flatMap(stream -> convert(stream, modelFilePath)); // Pass the full file path ID to convert
	}
}

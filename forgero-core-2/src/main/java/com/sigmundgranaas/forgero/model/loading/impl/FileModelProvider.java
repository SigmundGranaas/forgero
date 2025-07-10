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
import com.sigmundgranaas.forgero.model.loading.impl.dto.CompositeModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.StaticModelDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class FileModelProvider implements ModelProvider {
	private final ResourceProvider resourceProvider;
	private final ModelTranslator translator;
	public FileModelProvider(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.translator = new ModelTranslator();
	}
	@Override
	public Optional<Model> get(OpenIdentifier id) {
		// Corrected the path to include the 'generated' directory
		OpenIdentifier modelPath = new OpenIdentifier(id.namespace(), "models/" + id.path() + ".json");
		return resourceProvider.read(modelPath).flatMap(stream -> parseAndTranslate(stream, id));
	}
	private Optional<Model> parseAndTranslate(InputStream stream, OpenIdentifier id) {
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			JsonElement modelJson = JsonParser.parseReader(reader);

			// Add robustness for null or empty files
			if (modelJson == null || modelJson.isJsonNull()) {
				System.err.println("Failed to parse model " + id + ": File is empty or contains only 'null'.");
				return Optional.empty();
			}

			DataResult<ModelDTO> result = ModelCodecs.MODEL_DTO_CODEC.parse(JsonOps.INSTANCE, modelJson);
			if (result.error().isPresent()) {
				System.err.println("Failed to parse model " + id + ": " + result.error().get().message());
				return Optional.empty();
			}
			return result.result().map(dto -> {
				if (dto instanceof StaticModelDTO staticDto) { return translator.translate(staticDto, id); }
				else if (dto instanceof CompositeModelDTO compositeDto) { return translator.translate(compositeDto, id); }
				return null;
			});
		} catch (JsonSyntaxException e) {
			System.err.println("Failed to parse model " + id + " due to a JSON syntax error: " + e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			System.err.println("An unexpected error occurred while parsing model " + id);
			e.printStackTrace();
			return Optional.empty();
		}
	}
}

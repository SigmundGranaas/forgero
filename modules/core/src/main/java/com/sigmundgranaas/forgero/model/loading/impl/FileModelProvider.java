package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class FileModelProvider implements ResourceConverter<Model> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileModelProvider.class);
	private static final String MODELS_BASE_PATH = "forgero/models/";
	private final ModelTranslator translator;

	public FileModelProvider() {
		this.translator = new ModelTranslator();
	}

	@Override
	public Optional<Model> convert(InputStream stream, OpenIdentifier resourceId) {
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			JsonElement modelJson = JsonParser.parseReader(reader);

			if (modelJson == null || modelJson.isJsonNull()) {
				LOGGER.error("Failed to parse model {}: File is empty or contains only 'null'.", resourceId);
				return Optional.empty();
			}

			DataResult<ModelDTO> result = ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, modelJson);
			if (result.error().isPresent()) {
				LOGGER.error("Failed to parse model {} due to codec error: {}", resourceId, result.error().get().message());
				return Optional.empty();
			}

			// Derive the model's public-facing ID from its file path.
			// e.g., "assets/minecraft/forgero/models/item/oak_handle.json" -> "minecraft:item/oak_handle"
			String path = resourceId.path();
			int basePathIndex = path.indexOf(MODELS_BASE_PATH);
			if (basePathIndex == -1) {
				LOGGER.warn("Model file {} is not in the expected '{}' directory. Skipping.", resourceId, MODELS_BASE_PATH);
				return Optional.empty();
			}
			// We remove the "forgero/models/" part but keep the subdirectory (item, armor, etc.)
			String relativePath = path.substring(basePathIndex + MODELS_BASE_PATH.length()).replace(".json", "");
			OpenIdentifier derivedId = new OpenIdentifier(resourceId.namespace(), relativePath);

			return result.result().map(dto -> translator.toDomain(derivedId, dto));
		} catch (JsonSyntaxException e) {
			LOGGER.error("Failed to parse model {} due to a JSON syntax error: {}", resourceId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing model {}: {}", resourceId, e.getMessage(), e);
			return Optional.empty();
		}
	}
}

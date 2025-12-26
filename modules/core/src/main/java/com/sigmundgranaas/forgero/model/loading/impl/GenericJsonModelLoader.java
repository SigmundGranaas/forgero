package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Generic JSON model loader using composition.
 *
 * This utility class encapsulates the common logic for loading models from JSON files:
 * 1. Parse JSON from input stream
 * 2. Use Mojang Codec to deserialize to DTO
 * 3. Translate DTO to domain model
 * 4. Handle errors consistently
 *
 * This eliminates duplication between FileModelProvider and FileArmorModelProvider.
 *
 * @param <DTO> The data transfer object type
 * @param <M>   The domain model type, must be Identifiable
 */
public class GenericJsonModelLoader<DTO, M extends Identifiable> {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericJsonModelLoader.class);

	private final Codec<DTO> codec;
	private final BiFunction<OpenIdentifier, DTO, M> translator;
	private final String modelType;

	/**
	 * Creates a new generic JSON model loader.
	 *
	 * @param codec      The Mojang codec for deserializing JSON to DTO
	 * @param translator Function to translate DTO to domain model
	 * @param modelType  Description of the model type (for logging)
	 */
	public GenericJsonModelLoader(Codec<DTO> codec, BiFunction<OpenIdentifier, DTO, M> translator, String modelType) {
		this.codec = codec;
		this.translator = translator;
		this.modelType = modelType;
	}

	/**
	 * Loads a model from an input stream.
	 *
	 * @param stream           The input stream containing JSON data
	 * @param resourceId       The resource identifier (file path)
	 * @param pathNormalizer   Function to derive the model ID from the file path
	 * @return An optional containing the model if loaded successfully
	 */
	public Optional<M> load(InputStream stream, OpenIdentifier resourceId, java.util.function.Function<String, OpenIdentifier> pathNormalizer) {
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			// Parse JSON
			JsonElement json = JsonParser.parseReader(reader);

			if (json == null || json.isJsonNull()) {
				LOGGER.error("Failed to parse {} {}: File is empty or contains only 'null'.", modelType, resourceId);
				return Optional.empty();
			}

			// Deserialize using codec
			DataResult<DTO> result = codec.parse(JsonOps.INSTANCE, json);
			if (result.error().isPresent()) {
				LOGGER.error("Failed to parse {} {} due to codec error: {}", modelType, resourceId, result.error().get().message());
				return Optional.empty();
			}

			// Derive model ID from file path
			OpenIdentifier derivedId = pathNormalizer.apply(resourceId.path());

			// Translate DTO to domain model
			return result.result().map(dto -> translator.apply(derivedId, dto));

		} catch (JsonSyntaxException e) {
			LOGGER.error("Failed to parse {} {} due to JSON syntax error: {}", modelType, resourceId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing {} {}: {}", modelType, resourceId, e.getMessage(), e);
			return Optional.empty();
		}
	}
}

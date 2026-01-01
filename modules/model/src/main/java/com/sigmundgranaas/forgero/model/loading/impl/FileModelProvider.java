package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Loads item models from JSON files using pure composition.
 *
 * This class COMPOSES a GenericJsonModelLoader rather than implementing
 * the JSON parsing logic itself. This eliminates duplication with
 * FileArmorModelProvider and demonstrates the composition pattern.
 */
public class FileModelProvider implements ResourceConverter<Model> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileModelProvider.class);
	private static final String MODELS_BASE_PATH = "forgero_models/";

	// Composition: CONTAINS a generic loader, delegates loading to it
	private final GenericJsonModelLoader<ModelDTO, Model> loader;

	/**
	 * Creates a provider with inheritance-aware tag predicates.
	 */
	public FileModelProvider(Supplier<TagResolver> resolverSupplier) {
		ModelTranslator translator = new ModelTranslator(resolverSupplier);
		this.loader = new GenericJsonModelLoader<>(
				ModelCodecs.MODEL_DTO_CODEC_DISPATCHER,
				translator::toDomain,
				"item model"
		);
	}

	/**
	 * Creates a provider with direct-only tag matching (no inheritance).
	 */
	public FileModelProvider() {
		this(null);
	}

	@Override
	public Optional<Model> convert(InputStream stream, OpenIdentifier resourceId) {
		return loader.load(stream, resourceId, this::normalizeModelPath);
	}

	/**
	 * Normalizes the file path to derive the model ID.
	 * E.g., "assets/forgero/forgero_models/item/oak_handle.json" -> "forgero:item/oak_handle"
	 */
	private OpenIdentifier normalizeModelPath(String path) {
		int basePathIndex = path.indexOf(MODELS_BASE_PATH);
		if (basePathIndex == -1) {
			LOGGER.warn("Model file path {} is not in the expected '{}' directory.", path, MODELS_BASE_PATH);
			// Fallback: just remove .json extension
			return new OpenIdentifier("forgero", path.replace(".json", ""));
		}

		// Extract the relative path after forgero_models/
		String relativePath = path.substring(basePathIndex + MODELS_BASE_PATH.length());
		relativePath = relativePath.replace(".json", "");

		// Extract namespace from path if present, otherwise default to "forgero"
		String namespace = "forgero";
		if (path.contains("/") && path.indexOf("/") < basePathIndex) {
			int namespaceEnd = path.indexOf("/", path.indexOf("assets/") + 7);
			if (namespaceEnd > 0 && path.indexOf("assets/") >= 0) {
				namespace = path.substring(path.indexOf("assets/") + 7, namespaceEnd);
			}
		}

		return new OpenIdentifier(namespace, relativePath);
	}
}


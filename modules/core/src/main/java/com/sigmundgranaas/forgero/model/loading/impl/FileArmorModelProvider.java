package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ArmorModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.io.InputStream;
import java.util.Optional;

/**
 * Loads armor models from JSON files.
 *
 * This class has been refactored to use the generic GenericJsonModelLoader,
 * eliminating duplication with FileModelProvider while maintaining the same
 * functionality.
 */
public class FileArmorModelProvider implements ResourceConverter<ArmorModel> {
	private static final String ARMOR_MODELS_PATH_PREFIX = "forgero_models/armor/";

	private final GenericJsonModelLoader<ArmorModelDTO, ArmorModel> loader;

	public FileArmorModelProvider(ResourceProvider resourceProvider) {
		ArmorModelTranslator translator = new ArmorModelTranslator();
		this.loader = new GenericJsonModelLoader<>(
				ArmorModelCodecs.ARMOR_MODEL_DTO_CODEC,
				translator::toDomain,
				"armor model"
		);
	}

	@Override
	public Optional<ArmorModel> convert(InputStream stream, OpenIdentifier resourceId) {
		return loader.load(stream, resourceId, this::normalizeArmorModelPath);
	}

	/**
	 * Normalizes the file path to derive the armor model ID.
	 * E.g., "assets/forgero/forgero_models/armor/iron_helmet.json" -> "forgero:iron_helmet"
	 */
	private OpenIdentifier normalizeArmorModelPath(String path) {
		int prefixIndex = path.indexOf(ARMOR_MODELS_PATH_PREFIX);
		if (prefixIndex == -1) {
			// Fallback: just remove .json extension
			return new OpenIdentifier("forgero", path.replace(".json", ""));
		}

		String relativePath = path.substring(prefixIndex + ARMOR_MODELS_PATH_PREFIX.length());
		relativePath = relativePath.replace(".json", "");

		// Extract namespace from path if present, otherwise default to "forgero"
		String namespace = "forgero";
		if (path.contains("/") && path.indexOf("/") < prefixIndex) {
			int namespaceEnd = path.indexOf("/", path.indexOf("assets/") + 7);
			if (namespaceEnd > 0) {
				namespace = path.substring(path.indexOf("assets/") + 7, namespaceEnd);
			}
		}

		return new OpenIdentifier(namespace, relativePath);
	}
}

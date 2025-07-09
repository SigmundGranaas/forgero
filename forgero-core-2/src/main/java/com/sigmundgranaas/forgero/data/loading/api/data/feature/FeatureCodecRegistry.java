package com.sigmundgranaas.forgero.data.loading.api.data.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants; // Import CodecConstants
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier; // Import OpenIdentifier
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A runtime registry for FeatureData codecs. This allows for dynamic registration of new feature types,
 * for example by addons or other modules, without modifying the core data parsing logic.
 */
public class FeatureCodecRegistry {
	private static final Logger LOGGER = LogManager.getLogger(FeatureCodecRegistry.class);
	// Changed map key from String to OpenIdentifier
	private static final Map<OpenIdentifier, Codec<? extends FeatureData>> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Registers a new feature codec for a given type string.
	 *
	 * @param type  The unique identifier for the feature type (e.g., "forgero:vein_mining").
	 * @param codec The codec responsible for parsing this feature type.
	 */
	public static void register(String typeStr, Codec<? extends FeatureData> codec) { // Keep String for external registration
		OpenIdentifier type = CodecConstants.IDENTIFIER_FACTORY.of(typeStr); // Convert to OpenIdentifier internally
		if (REGISTRY.containsKey(type)) {
			LOGGER.warn("Overwriting feature codec for type: {}", type);
		}
		REGISTRY.put(type, codec);
	}

	/**
	 * Retrieves a codec from the registry for a given type.
	 *
	 * @param typeStr The type identifier of the codec to retrieve (as String).
	 * @return A DataResult containing the codec if found, or an error if not.
	 */
	public static DataResult<Codec<? extends FeatureData>> get(String typeStr) { // Keep String for external lookup
		OpenIdentifier type = CodecConstants.IDENTIFIER_FACTORY.of(typeStr); // Convert to OpenIdentifier internally
		Codec<? extends FeatureData> codec = REGISTRY.get(type);
		if (codec == null) {
			return DataResult.error(() -> "Unknown feature type: " + type);
		}
		return DataResult.success(codec);
	}
}

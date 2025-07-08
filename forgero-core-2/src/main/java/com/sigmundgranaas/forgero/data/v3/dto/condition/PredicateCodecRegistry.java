package com.sigmundgranaas.forgero.data.v3.dto.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A runtime registry for PredicateData codecs. This allows for dynamic registration of new predicate types,
 * for example by addons or other modules, without modifying the core data parsing logic.
 */
public class PredicateCodecRegistry {
	private static final Logger LOGGER = LogManager.getLogger(PredicateCodecRegistry.class);
	private static final Map<OpenIdentifier, Codec<? extends PredicateData>> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * Registers a new predicate codec for a given type string.
	 *
	 * @param type  The unique identifier for the predicate type (e.g., "forgero:self_has_tag").
	 * @param codec The codec responsible for parsing this predicate type.
	 */
	public static void register(String typeStr, Codec<? extends PredicateData> codec) {
		OpenIdentifier type = CodecConstants.IDENTIFIER_FACTORY.of(typeStr);
		if (REGISTRY.containsKey(type)) {
			LOGGER.warn("Overwriting predicate codec for type: {}", type);
		}
		REGISTRY.put(type, codec);
	}

	/**
	 * Retrieves a codec from the registry for a given type.
	 *
	 * @param typeStr The type identifier of the codec to retrieve (as String).
	 * @return A DataResult containing the codec if found, or an error if not.
	 */
	public static DataResult<Codec<? extends PredicateData>> get(String typeStr) {
		OpenIdentifier type = CodecConstants.IDENTIFIER_FACTORY.of(typeStr);
		Codec<? extends PredicateData> codec = REGISTRY.get(type);
		if (codec == null) {
			return DataResult.error(() -> "Unknown predicate type: " + type);
		}
		return DataResult.success(codec);
	}
}

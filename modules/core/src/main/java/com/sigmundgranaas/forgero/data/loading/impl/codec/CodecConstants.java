package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

public class CodecConstants {
	public static final IdentifierFactory IDENTIFIER_FACTORY = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	public static final Codec<OpenIdentifier> OPEN_IDENTIFIER_CODEC = Codec.STRING.xmap(IDENTIFIER_FACTORY::of, OpenIdentifier::toString);

	/**
	 * Codec for identifiers that should preserve the full path (not canonicalized).
	 * Use this for context identifiers like "forgero:contexts/offensive" where the path matters.
	 */
	public static final Codec<OpenIdentifier> FULL_PATH_IDENTIFIER_CODEC = Codec.STRING.xmap(
			CodecConstants::parseFullPathIdentifier,
			OpenIdentifier::toString
	);

	public static final Codec<OpenIdentifier> TAG_IDENTIFIER_CODEC = Codec.STRING.xmap(
			CodecConstants::parseTagIdentifier,
			OpenIdentifier::toString
	);

	public static final Codec<JsonElement> JSON_ELEMENT_CODEC = JsonElementCodec.INSTANCE;

	private static OpenIdentifier parseTagIdentifier(String id) {
		if (id.contains(":")) {
			return OpenIdentifier.parse(id);
		}
		return new OpenIdentifier("forgero", id);
	}

	/**
	 * Parses an identifier without canonicalizing (preserves full path).
	 * Used for context identifiers where the path structure matters.
	 */
	private static OpenIdentifier parseFullPathIdentifier(String id) {
		if (id.contains(":")) {
			return OpenIdentifier.parse(id);
		}
		return new OpenIdentifier("forgero", id);
	}
}

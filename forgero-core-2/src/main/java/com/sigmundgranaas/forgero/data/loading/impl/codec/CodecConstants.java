package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;

/**
 * Provides common codecs and utilities for Forgero data serialization.
 */
public class CodecConstants {
	/**
	 * The central IdentifierFactory instance for creating OpenIdentifiers,
	 * configured with "forgero" as the default namespace.
	 */
	public static final IdentifierFactory IDENTIFIER_FACTORY = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	/**
	 * A Codec for serializing and deserializing OpenIdentifier objects to and from JSON Strings.
	 * It uses the IdentifierFactory to parse strings into OpenIdentifiers
	 * and OpenIdentifier's toString method to serialize OpenIdentifiers into strings.
	 */
	public static final Codec<OpenIdentifier> OPEN_IDENTIFIER_CODEC = Codec.STRING.xmap(IDENTIFIER_FACTORY::of, OpenIdentifier::toString);
}

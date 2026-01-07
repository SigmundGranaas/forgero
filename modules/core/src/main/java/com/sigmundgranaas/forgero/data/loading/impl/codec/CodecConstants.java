package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

public class CodecConstants {
	public static final IdentifierFactory IDENTIFIER_FACTORY = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	public static final Codec<OpenIdentifier> OPEN_IDENTIFIER_CODEC = Codec.STRING.xmap(IDENTIFIER_FACTORY::of, OpenIdentifier::toString);

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
}

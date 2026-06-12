package com.sigmundgranaas.forgero.common.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

/**
 * Public codecs an addon needs when authoring a data-carrying condition, property, or slot — so it
 * never has to reach into {@code data.loading.impl.codec.CodecConstants} (an internal package).
 */
public final class ForgeroCodecs {

	private ForgeroCodecs() {
	}

	/**
	 * Path-preserving codec for identifiers that name a <em>tag, slot type, attribute type or
	 * scope</em> — anything compared by value rather than looked up as a registry id. Forgero keeps
	 * the full path (e.g. {@code forgero:contexts/offensive}); a canonicalizing codec would collapse
	 * it to its last segment and silently never match. Use this for {@code OpenIdentifier} fields in
	 * your own codecs.
	 */
	public static final Codec<OpenIdentifier> IDENTIFIER = CodecConstants.TAG_IDENTIFIER_CODEC;
}

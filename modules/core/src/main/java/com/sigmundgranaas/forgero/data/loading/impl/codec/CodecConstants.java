package com.sigmundgranaas.forgero.data.loading.impl.codec;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Shared identifier codecs. Two parsing behaviours exist; choosing the wrong one is a real
 * equality hazard, because identifiers are routinely compared with {@code equals()}.
 *
 * <h2>Canonicalizing vs path-preserving</h2>
 * <ul>
 *   <li>{@link #OPEN_IDENTIFIER_CODEC} <b>canonicalizes</b>: it strips directories and the file
 *       extension, keeping only the last path segment ({@code forgero:contexts/offensive} →
 *       {@code forgero:offensive}, {@code forgero:materials/iron.json} → {@code forgero:iron}).
 *       Use it <b>only</b> for registry component IDs that are resolved from file paths.</li>
 *   <li>{@link #TAG_IDENTIFIER_CODEC} / {@link #FULL_PATH_IDENTIFIER_CODEC} <b>preserve</b> the
 *       full path. Use these for everything that is compared by value rather than looked up as a
 *       file: tags, scopes, slot/part types, attribute types, condition operands. The two names
 *       are the same behaviour, kept distinct only for call-site readability (tags vs scopes).</li>
 * </ul>
 *
 * <h2>The hazard</h2>
 * Encoding the same logical identifier with a canonicalizing codec in one place and a preserving
 * one in another makes the two forms unequal ({@code offensive} ≠ {@code contexts/offensive}).
 * This has bitten attribute scopes and slot types. The rule above is what keeps them consistent:
 * attribute scopes, slot types and slot tags are now path-preserving through both data loading and
 * COF serialization, so a slot's identity does not change form across a save/load and such values
 * can be compared with a plain {@code equals()}. Only registry component ids (resolved from file
 * paths) canonicalize.
 */
public class CodecConstants {
	public static final IdentifierFactory IDENTIFIER_FACTORY = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	/**
	 * Canonicalizing codec — strips path to the last segment. Registry component IDs only.
	 * See the class javadoc for the equality hazard.
	 */
	public static final Codec<OpenIdentifier> OPEN_IDENTIFIER_CODEC = Codec.STRING.xmap(IDENTIFIER_FACTORY::of, OpenIdentifier::toString);

	/**
	 * Path-preserving codec for context/scope identifiers like {@code forgero:scope/part-composite}.
	 * Identical behaviour to {@link #TAG_IDENTIFIER_CODEC}; named for readability at scope sites.
	 */
	public static final Codec<OpenIdentifier> FULL_PATH_IDENTIFIER_CODEC = Codec.STRING.xmap(
			CodecConstants::parsePreservingPath,
			OpenIdentifier::toString
	);

	/**
	 * Path-preserving codec for tag/type identifiers. Identical behaviour to
	 * {@link #FULL_PATH_IDENTIFIER_CODEC}; named for readability at tag/type sites.
	 */
	public static final Codec<OpenIdentifier> TAG_IDENTIFIER_CODEC = Codec.STRING.xmap(
			CodecConstants::parsePreservingPath,
			OpenIdentifier::toString
	);

	public static final Codec<JsonElement> JSON_ELEMENT_CODEC = JsonElementCodec.INSTANCE;

	/**
	 * Parses an identifier without canonicalizing — the full path is preserved. Bare paths (no
	 * {@code ':'}) are placed in the {@code forgero} namespace.
	 */
	private static OpenIdentifier parsePreservingPath(String id) {
		if (id.contains(":")) {
			return OpenIdentifier.parse(id);
		}
		return new OpenIdentifier("forgero", id);
	}
}

package com.sigmundgranaas.forgero.data.pipeline.impl;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.DefinitionData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.DefinitionCodecRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * A dedicated resource converter that parses a raw JSON stream into a {@link RawDefinition}.
 * It uses a {@link DefinitionCodecRegistry} to look up the correct codec for the definition's "type" field.
 */
public class RawDefinitionConverter implements ResourceConverter<RawDefinition> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RawDefinitionConverter.class);
	private final IdentifierFactory identifierFactory;
	private final DefinitionCodecRegistry codecRegistry;

	public RawDefinitionConverter(IdentifierFactory identifierFactory, DefinitionCodecRegistry codecRegistry) {
		this.identifierFactory = identifierFactory;
		this.codecRegistry = codecRegistry;
	}

	@Override
	public Optional<RawDefinition> convert(java.io.InputStream stream, OpenIdentifier id) {
		try {
			String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			var root = JsonParser.parseString(jsonContent).getAsJsonObject();
			if (!root.has("type") || !root.get("type").isJsonPrimitive()) {
				LOGGER.error("Parsing failed for [{}]: Missing or invalid 'type' field.", id);
				return Optional.empty();
			}
			String typeName = root.get("type").getAsString();
			String typeId = identifierFactory.of(typeName).path();
			Codec<? extends DefinitionData> codec = codecRegistry.codecFor(typeId);

			// Use the id from the file path as the canonical ID
			OpenIdentifier canonicalId = identifierFactory.of(id.namespace(), id.name());
			// Optional top-level merge priority for same-id definitions across packs (default 0).
			int priority = root.has("priority") && root.get("priority").isJsonPrimitive()
					? root.get("priority").getAsInt()
					: 0;
			return codec.parse(JsonOps.INSTANCE, root)
					.map(dto -> new RawDefinition(canonicalId, dto, priority))
					.resultOrPartial(err -> LOGGER.error("Codec parsing failed for [{}]: {}", id, err));
		} catch (IOException | JsonSyntaxException e) {
			LOGGER.error("Failed to parse file [{}]: {}", id, e.getMessage());
			return Optional.empty();
		}
	}
}

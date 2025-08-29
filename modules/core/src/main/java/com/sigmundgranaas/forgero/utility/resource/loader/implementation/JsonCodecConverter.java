package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * A generic ResourceConverter that parses a JSON stream into an object of type T using a given Codec.
 *
 * @param <T> The type of the object to be parsed.
 */
public class JsonCodecConverter<T> implements ResourceConverter<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonCodecConverter.class);
	private final Codec<T> codec;

	public JsonCodecConverter(Codec<T> codec) {
		this.codec = codec;
	}

	@Override
	public Optional<T> convert(InputStream stream, OpenIdentifier id) {
		try (var reader = new InputStreamReader(stream)) {
			var json = JsonParser.parseReader(reader);
			return codec.parse(JsonOps.INSTANCE, json)
					.resultOrPartial(error -> LOGGER.error("Failed to parse resource {}: {}", id, error));
		} catch (JsonSyntaxException e) {
			LOGGER.error("Invalid JSON syntax in resource {}: {}", id, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing resource {}: {}", id, e.getMessage(), e);
			return Optional.empty();
		}
	}
}

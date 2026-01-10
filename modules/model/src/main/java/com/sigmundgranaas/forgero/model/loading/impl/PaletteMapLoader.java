package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PaletteMapLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaletteMapLoader.class);
	private static final Gson GSON = new Gson();
	private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

	private final ResourceProvider resourceProvider;
	private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

	public PaletteMapLoader(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}

	public Map<String, String> load(String reference) {
		return cache.computeIfAbsent(reference, this::loadFromResource);
	}

	private Map<String, String> loadFromResource(String reference) {
		OpenIdentifier identifier = OpenIdentifier.parse(reference);
		String path = "forgero/palette_maps/" + identifier.name() + ".json";
		OpenIdentifier fullPath = new OpenIdentifier(identifier.namespace(), path);

		Optional<InputStream> stream = resourceProvider.read(fullPath);
		if (stream.isEmpty()) {
			LOGGER.warn("Could not find palette_map_ref: {} (looked for {})", reference, fullPath);
			return Collections.emptyMap();
		}

		try (InputStreamReader reader = new InputStreamReader(stream.get(), StandardCharsets.UTF_8)) {
			Map<String, String> result = GSON.fromJson(reader, MAP_TYPE);
			LOGGER.debug("Loaded palette map '{}' with {} entries", reference, result.size());
			return result != null ? result : Collections.emptyMap();
		} catch (Exception e) {
			LOGGER.error("Failed to parse palette_map_ref: {}", reference, e);
			return Collections.emptyMap();
		}
	}
}

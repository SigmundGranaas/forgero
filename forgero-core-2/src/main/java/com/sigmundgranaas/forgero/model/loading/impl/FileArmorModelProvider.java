package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ArmorModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class FileArmorModelProvider implements ResourceConverter<ArmorModel> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileArmorModelProvider.class);
	private final ResourceProvider resourceProvider;
	private final ArmorModelTranslator translator;

	public FileArmorModelProvider(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.translator = new ArmorModelTranslator();
	}

	@Override
	public Optional<ArmorModel> convert(InputStream stream, OpenIdentifier resourceId) {
		try (InputStreamReader reader = new InputStreamReader(stream)) {
			JsonElement modelJson = JsonParser.parseReader(reader);

			if (modelJson == null || modelJson.isJsonNull()) {
				LOGGER.error("Failed to parse armor model {}: File is empty or contains only 'null'.", resourceId);
				return Optional.empty();
			}

			DataResult<ArmorModelDTO> result = ArmorModelCodecs.ARMOR_MODEL_DTO_CODEC.parse(JsonOps.INSTANCE, modelJson);
			if (result.error().isPresent()) {
				LOGGER.error("Failed to parse armor model {} due to codec error: {}", resourceId, result.error().get().message());
				return Optional.empty();
			}

			String idPath = resourceId.path();
			String normalizedPath = idPath.substring(idPath.indexOf("forgero_models/armor/") + "forgero_models/armor/".length());
			normalizedPath = normalizedPath.replace(".json", "");
			OpenIdentifier fileDerivedId = new OpenIdentifier(resourceId.namespace(), normalizedPath);

			return result.result().map(dto -> translator.toDomain(fileDerivedId, dto));
		} catch (JsonSyntaxException e) {
			LOGGER.error("Failed to parse armor model {} due to a JSON syntax error: {}", resourceId, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing armor model {}: {}", resourceId, e.getMessage(), e);
			return Optional.empty();
		}
	}
}

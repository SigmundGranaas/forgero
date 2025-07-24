package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ContextualModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs.PART_MODEL_TEMPLATE_CODEC;

/**
 * Implementation of {@link ModelTemplateProvider} that loads model templates from JSON files
 * located in the `resources/assets/{namespace}/model_templates/` directory.
 */
public class FileModelTemplateProvider implements ModelTemplateProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileModelTemplateProvider.class);
	private final List<PartModelTemplateDTO> partTemplates;
	private final List<ContextualModelTemplateDTO> contextualTemplates;
	private final List<EquipmentModelTemplateDTO> equipmentTemplates;
	private final List<ArmorModelTemplateDTO> armorTemplates;

	public FileModelTemplateProvider(ResourceProvider resourceProvider, String namespace) {
		this.partTemplates = loadTemplates(resourceProvider, namespace, "parts", PART_MODEL_TEMPLATE_CODEC);
		this.contextualTemplates = loadTemplates(resourceProvider, namespace, "contextual", ModelTemplateCodecs.CONTEXTUAL_MODEL_TEMPLATE_CODEC);
		this.equipmentTemplates = loadTemplates(resourceProvider, namespace, "equipment", ModelTemplateCodecs.EQUIPMENT_MODEL_TEMPLATE_CODEC);
		this.armorTemplates = loadTemplates(resourceProvider, namespace, "armor", ModelTemplateCodecs.ARMOR_MODEL_TEMPLATE_CODEC);
		LOGGER.info("Loaded {} part, {} contextual, {} equipment, and {} armor model templates.",
				partTemplates.size(), contextualTemplates.size(), equipmentTemplates.size(), armorTemplates.size());
	}


	private <T> List<T> loadTemplates(ResourceProvider provider, String namespace, String folder, Codec<T> codec) {
		OpenIdentifier root = new OpenIdentifier(namespace, "model_templates/" + folder);
		// Check if the root directory exists before trying to list files
		if (provider.read(root).isEmpty()) {
			LOGGER.debug("Template directory not found, skipping: {}", root);
			return Collections.emptyList();
		}

		return provider.list(root, true)
				.filter(id -> id.path().endsWith(".json"))
				.flatMap(id -> provider.read(id)
						.flatMap(stream -> parse(stream, codec, id))
						.stream()
				)
				.toList();
	}

	private <T> Optional<T> parse(java.io.InputStream stream, Codec<T> codec, OpenIdentifier id) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			var json = JsonParser.parseReader(reader);
			return codec.parse(JsonOps.INSTANCE, json)
					.resultOrPartial(error -> LOGGER.error("Failed to parse template file {}: {}", id, error));
		} catch (JsonSyntaxException e) {
			LOGGER.error("Invalid JSON syntax in template file {}: {}", id, e.getMessage());
			return Optional.empty();
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while parsing template file {}: {}", id, e.getMessage(), e);
			return Optional.empty();
		}
	}


	@Override
	public List<PartModelTemplateDTO> getPartTemplates() {
		return partTemplates;
	}

	@Override
	public List<ContextualModelTemplateDTO> getContextualTemplates() {
		return contextualTemplates;
	}

	@Override
	public List<EquipmentModelTemplateDTO> getEquipmentTemplates() {
		return equipmentTemplates;
	}

	@Override
	public List<ArmorModelTemplateDTO> getArmorTemplates() {
		return armorTemplates;
	}
}

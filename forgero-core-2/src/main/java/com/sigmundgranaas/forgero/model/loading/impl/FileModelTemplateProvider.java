package com.sigmundgranaas.forgero.model.loading.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ContextualModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.JsonCodecConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

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
		this.partTemplates = loadTemplates(resourceProvider, namespace, "parts", ModelTemplateCodecs.PART_MODEL_TEMPLATE_CODEC);
		this.contextualTemplates = loadTemplates(resourceProvider, namespace, "contextual", ModelTemplateCodecs.CONTEXTUAL_MODEL_TEMPLATE_CODEC);
		this.equipmentTemplates = loadTemplates(resourceProvider, namespace, "equipment", ModelTemplateCodecs.EQUIPMENT_MODEL_TEMPLATE_CODEC);
		this.armorTemplates = loadTemplates(resourceProvider, namespace, "armor", ModelTemplateCodecs.ARMOR_MODEL_TEMPLATE_CODEC);
		LOGGER.info("Loaded {} part, {} contextual, {} equipment, and {} armor model templates.",
				partTemplates.size(), contextualTemplates.size(), equipmentTemplates.size(), armorTemplates.size());
	}


	private <T> List<T> loadTemplates(ResourceProvider provider, String namespace, String folder, Codec<T> codec) {
		OpenIdentifier root = new OpenIdentifier(namespace, "model_templates/" + folder);
		// Check if the root directory exists before trying to list files
		if (provider.list(root, false).findAny().isEmpty()) {
			LOGGER.debug("Template directory not found or empty, skipping: {}", root);
			return Collections.emptyList();
		}

		JsonCodecConverter<T> converter = new JsonCodecConverter<>(codec);
		ResourceLoader<T> loader = new ResourceLoader<>(provider, converter);
		return loader.load(root, true).toList();
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

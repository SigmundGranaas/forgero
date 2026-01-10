package com.sigmundgranaas.forgero.model.loading.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.UpgradeModelTemplateDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.JsonCodecConverter;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileModelTemplateProvider implements ModelTemplateProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileModelTemplateProvider.class);

	private final List<PartModelTemplateDTO> itemTemplates;
	private final List<UpgradeModelTemplateDTO> upgradeTemplates;
	private final List<ArmorModelTemplateDTO> armorTemplates;

	public FileModelTemplateProvider(ResourceProvider resourceProvider) {
		PaletteMapLoader paletteMapLoader = new PaletteMapLoader(resourceProvider);

		List<PartModelTemplateDTO> partTemplates = loadAllFromNamespaces(resourceProvider, "item", ModelTemplateCodecs.PART_MODEL_TEMPLATE_CODEC);
		List<EquipmentModelTemplateDTO> equipmentTemplates = loadAllFromNamespaces(resourceProvider, "item", ModelTemplateCodecs.EQUIPMENT_MODEL_TEMPLATE_CODEC);

		this.itemTemplates = new ArrayList<>(partTemplates);
		this.itemTemplates.addAll(equipmentTemplates.stream()
				.map(equip -> new PartModelTemplateDTO(equip.type(), equip.target(), equip.models()))
				.toList());

		List<UpgradeModelTemplateDTO> rawUpgradeTemplates = loadAllFromNamespaces(resourceProvider, "upgrade", ModelTemplateCodecs.UPGRADE_MODEL_TEMPLATE_CODEC);
		this.upgradeTemplates = resolvePaletteMapRefs(rawUpgradeTemplates, paletteMapLoader);

		this.armorTemplates = loadAllFromNamespaces(resourceProvider, "armor", ModelTemplateCodecs.ARMOR_MODEL_TEMPLATE_CODEC);

		LOGGER.info("Loaded {} item, {} upgrade, and {} armor model templates from all namespaces.",
				itemTemplates.size(), upgradeTemplates.size(), armorTemplates.size());
	}

	private List<UpgradeModelTemplateDTO> resolvePaletteMapRefs(List<UpgradeModelTemplateDTO> templates, PaletteMapLoader loader) {
		return templates.stream()
				.map(template -> {
					if (template.palette_map_ref().isPresent() && template.paletteMap().isEmpty()) {
						Map<String, String> resolved = loader.load(template.palette_map_ref().get());
						return template.withResolvedPaletteMap(resolved);
					}
					return template;
				})
				.collect(Collectors.toList());
	}

	private <T> List<T> loadAllFromNamespaces(ResourceProvider provider, String folder, Codec<T> codec) {
		return provider.getNamespaces().stream()
				.map(namespace -> loadTemplatesFromNamespace(provider, namespace, folder, codec))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private <T> List<T> loadTemplatesFromNamespace(ResourceProvider provider, String namespace, String folder, Codec<T> codec) {
		OpenIdentifier root = new OpenIdentifier(namespace, "forgero/models/" + folder);
		JsonCodecConverter<T> converter = new JsonCodecConverter<>(codec);
		ResourceLoader<T> loader = new ResourceLoader<>(provider, converter);
		return loader.load(root, true).toList();
	}

	@Override
	public List<PartModelTemplateDTO> getItemTemplates() {
		return itemTemplates;
	}

	@Override
	public List<UpgradeModelTemplateDTO> getUpgradeTemplates() {
		return upgradeTemplates;
	}

	@Override
	public List<ArmorModelTemplateDTO> getArmorTemplates() {
		return armorTemplates;
	}
}

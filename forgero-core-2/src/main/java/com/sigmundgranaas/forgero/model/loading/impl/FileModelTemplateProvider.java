package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.api.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ContextualModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ModelTemplateProvider} that loads model templates from JSON files
 * located in the `resources/assets/{namespace}/model_templates/` directory.
 */
public class FileModelTemplateProvider implements ModelTemplateProvider {

	private final Map<String, PartModelTemplateDTO> partTemplates;
	private final List<ContextualModelTemplateDTO> contextualTemplates;
	private final List<EquipmentModelTemplateDTO> equipmentTemplates;

	public FileModelTemplateProvider(ResourceProvider resourceProvider, String namespace) {
		this.partTemplates = loadPartTemplates(resourceProvider, namespace);
		this.contextualTemplates = loadContextualTemplates(resourceProvider, namespace);
		this.equipmentTemplates = loadEquipmentTemplates(resourceProvider, namespace);
	}

	private Map<String, PartModelTemplateDTO> loadPartTemplates(ResourceProvider provider, String namespace) {
		OpenIdentifier partsRoot = new OpenIdentifier(namespace, "model_templates/parts");
		return provider.list(partsRoot, true)
				.map(id -> provider.read(id)
						.flatMap(stream -> parse(stream, ModelTemplateCodecs.PART_MODEL_TEMPLATE_CODEC))
						.map(dto -> Map.entry(id.name().replace(".json", ""), dto))
				)
				.flatMap(Optional::stream)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)); // Last one wins on duplicate
	}

	private List<ContextualModelTemplateDTO> loadContextualTemplates(ResourceProvider provider, String namespace) {
		OpenIdentifier contextualRoot = new OpenIdentifier(namespace, "model_templates/contextual");
		return provider.list(contextualRoot, true)
				.flatMap(id -> provider.read(id)
						.flatMap(stream -> parse(stream, ModelTemplateCodecs.CONTEXTUAL_MODEL_TEMPLATE_CODEC))
						.stream()
				)
				.toList();
	}

	private List<EquipmentModelTemplateDTO> loadEquipmentTemplates(ResourceProvider provider, String namespace) {
		OpenIdentifier equipmentRoot = new OpenIdentifier(namespace, "model_templates/equipment");
		return provider.list(equipmentRoot, true)
				.flatMap(id -> provider.read(id)
						.flatMap(stream -> parse(stream, ModelTemplateCodecs.EQUIPMENT_MODEL_TEMPLATE_CODEC))
						.stream()
				)
				.toList();
	}

	private <T> Optional<T> parse(java.io.InputStream stream, Codec<T> codec) {
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			var json = JsonParser.parseReader(reader);
			return codec.parse(JsonOps.INSTANCE, json).resultOrPartial(System.err::println);
		} catch (Exception e) {
			return Optional.empty();
		}
	}


	@Override
	public Map<String, PartModelTemplateDTO> getPartTemplates() {
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
}

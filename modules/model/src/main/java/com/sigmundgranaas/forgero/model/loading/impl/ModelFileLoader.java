package com.sigmundgranaas.forgero.model.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ArmorModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelExtensionCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelExtensionDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.UpgradeModelTemplateDTO;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A unified loader for all Forgero model and model template files.
 * It scans the `assets/<namespace>/forgero/models/` directory across all namespaces,
 * peeks at the "type" field in each JSON file, and dispatches it to the correct parser.
 */
public class ModelFileLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelFileLoader.class);

	private final ResourceProvider resourceProvider;
	private final ModelTranslator itemModelTranslator;
	private final ArmorModelTranslator armorModelTranslator;

	private final List<PartModelTemplateDTO> itemTemplates = new ArrayList<>();
	private final List<UpgradeModelTemplateDTO> upgradeTemplates = new ArrayList<>();
	private final List<ArmorModelTemplateDTO> armorTemplates = new ArrayList<>();
	private final List<Model> manualItemModels = new ArrayList<>();
	private final List<ArmorModel> manualArmorModels = new ArrayList<>();
	private final List<ModelDTO> manualItemModelDTOs = new ArrayList<>();
	private final List<ModelExtensionDTO> modelExtensions = new ArrayList<>();

	/**
	 * Creates a loader with inheritance-aware tag predicates.
	 */
	public ModelFileLoader(ResourceProvider resourceProvider, Supplier<TagResolver> resolverSupplier) {
		this.resourceProvider = resourceProvider;
		this.itemModelTranslator = new ModelTranslator(resolverSupplier);
		this.armorModelTranslator = new ArmorModelTranslator();
	}

	/**
	 * Creates a loader with direct-only tag matching (no inheritance).
	 */
	public ModelFileLoader(ResourceProvider resourceProvider) {
		this(resourceProvider, null);
	}

	public void load() {
		resourceProvider.getNamespaces().forEach(this::loadModelsFromNamespace);
		resolvePaletteMapRefs();
		LOGGER.info("Loaded {} item templates, {} upgrade templates, {} armor templates, {} manual item models, {} manual armor models, and {} model extensions from all namespaces.",
				itemTemplates.size(), upgradeTemplates.size(), armorTemplates.size(), manualItemModels.size(), manualArmorModels.size(), modelExtensions.size());
	}

	private void resolvePaletteMapRefs() {
		PaletteMapLoader paletteMapLoader = new PaletteMapLoader(resourceProvider);
		List<UpgradeModelTemplateDTO> resolved = upgradeTemplates.stream()
				.map(template -> {
					if (template.palette_map_ref().isPresent() && template.paletteMap().isEmpty()) {
						Map<String, String> loadedMap = paletteMapLoader.load(template.palette_map_ref().get());
						return template.withResolvedPaletteMap(loadedMap);
					}
					return template;
				})
				.collect(Collectors.toList());
		upgradeTemplates.clear();
		upgradeTemplates.addAll(resolved);
	}

	private void loadModelsFromNamespace(String namespace) {
		// Scan both forgero_models and model_templates directories
		OpenIdentifier modelsRoot = new OpenIdentifier(namespace, "forgero_models");
		var modelsList = resourceProvider.list(modelsRoot, true).toList();
		if (!modelsList.isEmpty()) {
			LOGGER.info("Found {} model files in namespace '{}' under forgero_models", modelsList.size(), namespace);
			if (LOGGER.isDebugEnabled()) {
				modelsList.forEach(id -> LOGGER.debug("  - {}", id));
			}
		}
		modelsList.forEach(this::parseResource);

		OpenIdentifier templatesRoot = new OpenIdentifier(namespace, "model_templates");
		var templatesList = resourceProvider.list(templatesRoot, true).toList();
		if (!templatesList.isEmpty()) {
			LOGGER.info("Found {} model files in namespace '{}' under model_templates", templatesList.size(), namespace);
		}
		templatesList.forEach(this::parseResource);
	}

	private void parseResource(OpenIdentifier id) {
		resourceProvider.read(id).ifPresent(stream -> {
			try (InputStreamReader reader = new InputStreamReader(stream)) {
				JsonElement element = JsonParser.parseReader(reader);
				if (!element.isJsonObject()) {
					LOGGER.warn("Skipping model file {} as it is not a JSON object.", id);
					return;
				}
				JsonObject json = element.getAsJsonObject();
				if (!json.has("type") || !json.get("type").isJsonPrimitive()) {
					// This is likely a vanilla model file, which is fine to ignore.
					LOGGER.trace("Skipping JSON file {} as it lacks a Forgero 'type' field.", id);
					return;
				}
				String type = json.get("type").getAsString();
				dispatchParse(id, type, json);
			} catch (Exception e) {
				LOGGER.error("Failed to parse model file {}: {}", id, e.getMessage());
			}
		});
	}

	private void dispatchParse(OpenIdentifier id, String type, JsonObject json) {
		switch (type) {
			case "forgero:part_model_template" ->
					ModelTemplateCodecs.PART_MODEL_TEMPLATE_CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> LOGGER.error("Failed to parse template {}: {}", id, err))
							.ifPresent(itemTemplates::add);

			case "forgero:equipment_model_template" ->
					ModelTemplateCodecs.EQUIPMENT_MODEL_TEMPLATE_CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> LOGGER.error("Failed to parse template {}: {}", id, err))
							.map(equip -> new PartModelTemplateDTO(equip.type(), equip.target(), equip.models()))
							.ifPresent(itemTemplates::add);

			case "forgero:upgrade_model_template" ->
					ModelTemplateCodecs.UPGRADE_MODEL_TEMPLATE_CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> LOGGER.error("Failed to parse template {}: {}", id, err))
							.ifPresent(upgradeTemplates::add);

			case "forgero:armor_model_template" ->
					ModelTemplateCodecs.ARMOR_MODEL_TEMPLATE_CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> LOGGER.error("Failed to parse template {}: {}", id, err))
							.ifPresent(armorTemplates::add);

			case "forgero:model_extension" ->
					ModelExtensionCodecs.MODEL_EXTENSION_CODEC.parse(JsonOps.INSTANCE, json)
							.resultOrPartial(err -> LOGGER.error("Failed to parse model extension {}: {}", id, err))
							.ifPresent(modelExtensions::add);

			case "forgero:armor_model" -> parseManualArmorModel(id, json);
			default -> parseManualItemModel(id, json);
		}
	}

	private void parseManualArmorModel(OpenIdentifier id, JsonObject json) {
		String path = id.path();
		String normalizedPath;
		if (path.contains("forgero_models/")) {
			normalizedPath = path.substring(path.indexOf("forgero_models/") + "forgero_models/".length());
		} else if (path.contains("model_templates/")) {
			normalizedPath = path.substring(path.indexOf("model_templates/") + "model_templates/".length());
		} else {
			LOGGER.warn("Skipping armor model {}: path does not contain expected directory prefix", id);
			return;
		}
		OpenIdentifier derivedId = new OpenIdentifier(id.namespace(), normalizedPath);

		ArmorModelCodecs.ARMOR_MODEL_DTO_CODEC.parse(JsonOps.INSTANCE, json)
				.resultOrPartial(err -> LOGGER.error("Failed to parse manual armor model {}: {}", id, err))
				.map(dto -> armorModelTranslator.toDomain(derivedId, dto))
				.ifPresent(manualArmorModels::add);
	}

	private void parseManualItemModel(OpenIdentifier id, JsonObject json) {
		String path = id.path();
		String normalizedPath;
		if (path.contains("forgero_models/")) {
			normalizedPath = path.substring(path.indexOf("forgero_models/") + "forgero_models/".length());
		} else if (path.contains("model_templates/")) {
			normalizedPath = path.substring(path.indexOf("model_templates/") + "model_templates/".length());
		} else {
			LOGGER.warn("Skipping item model {}: path does not contain expected directory prefix", id);
			return;
		}
		OpenIdentifier derivedId = new OpenIdentifier(id.namespace(), normalizedPath);

		ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, json)
				.resultOrPartial(err -> LOGGER.error("Failed to parse manual item model {}: {}", id, err))
				.ifPresent(dto -> {
					// Store raw DTO with ID embedded for extension merging
					ModelDTO withId = new ModelDTO(
							derivedId.toString(),
							dto.type(),
							dto.layers(),
							dto.slots(),
							dto.mountPoints(),
							dto.texture(),
							dto.textures(),
							dto.target(),
							dto.context(),
							dto.parent(),
							dto.display()
					);
					manualItemModelDTOs.add(withId);

					// Also translate and store domain model for backward compatibility
					manualItemModels.add(itemModelTranslator.toDomain(derivedId, dto));
				});
	}

	public List<PartModelTemplateDTO> getItemTemplates() { return itemTemplates; }
	public List<UpgradeModelTemplateDTO> getUpgradeTemplates() { return upgradeTemplates; }
	public List<ArmorModelTemplateDTO> getArmorTemplates() { return armorTemplates; }
	public List<Model> getManualItemModels() { return manualItemModels; }
	public List<ArmorModel> getManualArmorModels() { return manualArmorModels; }
	public List<ModelDTO> getManualItemModelDTOs() { return manualItemModelDTOs; }
	public List<ModelExtensionDTO> getModelExtensions() { return modelExtensions; }
}

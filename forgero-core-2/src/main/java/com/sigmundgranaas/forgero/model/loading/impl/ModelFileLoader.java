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
import com.sigmundgranaas.forgero.model.loading.impl.codec.ModelTemplateCodecs;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.UpgradeModelTemplateDTO;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

	public ModelFileLoader(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.itemModelTranslator = new ModelTranslator();
		this.armorModelTranslator = new ArmorModelTranslator();
	}

	public void load() {
		resourceProvider.getNamespaces().forEach(this::loadModelsFromNamespace);
		LOGGER.info("Loaded {} item templates, {} upgrade templates, {} armor templates, {} manual item models, and {} manual armor models from all namespaces.",
				itemTemplates.size(), upgradeTemplates.size(), armorTemplates.size(), manualItemModels.size(), manualArmorModels.size());
	}

	private void loadModelsFromNamespace(String namespace) {
		OpenIdentifier root = new OpenIdentifier(namespace, "forgero/models");
		resourceProvider.list(root, true).forEach(this::parseResource);
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

			case "forgero:armor_model" -> parseManualArmorModel(id, json);
			default -> parseManualItemModel(id, json);
		}
	}

	private void parseManualArmorModel(OpenIdentifier id, JsonObject json) {
		String path = id.path();
		String normalizedPath = path.substring(path.indexOf("forgero/models/") + "forgero/models/".length()).replace(".json", "");
		OpenIdentifier derivedId = new OpenIdentifier(id.namespace(), normalizedPath);

		ArmorModelCodecs.ARMOR_MODEL_DTO_CODEC.parse(JsonOps.INSTANCE, json)
				.resultOrPartial(err -> LOGGER.error("Failed to parse manual armor model {}: {}", id, err))
				.map(dto -> armorModelTranslator.toDomain(derivedId, dto))
				.ifPresent(manualArmorModels::add);
	}

	private void parseManualItemModel(OpenIdentifier id, JsonObject json) {
		String path = id.path();
		String normalizedPath = path.substring(path.indexOf("forgero/models/") + "forgero/models/".length()).replace(".json", "");
		OpenIdentifier derivedId = new OpenIdentifier(id.namespace(), normalizedPath);

		ModelCodecs.MODEL_DTO_CODEC_DISPATCHER.parse(JsonOps.INSTANCE, json)
				.resultOrPartial(err -> LOGGER.error("Failed to parse manual item model {}: {}", id, err))
				.map(dto -> itemModelTranslator.toDomain(derivedId, dto))
				.ifPresent(manualItemModels::add);
	}

	public List<PartModelTemplateDTO> getItemTemplates() { return itemTemplates; }
	public List<UpgradeModelTemplateDTO> getUpgradeTemplates() { return upgradeTemplates; }
	public List<ArmorModelTemplateDTO> getArmorTemplates() { return armorTemplates; }
	public List<Model> getManualItemModels() { return manualItemModels; }
	public List<ArmorModel> getManualArmorModels() { return manualArmorModels; }
}

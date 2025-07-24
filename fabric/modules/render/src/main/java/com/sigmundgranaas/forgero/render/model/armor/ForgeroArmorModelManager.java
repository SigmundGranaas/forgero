package com.sigmundgranaas.forgero.render.model.armor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeroArmorModelManager {
	private final EntityModelLoader modelLoader;
	private record ModelCacheKey(Identifier modelId, EquipmentSlot slot) {}
	private final Map<ModelCacheKey, EntityModel<? extends LivingEntity>> modelCache = new ConcurrentHashMap<>();

	public ForgeroArmorModelManager(EntityModelLoader modelLoader) {
		this.modelLoader = modelLoader;
	}

	@SuppressWarnings("unchecked")
	public <T extends LivingEntity> Optional<EntityModel<T>> getModel(Identifier modelIdentifier, EquipmentSlot slot, @Nullable BipedEntityModel<T> contextModel) {
		ModelCacheKey cacheKey = new ModelCacheKey(modelIdentifier, slot);

		EntityModel<? extends LivingEntity> cachedModel = modelCache.computeIfAbsent(cacheKey, key -> {
			// Determine which model layer to use.
			// This handles the vanilla case correctly.
			EntityModelLayer layer = getVanillaArmorLayer(key.slot);

			// Attempt to load a custom model first.
			ModelPart modelPart = loadCustomModelPart(key.modelId);

			// If a custom model part is found, use it. Otherwise, fall back to the vanilla layer.
			if (modelPart != null) {
				// Wrap the custom part. This assumes the custom model is dilated correctly for its intended slot.
				// This is a reasonable assumption for a data-driven system.
				return new CustomArmorModel<>(modelPart);
			} else {
				// Load the appropriate, pre-dilated vanilla armor model part.
				ModelPart vanillaArmorPart = modelLoader.getModelPart(layer);
				// Wrap it. This model is already an ArmorEntityModel, so it's guaranteed to be correct.
				return new CustomArmorModel<>(vanillaArmorPart);
			}
		});

		return Optional.of((EntityModel<T>) cachedModel);
	}

	/**
	 * Returns the appropriate vanilla EntityModelLayer based on the equipment slot.
	 * This correctly handles vanilla's armor dilation.
	 */
	private EntityModelLayer getVanillaArmorLayer(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS ? EntityModelLayers.PLAYER_INNER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR;
	}

	/**
	 * Attempts to load a ModelPart from a custom-defined EntityModelLayer.
	 * This allows your model files to specify a custom model like "forgero:my_cool_helmet".
	 * Returns null if no such custom model is found.
	 */
	@Nullable
	private ModelPart loadCustomModelPart(Identifier id) {
		// If the model is a vanilla one, don't treat it as a custom model.
		if ("minecraft".equals(id.getNamespace())) {
			return null;
		}

		// Try to load a custom model. Your data would define an entity model layer
		// like "forgero:iron-helmet" with a "main" part.
		String[] layerNames = {"main", "outer"}; // Check for common layer part names
		for (String layerName : layerNames) {
			try {
				EntityModelLayer layer = new EntityModelLayer(id, layerName);
				return modelLoader.getModelPart(layer);
			} catch (IllegalArgumentException e) {
				// This is expected if the layer doesn't exist. Continue to the next name.
			}
		}
		// No custom model part found for any common layer name.
		return null;
	}
}

package com.sigmundgranaas.forgero.armor.client.model;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.armor.ClientArmorInitializer;
import com.sigmundgranaas.forgero.armor.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;
import com.sigmundgranaas.forgero.model.resolution.impl.RecursiveModelResolver;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ForgeroItemModelOverrides extends ModelOverrideList {
	// This is the vanilla class responsible for creating geometry from layers.
	private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

	private final Baker baker;
	private final Function<SpriteIdentifier, Sprite> textureGetter;
	private final ModelBakeSettings settings;
	private final Identifier modelId;
	private final RecursiveModelResolver resolver;
	private final Map<String, BakedModel> modelCache = new ConcurrentHashMap<>();

	public ForgeroItemModelOverrides(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId) {
		super(baker, null, Collections.emptyList());
		this.baker = baker;
		this.textureGetter = textureGetter;
		this.settings = settings;
		this.modelId = modelId;
		this.resolver = new RecursiveModelResolver(ClientArmorInitializer.modelRegistry);
	}

	@Nullable
	@Override
	public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		if (!(stack.getItem() instanceof ForgeroHostItem host)) {
			return model;
		}

		Component component = host.getForgeroComponent();
		String cacheKey = generateCacheKey(component);

		return modelCache.computeIfAbsent(cacheKey, key -> bakeModel(component));
	}

	private String generateCacheKey(Component component) {
		return component.id().toString() + component.getChildren().stream().map(child -> child.id().toString()).collect(Collectors.joining());
	}

	private String determineParent(Component component) {
		Set<String> tags = component.getTags().stream().map(OpenIdentifier::name).collect(Collectors.toSet());
		if (tags.contains("tool") || tags.contains("sword") || tags.contains("pickaxe") || tags.contains("axe") || tags.contains("shovel") || tags.contains("hoe")) {
			return "minecraft:item/handheld";
		}
		return "minecraft:item/generated";
	}

	private BakedModel bakeModel(Component component) {
		List<RenderableTexture> textures = resolver.resolve(component).orElse(Collections.emptyList());
		if (textures.isEmpty()) {
			return null;
		}

		// Step 1: Create the raw JSON structure in memory
		JsonObject root = new JsonObject();
		String parentModel = determineParent(component);
		root.addProperty("parent", parentModel);
		root.addProperty("gui_light", "front");
		JsonObject textureJson = new JsonObject();
		for (int i = 0; i < textures.size(); i++) {
			textureJson.addProperty("layer" + i, textures.get(i).texture());
		}
		root.add("textures", textureJson);

		try {
			// Step 2: Deserialize the JSON to a data-only unbaked model
			JsonUnbakedModel rawModel = JsonUnbakedModel.deserialize(root.toString());
			rawModel.id = modelId.toString();

			JsonUnbakedModel geometryModel = ITEM_MODEL_GENERATOR.create(textureGetter, rawModel);

			applyCustomTransformations(geometryModel.getElements(), textures);

			geometryModel.setParents(baker::getOrLoadModel);
			return geometryModel.bake(baker, textureGetter, settings, modelId);

		} catch (Exception e) {
			ClientArmorInitializer.LOGGER.error("Failed to bake dynamic Forgero model for {}. Reason: {}", modelId, e.getMessage());
			return null;
		}
	}

	/**
	 * Applies custom transformations to the generated model elements,
	 * replicating the logic from your old implementation for offsets and z-fighting prevention.
	 */
	private void applyCustomTransformations(List<ModelElement> elements, List<RenderableTexture> textures) {
		if (elements.size() != textures.size()) {
			// The generator might produce a different number of elements than textures,
			// so we can't reliably map them 1-to-1.
			// A more robust solution would be to map element.faces.get(direction).textureId back to a layer index.
			// For now, we'll assume a direct mapping if sizes match.
			return;
		}

		for (int i = 0; i < elements.size(); i++) {
			ModelElement element = elements.get(i);
			RenderableTexture texture = textures.get(i);

			// Apply Z-fighting prevention
			float z_offset = -0.001f * texture.order();
			element.from.add(0, 0, z_offset);
			element.to.add(0, 0, z_offset);


			// Apply X/Y offsets
			var offset = texture.offset();
			if (offset.x() != 0 || offset.y() != 0) {
				// The generator creates quads from (0,0) to (16,16).
				// It flips the y-axis, so we must negate our y-offset.
				element.from.add(offset.x(), -offset.y(), 0);
				element.to.add(offset.x(), -offset.y(), 0);
			}
		}
	}
}

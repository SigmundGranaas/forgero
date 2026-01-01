package com.sigmundgranaas.forgero.model.util;

import com.sigmundgranaas.forgero.model.api.Layered;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.api.ModelVariant;
import com.sigmundgranaas.forgero.model.api.Offset;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * Modular utility for collecting layers from Layered models using composition.
 *
 * This utility works with ANY model that implements the Layered capability,
 * demonstrating how composition enables code reuse without inheritance.
 */
public class LayerCollector {
	/**
	 * Collects all renderable textures from a layered model.
	 *
	 * @param layered The layered model (can be ArmorModel, CompositeModel, or any Layered)
	 * @param context The resolution context for evaluating variants
	 * @return A list of renderable textures in rendering order
	 */
	public static List<RenderableTexture> collectTextures(Layered layered, ModelResolutionContext context) {
		List<RenderableTexture> textures = new ArrayList<>();

		for (ModelLayer layer : layered.layers()) {
			textures.add(resolveLayer(layer, context, 0));
		}

		return textures;
	}

	/**
	 * Collects textures with a base order offset.
	 *
	 * @param layered    The layered model
	 * @param context    The resolution context
	 * @param baseOrder  The base rendering order to add to each layer
	 * @return A list of renderable textures with adjusted ordering
	 */
	public static List<RenderableTexture> collectTextures(Layered layered, ModelResolutionContext context, int baseOrder) {
		List<RenderableTexture> textures = new ArrayList<>();

		for (ModelLayer layer : layered.layers()) {
			textures.add(resolveLayer(layer, context, baseOrder));
		}

		return textures;
	}

	/**
	 * Resolves a single layer to a renderable texture, handling variants.
	 */
	private static RenderableTexture resolveLayer(ModelLayer layer, ModelResolutionContext context, int baseOrder) {
		String texture = layer.texture();
		Offset offset = layer.offset().orElse(new Offset(0, 0));

		// Check for active variant
		var activeVariant = layer.getActiveVariant(context);
		if (activeVariant.isPresent()) {
			ModelVariant variant = activeVariant.get();
			texture = variant.texture().orElse(texture);
			offset = variant.offset().orElse(offset);
		}

		return new RenderableTexture(texture, baseOrder + layer.order(), offset);
	}
}

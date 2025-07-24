package com.sigmundgranaas.forgero.model.generation.api;

import com.sigmundgranaas.forgero.model.generation.api.item.ItemModelGenerator;

/**
 * A record representing a single, self-contained instruction for generating a texture.
 * This is a pure data object produced by the {@link ItemModelGenerator} and consumed
 * by the {@link com.sigmundgranaas.forgero.model.texture.api.TextureGenerator}.
 *
 * @param template The identifier of the greyscale template image (e.g., "forgero:texture_template/part/pickaxe_head").
 * @param palette  The identifier of the palette image (e.g., "forgero:palette/iron").
 * @param output   The identifier and path for the final output PNG (e.g., "forgero:item/iron-pickaxe_head").
 */
public record TextureGenerationTask(
		String template,
		String palette,
		String output
) {
}

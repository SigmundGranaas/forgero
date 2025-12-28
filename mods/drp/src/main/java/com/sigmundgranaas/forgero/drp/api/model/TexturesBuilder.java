package com.sigmundgranaas.forgero.drp.api.model;

import com.sigmundgranaas.forgero.drp.impl.builder.TexturesBuilderImpl;

import java.util.Map;

/**
 * Builder for model texture assignments.
 */
public interface TexturesBuilder {

	/**
	 * Creates a new textures builder.
	 *
	 * @return A new builder instance
	 */
	static TexturesBuilder create() {
		return new TexturesBuilderImpl();
	}

	/**
	 * Sets the layer0 texture (primary texture for item/generated).
	 *
	 * @param textureId The texture identifier (e.g., "forgero:item/iron_blade")
	 * @return This builder for chaining
	 */
	TexturesBuilder layer0(String textureId);

	/**
	 * Sets the layer1 texture (secondary/overlay texture).
	 *
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	TexturesBuilder layer1(String textureId);

	/**
	 * Sets the layer2 texture.
	 *
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	TexturesBuilder layer2(String textureId);

	/**
	 * Sets a numbered layer texture.
	 *
	 * @param layer     The layer number (0-7)
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	TexturesBuilder layer(int layer, String textureId);

	/**
	 * Sets a custom texture variable.
	 *
	 * @param name      The texture variable name
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	TexturesBuilder variable(String name, String textureId);

	/**
	 * Sets the particle texture.
	 *
	 * @param textureId The texture identifier
	 * @return This builder for chaining
	 */
	TexturesBuilder particle(String textureId);

	/**
	 * Gets all texture mappings.
	 *
	 * @return Map of texture variable name to texture identifier
	 */
	Map<String, String> getTextures();
}

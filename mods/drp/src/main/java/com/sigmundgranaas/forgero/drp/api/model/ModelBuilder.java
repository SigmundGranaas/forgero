package com.sigmundgranaas.forgero.drp.api.model;

import com.sigmundgranaas.forgero.drp.impl.builder.ModelBuilderImpl;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for item and block models.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * pack.addModel(
 *     new Identifier("forgero", "item/iron_blade"),
 *     model -> model
 *         .parent("item/generated")
 *         .textures(tex -> tex
 *             .layer0("forgero:item/iron_blade")
 *         )
 * );
 * }</pre>
 */
public interface ModelBuilder {

	/**
	 * Creates a new model builder.
	 *
	 * @return A new builder instance
	 */
	static ModelBuilder create() {
		return new ModelBuilderImpl();
	}

	/**
	 * Sets the parent model.
	 *
	 * @param parent The parent model path (e.g., "item/generated", "item/handheld")
	 * @return This builder for chaining
	 */
	ModelBuilder parent(String parent);

	/**
	 * Configures the model textures.
	 *
	 * @param configurator Lambda to configure textures
	 * @return This builder for chaining
	 */
	ModelBuilder textures(Consumer<TexturesBuilder> configurator);

	/**
	 * Sets the textures directly.
	 *
	 * @param textures The configured textures builder
	 * @return This builder for chaining
	 */
	ModelBuilder textures(TexturesBuilder textures);

	/**
	 * Adds a model override (predicate-based model switching).
	 *
	 * @param configurator Lambda to configure the override
	 * @return This builder for chaining
	 */
	ModelBuilder addOverride(Consumer<ModelOverrideBuilder> configurator);

	/**
	 * Sets display transformations.
	 *
	 * @param displayType The display type (e.g., "gui", "ground", "thirdperson_righthand")
	 * @param rotation    The rotation values [x, y, z]
	 * @param translation The translation values [x, y, z]
	 * @param scale       The scale values [x, y, z]
	 * @return This builder for chaining
	 */
	ModelBuilder display(String displayType, float[] rotation, float[] translation, float[] scale);

	/**
	 * Gets the parent model path.
	 *
	 * @return The parent path, or null if not set
	 */
	String getParent();

	/**
	 * Gets the textures builder.
	 *
	 * @return The textures builder
	 */
	TexturesBuilder getTextures();

	/**
	 * Gets the model overrides.
	 *
	 * @return List of override builders
	 */
	List<ModelOverrideBuilder> getOverrides();

	/**
	 * Gets the display settings.
	 *
	 * @return Map of display type to settings
	 */
	Map<String, DisplaySettings> getDisplay();

	/**
	 * Display settings for a model.
	 */
	interface DisplaySettings {
		float[] rotation();
		float[] translation();
		float[] scale();
	}
}

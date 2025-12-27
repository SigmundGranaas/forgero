package com.sigmundgranaas.forgero.drp.api;

import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.TextureEntry;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Represents a dynamic resource pack that can be modified at runtime.
 *
 * <p>This interface provides methods for adding various types of resources
 * using strongly-typed builders that ensure correctness at compile time.</p>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods are thread-safe during the registration phase. After sealing,
 * the pack becomes immutable.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * DynamicResourcePack pack = DRPApi.getInstance()
 *     .createPack("mymod:dynamic")
 *     .build();
 *
 * pack.addTag(TagBuilder.items("mymod:tools")
 *     .add("mymod:custom_pickaxe"));
 *
 * pack.addShapelessRecipe(new Identifier("mymod", "recipe"),
 *     builder -> builder
 *         .addIngredient("minecraft:iron_ingot")
 *         .result("mymod:item", 1));
 * }</pre>
 */
public interface DynamicResourcePack {

	/**
	 * Gets the identifier for this resource pack.
	 *
	 * @return The pack identifier
	 */
	Identifier getId();

	// ============================================================
	// Tag Resources
	// ============================================================

	/**
	 * Adds a tag built with the given builder.
	 *
	 * @param builder A configured tag builder
	 * @return This pack for chaining
	 */
	DynamicResourcePack addTag(TagBuilder<?> builder);

	// ============================================================
	// Recipe Resources
	// ============================================================

	/**
	 * Adds a recipe built with the given builder.
	 *
	 * @param id      The recipe identifier
	 * @param builder A configured recipe builder
	 * @return This pack for chaining
	 */
	DynamicResourcePack addRecipe(Identifier id, RecipeBuilder<?> builder);

	/**
	 * Adds a shapeless recipe using a builder configuration lambda.
	 *
	 * @param id           The recipe identifier
	 * @param configurator Lambda to configure the recipe
	 * @return This pack for chaining
	 */
	DynamicResourcePack addShapelessRecipe(Identifier id, Consumer<ShapelessRecipeBuilder> configurator);

	/**
	 * Adds a shaped recipe using a builder configuration lambda.
	 *
	 * @param id           The recipe identifier
	 * @param configurator Lambda to configure the recipe
	 * @return This pack for chaining
	 */
	DynamicResourcePack addShapedRecipe(Identifier id, Consumer<ShapedRecipeBuilder> configurator);

	/**
	 * Adds a smithing recipe using a builder configuration lambda.
	 *
	 * @param id           The recipe identifier
	 * @param configurator Lambda to configure the recipe
	 * @return This pack for chaining
	 */
	DynamicResourcePack addSmithingRecipe(Identifier id, Consumer<SmithingRecipeBuilder> configurator);

	// ============================================================
	// Model Resources
	// ============================================================

	/**
	 * Adds an item model built with the given builder.
	 *
	 * @param id      The model identifier (e.g., "forgero:item/iron_pickaxe")
	 * @param builder A configured model builder
	 * @return This pack for chaining
	 */
	DynamicResourcePack addModel(Identifier id, ModelBuilder builder);

	/**
	 * Adds an item model using a builder configuration lambda.
	 *
	 * @param id           The model identifier
	 * @param configurator Lambda to configure the model
	 * @return This pack for chaining
	 */
	DynamicResourcePack addModel(Identifier id, Consumer<ModelBuilder> configurator);

	// ============================================================
	// Language Resources
	// ============================================================

	/**
	 * Adds language entries for the specified locale.
	 *
	 * @param locale  The locale code (e.g., "en_us")
	 * @param builder A configured language builder
	 * @return This pack for chaining
	 */
	DynamicResourcePack addLanguage(String locale, LanguageBuilder builder);

	/**
	 * Adds language entries using a builder configuration lambda.
	 *
	 * @param locale       The locale code (e.g., "en_us")
	 * @param configurator Lambda to configure the translations
	 * @return This pack for chaining
	 */
	DynamicResourcePack addLanguage(String locale, Consumer<LanguageBuilder> configurator);

	// ============================================================
	// Texture Resources
	// ============================================================

	/**
	 * Adds a texture to the pack.
	 *
	 * @param id    The texture identifier (e.g., "forgero:item/iron_blade")
	 * @param image The texture image
	 * @return This pack for chaining
	 */
	DynamicResourcePack addTexture(Identifier id, BufferedImage image);

	/**
	 * Adds a texture entry to the pack.
	 *
	 * @param entry A configured texture entry
	 * @return This pack for chaining
	 */
	DynamicResourcePack addTexture(TextureEntry entry);

	/**
	 * Adds entries to a texture atlas.
	 *
	 * @param atlasId The atlas identifier (e.g., "minecraft:blocks")
	 * @param builder A configured atlas builder
	 * @return This pack for chaining
	 */
	DynamicResourcePack addAtlas(Identifier atlasId, AtlasBuilder builder);

	/**
	 * Configures an atlas using a builder configuration lambda.
	 *
	 * @param atlasId      The atlas identifier
	 * @param configurator Lambda to configure the atlas
	 * @return This pack for chaining
	 */
	DynamicResourcePack addAtlas(Identifier atlasId, Consumer<AtlasBuilder> configurator);

	// ============================================================
	// Raw Data Access
	// ============================================================

	/**
	 * Adds raw JSON data at the specified path.
	 * Use this for resource types not covered by typed builders.
	 *
	 * @param path     The full resource path (e.g., "data/forgero/loot_tables/...")
	 * @param jsonData The JSON content as bytes
	 * @return This pack for chaining
	 */
	DynamicResourcePack addRawData(String path, byte[] jsonData);

	/**
	 * Adds raw asset data at the specified path.
	 *
	 * @param id   The asset identifier
	 * @param data The asset content as bytes
	 * @return This pack for chaining
	 */
	DynamicResourcePack addRawAsset(Identifier id, byte[] data);

	// ============================================================
	// Lifecycle
	// ============================================================

	/**
	 * Marks this pack as complete. After calling this, no more resources
	 * can be added and the pack is ready for registration.
	 *
	 * @return This pack (now immutable)
	 */
	DynamicResourcePack seal();

	/**
	 * Returns whether this pack has been sealed.
	 *
	 * @return true if sealed, false if still accepting resources
	 */
	boolean isSealed();

	/**
	 * Clears all resources from this pack.
	 * Only valid if the pack has not been sealed.
	 *
	 * @throws IllegalStateException if the pack is sealed
	 */
	void clear();
}

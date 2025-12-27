package com.sigmundgranaas.forgero.drp.impl.pack;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.TextureEntry;
import com.sigmundgranaas.forgero.drp.impl.serialization.JsonResourceWriter;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Implementation of DynamicResourcePack.
 */
public class DynamicResourcePackImpl implements DynamicResourcePack {

	private final Identifier id;
	private final String description;
	private final int priority;
	private final boolean validateResources;
	private final ResourceRegistry registry;
	private final JsonResourceWriter writer;
	private boolean sealed = false;

	public DynamicResourcePackImpl(Identifier id, String description, int priority, boolean validateResources) {
		this.id = id;
		this.description = description;
		this.priority = priority;
		this.validateResources = validateResources;
		this.registry = new ResourceRegistry();
		this.writer = new JsonResourceWriter();
	}

	@Override
	public Identifier getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public int getPriority() {
		return priority;
	}

	public ResourceRegistry getRegistry() {
		return registry;
	}

	private void checkNotSealed() {
		if (sealed) {
			throw new IllegalStateException("Cannot modify sealed resource pack: " + id);
		}
	}

	// ============================================================
	// Tag Resources
	// ============================================================

	@Override
	public DynamicResourcePack addTag(TagBuilder<?> builder) {
		checkNotSealed();
		Identifier tagId = new Identifier(builder.getId().getNamespace(),
				"tags/" + builder.getType() + "/" + builder.getId().getPath() + ".json");
		registry.addData(tagId, writer.writeTag(builder));
		return this;
	}

	// ============================================================
	// Recipe Resources
	// ============================================================

	@Override
	public DynamicResourcePack addRecipe(Identifier id, RecipeBuilder<?> builder) {
		checkNotSealed();
		Identifier recipeId = new Identifier(id.getNamespace(), "recipes/" + id.getPath() + ".json");

		byte[] data;
		if (builder instanceof ShapelessRecipeBuilder shapeless) {
			data = writer.writeShapelessRecipe(shapeless);
		} else if (builder instanceof ShapedRecipeBuilder shaped) {
			data = writer.writeShapedRecipe(shaped);
		} else if (builder instanceof SmithingRecipeBuilder smithing) {
			data = writer.writeSmithingRecipe(smithing);
		} else {
			throw new IllegalArgumentException("Unknown recipe builder type: " + builder.getClass());
		}

		registry.addData(recipeId, data);
		return this;
	}

	@Override
	public DynamicResourcePack addShapelessRecipe(Identifier id, Consumer<ShapelessRecipeBuilder> configurator) {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create();
		configurator.accept(builder);
		return addRecipe(id, builder);
	}

	@Override
	public DynamicResourcePack addShapedRecipe(Identifier id, Consumer<ShapedRecipeBuilder> configurator) {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create();
		configurator.accept(builder);
		return addRecipe(id, builder);
	}

	@Override
	public DynamicResourcePack addSmithingRecipe(Identifier id, Consumer<SmithingRecipeBuilder> configurator) {
		SmithingRecipeBuilder builder = SmithingRecipeBuilder.create();
		configurator.accept(builder);
		return addRecipe(id, builder);
	}

	// ============================================================
	// Model Resources
	// ============================================================

	@Override
	public DynamicResourcePack addModel(Identifier id, ModelBuilder builder) {
		checkNotSealed();
		Identifier modelId = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");
		registry.addAsset(modelId, writer.writeModel(builder));
		return this;
	}

	@Override
	public DynamicResourcePack addModel(Identifier id, Consumer<ModelBuilder> configurator) {
		ModelBuilder builder = ModelBuilder.create();
		configurator.accept(builder);
		return addModel(id, builder);
	}

	// ============================================================
	// Language Resources
	// ============================================================

	@Override
	public DynamicResourcePack addLanguage(String locale, LanguageBuilder builder) {
		checkNotSealed();
		// Language files go under the pack's namespace
		Identifier langId = new Identifier(id.getNamespace(), "lang/" + locale + ".json");

		// Merge with existing if present
		byte[] existing = registry.getAsset(langId);
		if (existing != null) {
			// For simplicity, we'll just overwrite. A more sophisticated implementation
			// would parse and merge the JSON.
		}

		registry.addAsset(langId, writer.writeLanguage(builder));
		return this;
	}

	@Override
	public DynamicResourcePack addLanguage(String locale, Consumer<LanguageBuilder> configurator) {
		LanguageBuilder builder = LanguageBuilder.create();
		configurator.accept(builder);
		return addLanguage(locale, builder);
	}

	// ============================================================
	// Texture Resources
	// ============================================================

	@Override
	public DynamicResourcePack addTexture(Identifier id, BufferedImage image) {
		checkNotSealed();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			Identifier textureId = new Identifier(id.getNamespace(), "textures/" + id.getPath() + ".png");
			registry.addAsset(textureId, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException("Failed to write texture: " + id, e);
		}
		return this;
	}

	@Override
	public DynamicResourcePack addTexture(TextureEntry entry) {
		return addTexture(entry.getId(), entry.getImage());
	}

	@Override
	public DynamicResourcePack addAtlas(Identifier atlasId, AtlasBuilder builder) {
		checkNotSealed();
		Identifier id = new Identifier(atlasId.getNamespace(), "atlases/" + atlasId.getPath() + ".json");
		registry.addAsset(id, writer.writeAtlas(builder));
		return this;
	}

	@Override
	public DynamicResourcePack addAtlas(Identifier atlasId, Consumer<AtlasBuilder> configurator) {
		AtlasBuilder builder = AtlasBuilder.create();
		configurator.accept(builder);
		return addAtlas(atlasId, builder);
	}

	// ============================================================
	// Raw Data Access
	// ============================================================

	@Override
	public DynamicResourcePack addRawData(String path, byte[] jsonData) {
		checkNotSealed();
		// Parse the path to create an identifier
		// Expected format: "data/namespace/path/to/file.json"
		if (path.startsWith("data/")) {
			String remainder = path.substring(5);
			int slashIndex = remainder.indexOf('/');
			if (slashIndex > 0) {
				String namespace = remainder.substring(0, slashIndex);
				String resourcePath = remainder.substring(slashIndex + 1);
				registry.addData(new Identifier(namespace, resourcePath), jsonData);
			}
		}
		return this;
	}

	@Override
	public DynamicResourcePack addRawAsset(Identifier id, byte[] data) {
		checkNotSealed();
		registry.addAsset(id, data);
		return this;
	}

	// ============================================================
	// Lifecycle
	// ============================================================

	@Override
	public DynamicResourcePack seal() {
		this.sealed = true;
		return this;
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void clear() {
		checkNotSealed();
		registry.clear();
	}
}

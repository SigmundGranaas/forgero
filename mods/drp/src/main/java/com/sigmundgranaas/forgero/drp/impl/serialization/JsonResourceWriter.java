package com.sigmundgranaas.forgero.drp.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.ResourcePackConstants.Keys;
import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelOverrideBuilder;
import com.sigmundgranaas.forgero.drp.api.model.TexturesBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.RecipeSerializerRegistry;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import com.sigmundgranaas.forgero.drp.impl.builder.AtlasBuilderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Converts builders to JSON byte arrays for resource packs.
 */
public class JsonResourceWriter {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Gson GSON_COMPACT = new Gson();

	private final boolean prettyPrint;

	public JsonResourceWriter(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	public JsonResourceWriter() {
		this(false);
	}

	private Gson gson() {
		return prettyPrint ? GSON : GSON_COMPACT;
	}

	/**
	 * Converts a tag builder to JSON bytes.
	 */
	public byte[] writeTag(TagBuilder<?> builder) {
		JsonObject json = new JsonObject();

		if (builder.isReplace()) {
			json.addProperty(Keys.REPLACE, true);
		}

		JsonArray values = new JsonArray();
		for (TagBuilder.TagEntry entry : builder.getEntries()) {
			JsonObject entryJson = new JsonObject();

			String id = entry.id().toString();
			if (entry.isTag()) {
				id = "#" + id;
			}
			entryJson.addProperty(Keys.ID, id);

			if (entry.isOptional()) {
				entryJson.addProperty(Keys.REQUIRED, false);
			}

			// For simple entries, just add the string; for optional entries, add the object
			if (entry.isOptional()) {
				values.add(entryJson);
			} else {
				values.add(entry.isTag() ? "#" + entry.id().toString() : entry.id().toString());
			}
		}

		json.add(Keys.VALUES, values);

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts any recipe builder to JSON bytes using the registered serializers.
	 * <p>
	 * This method uses the {@link RecipeSerializerRegistry} to find an appropriate
	 * serializer for the builder type. Custom recipe types can be supported by
	 * registering serializers with the registry.
	 *
	 * @param builder The recipe builder to serialize
	 * @return The JSON bytes, or empty if no serializer is registered
	 */
	public <T extends RecipeBuilder<?>> Optional<byte[]> writeRecipe(T builder) {
		return RecipeSerializerRegistry.serialize(builder)
			.map(json -> gson().toJson(json).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Converts a shapeless recipe builder to JSON bytes.
	 *
	 * @deprecated Use {@link #writeRecipe(RecipeBuilder)} for better extensibility
	 */
	@Deprecated
	public byte[] writeShapelessRecipe(ShapelessRecipeBuilder builder) {
		return writeRecipe(builder)
			.orElseThrow(() -> new IllegalStateException("No serializer registered for ShapelessRecipeBuilder"));
	}

	/**
	 * Converts a shaped recipe builder to JSON bytes.
	 *
	 * @deprecated Use {@link #writeRecipe(RecipeBuilder)} for better extensibility
	 */
	@Deprecated
	public byte[] writeShapedRecipe(ShapedRecipeBuilder builder) {
		return writeRecipe(builder)
			.orElseThrow(() -> new IllegalStateException("No serializer registered for ShapedRecipeBuilder"));
	}

	/**
	 * Converts a smithing recipe builder to JSON bytes.
	 *
	 * @deprecated Use {@link #writeRecipe(RecipeBuilder)} for better extensibility
	 */
	@Deprecated
	public byte[] writeSmithingRecipe(SmithingRecipeBuilder builder) {
		return writeRecipe(builder)
			.orElseThrow(() -> new IllegalStateException("No serializer registered for SmithingRecipeBuilder"));
	}

	/**
	 * Converts a model builder to JSON bytes.
	 */
	public byte[] writeModel(ModelBuilder builder) {
		JsonObject json = new JsonObject();

		if (builder.getParent() != null) {
			json.addProperty(Keys.PARENT, builder.getParent());
		}

		TexturesBuilder textures = builder.getTextures();
		if (textures != null && !textures.getTextures().isEmpty()) {
			JsonObject texturesJson = new JsonObject();
			for (Map.Entry<String, String> entry : textures.getTextures().entrySet()) {
				texturesJson.addProperty(entry.getKey(), entry.getValue());
			}
			json.add(Keys.TEXTURES, texturesJson);
		}

		if (!builder.getOverrides().isEmpty()) {
			JsonArray overrides = new JsonArray();
			for (ModelOverrideBuilder override : builder.getOverrides()) {
				JsonObject overrideJson = new JsonObject();

				JsonObject predicates = new JsonObject();
				for (Map.Entry<String, Float> pred : override.getPredicates().entrySet()) {
					predicates.addProperty(pred.getKey(), pred.getValue());
				}
				overrideJson.add(Keys.PREDICATE, predicates);
				overrideJson.addProperty(Keys.MODEL, override.getModel());

				overrides.add(overrideJson);
			}
			json.add(Keys.OVERRIDES, overrides);
		}

		if (!builder.getDisplay().isEmpty()) {
			JsonObject display = new JsonObject();
			for (Map.Entry<String, ModelBuilder.DisplaySettings> entry : builder.getDisplay().entrySet()) {
				JsonObject settings = new JsonObject();
				var ds = entry.getValue();

				if (ds.rotation() != null) {
					JsonArray rotation = new JsonArray();
					for (float v : ds.rotation()) rotation.add(v);
					settings.add(Keys.ROTATION, rotation);
				}
				if (ds.translation() != null) {
					JsonArray translation = new JsonArray();
					for (float v : ds.translation()) translation.add(v);
					settings.add(Keys.TRANSLATION, translation);
				}
				if (ds.scale() != null) {
					JsonArray scale = new JsonArray();
					for (float v : ds.scale()) scale.add(v);
					settings.add(Keys.SCALE, scale);
				}

				display.add(entry.getKey(), settings);
			}
			json.add(Keys.DISPLAY, display);
		}

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts a language builder to JSON bytes.
	 */
	public byte[] writeLanguage(LanguageBuilder builder) {
		JsonObject json = new JsonObject();
		for (Map.Entry<String, String> entry : builder.getEntries().entrySet()) {
			json.addProperty(entry.getKey(), entry.getValue());
		}
		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts an atlas builder to JSON bytes.
	 */
	public byte[] writeAtlas(AtlasBuilder builder) {
		JsonObject json = new JsonObject();
		JsonArray sources = new JsonArray();

		for (AtlasBuilder.AtlasSource source : builder.getSources()) {
			JsonObject sourceJson = new JsonObject();
			sourceJson.addProperty(Keys.TYPE, source.type());

			if (source instanceof AtlasBuilderImpl.DirectorySourceImpl dir) {
				sourceJson.addProperty(Keys.SOURCE, dir.source());
				sourceJson.addProperty(Keys.PREFIX, dir.prefix());
			} else if (source instanceof AtlasBuilderImpl.SingleSourceImpl single) {
				sourceJson.addProperty(Keys.RESOURCE, single.resource().toString());
				if (!single.resource().equals(single.sprite())) {
					sourceJson.addProperty(Keys.SPRITE, single.sprite().toString());
				}
			} else if (source instanceof AtlasBuilderImpl.FilterSourceImpl filter) {
				JsonObject pattern = new JsonObject();
				if (filter.namespacePattern() != null) {
					pattern.addProperty(Keys.NAMESPACE, filter.namespacePattern());
				}
				if (filter.pathPattern() != null) {
					pattern.addProperty(Keys.PATH, filter.pathPattern());
				}
				sourceJson.add(Keys.PATTERN, pattern);
			}

			sources.add(sourceJson);
		}

		json.add(Keys.SOURCES, sources);
		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}
}

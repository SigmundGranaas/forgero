package com.sigmundgranaas.forgero.drp.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelOverrideBuilder;
import com.sigmundgranaas.forgero.drp.api.model.TexturesBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import com.sigmundgranaas.forgero.drp.impl.builder.AtlasBuilderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

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
			json.addProperty("replace", true);
		}

		JsonArray values = new JsonArray();
		for (TagBuilder.TagEntry entry : builder.getEntries()) {
			JsonObject entryJson = new JsonObject();

			String id = entry.id().toString();
			if (entry.isTag()) {
				id = "#" + id;
			}
			entryJson.addProperty("id", id);

			if (entry.isOptional()) {
				entryJson.addProperty("required", false);
			}

			// For simple entries, just add the string; for optional entries, add the object
			if (entry.isOptional()) {
				values.add(entryJson);
			} else {
				values.add(entry.isTag() ? "#" + entry.id().toString() : entry.id().toString());
			}
		}

		json.add("values", values);

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts a shapeless recipe builder to JSON bytes.
	 */
	public byte[] writeShapelessRecipe(ShapelessRecipeBuilder builder) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:crafting_shapeless");

		if (builder.getGroup() != null) {
			json.addProperty("group", builder.getGroup());
		}

		JsonArray ingredients = new JsonArray();
		for (IngredientBuilder ingredient : builder.getIngredients()) {
			ingredients.add(writeIngredient(ingredient));
		}
		json.add("ingredients", ingredients);

		JsonObject result = new JsonObject();
		result.addProperty("item", builder.getResult().toString());
		if (builder.getResultCount() > 1) {
			result.addProperty("count", builder.getResultCount());
		}
		json.add("result", result);

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts a shaped recipe builder to JSON bytes.
	 */
	public byte[] writeShapedRecipe(ShapedRecipeBuilder builder) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:crafting_shaped");

		if (builder.getGroup() != null) {
			json.addProperty("group", builder.getGroup());
		}

		JsonArray pattern = new JsonArray();
		for (String row : builder.getPattern()) {
			pattern.add(row);
		}
		json.add("pattern", pattern);

		JsonObject keys = new JsonObject();
		for (Map.Entry<Character, IngredientBuilder> entry : builder.getKeys().entrySet()) {
			keys.add(String.valueOf(entry.getKey()), writeIngredient(entry.getValue()));
		}
		json.add("key", keys);

		JsonObject result = new JsonObject();
		result.addProperty("item", builder.getResult().toString());
		if (builder.getResultCount() > 1) {
			result.addProperty("count", builder.getResultCount());
		}
		json.add("result", result);

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Converts a smithing recipe builder to JSON bytes.
	 */
	public byte[] writeSmithingRecipe(SmithingRecipeBuilder builder) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:smithing_transform");

		json.add("template", writeIngredient(builder.getTemplate()));
		json.add("base", writeIngredient(builder.getBase()));
		json.add("addition", writeIngredient(builder.getAddition()));

		JsonObject result = new JsonObject();
		result.addProperty("item", builder.getResult().toString());
		json.add("result", result);

		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * Writes an ingredient to a JsonElement.
	 */
	private com.google.gson.JsonElement writeIngredient(IngredientBuilder builder) {
		var entries = builder.getEntries();

		if (entries.size() == 1) {
			// Single ingredient - return object
			return writeIngredientEntry(entries.get(0));
		} else {
			// Multiple ingredients - return array (OR)
			JsonArray array = new JsonArray();
			for (var entry : entries) {
				array.add(writeIngredientEntry(entry));
			}
			return array;
		}
	}

	private JsonObject writeIngredientEntry(IngredientBuilder.IngredientEntry entry) {
		JsonObject obj = new JsonObject();
		if (entry.isTag()) {
			obj.addProperty("tag", entry.id().toString());
		} else {
			obj.addProperty("item", entry.id().toString());
		}
		return obj;
	}

	/**
	 * Converts a model builder to JSON bytes.
	 */
	public byte[] writeModel(ModelBuilder builder) {
		JsonObject json = new JsonObject();

		if (builder.getParent() != null) {
			json.addProperty("parent", builder.getParent());
		}

		TexturesBuilder textures = builder.getTextures();
		if (textures != null && !textures.getTextures().isEmpty()) {
			JsonObject texturesJson = new JsonObject();
			for (Map.Entry<String, String> entry : textures.getTextures().entrySet()) {
				texturesJson.addProperty(entry.getKey(), entry.getValue());
			}
			json.add("textures", texturesJson);
		}

		if (!builder.getOverrides().isEmpty()) {
			JsonArray overrides = new JsonArray();
			for (ModelOverrideBuilder override : builder.getOverrides()) {
				JsonObject overrideJson = new JsonObject();

				JsonObject predicates = new JsonObject();
				for (Map.Entry<String, Float> pred : override.getPredicates().entrySet()) {
					predicates.addProperty(pred.getKey(), pred.getValue());
				}
				overrideJson.add("predicate", predicates);
				overrideJson.addProperty("model", override.getModel());

				overrides.add(overrideJson);
			}
			json.add("overrides", overrides);
		}

		if (!builder.getDisplay().isEmpty()) {
			JsonObject display = new JsonObject();
			for (Map.Entry<String, ModelBuilder.DisplaySettings> entry : builder.getDisplay().entrySet()) {
				JsonObject settings = new JsonObject();
				var ds = entry.getValue();

				if (ds.rotation() != null) {
					JsonArray rotation = new JsonArray();
					for (float v : ds.rotation()) rotation.add(v);
					settings.add("rotation", rotation);
				}
				if (ds.translation() != null) {
					JsonArray translation = new JsonArray();
					for (float v : ds.translation()) translation.add(v);
					settings.add("translation", translation);
				}
				if (ds.scale() != null) {
					JsonArray scale = new JsonArray();
					for (float v : ds.scale()) scale.add(v);
					settings.add("scale", scale);
				}

				display.add(entry.getKey(), settings);
			}
			json.add("display", display);
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
			sourceJson.addProperty("type", source.type());

			if (source instanceof AtlasBuilderImpl.DirectorySourceImpl dir) {
				sourceJson.addProperty("source", dir.source());
				sourceJson.addProperty("prefix", dir.prefix());
			} else if (source instanceof AtlasBuilderImpl.SingleSourceImpl single) {
				sourceJson.addProperty("resource", single.resource().toString());
				if (!single.resource().equals(single.sprite())) {
					sourceJson.addProperty("sprite", single.sprite().toString());
				}
			} else if (source instanceof AtlasBuilderImpl.FilterSourceImpl filter) {
				JsonObject pattern = new JsonObject();
				if (filter.namespacePattern() != null) {
					pattern.addProperty("namespace", filter.namespacePattern());
				}
				if (filter.pathPattern() != null) {
					pattern.addProperty("path", filter.pathPattern());
				}
				sourceJson.add("pattern", pattern);
			}

			sources.add(sourceJson);
		}

		json.add("sources", sources);
		return gson().toJson(json).getBytes(StandardCharsets.UTF_8);
	}
}

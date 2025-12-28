package com.sigmundgranaas.forgero.drp.api;

/**
 * Constants for resource pack generation.
 * <p>
 * These constants define the standard JSON keys and values used in
 * Minecraft resource packs and data packs.
 */
public final class ResourcePackConstants {

	private ResourcePackConstants() {
		// Prevent instantiation
	}

	// ============================================================
	// JSON Keys
	// ============================================================

	/**
	 * Standard JSON keys used across multiple resource types.
	 */
	public static final class Keys {
		public static final String TYPE = "type";
		public static final String ID = "id";
		public static final String ITEM = "item";
		public static final String TAG = "tag";
		public static final String COUNT = "count";
		public static final String GROUP = "group";
		public static final String RESULT = "result";
		public static final String REPLACE = "replace";
		public static final String VALUES = "values";
		public static final String REQUIRED = "required";
		public static final String INGREDIENTS = "ingredients";
		public static final String PATTERN = "pattern";
		public static final String KEY = "key";
		public static final String TEMPLATE = "template";
		public static final String BASE = "base";
		public static final String ADDITION = "addition";
		public static final String PARENT = "parent";
		public static final String TEXTURES = "textures";
		public static final String OVERRIDES = "overrides";
		public static final String PREDICATE = "predicate";
		public static final String MODEL = "model";
		public static final String DISPLAY = "display";
		public static final String ROTATION = "rotation";
		public static final String TRANSLATION = "translation";
		public static final String SCALE = "scale";
		public static final String SOURCES = "sources";
		public static final String SOURCE = "source";
		public static final String PREFIX = "prefix";
		public static final String RESOURCE = "resource";
		public static final String SPRITE = "sprite";
		public static final String NAMESPACE = "namespace";
		public static final String PATH = "path";

		private Keys() {
		}
	}

	// ============================================================
	// Recipe Types
	// ============================================================

	/**
	 * Standard Minecraft recipe type identifiers.
	 */
	public static final class RecipeTypes {
		public static final String CRAFTING_SHAPELESS = "minecraft:crafting_shapeless";
		public static final String CRAFTING_SHAPED = "minecraft:crafting_shaped";
		public static final String SMITHING_TRANSFORM = "minecraft:smithing_transform";
		public static final String SMITHING_TRIM = "minecraft:smithing_trim";
		public static final String SMELTING = "minecraft:smelting";
		public static final String BLASTING = "minecraft:blasting";
		public static final String SMOKING = "minecraft:smoking";
		public static final String CAMPFIRE_COOKING = "minecraft:campfire_cooking";
		public static final String STONECUTTING = "minecraft:stonecutting";

		private RecipeTypes() {
		}
	}

	// ============================================================
	// Atlas Source Types
	// ============================================================

	/**
	 * Atlas source type identifiers.
	 */
	public static final class AtlasSourceTypes {
		public static final String DIRECTORY = "directory";
		public static final String SINGLE = "single";
		public static final String FILTER = "filter";
		public static final String UNSTITCH = "unstitch";
		public static final String PALETTED_PERMUTATIONS = "paletted_permutations";

		private AtlasSourceTypes() {
		}
	}

	// ============================================================
	// Resource Paths
	// ============================================================

	/**
	 * Standard resource path prefixes and formats.
	 */
	public static final class Paths {
		public static final String DATA_PREFIX = "data";
		public static final String ASSETS_PREFIX = "assets";
		public static final String RECIPES_PATH = "recipes";
		public static final String TAGS_PATH = "tags";
		public static final String ITEMS_TAG_PATH = "tags/items";
		public static final String BLOCKS_TAG_PATH = "tags/blocks";
		public static final String MODELS_PATH = "models";
		public static final String TEXTURES_PATH = "textures";
		public static final String LANG_PATH = "lang";
		public static final String ATLASES_PATH = "atlases";

		/**
		 * Formats a recipe path: data/{namespace}/recipes/{name}.json
		 */
		public static String recipe(String namespace, String name) {
			return String.format("%s/%s/%s/%s.json", DATA_PREFIX, namespace, RECIPES_PATH, name);
		}

		/**
		 * Formats an item tag path: data/{namespace}/tags/items/{name}.json
		 */
		public static String itemTag(String namespace, String name) {
			return String.format("%s/%s/%s/%s.json", DATA_PREFIX, namespace, ITEMS_TAG_PATH, name);
		}

		/**
		 * Formats a block tag path: data/{namespace}/tags/blocks/{name}.json
		 */
		public static String blockTag(String namespace, String name) {
			return String.format("%s/%s/%s/%s.json", DATA_PREFIX, namespace, BLOCKS_TAG_PATH, name);
		}

		/**
		 * Formats a model path: assets/{namespace}/models/{name}.json
		 */
		public static String model(String namespace, String name) {
			return String.format("%s/%s/%s/%s.json", ASSETS_PREFIX, namespace, MODELS_PATH, name);
		}

		/**
		 * Formats a language path: assets/{namespace}/lang/{locale}.json
		 */
		public static String language(String namespace, String locale) {
			return String.format("%s/%s/%s/%s.json", ASSETS_PREFIX, namespace, LANG_PATH, locale);
		}

		/**
		 * Formats an atlas path: assets/{namespace}/atlases/{name}.json
		 */
		public static String atlas(String namespace, String name) {
			return String.format("%s/%s/%s/%s.json", ASSETS_PREFIX, namespace, ATLASES_PATH, name);
		}

		private Paths() {
		}
	}

	// ============================================================
	// Namespaces
	// ============================================================

	/**
	 * Common namespace constants.
	 */
	public static final class Namespaces {
		public static final String MINECRAFT = "minecraft";
		public static final String COMMON = "c";

		private Namespaces() {
		}
	}

	// ============================================================
	// Model Parents
	// ============================================================

	/**
	 * Common model parent identifiers.
	 */
	public static final class ModelParents {
		public static final String ITEM_GENERATED = "item/generated";
		public static final String ITEM_HANDHELD = "item/handheld";
		public static final String BLOCK_CUBE_ALL = "block/cube_all";

		private ModelParents() {
		}
	}

	// ============================================================
	// Texture Layers
	// ============================================================

	/**
	 * Standard texture layer keys.
	 */
	public static final class TextureLayers {
		public static final String LAYER0 = "layer0";
		public static final String LAYER1 = "layer1";
		public static final String LAYER2 = "layer2";
		public static final String LAYER3 = "layer3";
		public static final String LAYER4 = "layer4";
		public static final String PARTICLE = "particle";

		public static String layer(int index) {
			return "layer" + index;
		}

		private TextureLayers() {
		}
	}
}

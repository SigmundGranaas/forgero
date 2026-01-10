package com.sigmundgranaas.forgero.mod;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.RecipeGenApi;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.api.template.TemplateLoader;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.impl.variable.converters.StringListConverter;
import com.sigmundgranaas.forgero.recipegen.integration.DRPRecipeInjector;
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Recipe generation plugin for Forgero.
 * <p>
 * Registers variable converters and operations for recipe template generation,
 * then injects generated recipes via the Dynamic Resource Pack (DRP) system.
 * <p>
 * This replaces the legacy fabric/modules/generator recipe generation system
 * with the new mods/recipe-generator architecture.
 */
public class RecipeGenPlugin implements PostLoadPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger("Forgero-RecipeGen");

	/**
	 * Static holder for generated recipes that the RecipeInjectionMixin can access.
	 * This is populated during PostLoadPlugin execution, before RecipeManager.apply() is called
	 * for the first time when a world loads.
	 */
	private static final Map<Identifier, JsonObject> GENERATED_RECIPES = new ConcurrentHashMap<>();

	/**
	 * Returns an unmodifiable view of the generated recipes for mixin injection.
	 * Called by RecipeInjectionMixin during RecipeManager.apply().
	 */
	public static Map<Identifier, JsonObject> getGeneratedRecipes() {
		return Collections.unmodifiableMap(GENERATED_RECIPES);
	}

	/**
	 * Clears the generated recipes. Should be called after injection to free memory.
	 */
	public static void clearGeneratedRecipes() {
		GENERATED_RECIPES.clear();
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		LOGGER.info("Initializing Forgero Recipe Generation...");

		registerVariableConverters(context);
		registerOperations(context);
		
		int tagCount = generateItemTags(context);
		LOGGER.info("Generated {} item tags from component tags", tagCount);

		int count = injectRecipes(context);
		LOGGER.info("Generated {} recipes from templates", count);
	}

	@Override
	public String getId() {
		return "forgero:recipe_gen";
	}

	private void registerVariableConverters(DataLoadingContext context) {
		RecipeGenApi api = RecipeGenApi.getInstance();
		api.variables().register("forgero:string_list", new StringListConverter());
		api.variables().register("forgero:tag_converter", new ForgeroTagVariableConverter(context));
	}

	/**
	 * Registers operations for transforming Component variables into strings.
	 */
	private void registerOperations(DataLoadingContext context) {
		RecipeGenApi api = RecipeGenApi.getInstance();
		ComponentConverter converter = context.converter();
		TagResolver tagResolver = context.tagResolver();

		// Helper functions for operations
		Function<Component, String> nameOp = component -> component.id().path();
		Function<Component, String> namespaceOp = component -> component.id().namespace();

		Function<Component, String> containerIdOp = (component) -> {
			// Convert component to ItemStack, then get item identifier
			return converter.toStack(component)
					.map(stack -> Registries.ITEM.getId(stack.getItem()).toString())
					.orElse(component.id().toString());
		};

		Function<Component, String> containerNamespaceOp = (component) -> {
			// Get the namespace of the host item (e.g., "minecraft" for minecraft:oak_planks)
			return converter.toStack(component)
					.map(stack -> Registries.ITEM.getId(stack.getItem()).getNamespace())
					.orElse(component.id().namespace());
		};

		Function<Component, String> tagOrItemOp = (component) -> {
			String containerId = containerIdOp.apply(component);
			Identifier id = new Identifier(containerId);
			
			// Check if this ID corresponds to an item tag (e.g., minecraft:planks, c:ingots/iron)
			// If the item in the registry is AIR and the ID looks like a tag path, use tag
			Item item = Registries.ITEM.get(id);
			if (item == Items.AIR && !Registries.ITEM.containsId(id)) {
				return "tag";
			}
			return "item";
		};

		Function<Component, String> materialOp = (component) -> {
			// Extract primary material if it exists
			return component.getChildren().stream()
					.filter(part -> part.id().path().contains("material"))
					.findFirst()
					.map(part -> part.id().path())
					.orElse("");
		};

		// Register all operations matching legacy system
		api.operations().register("forgero:component_name", "name",
				OperationFactory.forClass(Component.class, nameOp));
		api.operations().register("forgero:component_namespace", "namespace",
				OperationFactory.forClass(Component.class, namespaceOp));
		api.operations().register("forgero:component_identifier", "identifier",
				OperationFactory.forClass(Component.class, c -> c.id().toString()));
		api.operations().register("forgero:component_identifier", "id",
				OperationFactory.forClass(Component.class, c -> c.id().toString()));
		api.operations().register("forgero:component_identifier", "container_id",
				OperationFactory.forClass(Component.class, containerIdOp));
		api.operations().register("forgero:container_namespace", "container_namespace",
				OperationFactory.forClass(Component.class, containerNamespaceOp));
		api.operations().register("forgero:tag_or_item", "tagOrItem",
				OperationFactory.forClass(Component.class, tagOrItemOp));
		api.operations().register("forgero:component_material", "material",
				OperationFactory.forClass(Component.class, materialOp));

		api.operations().register("forgero:component_name_replace_planks", "name_replace_planks",
				OperationFactory.forClass(Component.class, (Component component) -> component.id().path().replace("_planks", "")));
	}

	private int generateItemTags(DataLoadingContext context) {
		DynamicResourcePack pack = DRPApi.getInstance()
				.createPack("forgero:generated_item_tags")
				.description("Auto-generated item tags from Forgero component tags")
				.build();

		ComponentConverter converter = context.converter();
		var taggedComponents = context.taggedComponents();
		
		Set<String> partTagPatterns = Set.of(
				"forgero:parts/binding",
				"forgero:parts/sword_guard",
				"forgero:parts/handle",
				"forgero:parts/types/pickaxe_head",
				"forgero:parts/types/axe_head",
				"forgero:parts/types/shovel_head",
				"forgero:parts/types/hoe_head",
				"forgero:parts/types/sword_blade",
				"forgero:parts/categories/handle",
				"forgero:parts/categories/head",
				"forgero:parts/categories/blade"
		);

		int count = 0;
		for (String tagPattern : partTagPatterns) {
			OpenIdentifier tagId = OpenIdentifier.parse(tagPattern);
			List<Component> components = taggedComponents.findByTag(tagId);
			
			if (components.isEmpty()) {
				LOGGER.debug("No components found for tag: {}", tagPattern);
				continue;
			}

			var tagBuilder = TagBuilder.items(tagPattern);
			for (Component component : components) {
				converter.toStack(component).ifPresent(stack -> {
					Identifier itemId = Registries.ITEM.getId(stack.getItem());
					if (!itemId.equals(Registries.ITEM.getDefaultId())) {
						tagBuilder.add(itemId);
					}
				});
			}

			if (!tagBuilder.getEntries().isEmpty()) {
				pack.addTag(tagBuilder);
				count++;
				LOGGER.debug("Created item tag {} with {} entries", tagPattern, tagBuilder.getEntries().size());
			}
		}

		pack.seal();
		DRPApi.getInstance().register(pack, ResourcePackPhase.BEFORE_VANILLA);
		
		return count;
	}

	private int injectRecipes(DataLoadingContext context) {
		Collection<GeneratedRecipe> allRecipes = RecipeGenApi.getInstance().processor()
				.fromDirectory("recipe_generators")
				.namespace("forgero")
				.withLoader(new MinecraftTemplateLoader(context))
				.withModLoadedCheck(FabricLoader.getInstance()::isModLoaded)
				.process();

		List<GeneratedRecipe> validRecipes = allRecipes.stream()
				.filter(this::hasValidItems)
				.toList();

		int invalidCount = allRecipes.size() - validRecipes.size();
		if (invalidCount > 0) {
			LOGGER.warn("Filtered out {} recipes with non-existent result items", invalidCount);
			
			Set<String> missingItems = new java.util.HashSet<>();
			for (GeneratedRecipe recipe : allRecipes) {
				if (!hasValidItems(recipe)) {
					collectMissingItems(recipe.json(), missingItems);
				}
			}
			LOGGER.warn("Missing items referenced by recipes: {}", missingItems.stream().sorted().limit(50).toList());
		}

		// Store recipes in static holder for mixin injection
		// This is accessed by RecipeInjectionMixin when RecipeManager.apply() is called
		for (GeneratedRecipe recipe : validRecipes) {
			GENERATED_RECIPES.put(recipe.id(), recipe.json());
		}
		
		LOGGER.debug("Stored {} recipes for mixin injection", GENERATED_RECIPES.size());
		return validRecipes.size();
	}

	private boolean hasValidItems(GeneratedRecipe recipe) {
		JsonObject json = recipe.json();
		return hasValidResultItem(json) && hasValidKeyItems(json) && hasValidIngredients(json);
	}

	private boolean hasValidResultItem(JsonObject json) {
		if (!json.has("result")) {
			return true;
		}

		JsonElement result = json.get("result");
		String itemId = extractItemId(result);
		return itemId == null || itemExists(itemId);
	}

	private boolean hasValidKeyItems(JsonObject json) {
		if (!json.has("key")) {
			return true;
		}

		JsonObject key = json.getAsJsonObject("key");
		for (String keyName : key.keySet()) {
			JsonElement keyElement = key.get(keyName);
			if (keyElement.isJsonObject()) {
				JsonObject keyObj = keyElement.getAsJsonObject();
				if (keyObj.has("item") && !itemExists(keyObj.get("item").getAsString())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean hasValidIngredients(JsonObject json) {
		if (!json.has("ingredients")) {
			return true;
		}

		JsonElement ingredients = json.get("ingredients");
		if (!ingredients.isJsonArray()) {
			return true;
		}

		for (JsonElement ingredient : ingredients.getAsJsonArray()) {
			if (ingredient.isJsonObject()) {
				JsonObject ingredientObj = ingredient.getAsJsonObject();
				if (ingredientObj.has("item")) {
					String itemId = ingredientObj.get("item").getAsString();
					if (!itemExists(itemId)) {
						LOGGER.debug("Invalid ingredient item: {}", itemId);
						return false;
					}
				}
			}
		}
		return true;
	}

	private String extractItemId(JsonElement element) {
		if (element.isJsonObject()) {
			JsonObject obj = element.getAsJsonObject();
			return obj.has("item") ? obj.get("item").getAsString() : null;
		} else if (element.isJsonPrimitive()) {
			return element.getAsString();
		}
		return null;
	}

	private boolean itemExists(String itemId) {
		Identifier id = new Identifier(itemId);
		return Registries.ITEM.get(id) != Items.AIR;
	}

	private void collectMissingItems(JsonObject json, Set<String> missingItems) {
		if (json.has("result")) {
			String itemId = extractItemId(json.get("result"));
			if (itemId != null && !itemExists(itemId)) {
				missingItems.add(itemId);
			}
		}
		if (json.has("key")) {
			JsonObject key = json.getAsJsonObject("key");
			for (String keyName : key.keySet()) {
				JsonElement keyElement = key.get(keyName);
				if (keyElement.isJsonObject()) {
					JsonObject keyObj = keyElement.getAsJsonObject();
					if (keyObj.has("item")) {
						String itemId = keyObj.get("item").getAsString();
						if (!itemExists(itemId)) {
							missingItems.add(itemId);
						}
					}
				}
			}
		}
		if (json.has("ingredients") && json.get("ingredients").isJsonArray()) {
			for (JsonElement ingredient : json.getAsJsonArray("ingredients")) {
				if (ingredient.isJsonObject()) {
					JsonObject ingredientObj = ingredient.getAsJsonObject();
					if (ingredientObj.has("item")) {
						String itemId = ingredientObj.get("item").getAsString();
						if (!itemExists(itemId)) {
							missingItems.add(itemId);
						}
					}
				}
			}
		}
	}

	/**
	 * Template loader that reads JSON templates from Minecraft's resource manager.
	 * <p>
	 * This implementation loads recipe templates from data packs in the
	 * "data/namespace/recipe_generators/" directory using Minecraft's ResourceManager.
	 */
	private static class MinecraftTemplateLoader implements TemplateLoader {
		private final DataLoadingContext context;

		public MinecraftTemplateLoader(DataLoadingContext context) {
			this.context = context;
		}

		@Override
		public Collection<JsonObject> load(String path) {
			// Load from all namespaces - not commonly used
			// For Forgero, we primarily use load(namespace, path)
			return List.of();
		}

		@Override
		public Collection<JsonObject> load(String namespace, String path) {
			List<JsonObject> templates = new java.util.ArrayList<>();

			try {
				// Load templates from mod resources using FabricLoader
				// Find all mods that might have recipe_generators
				var mods = FabricLoader.getInstance().getAllMods();

				for (var mod : mods) {
					String modId = mod.getMetadata().getId();
					
					if (isLegacyModule(modId)) {
						continue;
					}
					
					boolean isTargetNamespace = modId.equals(namespace);
					boolean isForgeroResource = mod.getMetadata().containsCustomValue("forgeroResource");

					if (!isTargetNamespace && !isForgeroResource) {
						continue;
					}

					// Look for template files in the mod's resources
					var resourcePath = "data/" + namespace + "/" + path;
					mod.findPath(resourcePath).ifPresent(templatesDir -> {
						try {
							if (java.nio.file.Files.exists(templatesDir) && java.nio.file.Files.isDirectory(templatesDir)) {
								java.nio.file.Files.walk(templatesDir)
										.filter(p -> p.toString().endsWith(".json"))
										.forEach(jsonFile -> {
											try (var reader = java.nio.file.Files.newBufferedReader(jsonFile,
													java.nio.charset.StandardCharsets.UTF_8)) {
												JsonElement element = JsonParser.parseReader(reader);
												if (element.isJsonObject()) {
													templates.add(element.getAsJsonObject());
												}
											} catch (Exception e) {
												LOGGER.warn("Failed to parse recipe template: {}", jsonFile, e);
											}
										});
							}
						} catch (Exception e) {
							LOGGER.warn("Failed to walk template directory: {}", templatesDir, e);
						}
					});
				}

				LOGGER.info("Loaded {} recipe templates from {}/{}", templates.size(), namespace, path);
		for (JsonObject template : templates) {
			String type = template.has("type") ? template.get("type").getAsString() : "unknown";
			String id = template.has("identifier") ? template.get("identifier").getAsString() : "unknown";
			LOGGER.debug("Template: type={}, id={}", type, id);
		}
			} catch (Exception e) {
				LOGGER.error("Failed to load recipe templates from {}/{}", namespace, path, e);
			}

			return templates;
		}

		private static final Set<String> LEGACY_MODULES = Set.of(
				"forgero-vanilla",
				"forgero-compat",
				"forgero-fabric-core",
				"forgero-fabric-compat",
				"minecraft-common"
		);

		private boolean isLegacyModule(String modId) {
			return LEGACY_MODULES.contains(modId);
		}
	}

	private static class ForgeroTagVariableConverter implements VariableConverter {
		private final DataLoadingContext context;

		public ForgeroTagVariableConverter(DataLoadingContext context) {
			this.context = context;
		}

		@Override
		public Collection<?> convert(JsonElement entry) {
			var jsonObject = entry.getAsJsonObject();
			JsonElement tagElement = jsonObject.get("tag");

			List<Component> components;
			if (tagElement.isJsonArray()) {
				components = StreamSupport.stream(tagElement.getAsJsonArray().spliterator(), false)
						.flatMap(tag -> findComponentsByTag(tag.getAsString()).stream())
						.distinct()
						.collect(Collectors.toList());
			} else {
				components = findComponentsByTag(tagElement.getAsString());
			}

			if (jsonObject.has("filter")) {
				Set<String> exclusions = StreamSupport.stream(jsonObject.getAsJsonArray("filter").spliterator(), false)
						.map(JsonElement::getAsString)
						.collect(Collectors.toSet());
				components.removeIf(component -> exclusions.contains(component.id().toString()));
			}

			return components;
		}

		@Override
		public boolean matches(JsonElement entry) {
			return entry.isJsonObject() && entry.getAsJsonObject().has("tag");
		}

		@Override
		public int priority() {
			return 100;
		}

		private List<Component> findComponentsByTag(String tagId) {
			OpenIdentifier tag = OpenIdentifier.parse(tagId);
			List<Component> result = context.taggedComponents().findByTag(tag);
			if (result.isEmpty()) {
				LOGGER.warn("No components found for tag: {}", tagId);
			}
			return result;
		}
	}
}

package com.sigmundgranaas.forgero.mod;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;
import com.sigmundgranaas.forgero.recipegen.api.RecipeGenApi;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.api.template.TemplateLoader;
import com.sigmundgranaas.forgero.recipegen.api.variable.VariableConverter;
import com.sigmundgranaas.forgero.recipegen.impl.variable.converters.StringListConverter;
import com.sigmundgranaas.forgero.recipegen.integration.DRPRecipeInjector;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		LOGGER.info("Initializing Forgero Recipe Generation...");

		// Register converters and operations
		registerVariableConverters(context);
		registerOperations(context);

		// Inject recipes from templates
		int count = injectRecipes(context);

		LOGGER.info("Generated {} recipes from templates", count);
	}

	@Override
	public String getId() {
		return "forgero:recipe_gen";
	}

	/**
	 * Registers variable converters for recipe template expansion.
	 */
	private void registerVariableConverters(DataLoadingContext context) {
		RecipeGenApi api = RecipeGenApi.getInstance();

		// String list converter (for simple arrays)
		api.variables().register("forgero:string_list", new StringListConverter());

		// Forgero type converter (for TOOL_MATERIAL, WOOD, STONE, etc.)
		api.variables().register("forgero:type_converter", new ForgeroTypeVariableConverter(context));
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

		Function<Component, String> tagOrItemOp = (component) -> {
			// Check if component has tags, otherwise it's an item
			return component.getTags().isEmpty() ? "item" : "tag";
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
		api.operations().register("forgero:tag_or_item", "tagOrItem",
				OperationFactory.forClass(Component.class, tagOrItemOp));
		api.operations().register("forgero:component_material", "material",
				OperationFactory.forClass(Component.class, materialOp));

		// Edge case: wood materials use "_planks" suffix that needs removal
		api.operations().register("forgero:component_name_replace_planks", "name_replace_planks",
				OperationFactory.forClass(Component.class, (Component component) -> component.id().path().replace("_planks", "")));
	}

	/**
	 * Injects generated recipes into the game via DRP.
	 *
	 * @param context The data loading context for accessing the resource manager
	 * @return The number of recipes generated
	 */
	private int injectRecipes(DataLoadingContext context) {
		return DRPRecipeInjector.builder()
				.packId("forgero:generated_recipes")
				.templateDirectory("recipe_generators")
				.namespace("forgero")
				.templateLoader(new MinecraftTemplateLoader(context))
				.modLoadedCheck(FabricLoader.getInstance()::isModLoaded)
				.enableHotReload(true) // Enable hot-reload during development
				.build()
				.inject();
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
					// Only load from mods that match the requested namespace or are marked as Forgero resources
					boolean isTargetNamespace = mod.getMetadata().getId().equals(namespace);
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
			} catch (Exception e) {
				LOGGER.error("Failed to load recipe templates from {}/{}", namespace, path, e);
			}

			return templates;
		}
	}

	/**
	 * Variable converter that queries Forgero's component registry for components of a given type.
	 * <p>
	 * Supports type-based variable expansion like:
	 * <pre>{@code
	 * "variables": {
	 *   "material": {"type": "TOOL_MATERIAL"}
	 * }
	 * }</pre>
	 * <p>
	 * Also supports filtering:
	 * <pre>{@code
	 * "variables": {
	 *   "material": {
	 *     "type": "WOOD",
	 *     "filter": ["forgero:oak", "forgero:birch"]
	 *   }
	 * }
	 * }</pre>
	 */
	private static class ForgeroTypeVariableConverter implements VariableConverter {
		private final DataLoadingContext context;

		public ForgeroTypeVariableConverter(DataLoadingContext context) {
			this.context = context;
		}

		@Override
		public Collection<?> convert(JsonElement entry) {
			var jsonObject = entry.getAsJsonObject();
			JsonElement typeElement = jsonObject.get("type");

			// Get components by type/tag
			List<Component> components;
			if (typeElement.isJsonArray()) {
				// Multiple types: ["WOOD", "STONE"]
				components = StreamSupport.stream(typeElement.getAsJsonArray().spliterator(), false)
						.flatMap(type -> findComponentsByType(type.getAsString()).stream())
						.distinct()
						.collect(Collectors.toList());
			} else {
				// Single type: "TOOL_MATERIAL"
				String type = typeElement.getAsString();
				components = findComponentsByType(type);
			}

			// Apply filter if present
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
			if (entry.isJsonObject()) {
				return entry.getAsJsonObject().has("type");
			}
			return false;
		}

		@Override
		public int priority() {
			return 100; // High priority to ensure it matches before fallbacks
		}

		/**
		 * Finds all components of a given type from Forgero's registry.
		 * <p>
		 * First tries to find by tag (e.g., "forgero:TOOL_MATERIAL"),
		 * then falls back to type category matching.
		 *
		 * @param type The type string (e.g., "TOOL_MATERIAL", "WOOD", "STONE")
		 * @return List of components matching the type
		 */
		private List<Component> findComponentsByType(String type) {
			// Try to find components by tag first
			OpenIdentifier typeTag = OpenIdentifier.parse("forgero:" + type.toLowerCase());
			List<Component> taggedComponents = context.taggedComponents().findByTag(typeTag);

			if (!taggedComponents.isEmpty()) {
				return taggedComponents;
			}

			// Fallback: find by type in ID or tags
			ComponentRegistry registry = context.componentRegistry();
			return registry.all().stream()
					.filter(component -> component.id().path().contains(type.toLowerCase()) ||
							component.getTags().stream().anyMatch(tag -> tag.path().equalsIgnoreCase(type)))
					.toList();
		}
	}
}

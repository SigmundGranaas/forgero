package com.sigmundgranaas.forgero.recipegen.integration;

import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.HotReloadListener;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.recipegen.api.GeneratedRecipe;
import com.sigmundgranaas.forgero.recipegen.api.RecipeGenApi;
import com.sigmundgranaas.forgero.recipegen.api.template.TemplateLoader;

import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Integrates the Recipe Generator with the Dynamic Resource Pack (DRP) system.
 *
 * <p>This class provides a bridge between generated recipes and DRP, allowing
 * recipes to be injected into the game's resource system with support for
 * hot-reloading.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * DRPRecipeInjector injector = DRPRecipeInjector.builder()
 *     .packId("mymod:generated_recipes")
 *     .templateDirectory("recipe_generators")
 *     .templateLoader(myLoader)
 *     .modLoadedCheck(FabricLoader.getInstance()::isModLoaded)
 *     .enableHotReload(true)
 *     .build();
 *
 * injector.inject();
 * }</pre>
 */
public class DRPRecipeInjector {

	private final Identifier packId;
	private final String templateDirectory;
	private final String namespace;
	private final TemplateLoader templateLoader;
	private final Predicate<String> isModLoaded;
	private final boolean hotReloadEnabled;
	private final ResourcePackPhase phase;

	private DynamicResourcePack pack;

	private DRPRecipeInjector(Builder builder) {
		this.packId = builder.packId;
		this.templateDirectory = builder.templateDirectory;
		this.namespace = builder.namespace;
		this.templateLoader = builder.templateLoader;
		this.isModLoaded = builder.isModLoaded;
		this.hotReloadEnabled = builder.hotReloadEnabled;
		this.phase = builder.phase;
	}

	/**
	 * Creates a new builder for DRPRecipeInjector.
	 *
	 * @return A new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Generates recipes and injects them into DRP.
	 *
	 * @return The number of recipes injected
	 */
	public int inject() {
		DRPApi drp = DRPApi.getInstance();

		// Create the pack
		pack = drp.createPack(packId)
				.description("Generated Recipes")
				.build();

		// Generate and inject recipes
		int count = injectRecipesIntoPack(pack);

		// Register the pack
		drp.register(pack, phase);

		// Enable hot-reload if requested
		if (hotReloadEnabled) {
			drp.enableHotReload(pack, this::onHotReload);
		}

		return count;
	}

	/**
	 * Injects recipes from a custom supplier into the specified pack.
	 *
	 * @param pack     The DRP pack to add recipes to
	 * @param supplier Supplier of generated recipes
	 * @return The number of recipes injected
	 */
	public static int injectRecipes(DynamicResourcePack pack, Supplier<Collection<GeneratedRecipe>> supplier) {
		Collection<GeneratedRecipe> recipes = supplier.get();

		for (GeneratedRecipe recipe : recipes) {
			addRecipeToPack(pack, recipe);
		}

		return recipes.size();
	}

	/**
	 * Injects a collection of recipes into the specified pack.
	 *
	 * @param pack    The DRP pack to add recipes to
	 * @param recipes The recipes to inject
	 * @return The number of recipes injected
	 */
	public static int injectRecipes(DynamicResourcePack pack, Collection<GeneratedRecipe> recipes) {
		for (GeneratedRecipe recipe : recipes) {
			addRecipeToPack(pack, recipe);
		}
		return recipes.size();
	}

	private int injectRecipesIntoPack(DynamicResourcePack pack) {
		Collection<GeneratedRecipe> recipes = RecipeGenApi.getInstance().processor()
				.fromDirectory(templateDirectory)
				.namespace(namespace)
				.withLoader(templateLoader)
				.withModLoadedCheck(isModLoaded)
				.process();

		for (GeneratedRecipe recipe : recipes) {
			addRecipeToPack(pack, recipe);
		}

		return recipes.size();
	}

	private void onHotReload(DynamicResourcePack pack) {
		pack.clear();
		injectRecipesIntoPack(pack);
	}

	private static void addRecipeToPack(DynamicResourcePack pack, GeneratedRecipe recipe) {
		// Convert to raw JSON data for DRP
		byte[] jsonBytes = recipe.json().toString().getBytes(StandardCharsets.UTF_8);

		// Build the data path: data/namespace/recipes/path.json
		String path = String.format("data/%s/recipes/%s.json",
				recipe.id().getNamespace(),
				recipe.id().getPath());

		pack.addRawData(path, jsonBytes);
	}

	/**
	 * Gets the DRP pack used by this injector.
	 *
	 * @return The DynamicResourcePack, or null if inject() hasn't been called
	 */
	public DynamicResourcePack getPack() {
		return pack;
	}

	/**
	 * Builder for DRPRecipeInjector.
	 */
	public static class Builder {
		private Identifier packId = new Identifier("recipegen", "generated");
		private String templateDirectory = "recipe_generators";
		private String namespace = "forgero";
		private TemplateLoader templateLoader;
		private Predicate<String> isModLoaded = s -> true;
		private boolean hotReloadEnabled = false;
		private ResourcePackPhase phase = ResourcePackPhase.BEFORE_VANILLA;

		/**
		 * Sets the resource pack identifier.
		 *
		 * @param id The pack ID (e.g., "mymod:generated_recipes")
		 * @return This builder
		 */
		public Builder packId(String id) {
			this.packId = new Identifier(id);
			return this;
		}

		/**
		 * Sets the resource pack identifier.
		 *
		 * @param id The pack ID
		 * @return This builder
		 */
		public Builder packId(Identifier id) {
			this.packId = id;
			return this;
		}

		/**
		 * Sets the template directory to load from.
		 *
		 * @param directory The directory path (e.g., "recipe_generators")
		 * @return This builder
		 */
		public Builder templateDirectory(String directory) {
			this.templateDirectory = directory;
			return this;
		}

		/**
		 * Sets the namespace for loading templates.
		 *
		 * @param namespace The namespace (e.g., "forgero")
		 * @return This builder
		 */
		public Builder namespace(String namespace) {
			this.namespace = namespace;
			return this;
		}

		/**
		 * Sets the template loader.
		 *
		 * @param loader The template loader
		 * @return This builder
		 */
		public Builder templateLoader(TemplateLoader loader) {
			this.templateLoader = loader;
			return this;
		}

		/**
		 * Sets the mod-loaded check predicate.
		 *
		 * @param isModLoaded Predicate that checks if a mod is loaded
		 * @return This builder
		 */
		public Builder modLoadedCheck(Predicate<String> isModLoaded) {
			this.isModLoaded = isModLoaded;
			return this;
		}

		/**
		 * Enables or disables hot-reload support.
		 *
		 * @param enabled Whether to enable hot-reload
		 * @return This builder
		 */
		public Builder enableHotReload(boolean enabled) {
			this.hotReloadEnabled = enabled;
			return this;
		}

		/**
		 * Sets the resource pack injection phase.
		 *
		 * @param phase The injection phase
		 * @return This builder
		 */
		public Builder phase(ResourcePackPhase phase) {
			this.phase = phase;
			return this;
		}

		/**
		 * Builds the DRPRecipeInjector.
		 *
		 * @return A new DRPRecipeInjector instance
		 */
		public DRPRecipeInjector build() {
			return new DRPRecipeInjector(this);
		}
	}
}

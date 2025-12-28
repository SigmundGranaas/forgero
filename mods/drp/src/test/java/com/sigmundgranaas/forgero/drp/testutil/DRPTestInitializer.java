package com.sigmundgranaas.forgero.drp.testutil;

import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmeltingRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test mod initializer that registers test resources for GameTests.
 * These resources are used to verify the DRP module works correctly in-game.
 */
public class DRPTestInitializer implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("DRP-Test");
	public static final String TEST_NAMESPACE = "drp_test";

	private static DynamicResourcePack testPack;
	private static boolean initialized = false;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing DRP Test Resources...");
		registerTestResources();
		LOGGER.info("DRP Test Resources registered successfully.");
	}

	/**
	 * Registers all test resources for GameTests.
	 */
	public static void registerTestResources() {
		if (initialized) {
			return;
		}

		DRPApi api = DRPApi.getInstance();
		testPack = api.createPack(TEST_NAMESPACE + ":test_pack")
				.description("DRP Test Resources")
				.priority(100)
				.build();

		registerTestTags(testPack);
		registerTestRecipes(testPack);
		registerTestModels(testPack);
		registerTestLanguage(testPack);

		api.register(testPack, ResourcePackPhase.BEFORE_VANILLA);
		initialized = true;
	}

	/**
	 * Registers test tags that can be verified in GameTests.
	 */
	private static void registerTestTags(DynamicResourcePack pack) {
		// Simple item tag with vanilla items
		pack.addTag(TagBuilder.items(TEST_NAMESPACE + ":test_items")
				.add("minecraft:diamond")
				.add("minecraft:emerald")
				.add("minecraft:gold_ingot"));

		// Tag that includes another tag
		pack.addTag(TagBuilder.items(TEST_NAMESPACE + ":test_gems")
				.add("minecraft:diamond")
				.add("minecraft:emerald")
				.includeTag("minecraft:beacon_payment_items"));

		// Block tag for mining
		pack.addTag(TagBuilder.blocks(TEST_NAMESPACE + ":test_blocks")
				.add("minecraft:stone")
				.add("minecraft:granite")
				.add("minecraft:diorite"));

		// Tag with optional entries
		pack.addTag(TagBuilder.items(TEST_NAMESPACE + ":optional_items")
				.add("minecraft:stick")
				.addOptional("nonexistent_mod:fake_item"));
	}

	/**
	 * Registers test recipes that can be verified in GameTests.
	 */
	private static void registerTestRecipes(DynamicResourcePack pack) {
		// Simple shapeless recipe: diamond + stick = arrow (for testing)
		pack.addShapelessRecipe(
				new Identifier(TEST_NAMESPACE, "test_shapeless"),
				builder -> builder
						.addIngredient("minecraft:diamond")
						.addIngredient("minecraft:stick")
						.result("minecraft:arrow", 64)
						.group("drp_test")
		);

		// Shaped recipe: simple pattern
		pack.addShapedRecipe(
				new Identifier(TEST_NAMESPACE, "test_shaped"),
				builder -> builder
						.pattern("GGG")
						.pattern("GSG")
						.pattern("GGG")
						.key('G', "minecraft:gold_ingot")
						.key('S', "minecraft:diamond")
						.result("minecraft:golden_apple", 1)
						.group("drp_test")
		);

		// Recipe using tag ingredient
		pack.addShapelessRecipe(
				new Identifier(TEST_NAMESPACE, "test_tag_recipe"),
				builder -> builder
						.addTagIngredient("minecraft:planks")
						.addTagIngredient("minecraft:planks")
						.result("minecraft:stick", 8)
						.group("drp_test")
		);

		// Smelting recipe: diamond -> coal (for testing)
		pack.addRecipe(
				new Identifier(TEST_NAMESPACE, "test_smelting"),
				SmeltingRecipeBuilder.smelting()
						.input("minecraft:diamond")
						.result("minecraft:coal")
						.experience(1.0f)
						.cookingTime(100)
						.group("drp_test")
		);

		// Blasting recipe: gold ore -> gold ingot (fast)
		pack.addRecipe(
				new Identifier(TEST_NAMESPACE, "test_blasting"),
				SmeltingRecipeBuilder.blasting()
						.input("minecraft:raw_gold")
						.result("minecraft:gold_ingot")
						.experience(1.0f)
						.cookingTime(50)
						.group("drp_test")
		);
	}

	/**
	 * Registers test models.
	 */
	private static void registerTestModels(DynamicResourcePack pack) {
		// Simple item model
		pack.addModel(
				new Identifier(TEST_NAMESPACE, "item/test_model"),
				builder -> builder
						.parent("minecraft:item/generated")
						.textures(tex -> tex
								.layer0("minecraft:item/diamond"))
		);
	}

	/**
	 * Registers test language entries.
	 */
	private static void registerTestLanguage(DynamicResourcePack pack) {
		pack.addLanguage("en_us", lang -> lang
				.add("drp_test.message", "DRP Test Message")
				.add("drp_test.tooltip", "This is a test tooltip")
				.tooltip("drp_test.durability", "Durability: %s/%s"));
	}

	/**
	 * Gets the test pack for verification.
	 */
	public static DynamicResourcePack getTestPack() {
		return testPack;
	}

	/**
	 * Checks if test resources have been initialized.
	 */
	public static boolean isInitialized() {
		return initialized;
	}
}

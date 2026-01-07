package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
import com.sigmundgranaas.forgero.mc.testcommon.helpers.PlayerFactory;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Integration tests for all Forgero recipe types.
 * <p>
 * These tests simulate realistic player crafting scenarios using a crafting table
 * to verify that all migrated recipe generators produce working recipes.
 * <p>
 * Recipe Types Tested:
 * <ul>
 *   <li>forgero:schematic_part_crafting - Part creation with schematics</li>
 *   <li>forgero:shaped_recipe - Tool/weapon assembly with structure preservation</li>
 *   <li>minecraft:crafting_shaped - Simple wood recipes and upgrades</li>
 * </ul>
 */
public class RecipeCraftingIntegrationTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(RecipeCraftingIntegrationTest.class);

	// ============================================================
	// Schematic Part Crafting Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_pickaxe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("pickaxe_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "pickaxe_head-schematic must exist in registry");
		LOGGER.info("Schematic item: {}", Registries.ITEM.getId(schematicStack.getItem()));

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		// Debug: List all schematic_part_crafting recipes
		long schematicRecipeCount = recipeManager.listAllOfType(RecipeType.CRAFTING).stream()
				.filter(r -> r.getId().getPath().contains("pickaxe_head"))
				.peek(r -> LOGGER.info("Found pickaxe_head recipe: {} (type={})", r.getId(), r.getType()))
				.count();
		LOGGER.info("Total pickaxe_head recipes found: {}", schematicRecipeCount);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron pickaxe head should match (found " + schematicRecipeCount + " pickaxe_head recipes)");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("pickaxe_head"),
					"Result should be a pickaxe head: " + resultComp.get().id());

			LOGGER.info("Schematic crafting test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_axe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("axe_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "axe_head-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron axe head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("axe_head"),
					"Result should be an axe head: " + resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Tool Assembly Tests (forgero:shaped_recipe)
	// ============================================================

	/**
	 * Tests assembling an iron pickaxe from head + handle.
	 * This tests the migrated tool/tool.json recipe.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_assembly_iron_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get the parts
		var headOpt = ctx.component("forgero:iron-pickaxe_head");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(headOpt.isPresent(), "iron-pickaxe_head must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var headStack = ctx.toStack(headOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		// Try vertical pattern: head on top, handle below
		RecipeInputInventory inventory = createCraftingInventory(
				headStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			// Try alternate placement
			inventory = createCraftingInventory(
					ItemStack.EMPTY, headStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Tool assembly recipe for iron pickaxe should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled tool should not be empty");

			// Verify it's a pickaxe with correct stats
			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("pickaxe"),
					"Result should be a pickaxe: " + resultId);

			// Verify stats are present
			int durability = query.getMaxDurability(result);
			context.assertTrue(durability > 0, "Assembled pickaxe should have durability");

			LOGGER.info("Tool assembly test passed: {} with durability {}", resultId, durability);
		}

		context.complete();
	}

	/**
	 * Tests assembling an iron sword from blade + handle.
	 * This tests the migrated weapon/sword.json recipe.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void weapon_assembly_iron_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get the parts
		var bladeOpt = ctx.component("forgero:iron-sword_blade");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(bladeOpt.isPresent(), "iron-sword_blade must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var bladeStack = ctx.toStack(bladeOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		// Vertical pattern: blade on top, handle below
		RecipeInputInventory inventory = createCraftingInventory(
				bladeStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			inventory = createCraftingInventory(
					ItemStack.EMPTY, bladeStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Weapon assembly recipe for iron sword should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled sword should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("sword"),
					"Result should be a sword: " + resultId);

			float attackDamage = query.getAttackDamage(result);
			context.assertTrue(attackDamage >= 4.0f, "Iron sword should have attack damage >= 4.0 (iron material provides 4.0): " + attackDamage);

			LOGGER.info("Weapon assembly test passed: {} with damage {}", resultId, attackDamage);
		}

		context.complete();
	}

	// ============================================================
	// Simple Wood Recipe Tests (minecraft:crafting_shaped)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_pickaxe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern from recipe: " x " / "x x"
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak pickaxe head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("pickaxe_head"),
					"Result should be oak pickaxe head: " + resultId);

			LOGGER.info("Simple wood recipe test passed: {}", resultId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_handle(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: " x" / "x "
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak handle should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("handle"),
					"Result should be oak handle: " + resultId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_sword_blade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "x" / "x" / "x"
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak sword blade should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("sword_blade"),
					"Result should be oak sword blade: " + resultId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_axe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: "xx" / "x "
		RecipeInputInventory inventory = createCraftingInventory(
				new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak axe head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("axe_head"),
					"Result should be oak axe head: " + resultId);
		}

		context.complete();
	}

	// ============================================================
	// Full Crafting Pipeline Test (Player Simulation)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void full_crafting_pipeline_wood_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Step 1: Craft oak pickaxe head
		RecipeInputInventory headInventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> headRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, headInventory, context.getWorld());
		context.assertTrue(headRecipe.isPresent(), "Head recipe must exist");

		ItemStack craftedHead = headRecipe.get().craft(headInventory, context.getWorld().getRegistryManager());
		context.assertFalse(craftedHead.isEmpty(), "Crafted head must not be empty");

		// Step 2: Craft oak handle
		RecipeInputInventory handleInventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> handleRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, handleInventory, context.getWorld());
		context.assertTrue(handleRecipe.isPresent(), "Handle recipe must exist");

		ItemStack craftedHandle = handleRecipe.get().craft(handleInventory, context.getWorld().getRegistryManager());
		context.assertFalse(craftedHandle.isEmpty(), "Crafted handle must not be empty");

		// Step 3: Assemble into pickaxe
		RecipeInputInventory assemblyInventory = createCraftingInventory(
				craftedHead, ItemStack.EMPTY, ItemStack.EMPTY,
				craftedHandle, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> assemblyRecipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, assemblyInventory, context.getWorld());

		if (assemblyRecipe.isEmpty()) {
			// Try center column
			assemblyInventory = createCraftingInventory(
					ItemStack.EMPTY, craftedHead, ItemStack.EMPTY,
					ItemStack.EMPTY, craftedHandle, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			assemblyRecipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, assemblyInventory, context.getWorld());
		}

		context.assertTrue(assemblyRecipe.isPresent(), "Assembly recipe must exist");

		ItemStack craftedPickaxe = assemblyRecipe.get().craft(assemblyInventory, context.getWorld().getRegistryManager());
		context.assertFalse(craftedPickaxe.isEmpty(), "Crafted pickaxe must not be empty");

		// Step 4: Verify properties
		var pickaxeComp = ctx.toComponent(craftedPickaxe);
		context.assertTrue(pickaxeComp.isPresent(), "Pickaxe must be a Forgero component");

		String pickaxeId = pickaxeComp.get().id().toString();
		context.assertTrue(pickaxeId.contains("pickaxe"), "Should be a pickaxe: " + pickaxeId);

		int durability = query.getMaxDurability(craftedPickaxe);
		float miningSpeed = query.getMiningSpeed(craftedPickaxe);

		context.assertTrue(durability > 0, "Pickaxe should have durability");
		context.assertTrue(miningSpeed > 0, "Pickaxe should have mining speed");

		LOGGER.info("Full pipeline test passed: {} (durability={}, miningSpeed={})",
				pickaxeId, durability, miningSpeed);

		context.complete();
	}

	// ============================================================
	// Material Tier Coverage Tests (Diamond, Netherite)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_diamond_pickaxe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("pickaxe_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "pickaxe_head-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.DIAMOND),
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for diamond pickaxe head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("diamond") &&
							resultComp.get().id().toString().contains("pickaxe_head"),
					"Result should be a diamond pickaxe head: " + resultComp.get().id());

			LOGGER.info("Diamond schematic crafting test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_diamond_sword_blade(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("sword_blade-schematic");
		context.assertFalse(schematicStack.isEmpty(), "sword_blade-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.DIAMOND),
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for diamond sword blade should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("diamond") &&
							resultComp.get().id().toString().contains("sword_blade"),
					"Result should be a diamond sword blade: " + resultComp.get().id());
		}

		context.complete();
	}

	/**
	 * Tests assembling a diamond pickaxe from head + handle.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_assembly_diamond_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var headOpt = ctx.component("forgero:diamond-pickaxe_head");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(headOpt.isPresent(), "diamond-pickaxe_head must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var headStack = ctx.toStack(headOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		RecipeInputInventory inventory = createCraftingInventory(
				headStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			inventory = createCraftingInventory(
					ItemStack.EMPTY, headStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Tool assembly recipe for diamond pickaxe should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled tool should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("diamond") && resultId.contains("pickaxe"),
					"Result should be a diamond pickaxe: " + resultId);

			int durability = query.getMaxDurability(result);
			context.assertTrue(durability > 250, "Diamond pickaxe should have high durability: " + durability);

			LOGGER.info("Diamond tool assembly test passed: {} with durability {}", resultId, durability);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void weapon_assembly_diamond_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var bladeOpt = ctx.component("forgero:diamond-sword_blade");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(bladeOpt.isPresent(), "diamond-sword_blade must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var bladeStack = ctx.toStack(bladeOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		RecipeInputInventory inventory = createCraftingInventory(
				bladeStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			inventory = createCraftingInventory(
					ItemStack.EMPTY, bladeStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Weapon assembly recipe for diamond sword should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled sword should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("diamond") && resultId.contains("sword"),
					"Result should be a diamond sword: " + resultId);

			float attackDamage = query.getAttackDamage(result);
			context.assertTrue(attackDamage >= 5.0f, "Diamond sword should have attack damage >= 5.0 (diamond material provides 5.0): " + attackDamage);

			LOGGER.info("Diamond weapon assembly test passed: {} with damage {}", resultId, attackDamage);
		}

		context.complete();
	}

	// ============================================================
	// Missing Part Types Tests (shovel_head, hoe_head, binding)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_shovel_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("shovel_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "shovel_head-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron shovel head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("shovel_head"),
					"Result should be a shovel head: " + resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_hoe_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("hoe_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "hoe_head-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron hoe head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("hoe_head"),
					"Result should be a hoe head: " + resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_handle(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("handle-schematic");
		context.assertFalse(schematicStack.isEmpty(), "handle-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron handle should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("handle"),
					"Result should be a handle: " + resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_binding(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("binding-schematic");
		context.assertFalse(schematicStack.isEmpty(), "binding-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron binding should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("binding"),
					"Result should be a binding: " + resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_sword_guard(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("sword_guard-schematic");
		context.assertFalse(schematicStack.isEmpty(), "sword_guard-schematic must exist in registry");

		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron sword guard should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("sword_guard"),
					"Result should be a sword guard: " + resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Missing Tool Assembly Tests (shovel, hoe)
	// ============================================================

	/**
	 * Tests assembling an iron shovel from head + handle.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_assembly_iron_shovel(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var headOpt = ctx.component("forgero:iron-shovel_head");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(headOpt.isPresent(), "iron-shovel_head must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var headStack = ctx.toStack(headOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		RecipeInputInventory inventory = createCraftingInventory(
				headStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			inventory = createCraftingInventory(
					ItemStack.EMPTY, headStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Tool assembly recipe for iron shovel should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled tool should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("shovel"),
					"Result should be a shovel: " + resultId);

			LOGGER.info("Shovel assembly test passed: {}", resultId);
		}

		context.complete();
	}

	/**
	 * Tests assembling an iron hoe from head + handle.
	 */
	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_assembly_iron_hoe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var headOpt = ctx.component("forgero:iron-hoe_head");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(headOpt.isPresent(), "iron-hoe_head must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var headStack = ctx.toStack(headOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		RecipeInputInventory inventory = createCraftingInventory(
				headStack, ItemStack.EMPTY, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		if (recipe.isEmpty()) {
			inventory = createCraftingInventory(
					ItemStack.EMPTY, headStack, ItemStack.EMPTY,
					ItemStack.EMPTY, handleStack, ItemStack.EMPTY,
					ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
			);
			recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld());
		}

		context.assertTrue(recipe.isPresent(),
				"Tool assembly recipe for iron hoe should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled tool should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("hoe"),
					"Result should be a hoe: " + resultId);

			LOGGER.info("Hoe assembly test passed: {}", resultId);
		}

		context.complete();
	}

	// ============================================================
	// Simple Wood Recipe Tests (additional parts)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_shovel_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: " x " / "xxx"
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak shovel head should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("shovel_head"),
					"Result should be oak shovel head: " + resultId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void simple_wood_oak_binding(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Pattern: " i " / "i i" / " i "
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY,
				new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS),
				ItemStack.EMPTY, new ItemStack(Items.OAK_PLANKS), ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Simple wood recipe for oak binding should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("oak") && resultId.contains("binding"),
					"Result should be oak binding: " + resultId);
		}

		context.complete();
	}

	// ============================================================
	// Upgrade Recipe Tests (tool_with_binding, sword_with_guard)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void tool_with_binding_iron_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var headOpt = ctx.component("forgero:iron-pickaxe_head");
		var bindingOpt = ctx.component("forgero:oak-binding");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(headOpt.isPresent(), "iron-pickaxe_head must exist");
		context.assertTrue(bindingOpt.isPresent(), "oak-binding must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var headStack = ctx.toStack(headOpt.get()).orElseThrow();
		var bindingStack = ctx.toStack(bindingOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		// Pattern: "  a" / " b " / "c  " (head top-right, binding middle, handle bottom-left)
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, headStack,
				ItemStack.EMPTY, bindingStack, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Tool with binding recipe for iron pickaxe should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled tool should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("pickaxe"),
					"Result should be a pickaxe: " + resultId);

			LOGGER.info("Tool with binding test passed: {}", resultId);
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void sword_with_guard_iron_sword(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		var bladeOpt = ctx.component("forgero:iron-sword_blade");
		var guardOpt = ctx.component("forgero:oak-sword_guard");
		var handleOpt = ctx.component("forgero:oak-handle");

		context.assertTrue(bladeOpt.isPresent(), "iron-sword_blade must exist");
		context.assertTrue(guardOpt.isPresent(), "oak-sword_guard must exist");
		context.assertTrue(handleOpt.isPresent(), "oak-handle must exist");

		var bladeStack = ctx.toStack(bladeOpt.get()).orElseThrow();
		var guardStack = ctx.toStack(guardOpt.get()).orElseThrow();
		var handleStack = ctx.toStack(handleOpt.get()).orElseThrow();

		// Pattern: "  a" / " b " / "c  " (blade top-right, guard middle, handle bottom-left)
		RecipeInputInventory inventory = createCraftingInventory(
				ItemStack.EMPTY, ItemStack.EMPTY, bladeStack,
				ItemStack.EMPTY, guardStack, ItemStack.EMPTY,
				handleStack, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Sword with guard recipe for iron sword should exist");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Assembled sword should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");

			String resultId = resultComp.get().id().toString();
			context.assertTrue(resultId.contains("sword"),
					"Result should be a sword: " + resultId);

			float attackDamage = query.getAttackDamage(result);
			context.assertTrue(attackDamage > 0, "Sword should have attack damage: " + attackDamage);

			LOGGER.info("Sword with guard test passed: {} with damage {}", resultId, attackDamage);
		}

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private static ItemStack getForgeroItem(String id) {
		Identifier itemId = new Identifier("forgero", id);
		if (!Registries.ITEM.containsId(itemId)) {
			return ItemStack.EMPTY;
		}
		Item item = Registries.ITEM.get(itemId);
		return new ItemStack(item);
	}

	private static RecipeInputInventory createCraftingInventory(ItemStack... stacks) {
		CraftingInventory inventory = new CraftingInventory(new ScreenHandler(null, -1) {
			@Override
			public ItemStack quickMove(PlayerEntity player, int slot) {
				return ItemStack.EMPTY;
			}

			@Override
			public boolean canUse(PlayerEntity player) {
				return true;
			}
		}, 3, 3);

		for (int i = 0; i < stacks.length && i < 9; i++) {
			inventory.setStack(i, stacks[i]);
		}

		return inventory;
	}
}

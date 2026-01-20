package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestContext;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for mining tool head variants (hammer, mandrill pickaxe, spade, felling axe).
 * <p>
 * These heads are alternative part variants that work with existing tool equipment.
 * When combined with a handle, they produce the base tool (pickaxe/axe/shovel)
 * but with the specialized head component that provides different stats.
 * <p>
 * Key validations:
 * <ul>
 *   <li>Mining head parts exist as unique items</li>
 *   <li>Mining heads have correct type tags (pickaxe_head, axe_head, shovel_head)</li>
 *   <li>Schematic recipes craft the mining heads</li>
 *   <li>Assembly recipes produce base tools with mining head components</li>
 *   <li>Resulting tools have enhanced stats from mining heads</li>
 * </ul>
 */
public class MiningToolsTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MiningToolsTest.class);

	// ============================================================
	// Mining Head Part Existence Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_hammer_head_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-hammer_head")
				.orElseThrow(() -> new AssertionError("Iron hammer head component must exist"));

		ItemStack stack = ctx.toStack(head)
				.orElseThrow(() -> new AssertionError("Iron hammer head must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron hammer head ItemStack must not be empty");
		assertEquals("Iron Hammer Head", stack.getName().getString(),
				"Iron hammer head must display as 'Iron Hammer Head'");

		LOGGER.info("Hammer head test passed: {}", head.id());
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_mandrill_pickaxe_head_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-mandrill_pickaxe_head")
				.orElseThrow(() -> new AssertionError("Iron mandrill pickaxe head component must exist"));

		ItemStack stack = ctx.toStack(head)
				.orElseThrow(() -> new AssertionError("Iron mandrill pickaxe head must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron mandrill pickaxe head ItemStack must not be empty");
		assertEquals("Iron Mandrill Pickaxe Head", stack.getName().getString(),
				"Iron mandrill pickaxe head must display as 'Iron Mandrill Pickaxe Head'");

		LOGGER.info("Mandrill pickaxe head test passed: {}", head.id());
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_spade_head_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-spade_head")
				.orElseThrow(() -> new AssertionError("Iron spade head component must exist"));

		ItemStack stack = ctx.toStack(head)
				.orElseThrow(() -> new AssertionError("Iron spade head must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron spade head ItemStack must not be empty");
		assertEquals("Iron Spade Head", stack.getName().getString(),
				"Iron spade head must display as 'Iron Spade Head'");

		LOGGER.info("Spade head test passed: {}", head.id());
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void iron_felling_axe_head_exists_and_converts(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-felling_axe_head")
				.orElseThrow(() -> new AssertionError("Iron felling axe head component must exist"));

		ItemStack stack = ctx.toStack(head)
				.orElseThrow(() -> new AssertionError("Iron felling axe head must convert to ItemStack"));

		assertFalse(stack.isEmpty(), "Iron felling axe head ItemStack must not be empty");
		assertEquals("Iron Felling Axe Head", stack.getName().getString(),
				"Iron felling axe head must display as 'Iron Felling Axe Head'");

		LOGGER.info("Felling axe head test passed: {}", head.id());
		context.complete();
	}

	// ============================================================
	// Mining Head Tag Tests (verifying correct type tags)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void hammer_head_has_pickaxe_head_type_tag(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-hammer_head")
				.orElseThrow(() -> new AssertionError("Iron hammer head must exist"));

		boolean hasPickaxeHeadTag = head.getTags().stream()
				.anyMatch(tag -> tag.toString().contains("parts/types/pickaxe_head"));

		assertTrue(hasPickaxeHeadTag,
				"Hammer head must have forgero:parts/types/pickaxe_head tag. Tags: " + head.getTags());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mandrill_pickaxe_head_has_pickaxe_head_type_tag(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-mandrill_pickaxe_head")
				.orElseThrow(() -> new AssertionError("Iron mandrill pickaxe head must exist"));

		boolean hasPickaxeHeadTag = head.getTags().stream()
				.anyMatch(tag -> tag.toString().contains("parts/types/pickaxe_head"));

		assertTrue(hasPickaxeHeadTag,
				"Mandrill pickaxe head must have forgero:parts/types/pickaxe_head tag. Tags: " + head.getTags());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void spade_head_has_shovel_head_type_tag(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-spade_head")
				.orElseThrow(() -> new AssertionError("Iron spade head must exist"));

		boolean hasShovelHeadTag = head.getTags().stream()
				.anyMatch(tag -> tag.toString().contains("parts/types/shovel_head"));

		assertTrue(hasShovelHeadTag,
				"Spade head must have forgero:parts/types/shovel_head tag. Tags: " + head.getTags());

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void felling_axe_head_has_axe_head_type_tag(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		Component head = ctx.component("forgero:iron-felling_axe_head")
				.orElseThrow(() -> new AssertionError("Iron felling axe head must exist"));

		boolean hasAxeHeadTag = head.getTags().stream()
				.anyMatch(tag -> tag.toString().contains("parts/types/axe_head"));

		assertTrue(hasAxeHeadTag,
				"Felling axe head must have forgero:parts/types/axe_head tag. Tags: " + head.getTags());

		context.complete();
	}

	// ============================================================
	// Schematic Crafting Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_hammer_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("hammer_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "hammer_head-schematic must exist in registry");

		// Hammer head requires 4 materials
		RecipeInputInventory inventory = createCraftingInventory(
				schematicStack,
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				new ItemStack(Items.IRON_INGOT),
				ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);

		Optional<CraftingRecipe> recipe = recipeManager.getFirstMatch(
				RecipeType.CRAFTING, inventory, context.getWorld());

		context.assertTrue(recipe.isPresent(),
				"Schematic recipe for iron hammer head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("hammer_head"),
					"Result should be a hammer head: " + resultComp.get().id());

			LOGGER.info("Hammer head schematic crafting test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void schematic_crafting_iron_spade_head(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack schematicStack = getForgeroItem("spade_head-schematic");
		context.assertFalse(schematicStack.isEmpty(), "spade_head-schematic must exist in registry");

		// Spade head requires 2 materials
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
				"Schematic recipe for iron spade head should match");

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().craft(inventory, context.getWorld().getRegistryManager());
			context.assertFalse(result.isEmpty(), "Crafted item should not be empty");

			var resultComp = ctx.toComponent(result);
			context.assertTrue(resultComp.isPresent(), "Result must be a Forgero component");
			context.assertTrue(resultComp.get().id().toString().contains("spade_head"),
					"Result should be a spade head: " + resultComp.get().id());

			LOGGER.info("Spade head schematic crafting test passed: {}", resultComp.get().id());
		}

		context.complete();
	}

	// ============================================================
	// Tool Assembly Tests (mining head + handle = base tool)
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void hammer_head_plus_handle_creates_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Get the parts
		ItemStack hammerHead = getItemStackOrFail(ctx, "forgero:iron-hammer_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		// Try assembly
		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, hammerHead, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for hammer head + handle must exist"));

		RecipeInputInventory inventory = createVerticalInventory(hammerHead, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		// Verify result is a pickaxe
		assertFalse(result.isEmpty(), "Crafted tool must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		String resultId = resultComp.id().toString();
		assertTrue(resultId.contains("pickaxe"),
				"Hammer head + handle should create a pickaxe, got: " + resultId);

		// Verify the pickaxe contains the hammer head component
		if (resultComp instanceof StructuredComponent structured) {
			var headPart = structured.structure().allParts().stream()
					.filter(slot -> slot.id().name().equals("head"))
					.findFirst();
			
			assertTrue(headPart.isPresent(), "Pickaxe should have a head slot");
			assertTrue(headPart.get().content().id().toString().contains("hammer_head"),
					"Pickaxe head should be hammer_head, got: " + headPart.get().content().id());
		}

		LOGGER.info("Hammer head assembly test passed: {} with hammer_head component", resultId);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mandrill_pickaxe_head_plus_handle_creates_pickaxe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack mandrillHead = getItemStackOrFail(ctx, "forgero:iron-mandrill_pickaxe_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, mandrillHead, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for mandrill pickaxe head + handle must exist"));

		RecipeInputInventory inventory = createVerticalInventory(mandrillHead, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		assertFalse(result.isEmpty(), "Crafted tool must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		String resultId = resultComp.id().toString();
		assertTrue(resultId.contains("pickaxe"),
				"Mandrill pickaxe head + handle should create a pickaxe, got: " + resultId);

		LOGGER.info("Mandrill pickaxe head assembly test passed: {}", resultId);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void spade_head_plus_handle_creates_shovel(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack spadeHead = getItemStackOrFail(ctx, "forgero:iron-spade_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, spadeHead, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for spade head + handle must exist"));

		RecipeInputInventory inventory = createVerticalInventory(spadeHead, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		assertFalse(result.isEmpty(), "Crafted tool must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		String resultId = resultComp.id().toString();
		assertTrue(resultId.contains("shovel"),
				"Spade head + handle should create a shovel, got: " + resultId);

		LOGGER.info("Spade head assembly test passed: {}", resultId);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void felling_axe_head_plus_handle_creates_axe(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		ItemStack fellingAxeHead = getItemStackOrFail(ctx, "forgero:iron-felling_axe_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe recipe = findAssemblyRecipe(context, recipeManager, fellingAxeHead, handle)
				.orElseThrow(() -> new AssertionError(
						"Assembly recipe for felling axe head + handle must exist"));

		RecipeInputInventory inventory = createVerticalInventory(fellingAxeHead, handle);
		ItemStack result = recipe.craft(inventory, context.getWorld().getRegistryManager());

		assertFalse(result.isEmpty(), "Crafted tool must not be empty");

		Component resultComp = ctx.toComponent(result)
				.orElseThrow(() -> new AssertionError("Crafted item must be a Forgero component"));

		String resultId = resultComp.id().toString();
		assertTrue(resultId.contains("axe"),
				"Felling axe head + handle should create an axe, got: " + resultId);

		LOGGER.info("Felling axe head assembly test passed: {}", resultId);
		context.complete();
	}

	// ============================================================
	// Mining Head Stat Bonus Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void hammer_head_pickaxe_has_higher_durability_than_regular(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Create regular iron pickaxe
		ItemStack regularHead = getItemStackOrFail(ctx, "forgero:iron-pickaxe_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe regularRecipe = findAssemblyRecipe(context, recipeManager, regularHead, handle)
				.orElseThrow(() -> new AssertionError("Regular pickaxe recipe must exist"));

		ItemStack regularPickaxe = regularRecipe.craft(
				createVerticalInventory(regularHead, handle),
				context.getWorld().getRegistryManager());

		// Create hammer pickaxe
		ItemStack hammerHead = getItemStackOrFail(ctx, "forgero:iron-hammer_head");

		CraftingRecipe hammerRecipe = findAssemblyRecipe(context, recipeManager, hammerHead, handle)
				.orElseThrow(() -> new AssertionError("Hammer pickaxe recipe must exist"));

		ItemStack hammerPickaxe = hammerRecipe.craft(
				createVerticalInventory(hammerHead, handle),
				context.getWorld().getRegistryManager());

		// Compare durability
		int regularDurability = query.getMaxDurability(regularPickaxe);
		int hammerDurability = query.getMaxDurability(hammerPickaxe);

		assertTrue(hammerDurability > regularDurability,
				"Hammer pickaxe durability (" + hammerDurability + 
				") should be higher than regular pickaxe (" + regularDurability + ")");

		LOGGER.info("Hammer durability bonus test passed: {} > {}", hammerDurability, regularDurability);
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mandrill_pickaxe_has_higher_mining_speed_than_regular(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);
		var query = ForgeroApi.itemQuery();
		RecipeManager recipeManager = context.getWorld().getRecipeManager();

		// Create regular iron pickaxe
		ItemStack regularHead = getItemStackOrFail(ctx, "forgero:iron-pickaxe_head");
		ItemStack handle = getItemStackOrFail(ctx, "forgero:oak-handle");

		CraftingRecipe regularRecipe = findAssemblyRecipe(context, recipeManager, regularHead, handle)
				.orElseThrow(() -> new AssertionError("Regular pickaxe recipe must exist"));

		ItemStack regularPickaxe = regularRecipe.craft(
				createVerticalInventory(regularHead, handle),
				context.getWorld().getRegistryManager());

		// Create mandrill pickaxe
		ItemStack mandrillHead = getItemStackOrFail(ctx, "forgero:iron-mandrill_pickaxe_head");

		CraftingRecipe mandrillRecipe = findAssemblyRecipe(context, recipeManager, mandrillHead, handle)
				.orElseThrow(() -> new AssertionError("Mandrill pickaxe recipe must exist"));

		ItemStack mandrillPickaxe = mandrillRecipe.craft(
				createVerticalInventory(mandrillHead, handle),
				context.getWorld().getRegistryManager());

		// Compare mining speed
		float regularSpeed = query.getMiningSpeed(regularPickaxe);
		float mandrillSpeed = query.getMiningSpeed(mandrillPickaxe);

		assertTrue(mandrillSpeed > regularSpeed,
				"Mandrill pickaxe mining speed (" + mandrillSpeed + 
				") should be higher than regular pickaxe (" + regularSpeed + ")");

		LOGGER.info("Mandrill mining speed bonus test passed: {} > {}", mandrillSpeed, regularSpeed);
		context.complete();
	}

	// ============================================================
	// Material Coverage Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_mining_heads_exist_for_diamond(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		String[] requiredHeads = {
				"forgero:diamond-hammer_head",
				"forgero:diamond-mandrill_pickaxe_head",
				"forgero:diamond-spade_head",
				"forgero:diamond-felling_axe_head"
		};

		for (String headId : requiredHeads) {
			Component head = ctx.component(headId)
					.orElseThrow(() -> new AssertionError(
							"Mining head must exist: " + headId));

			ItemStack stack = ctx.toStack(head)
					.orElseThrow(() -> new AssertionError(
							"Mining head must convert to ItemStack: " + headId));

			assertFalse(stack.isEmpty(),
					"Mining head ItemStack must not be empty: " + headId);
		}

		LOGGER.info("Diamond mining heads coverage test passed");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void all_mining_heads_exist_for_netherite(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		String[] requiredHeads = {
				"forgero:netherite-hammer_head",
				"forgero:netherite-mandrill_pickaxe_head",
				"forgero:netherite-spade_head",
				"forgero:netherite-felling_axe_head"
		};

		for (String headId : requiredHeads) {
			Component head = ctx.component(headId)
					.orElseThrow(() -> new AssertionError(
							"Mining head must exist: " + headId));

			ItemStack stack = ctx.toStack(head)
					.orElseThrow(() -> new AssertionError(
							"Mining head must convert to ItemStack: " + headId));

			assertFalse(stack.isEmpty(),
					"Mining head ItemStack must not be empty: " + headId);
		}

		LOGGER.info("Netherite mining heads coverage test passed");
		context.complete();
	}

	// ============================================================
	// Negative Tests
	// ============================================================

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mining_heads_must_not_create_dedicated_equipment_items(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		// These equipment IDs should NOT exist (we removed the dedicated equipment)
		String[] forbiddenEquipment = {
				"forgero:iron-hammer",
				"forgero:iron-mandrill_pickaxe",
				"forgero:iron-spade",
				"forgero:iron-felling_axe"
		};

		for (String equipmentId : forbiddenEquipment) {
			Optional<Component> equipment = ctx.component(equipmentId);
			assertFalse(equipment.isPresent(),
					"Dedicated equipment item should NOT exist: " + equipmentId + 
					". Mining heads should use base tool items (pickaxe/axe/shovel) instead.");
		}

		LOGGER.info("No dedicated equipment items test passed");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void mining_head_names_must_not_be_translation_keys(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		String[] heads = {
				"forgero:iron-hammer_head",
				"forgero:iron-mandrill_pickaxe_head",
				"forgero:iron-spade_head",
				"forgero:iron-felling_axe_head"
		};

		for (String headId : heads) {
			ItemStack stack = getItemStackOrFail(ctx, headId);
			String name = stack.getName().getString();

			assertFalse(name.startsWith("item."),
					"Head " + headId + " name must not be a translation key, got: " + name);
			assertFalse(name.contains("forgero:"),
					"Head " + headId + " name must not contain namespace, got: " + name);
		}

		context.complete();
	}

	// ============================================================
	// Helper Methods
	// ============================================================

	private ItemStack getItemStackOrFail(ForgeroTestContext ctx, String componentId) {
		Component component = ctx.component(componentId)
				.orElseThrow(() -> new AssertionError(
						"Component must exist: " + componentId));

		return ctx.toStack(component)
				.orElseThrow(() -> new AssertionError(
						"Component must convert to ItemStack: " + componentId));
	}

	private static ItemStack getForgeroItem(String id) {
		Identifier itemId = new Identifier("forgero", id);
		if (!Registries.ITEM.containsId(itemId)) {
			return ItemStack.EMPTY;
		}
		Item item = Registries.ITEM.get(itemId);
		return new ItemStack(item);
	}

	private static RecipeInputInventory createVerticalInventory(ItemStack top, ItemStack bottom) {
		return createCraftingInventory(
				top, ItemStack.EMPTY, ItemStack.EMPTY,
				bottom, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
	}

	private static Optional<CraftingRecipe> findAssemblyRecipe(
			TestContext context, RecipeManager recipeManager, ItemStack head, ItemStack handle) {
		
		// Try column 0
		RecipeInputInventory inv1 = createCraftingInventory(
				head, ItemStack.EMPTY, ItemStack.EMPTY,
				handle, ItemStack.EMPTY, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
		var recipe = recipeManager.getFirstMatch(RecipeType.CRAFTING, inv1, context.getWorld());
		if (recipe.isPresent()) return recipe;

		// Try column 1
		RecipeInputInventory inv2 = createCraftingInventory(
				ItemStack.EMPTY, head, ItemStack.EMPTY,
				ItemStack.EMPTY, handle, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
		);
		return recipeManager.getFirstMatch(RecipeType.CRAFTING, inv2, context.getWorld());
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

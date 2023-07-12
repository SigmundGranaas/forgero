package com.sigmundgranaas.forgerofabric.testutil;

import static com.sigmundgranaas.forgero.fabric.block.DummyHandler.dummyHandler;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeTester<T extends Inventory, R extends Recipe<T>> implements Supplier<Boolean> {

	private final TestContext context;

	private final T inventory;

	private final RecipeType<R> type;

	private final Item expectedResult;

	public RecipeTester(TestContext context, T inventory, Item expectedResult, RecipeType<R> type) {
		this.context = context;
		this.inventory = inventory;
		this.expectedResult = expectedResult;
		this.type = type;
	}

	public static RecipeTester<CraftingInventory, CraftingRecipe> ofTool(String head, String handle, String outCome, TestContext context) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		inventory.setStack(1, new ItemStack(itemFromString(head)));
		inventory.setStack(3, new ItemStack(itemFromString(handle)));
		Item outcome = itemFromString(outCome);

		return new RecipeTester<>(context, inventory, outcome, RecipeType.CRAFTING);
	}

	public static RecipeTester<CraftingInventory, CraftingRecipe> ofTool(String head, String binding, String handle, String outCome, TestContext context) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		inventory.setStack(2, new ItemStack(itemFromString(head)));
		inventory.setStack(4, new ItemStack(itemFromString(binding)));
		inventory.setStack(6, new ItemStack(itemFromString(handle)));
		Item outcome = itemFromString(outCome);

		return new RecipeTester<>(context, inventory, outcome, RecipeType.CRAFTING);
	}

	private static Item itemFromString(String identifier) {
		Item item = Registry.ITEM.get(new Identifier(identifier));
		if (item == Items.AIR) {
			return Registry.ITEM.get(new Identifier("forgero:" + identifier));
		}
		return item;
	}

	public static RecipeTester<CraftingInventory, CraftingRecipe> ofPart(String schematic, String material, int count, String result, TestContext context) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		inventory.setStack(0, new ItemStack(itemFromString(schematic)));
		IntStream.range(0, count).forEach(index -> inventory.setStack(index + 1, new ItemStack(itemFromString(material))));
		Item outcome = itemFromString(result);

		return new RecipeTester<>(context, inventory, outcome, RecipeType.CRAFTING);
	}

	public static RecipeTester<CraftingInventory, CraftingRecipe> repairKit(String kit, ItemStack tool, String result, TestContext context) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		inventory.setStack(0, new ItemStack(itemFromString(kit)));
		inventory.setStack(1, tool);

		Item outcome = itemFromString(result);
		return new RecipeTester<>(context, inventory, outcome, RecipeType.CRAFTING);
	}

	public static RecipeTester<Inventory, SmithingRecipe> smithingUpgrade(String target, String upgrade, TestContext context) {
		SimpleInventory inventory = new SimpleInventory(2);
		inventory.setStack(0, new ItemStack(itemFromString(target)));
		inventory.setStack(1, new ItemStack(itemFromString(upgrade)));

		Item outcome = itemFromString(target);
		return new RecipeTester<>(context, inventory, outcome, RecipeType.SMITHING);
	}

	public static RecipeTester<CraftingInventory, CraftingRecipe> craftingTableUpgrade(String target, String upgrade, TestContext context) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		inventory.setStack(0, new ItemStack(itemFromString(target)));
		inventory.setStack(1, new ItemStack(itemFromString(upgrade)));

		Item outcome = itemFromString(target);
		return new RecipeTester<>(context, inventory, outcome, RecipeType.CRAFTING);
	}

	public Optional<ItemStack> craft() {
		return context.getWorld()
				.getRecipeManager()
				.getFirstMatch(type, inventory, context.getWorld())
				.map(recipe -> recipe.craft(inventory))
				.filter(stack -> stack.isOf(expectedResult))
				.stream()
				.findAny();
	}

	public Boolean get() {
		return craft()
				.isPresent();
	}
}

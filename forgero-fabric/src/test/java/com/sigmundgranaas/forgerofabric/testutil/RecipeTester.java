package com.sigmundgranaas.forgerofabric.testutil;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.dummyHandler;

public class RecipeTester implements Supplier<Boolean> {

    private final TestContext context;

    private final CraftingInventory inventory;

    private final Item expectedResult;

    public RecipeTester(TestContext context, CraftingInventory inventory, Item expectedResult) {
        this.context = context;
        this.inventory = inventory;
        this.expectedResult = expectedResult;
    }

    public static RecipeTester ofTool(String head, String handle, String outCome, TestContext context) {
        CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
        inventory.setStack(1, new ItemStack(itemFromString(head)));
        inventory.setStack(3, new ItemStack(itemFromString(handle)));
        Item outcome = itemFromString(outCome);

        return new RecipeTester(context, inventory, outcome);
    }

    private static Item itemFromString(String identifier) {
        Item item = Registries.ITEM.get(new Identifier(identifier));
        if (item == Items.AIR) {
            return Registries.ITEM.get(new Identifier("forgero:" + identifier));
        }
        return item;
    }

    public static RecipeTester ofPart(String schematic, String material, int count, String result, TestContext context) {
        CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
        inventory.setStack(0, new ItemStack(itemFromString(schematic)));
        IntStream.range(0, count).forEach(index -> inventory.setStack(index + 1, new ItemStack(itemFromString(material))));
        Item outcome = itemFromString(result);

        return new RecipeTester(context, inventory, outcome);
    }

    public static RecipeTester repairKit(String kit, ItemStack tool, String result, TestContext context) {
        CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
        inventory.setStack(0, new ItemStack(itemFromString(kit)));
        inventory.setStack(1,tool);

        Item outcome = itemFromString(result);
        return new RecipeTester(context, inventory, outcome);
    }

    public Optional<ItemStack> craft() {
        return context.getWorld()
                .getRecipeManager()
                .getFirstMatch(RecipeType.CRAFTING, inventory, context.getWorld())
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

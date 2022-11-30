package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.recipe.RecipeCollection;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import com.sigmundgranaas.forgero.schematic.Schematic;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.gametest.GameTestHelper.createDummyServerPlayer;
import static com.sigmundgranaas.forgero.gametest.RecipeHelper.parseCraftingTableRecipe;
import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithCraftingScreenHandler;

public class RecipeCreation {

    public static Item testHandleRecipe(Item ingredient, Schematic pattern, ServerPlayerEntity player) {
        CraftingScreenHandler handler = ((CraftingScreenHandler) player.currentScreenHandler);
        handler.getSlot(1).setStack(new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getStringIdentifier()))));
        for (int i = 0; i < pattern.getMaterialCount(); i++) {
            handler.getSlot(i + 2).setStack(new ItemStack(ingredient));
        }

        Item actualOutput = handler.getSlot(handler.getCraftingResultSlotIndex()).getStack().getItem();
        handler.clearCraftingSlots();
        return actualOutput;
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing")
    public void testCraftAllHandles(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithCraftingScreenHandler(context);

        int total = 0;
        int correct = 0;
        for (Item toolPart : ForgeroItemRegistry.TOOL_PART_ITEM.getHandles().stream().map(ToolPartItem::getItem).toList()
        ) {
            if (((ToolPartItem) toolPart).getPrimaryMaterial().getIngredient().item == null) {
                break;
            }
            Item output = testHandleRecipe(Registry.ITEM.get(new Identifier(((ToolPartItem) toolPart).getPrimaryMaterial().getIngredient().item)), ((ToolPartItem) toolPart).getPart().getSchematic(), mockPlayer);
            if (output instanceof ToolPartItem && ((ToolPartItem) output).getPart().getToolPartIdentifier().equals(((ToolPartItem) toolPart).getPart().getToolPartIdentifier())) {
                total++;
                correct++;
            } else {
                total++;
                ForgeroInitializer.LOGGER.error("Expected {}, but got {}", ((ToolPartItem) toolPart).getIdentifier(), output.asItem().getName());
                //throw new GameTestException(String.format("Crafting recipe for %s is bad", ((ToolPartItem) toolPart).getIdentifier()));
            }
        }

        if (total == correct) {
            ForgeroInitializer.LOGGER.info("tested {} recipes, where {}/{} were correct", total, correct, total);
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.info("tested {} recipes, where {}/{} were correct", total, correct, total);
            throw new GameTestException("recipe testing failed");
        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing tools", required = true)
    public void testingToolRecipe(TestContext context) {
        testingGeneratedRecipesStreamReduce(context, RecipeTypes.TOOL_RECIPE);
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing tools with binding", required = true)
    public void testingToolWithBindingRecipe(TestContext context) {
        testingGeneratedRecipesStreamReduce(context, RecipeTypes.TOOL_WITH_BINDING_RECIPE);
    }


    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing schematics", required = false)
    public void testingSchematicRecipes(TestContext context) {
        testingGeneratedRecipesStreamReduce(context, RecipeTypes.TOOLPART_SCHEMATIC_RECIPE);
    }


    public void testingGeneratedRecipes(TestContext context, RecipeTypes type) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(1, 1, 1)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(1, 1, 1)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        var recipes = RecipeCollection.INSTANCE.getRecipes().stream().filter(recipe -> recipe.getRecipeType() == type).toList();
        int total = recipes.size();
        int correct = 0;
        for (RecipeWrapper recipe : recipes) {

            var parsedRecipe = parseCraftingTableRecipe(recipe);
            var desiredOutput = RecipeHelper.getDesiredOutPut(recipe);
            var actualOutput = RecipeHelper.craftRecipe(parsedRecipe, mockPlayer);

            if (desiredOutput.getItem() == actualOutput.getItem()) {
                total++;
                correct++;
            } else if (actualOutput.getItem() != Items.AIR) {
                ForgeroInitializer.LOGGER.warn("expected {} to be output, but got {}. There is a possible conflict in the recipes", desiredOutput.toString(), actualOutput.toString());
            }
        }
        if (total == correct) {
            ForgeroInitializer.LOGGER.info("tested {} {} recipes, where {}/{} were correct", total, type.toString(), correct, total);
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("tested {} {} recipes, where {}/{} were correct", total, type.toString(), correct, total);
            throw new GameTestException("recipe testing failed");
        }
    }

    public void testingGeneratedRecipesStreamReduce(TestContext context, RecipeTypes type) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(1, 1, 1)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(1, 1, 1)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        var recipes = RecipeCollection.INSTANCE.getRecipes().stream().filter(recipe -> recipe.getRecipeType() == type).toList();
        int total = recipes.size();
        int correct = recipes.stream().map(recipe -> {
            var parsedRecipe = parseCraftingTableRecipe(recipe);
            var desiredOutput = RecipeHelper.getDesiredOutPut(recipe);
            var actualOutput = RecipeHelper.craftRecipe(parsedRecipe, mockPlayer);

            if (desiredOutput.getItem() == actualOutput.getItem()) {
                return 1;
            } else if (actualOutput.getItem() != Items.AIR) {
                ForgeroInitializer.LOGGER.warn("expected {} to be output, but got {}. There is a possible conflict in the recipes", desiredOutput.toString(), actualOutput.toString());

                return 0;
            }
            return 0;
        }).reduce(0, Integer::sum);

        if (total == correct) {
            ForgeroInitializer.LOGGER.info("tested {} {} recipes, where {}/{} were correct", total, type.toString(), correct, total);
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("tested {} {} recipes, where {}/{} were correct", total, type.toString(), correct, total);
            throw new GameTestException("recipe testing failed");
        }
    }

}

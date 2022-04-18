package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.recipe.RecipeCollection;
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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.gametest.GameTestHelper.createDummyServerPlayer;
import static com.sigmundgranaas.forgero.gametest.RecipeHelper.parseCraftingTableRecipe;
import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithCraftingScreenHandler;

public class RecipeCreation {

    public static Item testHandleRecipe(Item ingredient, Pattern pattern, ServerPlayerEntity player) {
        CraftingScreenHandler handler = ((CraftingScreenHandler) player.currentScreenHandler);
        handler.getSlot(1).setStack(new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, pattern.getPatternIdentifier()))));
        for (int i = 0; i < pattern.getMaterialCount(); i++) {
            handler.getSlot(i + 2).setStack(new ItemStack(ingredient));
        }

        Item actualOutput = handler.getSlot(handler.getCraftingResultSlotIndex()).getStack().getItem();
        handler.clearCraftingSlots();
        return actualOutput;
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing")
    public void testCraftAllHandles(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithCraftingScreenHandler(context);

        int total = 0;
        int correct = 0;
        for (Item toolPart : ItemCollection.INSTANCE.getToolParts().stream().filter(toolPart -> ((ToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HANDLE).collect(Collectors.toList())
        ) {
            Item output = testHandleRecipe(Registry.ITEM.get(new Identifier(((ToolPartItem) toolPart).getPrimaryMaterial().getIngredient())), ((ToolPartItem) toolPart).getPart().getPattern(), mockPlayer);
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

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing", required = true)
    public void testingGeneratedRecipes(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(1, 1, 1)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(1, 1, 1)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        AtomicInteger total = new AtomicInteger();
        AtomicInteger correct = new AtomicInteger();
        RecipeCollection.INSTANCE.getRecipes().stream().filter(RecipeHelper::isCraftingTableRecipe).forEach(recipe -> {
            var parsedRecipe = parseCraftingTableRecipe(recipe);
            var desiredOutput = RecipeHelper.getDesiredOutPut(recipe);
            var actualOutput = RecipeHelper.craftRecipe(parsedRecipe, mockPlayer);

            if (desiredOutput.getItem() == actualOutput.getItem()) {
                total.getAndIncrement();
                correct.getAndIncrement();
            } else if (actualOutput.getItem() != Items.AIR) {
                ForgeroInitializer.LOGGER.warn("expected {} to be output, but got {}. There is a possible conflict in the recipes", desiredOutput.toString(), actualOutput.toString());
                total.getAndIncrement();
            } else {
                total.getAndIncrement();
            }
        });

        if (total.get() == correct.get()) {
            ForgeroInitializer.LOGGER.info("tested {} recipes, where {}/{} were correct", total.get(), correct.get(), total.get());
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("tested {} recipes, where {}/{} were correct", total.get(), correct.get(), total.get());
            throw new GameTestException("recipe testing failed");
        }
    }

}

package com.sigmundgranaas.forgero.gametest;

import com.google.gson.JsonArray;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

import static com.sigmundgranaas.forgero.gametest.GameTestHelper.createDummyServerPlayer;

public class RecipeHelper {
    public static ItemStack getDesiredOutPut(RecipeWrapper wrapper) {
        Identifier itemId = new Identifier(wrapper.getRecipe().getAsJsonObject("result").get("item").getAsString());
        return new ItemStack(Registry.ITEM.get(itemId));
    }

    public static ItemStack craftRecipe(List<Pair<Integer, ItemStack>> ingredients, ServerPlayerEntity player) {
        CraftingScreenHandler handler = ((CraftingScreenHandler) player.currentScreenHandler);
        ingredients.forEach(ingredient -> handler.getSlot(ingredient.getLeft()).setStack(ingredient.getRight()));
        ItemStack actualOutput = handler.getSlot(handler.getCraftingResultSlotIndex()).getStack().copy();
        handler.clearCraftingSlots();
        return actualOutput;
    }

    public static List<Pair<Integer, ItemStack>> parseCraftingTableRecipe(RecipeWrapper wrapper) {
        List<Pair<Integer, ItemStack>> ingredients = new ArrayList<>();
        JsonArray pattern = wrapper.getRecipe().getAsJsonArray("pattern");
        for (int i = 0; i < pattern.size(); i++) {
            String patternLine = wrapper.getRecipe().getAsJsonArray("pattern").get(i).getAsString();
            char emptyPattern = ' ';
            for (int j = 0; j < patternLine.length(); j++) {
                if (patternLine.charAt(j) != emptyPattern) {
                    String itemIdentifier;
                    if (wrapper.getRecipe().getAsJsonObject("key").getAsJsonObject(String.valueOf(patternLine.charAt(j))).has("item")) {
                        itemIdentifier = wrapper.getRecipe().getAsJsonObject("key").getAsJsonObject(String.valueOf(patternLine.charAt(j))).get("item").getAsString();
                    } else if (wrapper.getRecipe().getAsJsonObject("key").getAsJsonObject(String.valueOf(patternLine.charAt(j))).get("tag").getAsString().contains("handle")) {
                        itemIdentifier = "forgero:oak-handle";
                    } else {
                        itemIdentifier = "forgero:oak-binding";
                    }
                    Identifier itemId = new Identifier(itemIdentifier);
                    ItemStack stack = new ItemStack(Registry.ITEM.get(itemId));
                    ingredients.add(new Pair<>((3 * i + j) + 1, stack));
                }
            }
        }
        return ingredients;
    }

    public static ServerPlayerEntity setUpDummyPlayerWithCraftingScreenHandler(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(1, 1, 1)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(1, 1, 1)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        return mockPlayer;
    }

    public static ServerPlayerEntity setUpDummyPlayerWithSmithingScreenHandler(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(1, 1, 1), Blocks.SMITHING_TABLE);
        SmithingTableBlock smithingTableBlock = (SmithingTableBlock) context.getBlockState(new BlockPos(1, 1, 1)).getBlock();
        mockPlayer.currentScreenHandler = smithingTableBlock.createScreenHandlerFactory(smithingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(1, 1, 1)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        return mockPlayer;
    }
}

package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class ToolRecipe extends ShapedRecipe {

    public ToolRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack headItem = null;
        ItemStack handleItem = null;


        List<Ingredient> ingredients = super.getIngredients();

        for (int ingredientSlot = 0; ingredientSlot < ingredients.size(); ingredientSlot++) {
            if (ingredients.get(ingredientSlot).getMatchingStacks().length > 0) {
                for (int craftingSlot = 0; craftingSlot < craftingInventory.size(); craftingSlot++) {
                    if (ingredients.get(ingredientSlot).test(craftingInventory.getStack(craftingSlot))) {
                        ItemStack toolPart = craftingInventory.getStack(craftingSlot);
                        if (headItem == null) {
                            headItem = toolPart;
                        } else if (handleItem == null) {
                            handleItem = toolPart;
                        }
                    }
                }
            }
        }

        assert headItem != null;
        assert handleItem != null;

        ToolPartHead head;
        ToolPartHandle handle;

        if (headItem.hasNbt() && headItem.getNbt().contains(NBTFactory.HEAD_NBT_IDENTIFIER)) {
            head = (ToolPartHead) NBTFactory.INSTANCE.createToolPartFromNBT(headItem.getNbt().getCompound(NBTFactory.HEAD_NBT_IDENTIFIER));
        } else {
            head = ((ForgeroToolItem) getOutput().getItem()).getHead();
        }

        if (handleItem.hasNbt() && handleItem.getNbt().contains(NBTFactory.HANDLE_NBT_IDENTIFIER)) {
            handle = (ToolPartHandle) NBTFactory.INSTANCE.createToolPartFromNBT(handleItem.getNbt().getCompound(NBTFactory.HANDLE_NBT_IDENTIFIER));
        } else {
            handle = ((ForgeroToolItem) getOutput().getItem()).getHandle();
        }


        ForgeroTool tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle);

        ItemStack forgeroToolInstanceStack = new ItemStack(getOutput().getItem());
        forgeroToolInstanceStack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, NBTFactory.INSTANCE.createNBTFromTool(tool));
        return forgeroToolInstanceStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolRecipeSerializer.INSTANCE;
    }

    public static class ToolRecipeSerializer extends ShapedRecipe.Serializer implements ForgeroRecipeSerializer {
        public static ToolRecipeSerializer INSTANCE = new ToolRecipeSerializer();

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public ToolRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new ToolRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public ToolRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new ToolRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_RECIPE.getName());
        }
    }
}
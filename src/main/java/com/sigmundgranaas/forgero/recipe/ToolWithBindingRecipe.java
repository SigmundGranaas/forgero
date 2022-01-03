package com.sigmundgranaas.forgero.recipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

public class ToolWithBindingRecipe extends ShapedRecipe {

    public ToolWithBindingRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack headItem = null;
        ItemStack bindingItem = null;
        ItemStack handleItem = null;


        List<Ingredient> ingredients = super.getIngredients();

        for (int ingredientSlot = 0; ingredientSlot < ingredients.size(); ingredientSlot++) {
            if (ingredients.get(ingredientSlot).getMatchingStacks().length > 0) {
                ItemStack toolPart = craftingInventory.getStack(ingredientSlot);
                if (headItem == null) {
                    headItem = toolPart;
                } else if (bindingItem == null) {
                    bindingItem = toolPart;
                } else if (handleItem == null) {
                    handleItem = toolPart;
                }

            }
        }

        assert headItem != null;
        assert handleItem != null;
        assert bindingItem != null;

        ToolPartHead head;
        ToolPartHandle handle;
        ToolPartBinding binding;
        if (headItem.hasNbt() && headItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
            head = (ToolPartHead) NBTFactory.INSTANCE.createToolPartFromNBT(headItem.getNbt());
        } else {
            head = ((ForgeroToolItem) getOutput().getItem()).getHead();
        }

        if (handleItem.hasNbt() && handleItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
            handle = (ToolPartHandle) NBTFactory.INSTANCE.createToolPartFromNBT(handleItem.getNbt());
        } else {
            handle = ((ForgeroToolItem) getOutput().getItem()).getHandle();
        }

        if (bindingItem.hasNbt() && bindingItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
            binding = (ToolPartBinding) NBTFactory.INSTANCE.createToolPartFromNBT(bindingItem.getNbt());
        } else {
            binding = ForgeroToolPartFactory.INSTANCE.createToolPartBindingBuilder(((ToolPartItem) bindingItem.getItem()).getPrimaryMaterial()).createToolPart();
        }

        ForgeroTool tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle, binding);

        ItemStack forgeroToolInstanceStack = new ItemStack(getOutput().getItem());
        forgeroToolInstanceStack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, NBTFactory.INSTANCE.createNBTFromTool(tool));
        return forgeroToolInstanceStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolWithBindingRecipeSerializer.INSTANCE;
    }

    public static class ToolWithBindingRecipeSerializer extends ShapedRecipe.Serializer implements ForgeroRecipeSerializer {
        public static ToolWithBindingRecipeSerializer INSTANCE = new ToolWithBindingRecipeSerializer();

        @Override
        public ToolWithBindingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new ToolWithBindingRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public ToolWithBindingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new ToolWithBindingRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_WITH_BINDING_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
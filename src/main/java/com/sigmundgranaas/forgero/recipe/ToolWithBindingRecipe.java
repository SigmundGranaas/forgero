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
import net.minecraft.util.collection.DefaultedList;

public class ToolWithBindingRecipe extends ShapedRecipe {
    public ToolWithBindingRecipe(Identifier identifier, String string, int width, int height, DefaultedList<Ingredient> defaultedList, ItemStack itemStack) {
        super(identifier, string, width, height, defaultedList, itemStack);
    }


    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack headItem = null;
        ItemStack handleItem = null;
        ItemStack bindingItem = null;

        for (int itemSlot = 0; itemSlot < getIngredients().size(); itemSlot++) {
            if (getIngredients().get(0).test(craftingInventory.getStack(itemSlot))) {
                headItem = craftingInventory.getStack(itemSlot);
            } else if (getIngredients().get(1).test(craftingInventory.getStack(itemSlot))) {
                handleItem = craftingInventory.getStack(itemSlot);
            } else if (getIngredients().get(2).test(craftingInventory.getStack(itemSlot))) {
                bindingItem = craftingInventory.getStack(itemSlot);
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

        if (handleItem.hasNbt() && handleItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
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
        return new ToolWithBindingRecipeSerializer();
    }

    public static class ToolWithBindingRecipeSerializer extends ShapedRecipe.Serializer implements ForgeroRecipeSerializerTypes {
        @Override
        public ToolWithBindingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return (ToolWithBindingRecipe) super.read(identifier, jsonObject);
        }

        @Override
        public ToolWithBindingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return (ToolWithBindingRecipe) super.read(identifier, packetByteBuf);
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.MOD_NAMESPACE, RecipeTypes.TOOL_WITH_BINDING_RECIPE.getName());
        }
    }
}
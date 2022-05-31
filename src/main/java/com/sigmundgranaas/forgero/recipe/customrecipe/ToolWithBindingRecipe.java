package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.binding.ToolPartBinding;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.sigmundgranaas.forgero.item.NBTFactory.BINDING_NBT_IDENTIFIER;

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
            if (ingredients.get(ingredientSlot).getMatchingStacksClient().length > 0) {
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

        ToolPartHead head;
        ToolPartHandle handle;
        ToolPartBinding binding;
        if (headItem.hasTag() && headItem.getOrCreateTag().contains(NBTFactory.HEAD_NBT_IDENTIFIER)) {
            head = (ToolPartHead) NBTFactory.INSTANCE.createToolPartFromNBT(headItem.getOrCreateTag().getCompound(NBTFactory.HEAD_NBT_IDENTIFIER));
        } else {
            head = (ToolPartHead) ((ToolPartItem) headItem.getItem()).getPart();
        }

        if (handleItem.hasTag() && handleItem.getOrCreateTag().contains(NBTFactory.HANDLE_NBT_IDENTIFIER)) {
            handle = (ToolPartHandle) NBTFactory.INSTANCE.createToolPartFromNBT(handleItem.getOrCreateTag().getCompound(NBTFactory.HANDLE_NBT_IDENTIFIER));
        } else {
            handle = (ToolPartHandle) ((ToolPartItem) handleItem.getItem()).getPart();
        }

        if (bindingItem.hasTag() && bindingItem.getOrCreateTag().contains(BINDING_NBT_IDENTIFIER)) {
            binding = (ToolPartBinding) NBTFactory.INSTANCE.createToolPartFromNBT(bindingItem.getOrCreateTag().getCompound(BINDING_NBT_IDENTIFIER));
        } else {
            binding = ForgeroToolPartFactory.INSTANCE.createToolPartBindingBuilder(((ToolPartItem) bindingItem.getItem()).getPrimaryMaterial(), ((ToolPartItem) bindingItem.getItem()).getPart().getSchematic()).createToolPart();
        }

        ForgeroTool tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle, binding);

        ItemStack forgeroToolInstanceStack = new ItemStack(getOutput().getItem());
        forgeroToolInstanceStack.getOrCreateTag().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, NBTFactory.INSTANCE.createNBTFromTool(tool));
        return forgeroToolInstanceStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolWithBindingRecipeSerializer.INSTANCE;
    }

    public static class ToolWithBindingRecipeSerializer extends ShapedRecipe.Serializer implements ForgeroRecipeSerializer {
        public static final ToolWithBindingRecipeSerializer INSTANCE = new ToolWithBindingRecipeSerializer();

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
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.TOOL_WITH_BINDING_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
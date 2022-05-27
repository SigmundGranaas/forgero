package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import com.sigmundgranaas.forgero.item.items.ToolPartItemImpl;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;

public class SchematicToolPartRecipe extends ShapelessRecipe {

    public SchematicToolPartRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
    }


    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        ItemStack schematic = null;


        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack stack = craftingInventory.getStack(i);
            if (stack.getItem() instanceof SchematicItem) {
                schematic = stack;
            }
        }

        if (schematic == null) {
            return new ItemStack(Items.AIR);
        }


        ForgeroToolPart part = switch (((ToolPartItemImpl) getOutput().getItem()).getToolPartType()) {
            case HEAD ->
                    ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(((ToolPartItemImpl) getOutput().getItem()).getPrimaryMaterial(), (HeadSchematic) ((SchematicItem) schematic.getItem()).getSchematic()).createToolPart();
            case HANDLE ->
                    ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(((ToolPartItemImpl) getOutput().getItem()).getPrimaryMaterial(), ((SchematicItem) schematic.getItem()).getSchematic()).createToolPart();
            case BINDING ->
                    ForgeroToolPartFactory.INSTANCE.createToolPartBindingBuilder(((ToolPartItemImpl) getOutput().getItem()).getPrimaryMaterial(), ((SchematicItem) schematic.getItem()).getSchematic()).createToolPart();
        };

        ItemStack forgeroToolInstanceStack = getOutput().copy();
        forgeroToolInstanceStack.getOrCreateNbt().put(NBTFactory.getToolPartNBTIdentifier(part), NBTFactory.INSTANCE.createNBTFromToolPart(part));
        return forgeroToolInstanceStack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SchematicToolPartRecipeSerializer.INSTANCE;
    }

    public static class SchematicToolPartRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
        public static final SchematicToolPartRecipeSerializer INSTANCE = new SchematicToolPartRecipeSerializer();

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public SchematicToolPartRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new SchematicToolPartRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public SchematicToolPartRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new SchematicToolPartRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.TOOLPART_SCHEMATIC_RECIPE.getName());
        }
    }
}